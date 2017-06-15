package edu.northwestern.langlearn;

//import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
//import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
//import android.net.Uri;


import android.media.MediaPlayer;
import android.media.AudioManager;
//import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import org.jetbrains.anko.ToastsKt;


public class SleepMode extends AppCompatActivity {
    //    private int MY_DATA_CHECK_CODE = 0;
    //    private boolean ttsInitialized = false;
    public static final String SLEEP_FINISHED_INTENT = "com.sri.csl.langlearn.SLEEP_FINISHED";


    private PowerManager.WakeLock wl;
    private MediaPlayer mediaPlayer;

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    };


    public void updateJSONWords(String json) {
        Log.d("SleepMode", json);
        ToastsKt.longToast(SleepMode.this, "Words Updated");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);


         // mediaPlayer = MediaPlayer.create(SleepMode.this, R.raw.kvinnan);
         // mediaPlayer.start();
         // mediaPlayer.setOnCompletionListener(onCompletionListener);


        WordsAdapter wA = new WordsAdapter();
        // List<Word> w = wA.ParseJson(null);

        wA.fetchJSONWords("https://cortical.csl.sri.com/langlearn/user/corticalre", this);


        try {
            InputStream is = getResources().openRawResource(R.raw.corticalre);
            int size = is.available();
            byte[ ] buffer = new byte[ size ];
            int num  = is.read(buffer);

            is.close();

            String json = new String(buffer, "UTF-8");
            List<Word> words = wA.parseJSONWords(json);



            Log.d("SleepMode", "words.size: " + words.size());



            String url = words.get(0).getAudio_url();
            Log.d("SleepMode", words.get(0).getAudio_url());
//            Uri mpUrl = Uri.parse(words.get(0).getAudio_url());




            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(onCompletionListener);

        } catch (IOException ex) {
            ex.printStackTrace();
        }


        //        Intent checkTTSIntent = new Intent();
        //        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        //        BroadcastReceiver receiver = new BroadcastReceiver() {
        //            @Override
        //            public void onReceive(Context context, Intent intent) {
        //                sleepMode.this.sleepFinished();
        //            }
        //        };

        //        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(SLEEP_FINISHED_INTENT));
    }

    @Override
    protected void onDestroy() {
        Log.d("sleepMode", "onStop() called");

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();

        //        Intent serviceIntent = new Intent(this, sleepService.class);
        //        stopService(serviceIntent);
        //        wl.release();
        //        finish();
    }

    // prevent accidental press of the back button from exiting sleep mode.
    // @Override
    // public void onBackPressed() {
    //    return;
    //    // super.onBackPressed();
    // }

    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        if (requestCode == MY_DATA_CHECK_CODE) {
    //            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
    //                if (!ttsInitialized) {
    //                    ttsInitialized = true;
    //
    //                    Intent serviceIntent = new Intent(this, sleepService.class);
    //                    startService(serviceIntent);
    //                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
    //                    wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "LangLearnSleepLock");
    //                    wl.acquire();
    //                }
    //            }
    //            else {
    //                Intent installTTSIntent = new Intent();
    //                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    //                startActivity(installTTSIntent);
    //            }
    //        }
    //    }

    //    private void sleepFinished() {
    //        Intent myIntent = new Intent(this, participantMode.class);
    //        startActivity(myIntent);
    //    }
}
