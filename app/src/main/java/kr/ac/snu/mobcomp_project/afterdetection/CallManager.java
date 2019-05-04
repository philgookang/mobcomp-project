package kr.ac.snu.mobcomp_project.afterdetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import kr.ac.snu.mobcomp_project.MainActivity;

public class CallManager {

    private final int CALL_PHONE_PERMISSIONS = 1;
    private int prev_state;

   public void checkPermissionandCall(Fragment mfragment, View arg0){
        if (ActivityCompat.checkSelfPermission(mfragment.getContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mfragment.getActivity(),
                    Manifest.permission.CALL_PHONE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                mfragment.requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSIONS);
            } else {

                // No explanation needed, we can request the permission.
                mfragment.requestPermissions( new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSIONS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
        else {
            start_call(mfragment);
        }
    }
    public void start_call(Fragment mfragment) {
        String phone_no = "tel:" +((MainActivity)mfragment.getActivity()).designated_phone_number;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phone_no));

        mfragment.startActivity(callIntent);
        //mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
        //https://stackoverflow.com/questions/599443/how-to-hang-up-outgoing-call-in-android
    }
    public void RedialListen(TelephonyManager mTelephonyManager){
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
    }
}
