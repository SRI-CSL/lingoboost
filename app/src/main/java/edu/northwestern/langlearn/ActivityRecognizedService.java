package edu.northwestern.langlearn;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
// import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityRecognizedService extends IntentService {
    private static final String TAG = "ARService";
    private List<Map> activityList;

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
        // init();
    }

    public ActivityRecognizedService(String name) {
        super(name);
        // init();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        activityList = new ArrayList<>();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("ActivityRecogition", "Handling intent in ActivityRecognizedService");

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    // @Override
    // public IBinder onBind() {
    //
    // }

    // private void init() {
    //     activityList = new ArrayList<>();
    // }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Map<String, Integer> activityMap = new HashMap<>();
        String type = "Unknown";
        String msg = "";

        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e(TAG, "In Vehicle: " + activity.getConfidence());
                    type = "In Vehicle";
                    break;
                } case DetectedActivity.ON_BICYCLE: {
                    Log.e(TAG, "On Bicycle: " + activity.getConfidence());
                    type = "On Bicycle";
                    break;
                } case DetectedActivity.ON_FOOT: {
                    Log.e(TAG, "On Foot: " + activity.getConfidence());
                    type = "On Foot";
                    break;
                } case DetectedActivity.RUNNING: {
                    Log.e(TAG, "Running: " + activity.getConfidence());
                    type = "Running";
                    break;
                } case DetectedActivity.STILL: {
                    Log.e(TAG, "Still: " + activity.getConfidence());
                    type = "Still";
                    break;
                } case DetectedActivity.TILTING: {
                    Log.e(TAG, "Tilting: " + activity.getConfidence());
                    type = "Tilting";
                    break;
                } case DetectedActivity.WALKING: {
                    Log.e(TAG, "Walking: " + activity.getConfidence());
                    type = "Walking";
                    break;
                } case DetectedActivity.UNKNOWN: {
                    Log.e(TAG, "Unknown: " + activity.getConfidence());
                    break;
                }
            }

            if (activity.getConfidence() > 0) {
                msg += type + ": " + activity.getConfidence() + ", ";
            }

            activityMap.put(type, activity.getConfidence());
        }

        // ToastsKt.longToast(ActivityRecognizedService.this, msg); // doesn't work in the service!

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        msg = msg.substring(0, msg.length() - 2);
        builder.setContentTitle("Activity Recognized");
        builder.setContentText(msg);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(0, builder.build());
        // NotificationManagerCompat.from(this).notify(0, builder.build());

        if (activityList.size() > 100) {
            activityList.clear();
        }

        activityList.add(activityMap);
    }
}
