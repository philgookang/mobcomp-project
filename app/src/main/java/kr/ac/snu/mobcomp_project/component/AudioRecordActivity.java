package kr.ac.snu.mobcomp_project.component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import kr.ac.snu.mobcomp_project.R;

public class AudioRecordActivity extends AppCompatActivity {

    SpeechRecognition sr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sr = new SpeechRecognition(this);
        sr.setSpeechRecognitionListener(new SpeechRecognition.SpeechRecognitionListener() {
            @Override
            public void onResult(int res) {
                Log.d("CALLBACK", String.valueOf(res));
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(sr != null) {
            sr = null;
        }
        super.onDestroy();
    }

    public void startRecording(View view) {
        System.out.println("startRecording");
        if(sr != null) {
            sr.startRecording();
        }
    }

    public void stopRecording(View view) {
        System.out.println("stopRecording");
        if(sr != null) {
            sr.stopRecording();
        }
    }

}
