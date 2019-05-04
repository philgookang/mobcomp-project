package kr.ac.snu.mobcomp_project;

import android.os.Handler;

import kr.ac.snu.mobcomp_project.component.AccelerometerListener;

public class DrowsyDetector implements Runnable {
    private int mInterval = 1000;
    private AccelerometerListener accelerometerListener;
    private Handler mHandler;

    public DrowsyDetector(Handler mHandler_in, AccelerometerListener accelerometerListener_in) {
        mHandler = mHandler_in;
        accelerometerListener = accelerometerListener_in;
    }

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
        }finally {
            mHandler.postDelayed(this,mInterval);
        }
    }
}
