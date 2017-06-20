package edu.northwestern.langlearn

import android.os.Bundle
import android.content.Context
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment

class PrefActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frag =  PrefFragment()

        fragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
    }

    class PrefFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}
