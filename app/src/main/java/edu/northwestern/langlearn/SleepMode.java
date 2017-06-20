package edu.northwestern.langlearn;

//import android.content.Intent;
//import android.speech.tts.TextToSpeech;
//import android.net.Uri;
//import android.os.PowerManager;
//import android.media.MediaPlayer.OnPreparedListener;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import org.jetbrains.anko.ToastsKt;


public class SleepMode extends AppCompatActivity implements OnCompletionListener {
    //    private int MY_DATA_CHECK_CODE = 0;
    //    private boolean ttsInitialized = false;
    //    private PowerManager.WakeLock wl;

    private WordsProvider wordsProvider;
    private MediaPlayer mediaPlayer;
    private String jsonWords;
    private List<Word> words;
    private Handler handler = new Handler();

    private int wordsIndex = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (wordsIndex < words.size()) {
                ToastsKt.longToast(SleepMode.this, "Playing " + words.get(wordsIndex).getWord());
                playAudioUrl();
                wordsIndex++;
                handler.postDelayed(this, 5000);
            }
        }
    };

    public void updateJSONWords(String json) {
        this.jsonWords = json;
        this.words = wordsProvider.parseJSONWords(jsonWords);
        ToastsKt.longToast(SleepMode.this, "Words Updated");

        Log.d("SleepMode", "words.size: " + words.size());
        handler.postDelayed(runnable, 5000);
    }

    public void onCompletion(MediaPlayer mp) {
        Log.d("SleeMode", "nediaPlayer completion");
        mediaPlayer.release();
        mediaPlayer = null;
        playWhiteNoiseRaw();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);
        playWhiteNoiseRaw();

        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String user = sP.getString("user", "NA");

        wordsProvider = new WordsProvider("https://cortical.csl.sri.com/langlearn/user/" + user); // corticalre
        wordsProvider.fetchJSONWords(this);

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
        Log.d("sleepMode", "onDestroy");

        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();

        //        Intent serviceIntent = new Intent(this, SleepService.class);
        //        stopService(serviceIntent);
        //        wl.release();
        //        finish();
    }

    private void playAudioUrl() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            String url = words.get(wordsIndex).getAudio_url();

            Log.d("SleepMode", words.get(wordsIndex).getAudio_url());

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void playWhiteNoiseRaw() {
        mediaPlayer = MediaPlayer.create(SleepMode.this, R.raw.bnoise);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void loadWordsJsonRes() {
        try {
            InputStream is = getResources().openRawResource(R.raw.corticalre);
            int size = is.available();
            byte[ ] buffer = new byte[ size ];
            int num  = is.read(buffer);

            is.close();
            jsonWords = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void playMP3Raw() {
         mediaPlayer = MediaPlayer.create(SleepMode.this, R.raw.kvinnan);
         mediaPlayer.start();
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
    //                    Intent serviceIntent = new Intent(this, SleepService.class);
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
    //        Intent myIntent = new Intent(this, ParticipantMode.class);
    //        startActivity(myIntent);
    //    }
}
