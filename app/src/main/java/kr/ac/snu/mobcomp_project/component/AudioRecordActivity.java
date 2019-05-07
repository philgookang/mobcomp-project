package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
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

        File directory = new File(getFilesDir()+"");
        File[] files = directory.listFiles();
        for(int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        if(recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
        super.onDestroy();
    }

    public void startRecording(View view) {
        System.out.println("startRecording");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String fileDir = getFilesDir() + "/currAudio.3gp";
        recorder.setOutputFile(fileDir);
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException exp) {
            System.err.println(exp);
        }
    }

    public void stopRecording(View view) {
        System.out.println("stopRecording");
        if(recorder != null) {
            recorder.stop();
        }
    }

}
