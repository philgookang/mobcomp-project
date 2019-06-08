package kr.ac.snu.mobcomp_project;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import kr.ac.snu.mobcomp_project.afterdetection.CallManager;
import kr.ac.snu.mobcomp_project.component.AccelerometerListener;
import kr.ac.snu.mobcomp_project.component.CameraSourcePreview;
import kr.ac.snu.mobcomp_project.component.EyeTracker;
import kr.ac.snu.mobcomp_project.component.LocationMonitor;

public class TabFragment1 extends Fragment implements SurfaceHolder.Callback {
    //Calling
    private Button button;
    private final int CALL_PHONE_PERMISSIONS = 1;
    private int prev_state;
    private CallManager mCallManager;

    private CameraSourcePreview mPreview;

    //View
    ConstraintLayout layout;

    //Background thread for inference
    private int mInterval = 1000;
    private Handler mHandler;
    Runnable mRunnable;

    public TabFragment1() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load components
        ((MainActivity) getActivity()).mAccelerometerListener = new AccelerometerListener(this);
        ((MainActivity) getActivity()).mLocationMonitor = new LocationMonitor(getActivity(), savedInstanceState, this); // Why

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (((MainActivity) getActivity()).mCameraSource != null)
            ((MainActivity) getActivity()).mCameraSource.release();
    }

    public void updateValue(float acceleration, float[] gravity) {
        TextView txtacc = (TextView) layout.findViewById(R.id.acceleration);
        TextView txtgravity = (TextView) layout.findViewById(R.id.gravity);
        if (txtacc != null) {
            txtacc.setText(String.format("Acceleration | %.6f", acceleration));
        }
        if (txtgravity != null) {
            txtgravity.setText(String.format("Gravity | %.6f %.6f %.6f", gravity[0], gravity[1], gravity[2]));
        }
    }

    public void updateLocationValue(double latitude, double longitude, float speed) {
        TextView txtgps = (TextView) layout.findViewById(R.id.gps_location);
        if (txtgps != null) {
            System.out.println(latitude);
            Log.d("Fragment1", "latitude: " + String.valueOf(latitude) + "   longitude: " + String.valueOf(longitude) + "   speed: " + String.valueOf(speed));
            txtgps.setText(String.format("GPS | %.6f , %.6f , %.6f", latitude, longitude, speed));
            //txtgps.setText(String.format("Original Value a b c d e f g", (float)latitude));
        } else {
            System.out.println("Cannot find txtgps");
        }
    }

    public void updateDLInference(float[] lableProbArray, int length) {
        TextView txtinf = (TextView) layout.findViewById(R.id.Inference);
        if (txtinf != null) {
            String temp = "";
            for (int i = 0; i < length; i++) {
                temp = String.format("%s%.2f, ", temp, lableProbArray[i]);
            }
            txtinf.setText(String.format("DL | %s ", temp));
        } else {
            System.out.println("Cannot find txtinf");
        }
    }

    public void updateMLInference(int output) {
        TextView txtinf = (TextView) layout.findViewById(R.id.svm);
        if (txtinf != null) {
            txtinf.setText(String.format("ML | %d ", output));
        } else {
            System.out.println("Cannot find txtsvm");
        }
    }

    @Override
    public void onResume() {
        ((MainActivity) getActivity()).mAccelerometerListener.onThreadResume();
        ((MainActivity) getActivity()).mLocationMonitor.onResume();
        //getTime();

        // load inference task
        mHandler = new Handler();
        mRunnable = new DrowsyDetector(getActivity(), this, mHandler, ((MainActivity) getActivity()).mAccelerometerListener, ((MainActivity) getActivity()).mLocationMonitor);
        mRunnable.run();
        ((MainActivity) getActivity()).mRunnable = mRunnable;
/*        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //((MainActivity) getActivity()).mCameraSource.start(surfaceView.getHolder());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        super.onResume();
        startCameraSource();
    }

    @Override
    public void onPause() {
        ((MainActivity) getActivity()).mAccelerometerListener.onThreadPause();
        ((MainActivity) getActivity()).mLocationMonitor.onPause();
        // remove inference task
        ((DrowsyDetector) mRunnable).close();
        mHandler.removeCallbacks(mRunnable);
        ((MainActivity) getActivity()).mRunnable = null;
        if (((MainActivity) getActivity()).mCameraSource != null) {
            ((MainActivity) getActivity()).mCameraSource.stop();
        }
        super.onPause();
        mPreview.stop();
    }

    private Button labelbutton;
    public boolean drowsiness;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (ConstraintLayout) inflater.inflate(R.layout.tab_fragment_1, container, false);
        mPreview = (CameraSourcePreview)layout.findViewById(R.id.camerapreview);
        // Designated Call managing part
        mCallManager = new CallManager();
        button = layout.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                mCallManager.checkPermissionandCall(TabFragment1.this, arg0); // Permission Check
            }
        });

        mCallManager.RedialListen((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)); // Redial check
        labelbutton = layout.findViewById(R.id.record);
        drowsiness = false;
        labelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drowsiness) {
                    labelbutton.setText("Awake");
                } else {
                    labelbutton.setText("Drowsy");
                }
                drowsiness = !drowsiness;
            }
        });
        //timebutton = layout.findViewById(R.id.timebutton);
        /*timebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                getTime();
            }
        });*/
        //load camera preview
        requestPermissionCamera();

        return layout;
    }

    public int frontcam = 0;

    private final int CAMERA_PERMISSIONS = 9;

    public boolean requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Need Camera Permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA
            }, CAMERA_PERMISSIONS);
        } else {
            createCameraSource();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) { // Permisson check callback
        switch (requestCode) {
            case CALL_PHONE_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission Granted
                    if (mCallManager != null) {
                        mCallManager.start_call(this);
                    }
                } else {
                }
                return;
            }
            case CAMERA_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission Granted
                    createCameraSource();
                } else {

                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        ((MainActivity) getActivity()).mLocationMonitor.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
    /*public void getTime() {
        TextView currTime = layout.findViewById(R.id.date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String currDateTime = sdf.format(new Date());
        currTime.setText(currDateTime);
    }*/

    private static final String TAG = "EyeTracker";
    EyeTracker mEyeTracker;

    private class EyeTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override
        public Tracker<Face> create(Face face) {
            mEyeTracker = new EyeTracker(getActivity().getApplicationContext());
            return mEyeTracker;
        }
    }


    public void createCameraSource() {
        Context context = getActivity().getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new EyeTrackerFactory()).build());

        ((MainActivity) getActivity()).mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(360, 360)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
       // surfaceView = (SurfaceView) layout.findViewById(R.id.camerapreview);
     //   surfaceHolder = surfaceView.getHolder();
     //   surfaceHolder.addCallback(this);
    }


    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (ActivityCompat.checkSelfPermission((MainActivity) getActivity(),
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                ((MainActivity) getActivity()).mCameraSource.start(holder);
                Log.d("SURFACE", "Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
        if(surfaceHolder.getSurface() == null){
            return;
        }
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }
        if(camera != null){
            try{
                Camera.Parameters parameters = camera .getParameters();

                // 카메라의 회전이 가로/세로일때 화면을 설정한다.
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    camera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    camera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        */
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        /*
        if(camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        }
        */
        Log.d("SURFACE", "Destroyed");
        ((MainActivity) getActivity()).mCameraSource.stop();
    }

    private void startCameraSource() {

        if (((MainActivity) getActivity()).mCameraSource != null) {
            try {
                mPreview.start(((MainActivity) getActivity()).mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                ((MainActivity) getActivity()).mCameraSource.release();
                ((MainActivity) getActivity()).mCameraSource = null;
            }
        }
    }
}