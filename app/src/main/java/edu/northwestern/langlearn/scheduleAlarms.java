package edu.northwestern.langlearn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class scheduleAlarms extends BroadcastReceiver {
    int REMINDER_TIME=19; //24-hour hour when the reminder to do the task will occur
    int GIVEUP_TIME=3; //24-hour hour when we will reset back to vocab learning if sleep hasn't been activated.
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("langlearnboot","startup");
        //when the system boots up, create an alarm that will trigger a check of whether the subject has done the vocab test today
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, REMINDER_TIME);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Intent reminder = new Intent(context, alarmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,  reminder, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        //create another alarm that will reset back to the learning phase each day after sleep if the participant hasn't done it yet

        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, GIVEUP_TIME);
        AlarmManager resetMgr = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Intent reset = new Intent(context, resetActivity.class);
        PendingIntent resetIntent = PendingIntent.getBroadcast(context, 0,  reset, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, resetIntent);


    }

}