package kr.ac.snu.mobcomp_project.component;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;

import kr.ac.snu.mobcomp_project.TabFragment1;

public class AccelerometerListener implements SensorEventListener {
    //public SensorManager mSensorManager;
    public float[] mGravity;
    public float mAccel;
    public float mAccelCurrent;
    public float mAccelLast;
    //public Sensor mAccelerometer;


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Activity mActivity;
    private AccelerometerMonitorCallback mAccelerometerMonitorCallback;
    private TabFragment1 cur_fragment;

    public AccelerometerListener(TabFragment1 cur_fragment) {
        CreateAccelerometer(cur_fragment);
    }

    public void CreateAccelerometer(Fragment mFragment){
        cur_fragment = (TabFragment1) mFragment;
        mSensorManager = (SensorManager) mFragment.getActivity().getSystemService(Activity.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        mAccelerometerMonitorCallback = new AccelerometerMonitorCallback();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
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
            cur_fragment.updateValue(mAccel,mGravity);
        }
    }
    public void onThreadResume(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onThreadPause(){
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
