package edu.northwestern.langlearn;

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
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static android.os.Environment.getExternalStorageDirectory;

public class sleepMode extends AppCompatActivity implements SensorEventListener, OnInitListener {
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    boolean ttsInitialized=false;

    boolean SHAM=false; //should this phone actually give cues? if sham is true it won't
    int ONSET_DELAY=30*60*1000; //delay in ms before sounds actually start happening, 30 min seems like a good approximation
    int BACKOFF_TIME=60000; //backoff time if motion detected
    float MIN_CUE_VOLUME=0.08f; //lowest the cue volume can go in response to feedback
    float CUE_DELTA=0.00f; //how much to decrease volume with each cue
    int MOVEMENT_THRESH=10; //how much movement is needed to trigger a backoff
    int INITIAL_WORDS=40; //how many words were originally learned
    int NEW_WORDS=5; //how many new words to add with each test session
    float cue_volume=0.15f; //initial volume of cues, can decrease if there is a motion event.
    boolean shakeevent=false; //goes true on sharp motion
    int maxCycles=20;
    float oldx=0;
    float oldy=0;
    float oldz=0;
    long lastUpdate=0;
    String[] langcues;
    MediaPlayer mp;
    private SensorManager sm;
    private Sensor sa;
    boolean started=false;
    SharedPreferences prefs;
    FileWriter logWriter;
    AudioManager am;
    boolean cue;
    PowerManager.WakeLock wakeLock;

    @Override
    public void onBackPressed() { //prevent accidental press of the back button from exiting sleep mode.

        return;
    }
    public float getBatteryLevel() { //from http://stackoverflow.com/questions/15746709/get-battery-level-only-once-using-android-sdk
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }


    public boolean isExternalStorageWritable() { //check to make sure we can store data on the SD card.
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    void fileError() {
        Toast.makeText(sleepMode.this,
                "Logfile error. Unplug the phone and try again. If problem persists, contact nathanww@u.northwestern.edu", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(sleepMode.this, participantMode.class);
        sleepMode.this.startActivity(myIntent);
}
    void logTimestamp(String message) { //write something to the log file with a timestamp.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String current = sdf.format(new Date());
        try {
            logWriter.write(current + ":" + message + "\n");
            logWriter.flush();
        }
        catch (Exception e) { //fail silently if we can't write

        }

    }


    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.GERMAN);
            //myTTS.speak("TTS initialized", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (!ttsInitialized) {
                    myTTS = new TextToSpeech(this, this);
                    ttsInitialized = true;
                }
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
           final  float z = sensorEvent.values[2];
            sleepMode.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView grav = (TextView)findViewById(R.id.grav);
                    grav.setText(z+" ");
                }
            });
            //motion detection
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x+y+z - oldx - oldy - oldz) / diffTime * 10000;
                if (speed > MOVEMENT_THRESH) {
                    shakeevent=true;
                    logTimestamp("Motion detected, amplitude="+speed);
                }
                oldx=x;
                oldy=y;
                oldz=z;
            }

            if (z < -5) {
                if (!started) { //check to see if we've already kicked off this thread
                    started=true;

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                                SystemClock.sleep(ONSET_DELAY);
                                cue=true;
                                while (cue) {
                                    for (int cycle = 0; cycle < maxCycles; cycle++) {
                                        if (cue == false) {
                                            break;
                                        }
                                        for (int item = 0; item < langcues.length; item++) {
                                            if (cue == false ) {
                                                break;
                                            }
                                            SystemClock.sleep(10000); //wait 10 secs between cues
                                            prefs.edit().putInt("experimentstage", 3).apply(); //signal that sleep is in progress
                                            //disabled because we're now using tts and german
                                            /*MediaPlayer sendcue;
                                            sendcue = new MediaPlayer();
                                            sendcue = MediaPlayer.create(getApplicationContext(), cues[item]);
                                            sendcue.setVolume(cue_volume,cue_volume);
                                            sendcue.start();*/
                                            HashMap<String, String> params = new HashMap<String, String>();
                                            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, cue_volume+"");
                                            myTTS.speak(langcues[item].split(" ")[0], TextToSpeech.QUEUE_FLUSH, params);

                                            int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC); //get the system volume for logging
                                            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            float volume = (float)volumeLevel/maxVolume;
                                            logTimestamp("Cue="+langcues[item].split(" ")[0]+",volume="+cue_volume+", battery="+getBatteryLevel()+",systemvolume="+volume);
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
                                    prefs.edit().putInt("experimentstage", 1).apply(); //go back to vocab mode
                                    Intent myIntent = new Intent(sleepMode.this, participantMode.class);

                                    if (wakeLock.isHeld()) {
                                        wakeLock.release();
                                    }
                                    sleepMode.this.startActivity(myIntent);
                                    cue=false;
                                }




                        }
                    };

                    thread.start();
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this); //unregister acc listener

        //mp.stop(); //stop playing wite noise
    }

    protected void onResume() {
        super.onResume();
        sm.registerListener(this, sa, SensorManager.SENSOR_DELAY_NORMAL);
        //mp.seekTo(45000);
        //mp.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!isExternalStorageWritable()) { //if we can't write to the log file, abort
            fileError();
        }

        // Instantiate a partial wake lock to let the screen turn off while still continuing
        // the activity's work in the background

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "LangLearnSleepModeWakeLock");
        wakeLock.acquire();
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = this.getSharedPreferences(
                "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        int stage = prefs.getInt("learningstage", 0);
        if (stage > 0) {
            stage=stage-1; //since it will have been advanced at the end of the last test phase, we need to roll it back one so they only hear words they've learned.
        }
        int endIndex=INITIAL_WORDS+(stage*NEW_WORDS);
        String[] tempTranslations=getResources().getStringArray(R.array.translations);
        if (endIndex >= tempTranslations.length) {
            endIndex=tempTranslations.length-1;
        }
        langcues= Arrays.copyOfRange(tempTranslations,0,endIndex);
        Collections.shuffle(Arrays.asList(langcues)); //shuffle the translations array

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sa = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, sa , SensorManager.SENSOR_DELAY_NORMAL);
        mp = new MediaPlayer();
        mp = MediaPlayer.create(getApplicationContext(), R.raw.bnoise3); //10 minute sound file with fade in and fade out on ends
        mp.setVolume(0.1f,0.1f);
        mp.seekTo(45000);
        mp.setLooping(true);
        try { //set up the logging, if it doesn't work then go back to the participant screen
            logWriter = new FileWriter(getExternalStorageDirectory()+"/experimentlog.txt",true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        logTimestamp("File started");
        mp.start();
    }


//kill the app if we try to leave it
    @Override
    protected void onStop() {
        super.onStop();
        mp.stop();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        cue=false;
        try {
            logWriter.close();
        }
        catch (Exception e) {
            fileError();
        }
        finish();

}

}
