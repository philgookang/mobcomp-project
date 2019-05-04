package kr.ac.snu.mobcomp_project.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import kr.ac.snu.mobcomp_project.R;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitor;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitorCallback;
import kr.ac.snu.mobcomp_project.component.LocationMonitor;

public class LocationExampleActivity extends AppCompatActivity {

    Activity mActivity;
    LocationMonitor mLocationMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        mLocationMonitor = new LocationMonitor(mActivity, savedInstanceState);
    }

    @Override
    public void onResume() {
        mLocationMonitor.onResume();                              // <========== 필수
        super.onResume();
    }

    @Override
    protected void onPause() {
        mLocationMonitor.onPause();                              // <========== 필수
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mLocationMonitor.onSaveInstanceState(outState);          // <========== 필수
        super.onSaveInstanceState(outState);
    }
}
