package edu.northwestern.langlearn;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment frag = new MyPreferenceFragment();

        frag.ctx = getBaseContext();
        getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        private Context ctx;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onDestroy() {
            SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(ctx);
            String user = sP.getString("user", "NA");

            Log.d("PreferencesActivity", user);
            super.onDestroy();
        }
    }

}
