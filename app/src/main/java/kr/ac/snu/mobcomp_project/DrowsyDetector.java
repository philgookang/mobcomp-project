package kr.ac.snu.mobcomp_project;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Calendar;

import kr.ac.snu.mobcomp_project.component.AccelerometerMonitor;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitorConfig;
import kr.ac.snu.mobcomp_project.component.EyeTracker;
import kr.ac.snu.mobcomp_project.component.LocationMonitor;
import kr.ac.snu.mobcomp_project.component.SpeechRecognition;
import umich.cse.yctung.androidlibsvm.LibSVM;


public class DrowsyDetector implements Runnable {
    private int mInterval = 1000; // milisecond of inference/ data collection interval
    //Components
    private AccelerometerMonitor mAccelerometerMonitor;
    private SpeechRecognition mSpeechRecognition;
    private LocationMonitor locationMonitor;
    private EyeTracker mEyeTracker;
    private Calendar curtime;
    //SVM
    private LibSVM svm;
    //UI components
    private Handler mHandler;
    private TabFragment1 cur_fragment;
    private final String TAG = "DrowsyDetector";

    // Path of training data / model / testing data stored
    String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    String appFolderPath = systemPath + "libsvm/";
    String raw_data = "svm_train.txt";
    String predict_data = "svm_predict.txt";
    String scaled_data = "svm_train_scaled.txt";
    String predict_scaled_data = "svm_predict_scaled.txt";
    String model_name = "svm_model.mdl";
    String result_name = "result.txt";
    //Constants
    private final int DIM_BATCH_SIZE = 1;
    private final int SIZE_OF_WINDOW = 50;
    private final int NUMBER_OF_FEATURES = 6; // accel / grav0,1,2 / lat , long, speed / cur_time
    private final int NUMBER_OF_CLASSES = 2;
    // DL I/O
    private float[][][] raw_input = null;
    private float[][] labelProbArray = null;

    //SVM I/O
    File savedir;
    BufferedWriter train_writer;
    BufferedWriter predict_writer;
    public DrowsyDetector(Activity activity, TabFragment1 fragment, Handler mHandler_in ) {
        cur_fragment = fragment;
        mHandler = mHandler_in;
        locationMonitor = ((MainActivity)activity).mLocationMonitor;
        mAccelerometerMonitor = ((MainActivity)activity).mAccelerometerMonitor;
        mEyeTracker = fragment.mEyeTracker;
        mSpeechRecognition = ((MainActivity)activity).mSpeechRecognition;
        curtime = Calendar.getInstance();
        try{
            // initialize SVM
            svm = new LibSVM();
            savedir = new File(appFolderPath);
            if(!savedir.exists()){
                savedir.mkdir();
            }
            File svm_train_data = new File(appFolderPath + raw_data);
            if(!svm_train_data.exists()) {
                try{
                    svm_train_data.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            train_writer = new BufferedWriter(new FileWriter(appFolderPath + raw_data, true));
            //load DL model and allocate DL I/O
            tfliteModel = loadModelFile(activity);
            tflite = new Interpreter(tfliteModel, tfliteOptions);
            raw_input = new float[DIM_BATCH_SIZE][SIZE_OF_WINDOW][NUMBER_OF_FEATURES];
            labelProbArray = new float[1][getNumLabels()];
            Log.d(TAG,"Loaded a TFLITE classifier");

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void train_svm(){
        if(svm != null){
            svm.scale(appFolderPath + raw_data , appFolderPath + scaled_data);
            File svm_model = new File(appFolderPath + model_name);
            if(!svm_model.exists()) {
                try{
                    svm_model.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(svm_model.exists()) {
                svm.train("-t 2 " + appFolderPath + scaled_data + " " + appFolderPath + model_name);
                svm.predict(appFolderPath + scaled_data + " " + appFolderPath + model_name + " " + appFolderPath + result_name);
            }
        }
    }
    public void clear_svm_train_data(){
        File svm_train_data = new File(appFolderPath + raw_data);
        if(svm_train_data.exists()) {
            svm_train_data.delete();

        }
        try{
            svm_train_data.createNewFile();
            train_writer = new BufferedWriter(new FileWriter(appFolderPath + raw_data, true));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() { // run inference
        try{
            //DL inference
            if(tflite != null) {
                DLinputUpdate();
                tflite.run(raw_input, labelProbArray);
                cur_fragment.updateDLInference(labelProbArray[0],getNumLabels());
                Log.d(TAG,String.format("Inference : %s", Arrays.toString(labelProbArray[0])));
            }
            //ML inference
            // How do you use SVM in time-series data?
            if(svm != null){
                File svm_model = new File(appFolderPath + model_name);
                if(svm_model.exists()) {
                    svm.scale(appFolderPath + predict_data, appFolderPath + predict_scaled_data);
                    svm.predict(appFolderPath + predict_scaled_data + " " + appFolderPath + model_name + " " + appFolderPath + result_name);
                    String temp = "";
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(appFolderPath + result_name));
                        temp = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (temp != null && temp.matches("-?\\d+")) {
                        cur_fragment.updateMLInference(Integer.parseInt(temp));
                    }
                }
            }

        }finally {
            mHandler.postDelayed(this,mInterval);
        }
    }
    private void DLinputUpdate(){
        try {
            //Clear svm_predict.txt
            File svm_predict_data = new File(appFolderPath + predict_data);
            if(!svm_predict_data.exists()) {
                try{
                    svm_predict_data.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                svm_predict_data.delete();
                try{
                    svm_predict_data.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            predict_writer = new BufferedWriter(new FileWriter(appFolderPath + predict_data, false));
            // Collect data from sensor managers and write to svm train and predict
            String temp = "";
            for (int i = 0; i < DIM_BATCH_SIZE; i++) {
                    int label;
                    if(cur_fragment.drowsiness){
                        label = 1;
                    }
                    else{
                        label = 0;
                    }
                    train_writer.append(Integer.toString(label) + " ");
                    temp = temp + Integer.toString(label) + " ";
                for (int j = 0; j < SIZE_OF_WINDOW - 1; j++) {
                    for (int k = 0; k < NUMBER_OF_FEATURES; k++) {
                        raw_input[i][j][k] = raw_input[i][j + 1][k];
                        if(raw_input[i][j][k] != 0.0) {
                            train_writer.append(Integer.toString(j * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][j][k]) + " ");
                            temp = temp + Integer.toString(j * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][j][k]) + " ";
                        }
                    }
                }
                if (mAccelerometerMonitor != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][0] = mAccelerometerMonitor.status;
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][0] = AccelerometerMonitorConfig.IS_NOT_DROWSY;
                }
                if (mSpeechRecognition != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][1] = mSpeechRecognition.result;
                    //Log.d(TAG, Integer.toString(mSpeechRecognition.result));
                    /*if(mSpeechRecognition.result == 0){
                        Log.d(TAG, "I Can Hear You");
                    }*/
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][1] = 0.0f;
                }
                if(mEyeTracker != null){
                    raw_input[i][SIZE_OF_WINDOW - 1][2] = mEyeTracker.isEyeClosed? 1 : 0 ;
                }else{
                    raw_input[i][SIZE_OF_WINDOW - 1][2] = 0.0f;
                }
                if (locationMonitor != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][3] = (float) locationMonitor.latitude;
                    raw_input[i][SIZE_OF_WINDOW - 1][4] = (float) locationMonitor.longitude;
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][3] = 0.0f;
                    raw_input[i][SIZE_OF_WINDOW - 1][4] = 0.0f;
                }
                raw_input[i][SIZE_OF_WINDOW - 1][5] = ((float) curtime.get(Calendar.HOUR_OF_DAY));

                    for (int k = 0; k < NUMBER_OF_FEATURES; k++) {
                        if(raw_input[i][SIZE_OF_WINDOW - 1][k] != 0.0) {
                            train_writer.append(Integer.toString((SIZE_OF_WINDOW - 1) * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][SIZE_OF_WINDOW - 1][k]) + " ");
                            temp = temp + Integer.toString((SIZE_OF_WINDOW - 1) * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][SIZE_OF_WINDOW - 1][k]) + " ";
                        }
                    }


                    train_writer.newLine();
                    temp = temp +"\n";

            }
            predict_writer.write(temp,0,temp.length());
            predict_writer.flush();
            System.out.println(temp);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void close(){
        if(tflite != null)
            tflite.close();
        try {
            if(train_writer != null)
                train_writer.close();
            }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    // DL classifier loading part
    private MappedByteBuffer tfliteModel;
    private Interpreter tflite;
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException{
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private String getModelPath(){
        return "cnn_model.tflite";
    }
    private int getNumLabels(){
        return NUMBER_OF_CLASSES;
    }

}
