package edu.northwestern.langlearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.anko.ToastsKt;

public class SleepMode extends AppCompatActivity implements OnCompletionListener {
    private static final String TAG = "SleepMode";

    // private PowerManager.WakeLock wl;
    private WordsProvider wordsProvider;
    private MediaPlayer mediaPlayer;
    private MediaPlayer whiteNoisePlayer;
    private String jsonWords;
    private List<Word> words;
    private Handler handler = new Handler();
    private long delayMillis = 0;
    private int wordsIndex = 0;
    @Nullable
    private BroadcastReceiver receiver;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (wordsIndex < words.size()) {
                ToastsKt.longToast(SleepMode.this, "Playing " + words.get(wordsIndex).getWord());
                playAudioUrl();
                wordsIndex++;
                handler.postDelayed(this, delayMillis);
            }
        }
    };

    public void updateJSONWords(String json) {
        Log.d(TAG, "updateJSONWords");
        this.jsonWords = json;
        this.words = wordsProvider.parseJSONWords(jsonWords);
        ToastsKt.longToast(SleepMode.this, "Words Updated");
        Log.d(TAG, "words.size: " + words.size());
        handler.postDelayed(runnable, delayMillis);
    }

    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        destroyWordsPlayer();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ActivityRecognizedIntentServices.ACTIVITY_NOTIFICATION));
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive");

                Object extra = intent.getSerializableExtra(ActivityRecognizedIntentServices.ACTIVITY);

                if (extra instanceof HashMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> activity = (HashMap<String, Integer>)intent.getSerializableExtra(ActivityRecognizedIntentServices.ACTIVITY);
                    Log.d(TAG, "Activity: " + activity.toString());
                }
            }
        };

        setContentView(R.layout.activity_sleep_mode);
        playWhiteNoiseRaw();

        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String user = sP.getString("user", "NA");
        String delayListValue = sP.getString("inactivityDelay", "1");

        setDelayMillisFromPrefs(delayListValue);
        wordsProvider = new WordsProvider("https://cortical.csl.sri.com/langlearn/user/" + user); // corticalre
        wordsProvider.fetchJSONWords(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            destroyWordsPlayer();
        }

        if (whiteNoisePlayer != null) {
            whiteNoisePlayer.stop();
            whiteNoisePlayer.release();
            whiteNoisePlayer = null;
        }

        super.onDestroy();
        // Intent serviceIntent = new Intent(this, SleepService.class);
        // stopService(serviceIntent);
        // wl.release();
        // finish();
    }

    private void playAudioUrl() {
        Log.d(TAG, "playAudioUrl");

        try {
            String url = words.get(wordsIndex).getAudio_url();

            Log.d(TAG, words.get(wordsIndex).getAudio_url());
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
        Log.d(TAG, "playWhiteNoiseRaw");
        whiteNoisePlayer = MediaPlayer.create(SleepMode.this, R.raw.bnoise3);
        whiteNoisePlayer.setVolume(0.1f, 0.1f);
        whiteNoisePlayer.seekTo(45000);
        whiteNoisePlayer.setLooping(true);
        whiteNoisePlayer.start();
    }

    private void destroyWordsPlayer() {
        Log.d(TAG, "destroyWordsPlayer");
        mediaPlayer.release();
        mediaPlayer = null;
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

    private void setDelayMillisFromPrefs(String delayListValue) {
        long minutes;

        switch (delayListValue) {
            case "2":
                minutes = 45;
                break;
            case "3":
                minutes = 15;
                break;
            default:
                minutes = 30;
        }

        delayMillis = minutes * 60 * 1000;
        Log.d(TAG, "delayMillis: " + delayMillis);
    }

    // prevent accidental press of the back button from exiting sleep mode.
    // @Override
    // public void onBackPressed() {
    //    return;
    //    // super.onBackPressed();
    // }

    // protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //     if (requestCode == MY_DATA_CHECK_CODE) {
    //                 Intent serviceIntent = new Intent(this, SleepService.class);
    //                 startService(serviceIntent);
    //                 PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
    //                 wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "LangLearnSleepLock");
    //                 wl.acquire();
    //     }
    // }

}
