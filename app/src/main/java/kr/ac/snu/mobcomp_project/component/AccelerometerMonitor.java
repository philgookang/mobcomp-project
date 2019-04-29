package kr.ac.snu.mobcomp_project.component;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import static android.content.Context.SENSOR_SERVICE;

public class AccelerometerMonitor implements SensorEventListener {

    /* ADD THESE
     @Override
    public void onResume() {
        super.onResume();
        mAccelerometerMonitor.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAccelerometerMonitor.onPause();
    }
     */

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Activity mActivity;
    private AccelerometerMonitorCallback mAccelerometerMonitorCallback;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

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

            // Make this higher or lower according to how much motion you want to detect
            if(mAccel > 3){
                mAccelerometerMonitorCallback.callback(AccelerometerMonitorConfig.MOTION_MOVE);
            } else {
                mAccelerometerMonitorCallback.callback(AccelerometerMonitorConfig.MOTION_STOP);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
