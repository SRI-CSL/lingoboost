package edu.northwestern.langlearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class sleepMode extends AppCompatActivity {
    private int MY_DATA_CHECK_CODE = 0;
    boolean ttsInitialized=false;
    public static final String SLEEP_FINISHED_INTENT="com.sri.csl.langlearn.SLEEP_FINISHED";
    PowerManager.WakeLock wl;

    @Override
    public void onBackPressed() { //prevent accidental press of the back button from exiting sleep mode.
        return;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (!ttsInitialized) {
                    ttsInitialized = true;

                    Intent serviceIntent = new Intent(this, sleepService.class);
                    startService(serviceIntent);
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "LangLearnSleepLock");
                    wl.acquire();
                }
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sleepMode.this.sleepFinished();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(SLEEP_FINISHED_INTENT));
    }

    private void sleepFinished() {
        Intent myIntent = new Intent(this, participantMode.class);
        startActivity(myIntent);
    }

    //kill the app if we try to leave it
    @Override
    protected void onDestroy() {
        Log.d("sleepMode", "onStop() called");
        super.onDestroy();
        Intent serviceIntent = new Intent(this, sleepService.class);
        stopService(serviceIntent);
        wl.release();
        finish();
    }
}
