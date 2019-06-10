package kr.ac.snu.mobcomp_project.component;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class AccelerometerMonitor implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Activity mActivity;
    private AccelerometerMonitorCallback mAccelerometerMonitorCallback;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    public Integer status = AccelerometerMonitorConfig.IS_NOT_DROWSY;

    private List<Float> mWindow = new ArrayList<Float>();

    public AccelerometerMonitor(Activity _mActivity, AccelerometerMonitorCallback _mAccelerometerMonitorCallback) {

        mActivity = _mActivity;
        mAccelerometerMonitorCallback = _mAccelerometerMonitorCallback;

        mSensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    public void onResume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float xValue = event.values[0];
        float yValue = event.values[1];
        float zValue = event.values[2];
        Log.d("PPPPP", "x:"+xValue +";y:"+yValue+";z:"+zValue);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();

            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);

            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            // save data to window
            if (mWindow.size() >= AccelerometerMonitorConfig.WINDOW_SIZE) {

                // remove first level
                mWindow.remove(0);
            }

            // add new val into window
            mWindow.add(mAccel);

            // detect changes
            detect();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void detect() {

        Float min = null;
        Float max = null;

        for (Float f : mWindow) {
            if (max == null) {
                max = f;
                min = f;
                continue;
            }

            if (max < f) {
                max = f;
            }

            if (min > f) {
                min = f;
            }
        }

        Float diff = max - min;

        if (diff >= 30) {
            status = AccelerometerMonitorConfig.IS_DROWSY;
        } else {
            status = AccelerometerMonitorConfig.IS_NOT_DROWSY;
        }
    }
}
