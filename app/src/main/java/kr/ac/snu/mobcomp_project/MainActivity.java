package kr.ac.snu.mobcomp_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import kr.ac.snu.mobcomp_project.component.AccelerometerMonitorCallback;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitorConfig;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    private ViewPager vp;
    public String designated_phone_number;
    private SensorManager mSensorManager;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private Sensor mAccelerometer;
    private AccelerometerMonitorCallback mAccelerometerMonitorCallback;
    private TabFragment1 mfragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vp = (ViewPager)findViewById(R.id.vp);
        vp.setAdapter(new pagerAdapter( getSupportFragmentManager()));
        vp.setCurrentItem(0);
        //load components
        CreateAccelerometer();
        // load settings
        loadSetting();

    }
    private void CreateAccelerometer(){
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        mAccelerometerMonitorCallback = new AccelerometerMonitorCallback();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
        MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.tabfragment1,new TabFragment1(),"tabfrag1").commit();
        TabFragment1 tab1 = (TabFragment1)MainActivity.this.getSupportFragmentManager().findFragmentByTag("tabfrag1");
        if(tab1 != null ){
            System.out.println(tab1.txtacc);

            ((TextView)tab1.txtacc).setText("(Acceleration) ");// + String.valueOf(mAccel));
            ((TextView)tab1.txtgravity).setText("(Gravity) x : ");// + String.valueOf(mGravity[0])
                   // + " y : " + String.valueOf(mGravity[1])
                   // + " z : " + String.valueOf(mGravity[2]));
        }}

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void loadSetting(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.designated_phone_number_default);
        designated_phone_number = sharedPref.getString(getString(R.string.designated_phone_number), defaultValue);
    }

    private class pagerAdapter extends FragmentStatePagerAdapter
    {
        public pagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new TabFragment1();
                case 1:
                    return new TabFragment2();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
