package edu.northwestern.langlearn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

public class SleepService extends Service implements SensorEventListener, OnInitListener {
    int ONSET_DELAY = 30 * 60 * 1000; //delay in ms before sounds actually start happening, 30 min seems like a good approximation
    int BACKOFF_TIME = 60000; //backoff time if motion detected
    float MIN_CUE_VOLUME = 0.08f; //lowest the cue volume can go in response to feedback
    float CUE_DELTA = 0.00f; //how much to decrease volume with each cue
    double MOVEMENT_THRESH = 12.0; //how much movement is needed to trigger a backoff

    WordList words;

    float cue_volume = 0.15f; //initial volume of cues, can decrease if there is a motion event.
    volatile boolean shakeevent = false; //goes true on sharp motion
    int maxCycles = 20;
    float oldx = 0;
    float oldy = 0;
    float oldz = 0;
    long lastUpdate = 0;
    MediaPlayer mp;
    private SensorManager sm;
    private Sensor sa;
    SharedPreferences prefs;
    FileWriter logWriter;
    AudioManager am;
    volatile boolean cue;
    Thread sleepTalk;
    private TextToSpeech myTTS;


    public static final String SLEEP_FINISHED_INTENT = "com.sri.csl.langlearn.SLEEP_FINISHED";



    public float getBatteryLevel() { //from http://stackoverflow.com/questions/15746709/get-battery-level-only-once-using-android-sdk
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public boolean isExternalStorageWritable() { //check to make sure we can store data on the SD card.
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    void fileError() {
        Toast.makeText(this, "Logfile error. Unplug the phone and try again. If problem persists, contact nathanww@u.northwestern.edu", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(this, ParticipantMode.class);
        stopSelf();
    }

    void logTimestamp(String message) { //write something to the log file with a timestamp.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String current = sdf.format(new Date());
        try {
            String m = current + ":" + message;
            Log.d("SleepService", m);
            logWriter.write(m);
            logWriter.write("\n");
            logWriter.flush();
        } catch (Exception e) { //fail silently if we can't write
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.GERMAN);
        }
    }

    private void runSleepTalk() {
        if (sleepTalk != null) return;

        logTimestamp("Beginning sleep talk");


        sleepTalk = new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(ONSET_DELAY);
                cue = true;
                while (cue) {
                    for (int cycle = 0; cycle < maxCycles; cycle++) {
                        if (cue == false) {
                            break;
                        }
                        for (int item = 0; item < words.length; item++) {
                            if (cue == false) {
                                break;
                            }

                            SystemClock.sleep(10000); //wait 10 secs between cues
                            prefs.edit().putInt("experimentstage", 3).apply(); //signal that sleep is in progress
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, cue_volume + "");

                            String word = words.germanWords[item];
                            logTimestamp("Speaking " + word);
                            myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, params);

                            int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC); //get the system volume for logging
                            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            float volume = (float) volumeLevel / maxVolume;
                            logTimestamp("Cue=" + word + ",volume=" + cue_volume + ", battery=" + getBatteryLevel() + ",systemvolume=" + volume);
                            if (shakeevent) {

                                shakeevent = false;
                                if (cue_volume > MIN_CUE_VOLUME) {
                                    cue_volume = cue_volume - CUE_DELTA;
                                }
                                SystemClock.sleep(BACKOFF_TIME);
                            }
                        }
                    }
                    //we've done all the cues for tonight, reset to vocab test and go back to the participant mode
                    SleepService.this.sleepFinished();
                    cue = false;
                }
            }
        };

        sleepTalk.start();
    }

    private void sleepFinished() {
        prefs.edit().putInt("experimentstage", 1).apply(); //go back to vocab mode

        Intent sleepFinishedIntent = new Intent(SLEEP_FINISHED_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(sleepFinishedIntent);
        stopSelf();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            //motion detection
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                double speed = Math.sqrt((x - oldx) * (x - oldx) + (y - oldy) * (y - oldy) + (z - oldz) * (z - oldz))
                        / diffTime * 10000;
//                    float speed = Math.abs(x+y+z - oldx - oldy - oldz) / diffTime * 10000;
                if (speed > MOVEMENT_THRESH) {
                    shakeevent = true;
                    logTimestamp("Motion detected, amplitude=" + speed);
                }
                oldx = x;
                oldy = y;
                oldz = z;
            }

            if (z < -8) {
                runSleepTalk();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!isExternalStorageWritable()) { //if we can't write to the log file, abort
            fileError();
        }

        prefs = this.getSharedPreferences(
                "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        int stage = prefs.getInt("learningstage", 0);
        if (stage > 0) {
            stage = stage - 1; //since it will have been advanced at the end of the last test phase, we need to roll it back one so they only hear words they've learned.
        }

        words = new WordList(this, stage, true);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sa = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.bnoise3); //10 minute sound file with fade in and fade out on ends
        mp.setScreenOnWhilePlaying(false);
        mp.setVolume(0.1f, 0.1f);
        mp.seekTo(45000);
        mp.setLooping(true);
        try { //set up the logging, if it doesn't work then go back to the participant screen
            logWriter = new FileWriter(getExternalStorageDirectory() + "/experimentlog.txt", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        myTTS = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logTimestamp("File started");
        mp.start();
        sm.registerListener(this, sa, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();

        logTimestamp("Stopping background thread");
        cue = false;

        try {
            sleepTalk.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logTimestamp("Background thread stopped");
        try {
            logWriter.close();
        } catch (Exception e) {
            fileError();
        }
    }

}
