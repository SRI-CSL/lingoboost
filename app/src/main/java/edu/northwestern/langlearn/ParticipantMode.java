package edu.northwestern.langlearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class ParticipantMode extends AppCompatActivity {
    int MIN_SLEEP_START=20; //the start of the sleeping period
    int MAX_SLEEP_START=3; //end of the sleeping period
    int DAYS_PER_TEST=1; //do a test every <x> days

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_participant_mode);

        Permissions.verifyStoragePermissions(this);

        // final Handler handler = new Handler(); //set up a handler that will reinitialize this screen every 10 minutes (to keep updated when the sleep time windows roll around)
        // handler.postDelayed(new Runnable() {
        //     @Override
        //     public void run() {
        //         ParticipantMode.super.recreate();
        //     }
        // }, 30000);
        String[] headers = getResources().getStringArray(R.array.headers);
        String[] descriptions = getResources().getStringArray(R.array.descriptions);

        SharedPreferences prefs = this.getSharedPreferences(
                "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        int stage = prefs.getInt("experimentstage", 0);
        int lastTest = prefs.getInt("lastTestTime", -1000);
        int today=(int)((((System.currentTimeMillis() / 1000) / 60) / 60) / 24);

        Log.e("timediff",today+","+lastTest+","+(today-lastTest));

        // TextView header = (TextView)findViewById(R.id.header);
        // TextView text = (TextView)findViewById(R.id.text);
        // Button dButton = (Button)findViewById(R.id.debug); //button to go to debug mode

        // dButton.setOnClickListener( new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Intent myIntent = new Intent(ParticipantMode.this, MainActivity.class);
        //         ParticipantMode.this.startActivity(myIntent);
        //     }
        // });

        //        if (stage == 0) { //first run, we need to do the initial encoding
        //            header.setText(headers[0]);
        //            text.setText(descriptions[0]);
        //            Button pButton = (Button) findViewById(R.id.pbutton); //button to start participant mode
        //            pButton.setOnClickListener( new View.OnClickListener() {
        //                @Override
        //                public void onClick(View v) {
        //                    Intent myIntent = new Intent(ParticipantMode.this, encodingInstructions.class);
        //                    ParticipantMode.this.startActivity(myIntent);
        //                }
        //            });
        //        } else if (stage == 1 && (today - lastTest) >= DAYS_PER_TEST) { // we need to do the vocab test. Do this only if more than DAYS_PER_TEST days have elapsed since the last test, to allow for testing every other day
        //            header.setText(headers[1]);
        //            text.setText(descriptions[1]);
        //
        //            Button pButton = (Button) findViewById(R.id.pbutton); //button to start participant mode
        //            pButton.setOnClickListener( new View.OnClickListener() {
        //                @Override
        //                public void onClick(View v) {
        //                    Intent myIntent = new Intent(ParticipantMode.this, WordTest.class);
        //                    ParticipantMode.this.startActivity(myIntent);
        //                }
        //            });
        //        } else { // we need to do the sleep period
        //            Calendar c = Calendar.getInstance();
        //            int hour = c.get(Calendar.HOUR_OF_DAY);
        //
        //            if (hour >= MIN_SLEEP_START || hour <= MAX_SLEEP_START) { //we're in the sleeping window
        //                header.setText(headers[2]);
        //                text.setText(descriptions[2]);
        //                Button pButton = (Button) findViewById(R.id.pbutton); //button to start participant mode
        //                pButton.setEnabled(true);
        //                pButton.setOnClickListener( new View.OnClickListener() {
        //                    @Override
        //                    public void onClick(View v) {
        //                        Intent myIntent = new Intent(ParticipantMode.this, SleepMode.class);
        //
        //                        ParticipantMode.this.startActivity(myIntent);
        //                    }
        //                });
        //            } else {
        //                header.setText(headers[2]);
        //                text.setText(descriptions[3]);
        //                Button pButton = (Button) findViewById(R.id.pbutton);
        //                pButton.setEnabled(false);
        //            }
        //        }

    }
}
