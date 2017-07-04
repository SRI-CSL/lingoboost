package edu.northwestern.langlearn

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.NotificationCompat
import android.util.Log

import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import org.jetbrains.anko.longToast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ActivityRecognizedIntentServices : IntentService(ActivityRecognizedIntentServices.TAG) {
    companion object {
        private const val TAG = "ARIntentServices"

        const val ACTIVITY_NOTIFICATION = "com.sri.csl.langlearn.receiver"
        const val ACTIVITY = "activity";
        const val CONFIDENCE_BASE_TRACK_LEVEL = 0;
        const val STILL = "Still"
    }

    var toastActivityRecognized: Boolean = false
        private set
    var activityNotifications: Boolean = false
        private set

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        setIntentRedelivery(true)

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)

        toastActivityRecognized = sP.getBoolean("toastActivityRecognized", true)
        activityNotifications = sP.getBoolean("activityNotifications", false)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?): Unit  {
        Log.d(TAG, "onHandleIntent")

        if (ActivityRecognitionResult.hasResult(intent)) {
            val result: ActivityRecognitionResult = ActivityRecognitionResult.extractResult(intent)

            handleDetectedActivities(result.getProbableActivities())
        }
    }

    private fun handleDetectedActivities(probableActivities: List<DetectedActivity>): Unit
    {
        val activityMap = HashMap<String, Int>()
        var type = "Unknown"
        var msg = ""

        for (activity in probableActivities) {
            when (activity.getType()) {
                DetectedActivity.IN_VEHICLE -> type = "In Vehicle"
                DetectedActivity.ON_BICYCLE -> type = "On Bicycle"
                DetectedActivity.ON_FOOT -> type = "On Foot"
                DetectedActivity.RUNNING -> type = "Running"
                DetectedActivity.STILL -> type = ActivityRecognizedIntentServices.STILL
                DetectedActivity.TILTING -> type = "Tilting"
                DetectedActivity.WALKING -> type = "Walking"
                DetectedActivity.UNKNOWN -> type = "Unknown"
            }

            if (activity.getConfidence() > CONFIDENCE_BASE_TRACK_LEVEL) {
                msg = """${if (!msg.isEmpty()) "$msg, " else ""}$type: ${activity.getConfidence()}"""
                activityMap.put(type, activity.getConfidence())
            }
        }

        if (toastActivityRecognized) doAsync {
            uiThread { longToast(msg) }
        }

        if (activityNotifications) {
            val builder = NotificationCompat.Builder(this)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            builder.setContentTitle("Activity Recognized")
            builder.setContentText(msg)
            builder.setSmallIcon(R.mipmap.ic_launcher)
            notificationManager.notify(0, builder.build()) // NotificationManagerCompat.from(this).notify(0, builder.build());
        }

        publishResults(activityMap)
    }

    private fun publishResults(activityMap: HashMap<String, Int>) {
        val intent = Intent(ACTIVITY_NOTIFICATION)

        intent.putExtra(ACTIVITY, activityMap)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}