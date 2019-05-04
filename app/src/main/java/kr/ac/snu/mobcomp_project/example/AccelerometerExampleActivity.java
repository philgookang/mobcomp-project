package kr.ac.snu.mobcomp_project.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import kr.ac.snu.mobcomp_project.R;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitor;
import kr.ac.snu.mobcomp_project.component.AccelerometerMonitorCallback;

public class AccelerometerExampleActivity extends AppCompatActivity {

    Activity mActivity;
    AccelerometerMonitor mAccelerometerMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        mAccelerometerMonitor = new AccelerometerMonitor(this, mAccelerometerMonitorCallback);
    }

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

    AccelerometerMonitorCallback mAccelerometerMonitorCallback = new AccelerometerMonitorCallback() {
        @Override
        public void callback(int action) {
            Toast.makeText(mActivity, "aaaaa: " + Integer.toString(action), Toast.LENGTH_SHORT);
        }
    };
}