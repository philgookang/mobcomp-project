package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
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

    SpeechRecognizer sr = null;

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
            if(SpeechRecognizer.isRecognitionAvailable(this)) {
                sr = SpeechRecognizer.createSpeechRecognizer(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(sr != null) {
            sr.destroy();
        }
        super.onDestroy();
    }

    public void startRecording(View view) {
        System.out.println("startRecording");
        if(sr != null) {
            sr.startListening(new Intent());
        }
    }

    public void stopRecording(View view) {
        System.out.println("stopRecording");
        if(sr != null) {
            sr.stopListening();
        }
    }

}
