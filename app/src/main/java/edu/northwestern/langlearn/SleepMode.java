package edu.northwestern.langlearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jetbrains.anko.ToastsKt;
import org.jetbrains.annotations.NotNull;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;

public class SleepMode extends AppCompatActivity implements WordsProviderUpdate, OnCompletionListener, SensorEventListener {
    public static final long DEFAULT_START_WORDS_DELAY_MILLIS = 1800000; // 30m
    public static final long DEFAULT_BETWEEN_WORDS_DELAY_MILLIS = 5000; // 5s
    public static final boolean PLAY_ONLY_WHITE_NOISE_SHAM = false;
    public static final String JSON_ERROR_MESSAGE = "";
    public static final boolean PLAY_WHITE_NOISE = true;
    public static final String INACTIVITY_OPTION_PREF_DEFAULT = "1";
    public static final String MESSAGE_INTENT_EXTRA = "message";

    private static final String TAG = "SleepMode";
    private static final int BASE_STILL_ACCEPTANCE_CONFIDENCE = 60;

    private PowerManager.WakeLock wl;
    private WordsProvider wordsProvider;
    private MediaPlayer mediaPlayer;
    private MediaPlayer whiteNoisePlayer;
    private String jsonWords;
    private List<Word> words;
    private Handler playWordsIfStillHandler = new Handler();
    private Handler pauseBetweenWordsHandler = new Handler();
    private Handler tickSensorHandler = new Handler();
    private long delayMillis = DEFAULT_START_WORDS_DELAY_MILLIS;
    private long delayBetweenWords = DEFAULT_BETWEEN_WORDS_DELAY_MILLIS;

    // TODO: In progress fields
    private boolean feedback;

    private long repeatDelay;
    private long maxLoops;
    private Handler repeatDelayHandler = new Handler();
    private Handler maxTimeHandler = new Handler();
    private Runnable maxTimeRunner = new Runnable() {
        @Override
        public void run() {
            if (maxLoops != 0) maxLoops = 0;
        }
    };
    private float rightAndLeftWordsVolume = 0.50f;
    private float rightAndLeftWhiteNoiseVolume = 0.10f;
    private int wordsIndex = 0;
    private HashMap<String, Integer> lastActivity;
    private HashMap<String, Long> lastSensor;
    private HashMap<String, Float> lastAccel;
    private EvictingQueue<Integer> magnitudes = new EvictingQueue<>(60);
    @Nullable
    private BroadcastReceiver receiver;
    private Runnable checkPlayWordsIfStillRunner = new Runnable() {
        @Override
        public void run() {
            checkAndPlayWordsIfStill();
        }
    };
    private Runnable tickSensorRunner = new Runnable() {
        @Override
        public void run() {
            onTickSensor();
        }
    };
    private String logDateToStr;
    private TextView debugActivity;
    private TextView debugSensorOrientation;
    private TextView debugSensorAceelerationChange;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;
    private boolean resumePlayWords;
    private boolean playWhiteNoise;
    private int sysStreamVolume;
    private long beginMillis;
    private String prefsUser;
    private String server;

    public void updateJSONWords(@NonNull String json) {
        Log.d(TAG, "updateJSONWords");
        jsonWords = json;
        words = wordsProvider.parseJSONWords(jsonWords);

        if (wordsProvider.getJsonStartDelay() != DEFAULT_START_WORDS_DELAY_MILLIS) {
            delayMillis = wordsProvider.getJsonStartDelay();
            Log.d(TAG, "delayMillis: " + delayMillis);
        }

        if (wordsProvider.getJsonWordDelay() != DEFAULT_BETWEEN_WORDS_DELAY_MILLIS) {
            delayBetweenWords = wordsProvider.getJsonWordDelay();
            Log.d(TAG, "delayBetweenWords: " + delayBetweenWords);
        }


        // TODO: Handle defaults, if checks
        feedback = wordsProvider.getJsonFeedback();
        repeatDelay = wordsProvider.getJsonRepeatDelay();
        maxLoops = wordsProvider.getJsonMaxLoops();
        final long maxTime = wordsProvider.getJsonMaxTime();


        ToastsKt.longToast(SleepMode.this, "Words Updated");
        Log.d(TAG, "words.size: " + words.size());

        if (!wordsProvider.getJsonError().isEmpty()) {
            openMessageActivity(wordsProvider.getJsonError());
            return;
        }

        if (!wordsProvider.getJsonSham()) {
            if (playWordsIfStillHandler != null) {
                playWordsIfStillHandler.postDelayed(checkPlayWordsIfStillRunner, delayMillis);
            }
            if (maxTime > 0 && maxTimeHandler != null) {
                maxTimeHandler.postDelayed(maxTimeRunner, maxTime * 60 * 1000);
            }
        } else {
            Log.i(TAG, "Playing only white noise, sham was true");
        }
    }

    @NonNull
    @Override
    public AppCompatActivity getWordsProviderUpdateActivity() {
        return this;
    }

    public void openMessageActivity(@NonNull String messsage) {
        finish();
        WordsProviderUpdate.DefaultImpls.openMessageActivity(this, messsage);
    }

    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        destroyWordsPlayer();

        final SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        final String dateToStr = format.format(new Date());
        final String activityLog = "\"" + lastActivity.toString() + "\"";
        final String sensorLog = "\"" + lastSensor.toString() + "\"";
        final String accelLog = "\"" + magnitudes.toString() + "\"";

        sP.edit().putString(MainActivity.LAST_PRACTICE_TIME_PREF, dateToStr).apply();
        Log.d(TAG, MainActivity.LAST_PRACTICE_TIME_PREF + ": " + dateToStr);

        final AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        final float maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int sysStreamVolumePercent = Math.round(((sysStreamVolume / maxV) * 100f));
        final int whiteNoiseVolumePercent = Math.round(rightAndLeftWhiteNoiseVolume * 100f);
        final int wordsVolumePercent = Math.round(rightAndLeftWordsVolume * 100f);

        writeFileLog(dateToStr + "," + words.get(wordsIndex).getWord() + ","  + activityLog + "," + words.get(wordsIndex).getAudio_url()
                + "," + sysStreamVolumePercent + "," + whiteNoiseVolumePercent + "," + wordsVolumePercent
                + "," + sensorLog + "," + accelLog + "\n", true);
        wordsIndex++;

        if (!resumePlayWords) {
            pauseBetweenWordsHandler.postDelayed(checkPlayWordsIfStillRunner, delayBetweenWords);
        }
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

        final SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final int timeout = 60000; // 1 min
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        final String dateToStr = format.format(new Date());
        final String activityLog = "\"" + lastActivity.toString() + "\"";
        final LanglearnApplication application = (LanglearnApplication) getApplication();

        try {
            final PackageManager packageManager = getPackageManager();
            final String packageName = getPackageName();
            String packageVersion = null;

            if (packageManager != null && packageName != null) {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);

                if (packageInfo != null) {
                    packageVersion = packageInfo.versionName;
                }
                else {
                    Log.e(TAG, "Unable to retrieve package version from PackageInfo");
                }
            }
            else {
                Log.e(TAG, "Unable to retrieve package version");
            }

            writeFileLog(dateToStr + ",," + activityLog + ",,,,,,\n", true);
            String requestUrl = ServiceRequestUtilsKt.buildRequestUrl(server, prefsUser, "upload",
                    application.getSessionId(), packageVersion)
                    .appendQueryParameter("purpose", "sleep").toString();
            Fuel.upload(requestUrl)
                    .timeout(timeout)
                    .source(new Function2<Request, URL, File>() {
                        @Override
                        public File invoke(Request request, URL url) {
                            return new File(getFilesDir(), "log-sleep-" + logDateToStr + ".txt");
                        }
                    }).name(new Function0<String>() {
                @Override
                public String invoke() {
                    return "app_log_file";
                }
            }).responseString(new com.github.kittinunf.fuel.core.Handler<String>() {
                @Override
                public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                    Log.e(TAG, response.toString());
                    Log.e(TAG, error.toString());
                }

                @Override
                public void success(@NotNull Request request, @NotNull Response response, String data) {
                    Log.d(TAG, request.cUrlString());
                    Log.d(TAG, "https://" + server + "/user/" + prefsUser + "/upload " + response.getHttpStatusCode() + ":" + response.getHttpResponseMessage());
                }
            });
            super.onStop();
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "LangLearnSleepLock");
        wl.acquire();

        if (resumePlayWords) {
            Log.d(TAG, "resume play words if still");
            playWordsIfStillHandler.postDelayed(checkPlayWordsIfStillRunner, delayMillis);
            resumePlayWords = false;
        }

        if (playWhiteNoise) {
            playWhiteNoiseRaw();
        }

        if (words != null) {
            checkAndPlayWordsIfStill();
        }

        onTickSensor();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        sensorManager.unregisterListener(this);

        if (wl != null) {
            wl.release();
        }

        playWordsIfStillHandler.removeCallbacks(checkPlayWordsIfStillRunner);
        pauseBetweenWordsHandler.removeCallbacks(checkPlayWordsIfStillRunner);
        tickSensorHandler.removeCallbacks(tickSensorRunner);
        repeatDelayHandler.removeCallbacks(checkPlayWordsIfStillRunner);
        resumePlayWords = true;
        destroyWordsPlayer();
        destroyWhiteNoisePlayer();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);
        removeSensorStatusCheck();
        Log.d(TAG, "Creating lastActivity with Still:100 as default, a null indicates the phone has been still for a long period of time, so we set the default");
        lastActivity = new HashMap<String, Integer>();
        lastActivity.put("Still", 100);
        lastSensor = new HashMap<String, Long>();
        lastSensor.put("Azimuth", 0l);
        lastSensor.put("Pitch", 0l);
        lastSensor.put("Roll", 0l);
        lastAccel = new HashMap<String, Float>();
        lastAccel.put("x", 0.0f);
        lastAccel.put("y", 0.0f);
        lastAccel.put("z", 0.0f);
        lastAccel.put("lastx", 0.0f);
        lastAccel.put("lasty", 0.0f);
        lastAccel.put("lastz", 0.0f);
        createReceiver();
        writeCSVHeader();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        debugActivity = (TextView)findViewById(R.id.debug_activity);
        debugActivity.setText(lastActivity.toString());
        debugSensorOrientation = (TextView)findViewById(R.id.debug_sensor_orientation);
        debugSensorAceelerationChange = (TextView)findViewById(R.id.debug_sensor_acceleration_change);
        resumePlayWords = false;
        beginMillis = new Date().getTime();
        onTickSensor();
        checkPreferences();

        final Button pauseButton = (Button)findViewById(R.id.pause_sleep);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!resumePlayWords) {
                    Log.d(TAG, "pause play words");
                    playWordsIfStillHandler.removeCallbacks(checkPlayWordsIfStillRunner);
                    resumePlayWords = true;
                    pauseButton.setText(R.string.resume_button);
                } else {
                    Log.d(TAG, "resume play words if still");
                    playWordsIfStillHandler.postDelayed(checkPlayWordsIfStillRunner, delayMillis);
                    resumePlayWords = false;
                    pauseButton.setText(R.string.pause_button);
                }
            }
        });
        wordsProvider.fetchJSONWords(this);
    }

    @Override
    public void onBackPressed() {
        final Intent i = new Intent(SleepMode.this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (playWordsIfStillHandler != null) {
            playWordsIfStillHandler = null;
        }

        if (pauseBetweenWordsHandler != null) {
            pauseBetweenWordsHandler = null;
        }

        if (tickSensorHandler != null) {
            tickSensorHandler = null;
        }

        if (repeatDelayHandler != null) {
            repeatDelayHandler = null;
        }

        if (maxTimeHandler != null) {
            maxTimeHandler.removeCallbacks(maxTimeRunner);
            maxTimeHandler = null;
        }

        destroyWordsPlayer();
        destroyWhiteNoisePlayer();
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
            lastAccel.put("x", gravity[ 0 ]);
            lastAccel.put("y", gravity[ 1 ]);
            lastAccel.put("z", gravity[ 2 ]);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            final float R[] = new float[9];
            final float I[] = new float[9];
            final boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);

            if (success) {
                final float orientation[] = new float[3];

                SensorManager.getOrientation(R, orientation);

                final float azimuth = orientation[0]; // orientation contains: azimuth (yaw), pitch and roll
                final float pitch = orientation[1];
                final float roll = orientation[2];

                lastSensor.put("Azimuth", Math.round(Math.toDegrees(azimuth))); // -azimuth * 360 / (2 * 3.14159f));
                lastSensor.put("Pitch", Math.round(Math.toDegrees(pitch)));
                lastSensor.put("Roll", Math.round(Math.toDegrees(roll)));
            }
        }
    }

    private void checkPreferences() {
        final SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String delayListValue = sP.getString(MainActivity.INACTIVITY_DELAY_PREF, INACTIVITY_OPTION_PREF_DEFAULT);
        final int wordsVolume = sP.getInt(MainActivity.VOLUME_WORDS_PREF, MainActivity.WORDS_VOLUME_PREF_DEFAULT);
        final int whiteNoiseVolume = sP.getInt(MainActivity.VOLUME_WHITE_NOISE_PREF, MainActivity.WHITE_NOISE_VOLUME_PREF_DEFAULT);
        final String lastPracticeTime = sP.getString(MainActivity.LAST_PRACTICE_TIME_PREF, MainActivity.NA_PREF);
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final String prefsServer = sP.getString(MainActivity.CUSTOM_SERVER, "");

        prefsUser = sP.getString(MainActivity.SERVER_USER, MainActivity.NA_PREF);
        server = prefsServer.isEmpty()? "cortical.csl.sri.com" : prefsServer;
        playWhiteNoise = sP.getBoolean(MainActivity.PLAY_WHITE_NOISE_PREF, PLAY_WHITE_NOISE);
        setDelayMillisFromPrefs(delayListValue);
        setWordsVolumeFromPrefs(wordsVolume);
        setWhiteNoiseVolumeFromPrefs(whiteNoiseVolume);
        sysStreamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC); // 0 .. 15

        if (lastPracticeTime.equalsIgnoreCase("NA")) {
            wordsProvider = new WordsProvider("https://" + server + "/langlearn/user/" + prefsUser + "?purpose=sleep");
        } else {
            wordsProvider = new WordsProvider("https://" + server + "/langlearn/user/" + prefsUser + "/since/" + lastPracticeTime + "?purpose=sleep");
        }
    }

    private void onTickSensor() {
        // Log.d(TAG, "onTickSensor");

        final float x = lastAccel.get("x");
        final float y = lastAccel.get("y");
        final float z = lastAccel.get("z");
        final double dx = x - lastAccel.get("lastx");
        final double dy = y - lastAccel.get("lasty");
        final double dz = z - lastAccel.get("lastz");
        final double sqx = Math.pow(dx, 2);
        final double sqy = Math.pow(dy, 2);
        final double sqz = Math.pow(dz, 2);
        final double accelChangeMagnitude = Math.sqrt(sqx + sqy + sqz);
        final long magnitude = Math.round(accelChangeMagnitude);

        // Log.d(TAG, "Accel Change Mag: " + magnitude);
        lastAccel.put("lastx", x);
        lastAccel.put("lasty", y);
        lastAccel.put("lastz", z);
        magnitudes.add(Long.valueOf(magnitude).intValue());
        // Log.d(TAG, lastSensor.toString());
        debugSensorOrientation.setText(lastSensor.toString());
        debugSensorAceelerationChange.setText(String.format(Locale.US, "%2d", magnitude));
        tickSensorHandler.postDelayed(tickSensorRunner, 1000); // 1s

        final Date now = new Date();
        final long diffMillis = now.getTime() - beginMillis;
        final String elapsedHoursAndMinutes = String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(diffMillis),
                TimeUnit.MILLISECONDS.toSeconds(diffMillis) -  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffMillis))
        );
        final String currentDateToStr = new SimpleDateFormat("hh:mm a", Locale.US).format(now);
        final TextView elapsedTextTime = (TextView)findViewById(R.id.text_view_elapsed_time);
        final TextView currentTextTime = (TextView)findViewById(R.id.text_view_current_time);

        elapsedTextTime.setText(elapsedHoursAndMinutes);
        currentTextTime.setText(currentDateToStr);
    }

    private void checkAndPlayWordsIfStill() {
        Log.d(TAG, "checkAndPlayWordsIfStill");

        if (!resumePlayWords) {
            if (lastActivity.containsKey(ActivityRecognizedIntentServices.STILL) && lastActivity.get(ActivityRecognizedIntentServices.STILL) > BASE_STILL_ACCEPTANCE_CONFIDENCE) {
                if (shouldPlayAudioAfterWordsIndexUpdate()) {
                    // ToastsKt.longToast(SleepMode.this, "Playing " + words.get(wordsIndex).getWord());
                    Log.d(TAG, "Playing word " + wordsIndex + " of " + words.size());
                    playAudioUrl();
                }
            } else {
                playWordsIfStillHandler.postDelayed(checkPlayWordsIfStillRunner, delayMillis);
            }
        }
    }

    private boolean shouldPlayAudioAfterWordsIndexUpdate() {
        boolean shouldPlay = true;

        if (wordsIndex >= words.size()) {
            if (maxLoops == 0) {
                Log.d(TAG, "Stopping play, max loops completed");
                shouldPlay = false;
            } else {
                Log.d(TAG, "Repeating the words list, reached the end");
                wordsIndex = 0;

                if (maxLoops > 0) {
                    --maxLoops;
                }

                if (repeatDelay > 0) {
                    Log.d(TAG, "Pausing play, repeat delay");
                    repeatDelayHandler.postDelayed(checkPlayWordsIfStillRunner, repeatDelay * 60 * 1000);
                    shouldPlay = false;
                }
            }
        }

        return shouldPlay;
    }

    private void playAudioUrl() {
        Log.d(TAG, "playAudioUrl");

        try {
            final String url = words.get(wordsIndex).getAudio_url();

            Log.d(TAG, words.get(wordsIndex).getAudio_url());
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(rightAndLeftWordsVolume, rightAndLeftWordsVolume);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void playWhiteNoiseRaw() {
        Log.d(TAG, "playWhiteNoiseRaw");
        whiteNoisePlayer = MediaPlayer.create(SleepMode.this, R.raw.bnoise3);
        whiteNoisePlayer.setVolume(rightAndLeftWhiteNoiseVolume, rightAndLeftWhiteNoiseVolume);
        whiteNoisePlayer.seekTo(45000);
        whiteNoisePlayer.setLooping(true);
        whiteNoisePlayer.start();
    }

    private void destroyWordsPlayer() {
        Log.d(TAG, "destroyWordsPlayer");

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void destroyWhiteNoisePlayer() {
        Log.d(TAG, "destroyWhiteNoisePlayer");

        if (whiteNoisePlayer != null) {
            whiteNoisePlayer.stop();
            whiteNoisePlayer.release();
            whiteNoisePlayer = null;
        }
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

    private void writeFileLog(String toLog, boolean append) {
        Log.d(TAG, "writeFileLog");

        try {
            OutputStreamWriter outputStreamWriter;

            if (append) {
                outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput("log-sleep-" + logDateToStr + ".txt", Context.MODE_APPEND));
                outputStreamWriter.append(toLog);
            } else {
                outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput("log-sleep-" + logDateToStr + ".txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(toLog);
            }

            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeCSVHeader() {
        final String activityLog = "\"" + lastActivity.toString() + "\"";

        logDateToStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(new Date());
        writeFileLog("timestamp,word,activity,audio_url,system_volume,white_noise_volume,words_volume,orientation,acceleration\n", false);
        writeFileLog(logDateToStr + ",," +  activityLog + ",,,,,,\n", true);
    }

    @SuppressWarnings("unchecked")
    private void createReceiver() {
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Log.d(TAG, "onReceive");

                Object extra = intent.getSerializableExtra(ActivityRecognizedIntentServices.ACTIVITY);

                if (extra instanceof HashMap) {
                    lastActivity = (HashMap<String, Integer>)intent.getSerializableExtra(ActivityRecognizedIntentServices.ACTIVITY);
                    debugActivity.setText(lastActivity.toString());
                    // Log.d(TAG, "Activity: " + lastActivity.toString());
                }
            }
        };
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
            case "4":
                minutes = 5;
                break;
            default: // "1"
                minutes = 30;
                // minutes = 1; // local testing
        }

        delayMillis = minutes * 60 * 1000;
        Log.d(TAG, "delayMillis: " + delayMillis);
    }

    private void setWordsVolumeFromPrefs(int volume) {
        rightAndLeftWordsVolume = volume / 100f;
        Log.d(TAG, "rightAndLeftWordsVolume: " + rightAndLeftWordsVolume);
    }

    private void setWhiteNoiseVolumeFromPrefs(int volume) {
        rightAndLeftWhiteNoiseVolume = volume / 100f;
        Log.d(TAG, "rightAndLeftWhiteNoiseVolume: " + rightAndLeftWhiteNoiseVolume);
    }

    private void removeSensorStatusCheck() {
        final PackageManager pm = getPackageManager();
        final String pn = getPackageName();
        ApplicationInfo ai = null;

        try {
            ai = pm.getApplicationInfo(pn, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not find package " + pn);
            e.printStackTrace();
        }

        if (ai != null) {
            if ((ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.i(TAG, "FLAG_DEBUGGABLE");
            }
        }

        if (pn.equals("com.sri.lingoboost.debug")) {
            Log.i(TAG, "DEBUG!");
        } else {
            TextView s1 = (TextView)findViewById(R.id.debug_activity);
            TextView s2 = (TextView)findViewById(R.id.debug_sensor_orientation);
            TextView s3 = (TextView)findViewById(R.id.debug_sensor_acceleration_change);
            View v1 = (View)findViewById(R.id.view_slepp_1);
            View v2 = (View)findViewById(R.id.view_slepp_2);

            s1.setVisibility(View.GONE);
            s2.setVisibility(View.GONE);
            s3.setVisibility(View.GONE);
            v1.setVisibility(View.GONE);
            v2.setVisibility(View.GONE);
        }
    }
}
