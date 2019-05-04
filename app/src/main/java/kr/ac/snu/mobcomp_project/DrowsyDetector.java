package kr.ac.snu.mobcomp_project;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kr.ac.snu.mobcomp_project.component.AccelerometerListener;

public class DrowsyDetector implements Runnable {
    private int mInterval = 1000;
    private AccelerometerListener accelerometerListener;
    private Handler mHandler;
    public DrowsyDetector(Activity activity,Handler mHandler_in, AccelerometerListener accelerometerListener_in) {
        mHandler = mHandler_in;
        accelerometerListener = accelerometerListener_in;
        try{
            tfliteModel = loadModelFile(activity);
            tflite = new Interpreter(tfliteModel, tfliteOptions);
            inputData = ByteBuffer.allocateDirect( DIM_BATCH_SIZE * SIZE_OF_WINDOW * NUMBER_OF_FEATURES * SIZE_OF_FLOAT);
            inputData.order(ByteOrder.nativeOrder());
            labelProbArray = new float[1][getNumLabels()];
            System.out.println("Created a TFLITE classifier");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private byte[] randominput;
    private final int SIZE_OF_FLOAT = 4;
    @Override
    public void run() {
        try{
            if(accelerometerListener != null) {
                if(accelerometerListener.mGravity != null) {
                    System.out.println(String.format("%6f %6f",
                            accelerometerListener.mAccel,
                            accelerometerListener.mGravity[0]));
                }
            }
            if(tflite != null) {
                randominput = new byte[DIM_BATCH_SIZE * SIZE_OF_WINDOW * NUMBER_OF_FEATURES * SIZE_OF_FLOAT];
                new Random().nextBytes(randominput);
                inputData = ByteBuffer.wrap(randominput);
                //input_list.add()
                tflite.run(inputData, labelProbArray);
                System.out.println("Inference");
                System.out.println(Arrays.toString(labelProbArray[0]));
            }
            else {
                System.out.println("Inference Fail");
            }
        }finally {
            mHandler.postDelayed(this,mInterval);
        }
    }
    public void close(){
        tflite.close();
    }
    private final int DIM_BATCH_SIZE = 1;
    private final int SIZE_OF_WINDOW = 20;
    private final int NUMBER_OF_FEATURES = 3;
    private final int NUMBER_OF_CLASSES = 7;
    private ByteBuffer inputData = null;
    private float[][] labelProbArray = null;
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
