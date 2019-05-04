package kr.ac.snu.mobcomp_project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kr.ac.snu.mobcomp_project.component.AccelerometerListener;

public class TabFragment1 extends Fragment
{
    //Calling
    private Button button;
    private final int CALL_PHONE_PERMISSIONS = 1;
    private int prev_state;

    //Sensor

    private AccelerometerListener mAccelerometerListener;
    //View
    ConstraintLayout layout;

    //Background thread for inference
    private int mInterval = 1000;
    private Handler mHandler;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                if(mAccelerometerListener != null) {
                    if(mAccelerometerListener.mGravity != null) {
                        System.out.println(String.format("%6f %6f",
                                mAccelerometerListener.mAccel,
                                mAccelerometerListener.mGravity[0]));
                    }
                }
            }finally {
                mHandler.postDelayed(mRunnable,mInterval);
            }
        }
    };

    public TabFragment1()
    {

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load components
        mAccelerometerListener = new AccelerometerListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void updateValue(float acceleration, float[] gravity){
        TextView txtacc = (TextView) layout.findViewById(R.id.acceleration);
        TextView txtgravity = (TextView) layout.findViewById(R.id.gravity);
        if(txtacc != null) {
            txtacc.setText(String.format("Acceleration | %.6f", acceleration));
        }
        if(txtgravity != null) {
            txtgravity.setText(String.format("Gravity | %.6f %.6f %.6f", gravity[0],gravity[1],gravity[2]));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccelerometerListener.onThreadResume();

        // load inference task
        mHandler = new Handler();
        mRunnable.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAccelerometerListener.onThreadPause();

        // remove inference task
        mHandler.removeCallbacks(mRunnable);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (ConstraintLayout)inflater.inflate(R.layout.tab_fragment_1, container, false);
        button = layout.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                checkPermissionandCall(arg0);
            }
        });
        TelephonyManager mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
            private static final String TAG = "mPhoneStateListener";
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                //super.onCallStateChanged(state, phoneNumber);
                switch(state){
                    case TelephonyManager.CALL_STATE_RINGING :
                        Log.d(TAG,"CALL_STATE_RINGING");
                        prev_state = TelephonyManager.CALL_STATE_RINGING;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE :
                        Log.d(TAG,"CALL_STATE_IDLE");
                        if((prev_state == TelephonyManager.CALL_STATE_RINGING)){
                            Log.d(TAG,"REJECTED_OR_MISSED_CALL");
                        }
                        else if((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)){
                            Log.d(TAG,"ANSWERED_CALL");
                        }
                        prev_state = TelephonyManager.CALL_STATE_IDLE;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK :
                        Log.d(TAG,"CALL_STATE_OFFHOOK");
                        prev_state = TelephonyManager.CALL_STATE_OFFHOOK;
                        break;

                }
            }
        };
        mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
        return layout;
    }

    private void checkPermissionandCall(View arg0){
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CALL_PHONE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSIONS);
            } else {

                // No explanation needed, we can request the permission.
                requestPermissions( new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSIONS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
        else {
            start_call();
        }
    }
    private void start_call() {
        String phone_no = "tel:" +((MainActivity)getActivity()).designated_phone_number;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phone_no));

        startActivity(callIntent);
        //mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
        //https://stackoverflow.com/questions/599443/how-to-hang-up-outgoing-call-in-android
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case CALL_PHONE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    start_call();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
