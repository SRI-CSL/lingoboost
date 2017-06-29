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
    private static final String TAG = "MainActivity";
    private static final int SETTINGS = 1;
    @Nullable
    private GoogleApiClient googleApiClient;
    @Nullable
    private BroadcastReceiver receiver;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        Intent intent = new Intent(this, ActivityRecognizedIntentServices.class);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, 4000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
        setContentView(R.layout.activity_main);
        // Permissions.verifyStoragePermissions(this);

        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sP.edit().putString("user", "corticalre").apply();
        sP.edit().putString("volumeWhiteNoise", "0.1").apply();

        // String user = sP.getString("user", "NA");
        //
        // Log.d(TAG, getBaseContext().toString());
        // Log.d(TAG, user);

        // boolean playWHitenoise = sP.getBoolean("playWHitenoise", true);

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
                Intent myIntent = new Intent(MainActivity.this, SleepMode.class);
                MainActivity.this.startActivity(myIntent);
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


        // prefs.edit().putInt("lastTestTime", (int)(((((System.currentTimeMillis() + 21600000) / 1000) / 60) / 60) / 24)).apply();
        // Toast.makeText(MainActivity.this, "Scheduled", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTINGS:
                SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String user = sP.getString("user", "NA");

                Log.d(TAG, "Settings User: " + user);
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
}
