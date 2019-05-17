package kr.ac.snu.mobcomp_project;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;

import kr.ac.snu.mobcomp_project.component.LocationMonitor;
import umich.cse.yctung.androidlibsvm.LibSVM;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import kr.ac.snu.mobcomp_project.component.AccelerometerListener;

public class DrowsyDetector implements Runnable {
    private int mInterval = 1000;
    private AccelerometerListener accelerometerListener;
    private LocationMonitor locationMonitor;
    private Handler mHandler;
    private TabFragment1 cur_fragment;
    private final String TAG = "DrowsyDetector";
    private LibSVM svm;
    File savedir;
    BufferedWriter train_writer;
    private int number_of_training_data;
    public DrowsyDetector(Activity activity, TabFragment1 fragment, Handler mHandler_in,
                          AccelerometerListener accelerometerListener_in,
                          LocationMonitor locationMonitor_in) {
        cur_fragment = fragment;
        mHandler = mHandler_in;
        accelerometerListener = accelerometerListener_in;
        locationMonitor = locationMonitor_in;
        number_of_training_data = 0;
        try{
            // initialize SVM
            svm = new LibSVM();
            savedir = new File(appFolderPath);
            train_writer = new BufferedWriter(new FileWriter(savedir + "/svm_train.txt", true));
            if(!savedir.exists()){
                savedir.mkdir();
            }
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
    String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    String appFolderPath = systemPath + "libsvm/";

    @Override
    public void run() { // run inference
        try{
            //ML inference
            // How do you use SVM in time-series data?
            //DL inference
            if(tflite != null) {
                DLinputUpdate();
                tflite.run(raw_input, labelProbArray);
                cur_fragment.updateDLInference(labelProbArray[0],getNumLabels());
                Log.d(TAG,String.format("Inference : %s", Arrays.toString(labelProbArray[0])));
            }
        }finally {
            mHandler.postDelayed(this,mInterval);
        }
    }
    private final int MAX_TRAINING_DATA = 1000;
    private void DLinputUpdate(){
        try {

            for (int i = 0; i < DIM_BATCH_SIZE; i++) {
                if(number_of_training_data < MAX_TRAINING_DATA) {
                    Random rand = new Random();
                    train_writer.append(Integer.toString(rand.nextInt(2)) + " ");
                }
                for (int j = 0; j < SIZE_OF_WINDOW - 1; j++) {
                    for (int k = 0; k < NUMBER_OF_FEATURES; k++) {
                        raw_input[i][j][k] = raw_input[i][j + 1][k];
                        if(number_of_training_data < MAX_TRAINING_DATA) {
                        train_writer.append(Integer.toString(j * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][j][k]) + " ");
                        }
                    }
                }
                if (accelerometerListener != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][0] = accelerometerListener.mAccel;
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][0] = 0.0f;
                }
                if (accelerometerListener.mGravity != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][1] = accelerometerListener.mGravity[0];
                    raw_input[i][SIZE_OF_WINDOW - 1][2] = accelerometerListener.mGravity[1];
                    raw_input[i][SIZE_OF_WINDOW - 1][3] = accelerometerListener.mGravity[2];
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][1] = 0.0f;
                    raw_input[i][SIZE_OF_WINDOW - 1][2] = 0.0f;
                    raw_input[i][SIZE_OF_WINDOW - 1][3] = 0.0f;
                }
                if (locationMonitor != null) {
                    raw_input[i][SIZE_OF_WINDOW - 1][4] = (float) locationMonitor.latitude;
                    raw_input[i][SIZE_OF_WINDOW - 1][5] = (float) locationMonitor.longitude;
                    raw_input[i][SIZE_OF_WINDOW - 1][6] = (float) locationMonitor.speed;
                } else {
                    raw_input[i][SIZE_OF_WINDOW - 1][4] = 0.0f;
                    raw_input[i][SIZE_OF_WINDOW - 1][5] = 0.0f;
                    raw_input[i][SIZE_OF_WINDOW - 1][6] = 0.0f;
                }
                raw_input[i][SIZE_OF_WINDOW - 1][7] = ((float) System.currentTimeMillis()) / 1000;
                if(number_of_training_data < MAX_TRAINING_DATA) {
                    for (int k = 0; k < NUMBER_OF_FEATURES; k++) {
                        train_writer.append(Integer.toString((SIZE_OF_WINDOW - 1) * NUMBER_OF_FEATURES + k) + ":" + Float.toString(raw_input[i][SIZE_OF_WINDOW - 1][k]) + " ");
                    }
                }
                if(number_of_training_data < MAX_TRAINING_DATA) {
                    train_writer.newLine();
                }
                number_of_training_data++;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void close(){
        tflite.close();
        try {
            train_writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    //Constants
    private final int DIM_BATCH_SIZE = 1;
    private final int SIZE_OF_WINDOW = 20;
    private final int NUMBER_OF_FEATURES = 8; // accel / grav0,1,2 / lat , long, speed / cur_time
    private final int NUMBER_OF_CLASSES = 7;
    // DL I/O
    private float[][][] raw_input = null;
    private float[][] labelProbArray = null;


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
