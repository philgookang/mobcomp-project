package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

    public class SpeechRecognition implements RecognitionListener {

        private SpeechRecognitionListener listener = null;
        private boolean continueSR = false;


        public interface SpeechRecognitionListener {
            public void onResult(int res);
        }

        public void setSpeechRecognitionListener(SpeechRecognitionListener listener) {
            this.listener = listener;
        }

        SpeechRecognizer sr = null;

        public SpeechRecognition(Activity act) {

            if(ContextCompat.checkSelfPermission(act, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(act,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            } else {
                if(SpeechRecognizer.isRecognitionAvailable(act)) {
                    sr = SpeechRecognizer.createSpeechRecognizer(act);
                    sr.setRecognitionListener(this);
                }
            }
        }

        private void fireSRIntent() {
            if((sr != null) && continueSR) {
                Intent srIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                srIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
                srIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

                sr.startListening(srIntent);
            }
        }

        public void startRecording() {
            continueSR = true;
            fireSRIntent();
        }

        public void stopRecording() {
            continueSR = false;
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
            if(listener != null) {
                listener.onResult(1);
                fireSRIntent();
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

            if(listener != null) {
                listener.onResult(0);
                fireSRIntent();
            }
        }

    }
