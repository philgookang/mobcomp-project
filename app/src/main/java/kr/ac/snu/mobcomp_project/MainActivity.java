package kr.ac.snu.mobcomp_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.snu.mobcomp_project.component.AudioRecordActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getTime(View view) {
        TextView currTime = findViewById(R.id.date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String currDateTime = sdf.format(new Date());
        currTime.setText(currDateTime);
    }

    public void startRecordActivity(View view) {
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }
}
