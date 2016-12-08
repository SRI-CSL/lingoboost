package edu.northwestern.langlearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by nathan on 11/1/2016.
 */

public class resetActivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        int stage = prefs.getInt("experimentstage", 0);
        if (stage > 0) { //only do this if the person has already started.
            prefs.edit().putInt("experimentstage", 1).apply(); //go back to vocab mode
        }
    }
}
