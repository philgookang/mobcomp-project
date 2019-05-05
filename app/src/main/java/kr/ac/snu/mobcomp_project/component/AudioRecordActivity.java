package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.IOException;

import kr.ac.snu.mobcomp_project.R;

public class AudioRecordActivity extends AppCompatActivity {

    MediaRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        } else {
//            this.setUpRecorder();
            System.out.println(getFilesDir());
        }
    }

    private void setUpRecorder() {
        if(recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            String fileDir = getFilesDir() + "";
            recorder.setOutputFile(fileDir);
        } else {
            System.out.println("Recorder not null");
            recorder.release();
            recorder = null;
        }
    }

    public void startRecording(View view) {
        System.out.println("startRecording");
        if(recorder != null) {
            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException exp) {
                System.err.println(exp);
            }
        } else {
        }
    }

    public void stopRecording(View view) {
        System.out.println("stopRecording");
        if(recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

}
