package kr.ac.snu.mobcomp_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import kr.ac.snu.mobcomp_project.component.AccelerometerListener;
import kr.ac.snu.mobcomp_project.component.LocationMonitor;

public class MainActivity extends AppCompatActivity  {
    private ViewPager vp;
    public String designated_phone_number;
    public LocationMonitor mLocationMonitor;
    public long cur_time_in_sec;
    public Runnable mRunnable;
    //Sensor
    public AccelerometerListener mAccelerometerListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        vp = (ViewPager)findViewById(R.id.vp);
        vp.setAdapter(new pagerAdapter( getSupportFragmentManager()));
        vp.setCurrentItem(0);
        // Components


        // load settings
        loadSetting();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LocationMonitor.LOCATION_PERMISSIONS : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mLocationMonitor != null) {
                        mLocationMonitor.setup();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        //mLocationMonitor.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        //mLocationMonitor.onResume();
        super.onResume();
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
