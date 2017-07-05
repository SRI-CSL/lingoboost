package edu.northwestern.langlearn;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String USER_PREF = "user";
    public static final String LAST_PRACTICE_TIME_PREF = "lastPracticeTime";
    public static final String RESET_LAST_PRACTICED_PREF = "resetLastPraticed";
    public static final String VOLUME_WHITE_NOISE_PREF = "volumeWhiteNoise";
    public static final String PLAY_WHITE_NOISE_PREF = "playWhitenoise";
    public static final String WHITE_NOISE_VOLUME_PREF_DEFAULT = "0.5";
    public static final String INACTIVITY_DELAY_PREF = "inactivityDelay";
    public static final String NA_PREF = "NA";

    private static final String TAG = "MainActivity";
    private static final int SETTINGS = 1;
    @Nullable
    private GoogleApiClient googleApiClient;
    private PendingIntent activityPendingIntent;
    @Nullable
    private BroadcastReceiver receiver;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        Intent intent = new Intent(this, ActivityRecognizedIntentServices.class);

        activityPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, 4000, activityPendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    // @Override
    // protected void onResume() {
    //     Log.d(TAG, "onResume");
    //     super.onResume();
    //     LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ActivityRecognizedService.ACTIVITY_NOTIFICATION));
    // }

    // @Override
    // protected void onPause() {
    //     Log.d(TAG, "onPause");
    //     super.onPause();
    //     LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    // }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableUpNavigationInToolbar();
        checkSharedPreferences();
        createReceiver();
        // Permissions.verifyStoragePermissions(this);

        if (!checkPlayServices()) {
            return;
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        Button sleepButton = (Button)findViewById(R.id.sleep); // button to start sleep mode
        Button settingsButton = (Button)findViewById(R.id.settings);

        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SleepMode.class);

                startActivity(i);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PrefActivity.class);

                // startActivity(i);
                startActivityForResult(i, SETTINGS);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (googleApiClient != null) {
            Log.d(TAG, "Unregister and disconnect the GoogleApiClient");
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, activityPendingIntent);
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.disconnect();
            googleApiClient = null;
            activityPendingIntent = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTINGS:
                SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String user = sP.getString(USER_PREF, NA_PREF);
                boolean resetLastPraticed = sP.getBoolean(RESET_LAST_PRACTICED_PREF, false);

                Log.d(TAG, "Settings User: " + user);

                if (resetLastPraticed) {
                    sP.edit().putString(LAST_PRACTICE_TIME_PREF, NA_PREF).apply();
                    Log.d(TAG, "Settings " + LAST_PRACTICE_TIME_PREF + ": " + NA_PREF);
                    sP.edit().putBoolean(RESET_LAST_PRACTICED_PREF, false).apply();
                }

                break;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);

        if (result != ConnectionResult.SUCCESS) {
            int PLAY_SERVICES_REQUEST_CODE = 101;

            // Google Play Services app is not available or version is not up to date. Error the error condition here
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_REQUEST_CODE).show();
            }

            // Google Play Services app is not available or version is not up to date. Error the error condition here
            return false;
        }

        return true;
    }

    private void checkSharedPreferences() {
        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Log.d(TAG, LAST_PRACTICE_TIME_PREF + ": " + sP.getString("lastPracticeTime", NA_PREF));

        if (sP.getString(VOLUME_WHITE_NOISE_PREF, "NA").equalsIgnoreCase(NA_PREF)) {
            sP.edit().putString(VOLUME_WHITE_NOISE_PREF, WHITE_NOISE_VOLUME_PREF_DEFAULT).apply();
        }

        if (sP.getString(USER_PREF, "NA").equalsIgnoreCase(NA_PREF)) {
            Log.d(TAG, "Setting the defualt " + USER_PREF + " in prefs");
            sP.edit().putString(USER_PREF, "corticalre").apply();
        }
    }

    private void createReceiver() {
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
    }

    private void enableUpNavigationInToolbar() {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Unable to display the back navigation through the support action bar");
        }
    }
}
