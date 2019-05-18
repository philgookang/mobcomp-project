package kr.ac.snu.mobcomp_project.component;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class EyeTracker extends Tracker<Face> {

    Context context;
    private final float THRES = 0.5f;

    int count = 0;
    boolean isEyeClosed = false;

    public EyeTracker(Context context){
        this.context = context;
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face){
        float leftProb = face.getIsLeftEyeOpenProbability();
        float rightProb = face.getIsRightEyeOpenProbability();

        Log.e("EyeTracker", "Blink Count: " + count);

        if(leftProb < THRES || rightProb < THRES){
            //Drowsy
            String s = "Drowsy! Left: " + leftProb + " Right: " + rightProb;
            Log.e("EyeTracker", s);

            if(!isEyeClosed)
                isEyeClosed = true;
        }
        else{
            // Normal
            String s = "Normal! Left: " + leftProb + " Right: " + rightProb;
            Log.e("EyeTracker", s);

            if(isEyeClosed){
                isEyeClosed = false;
                count++;
            }
        }
    }

    public void onMissing(Detector.Detections<Face> detections){
        super.onMissing(detections);

    }

    @Override
    public void onDone(){
        super.onDone();
    }

}
