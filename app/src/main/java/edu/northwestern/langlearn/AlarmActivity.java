package edu.northwestern.langlearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class AlarmActivity extends BroadcastReceiver {

    int DAYS_PER_TEST=1; //do a test every <x> days, here used to control whether the alarm should actually go off or not on each day.

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Bundle bundle = intent.getExtras();
            String message = bundle.getString("alarm_message");

            //first read in the current experiment state to figure out what we should be doing
            SharedPreferences prefs = context.getSharedPreferences(
                    "edu.northwestern.langlearn", Context.MODE_PRIVATE);
            int stage = prefs.getInt("experimentstage", 0);
            //find days since last test
            int lastTest=prefs.getInt("lastTestTime", -1000);
            int today=(int)((((System.currentTimeMillis()/1000)/60)/60)/24);


            if (stage < 2 && (today-lastTest) >= DAYS_PER_TEST) { //don't alarm if vocab test has been completed, or if today is not a testing day.
                Intent newIntent = new Intent(context, AlertSubject.class);
                newIntent.putExtra("alarm_message", message);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}