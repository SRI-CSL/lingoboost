package edu.northwestern.langlearn;

//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.os.SystemClock;
//import android.widget.Toast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final int THIRTY_MINUTE_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Permissions.verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);

        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sP.edit().putString("user", "corticalre").apply();

        String strUserName = sP.getString("user", "NA");

        Log.d("MainActivity", getBaseContext().toString());
        Log.d("MainActivity", strUserName);

        // boolean bAppUpdates = sP.getBoolean("playWHitenoise", true);
        // String downloadType = sP.getString("inactivityDelay", "1");

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
                Intent i = new Intent(MainActivity.this, LanglearnPreferencesActivity.class);
//                startActivity(i);
                startActivityForResult(i, 1);
            }
        });

        //        Button resetButton = (Button) findViewById(R.id.reset); //button to reset progress
        //        resetButton.setOnClickListener( new View.OnClickListener() {
        //
        //            @Override
        //            public void onClick(View v) {
        //                prefs.edit().putInt("experimentstage", 1).apply();
        //                prefs.edit().putInt("learningstage", 0).apply();
        //                //prefs.edit().putInt("lastTestTime", -1000).apply();
        //
        //                prefs.edit().putInt("lastTestTime", (int)(((((System.currentTimeMillis()+21600000)/1000)/60)/60)/24)).apply();
        //            }
        //        });

        //        Button nTest = (Button) findViewById(R.id.ntest); //button to test notifications
        //        nTest.setOnClickListener( new View.OnClickListener() {
        //
        //            @Override
        //            public void onClick(View v) {
        //                Toast.makeText(MainActivity.this,
        //                        "Scheduled", Toast.LENGTH_LONG).show();
        //                AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        //                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
        //                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //                //alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),10000, pendingIntent);
        //                alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000, pendingIntent);
        //                //alarmMgr.setAndAllowWhileIdle();
        //               // Intent myIntent = new Intent(MainActivity.this, AlertSubject.class);
        //               // MainActivity.this.startActivity(myIntent);
        //            }
        //        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case THIRTY_MINUTE_SETTINGS:
                SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String user = sP.getString("user", "NA");

                Log.d("MainActivity", user);
                break;
        }
    }



    //    private void scheduleNotification(Notification notification, int delay) {
        //
        //        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        //        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        //        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        //        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //
        //        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        //        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        //    }

        //    private Notification getNotification(String content) {
        //        Notification.Builder builder = new Notification.Builder(this);
        //        builder.setContentTitle("Scheduled Notification");
        //        builder.setContentText(content);
        //        builder.setSmallIcon(R.mipmap.ic_launcher);
        //        return builder.build();
        //    }
}
