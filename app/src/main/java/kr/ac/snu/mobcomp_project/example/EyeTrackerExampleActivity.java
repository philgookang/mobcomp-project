package kr.ac.snu.mobcomp_project.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import kr.ac.snu.mobcomp_project.R;
import kr.ac.snu.mobcomp_project.component.CameraPreview;
import kr.ac.snu.mobcomp_project.component.EyeTracker;

public class EyeTrackerExampleActivity extends Activity {

    private static final String TAG = "EyeTracker";
    CameraSource mCameraSource;
    SurfaceView mSurfaceView;
    CameraPreview mCameraPreview;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_eyetracker);

        mSurfaceView = findViewById(R.id.camera_preview);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        startCamera();
        createCameraSource();
    }

    void startCamera(){
        mCameraPreview = new CameraPreview(this, this, Camera.CameraInfo.CAMERA_FACING_FRONT, mSurfaceView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == 0) {
                Toast.makeText(this, "Camera Authorized", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class EyeTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override
        public Tracker<Face> create(Face face) {
            return new EyeTracker(getApplicationContext());
        }
    }

        public void createCameraSource() {
            Context context = getApplicationContext();
            FaceDetector detector = new FaceDetector.Builder(context)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .build();

            detector.setProcessor(
                    new MultiProcessor.Builder<>(new EyeTrackerFactory())
                            .build());

            mCameraSource = new CameraSource.Builder(context, detector)
                    .setRequestedPreviewSize(1280, 720)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(10.0f)
                    .build();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            try {
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mCameraSource != null){
            mCameraSource.stop();
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        if(mCameraSource != null)
            mCameraSource.release();
    }
}

