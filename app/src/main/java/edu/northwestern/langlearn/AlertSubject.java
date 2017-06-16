package edu.northwestern.langlearn;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AlertSubject extends AppCompatActivity {
    MediaPlayer beep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_subject);
        AudioManager mgr=null;
        mgr=(AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 10, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        beep = new MediaPlayer();
        beep = MediaPlayer.create(getApplicationContext(), R.raw.alarmbeep);
        beep.setVolume(1,1);
        beep.setLooping(true);
        beep.start();
        Button startButton = (Button) findViewById(R.id.start); //button to start training
        startButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                beep.stop();
                Intent myIntent = new Intent(AlertSubject.this, ParticipantMode.class);
                AlertSubject.this.startActivity(myIntent);
            }
        });
    }
}
