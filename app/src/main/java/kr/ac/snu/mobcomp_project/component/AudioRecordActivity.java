package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import kr.ac.snu.mobcomp_project.R;

public class AudioRecordActivity extends AppCompatActivity implements RecognitionListener {

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
                sr.setRecognitionListener(this);
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
            Intent srIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            sr.startListening(srIntent);
        }
    }

    public void stopRecording(View view) {
        System.out.println("stopRecording");
        if(sr != null) {
            sr.stopListening();
        }
    }

    private void log1(String message){Log.d("moo", message);}
    private void log2(String message){Log.d("cow", message);}

    @Override public void onReadyForSpeech(Bundle params){log1("onReadyForSpeech");}
    @Override public void onBeginningOfSpeech(){log1("onBeginningOfSpeech");}
    @Override public void onRmsChanged(float rms_dB){log2("onRmsChanged");}
    @Override public void onBufferReceived(byte[] buffer){log1("onBufferReceived");}
    @Override public void onEndOfSpeech(){log1("onEndOfSpeech");}
    @Override public void onResults(Bundle results){
        log1("onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        for(int i = 0; i < scores.length; i++) {
            log1(matches.get(i));
            log1(String.valueOf(scores[i]));
        }
    }
    @Override public void onPartialResults(Bundle partialResults){log1("onPartialResults");}
    @Override public void onEvent(int eventType, Bundle params){log1("onEvent");}
    @Override public void onError(int error)
    {
        String message = "";

        if(error == SpeechRecognizer.ERROR_AUDIO)                           message = "audio";
        else if(error == SpeechRecognizer.ERROR_CLIENT)                     message = "client";
        else if(error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)   message = "insufficient permissions";
        else if(error == SpeechRecognizer.ERROR_NETWORK)                    message = "network";
        else if(error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT)            message = "network timeout";
        else if(error == SpeechRecognizer.ERROR_NO_MATCH)                   message = "no match found";
        else if(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY)            message = "recognizer busy";
        else if(error == SpeechRecognizer.ERROR_SERVER)                     message = "server";
        else if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)             message = "speech timeout";

        log1("error " + message);
    }

}
