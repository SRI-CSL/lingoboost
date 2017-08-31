package edu.northwestern.langlearn

import android.content.pm.PackageInfo
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.util.Log
import android.util.Patterns

class PrefActivity : PreferenceActivity() {
    private val ver by lazy {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName: String = packageInfo.versionName
        val versionCode: Int = packageInfo.versionCode

        "$versionName.$versionCode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frag =  PrefFragment(ver)

        fragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
    }

    class PrefFragment(val version: String) : PreferenceFragment() {
        constructor():this("undefined")

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            val pc: PreferenceCategory? = findPreference("pref_build_key") as PreferenceCategory
            val userEditPref = findPreference("user") as EditTextPreference

            pc?.setTitle(version)
            userEditPref.setOnPreferenceChangeListener { preference, newValue ->
                val user = newValue as String

                if (user.indexOf('@') != -1 ) {
                    //val m = Patterns.WEB_URL.matcher("https://${ user.split('@').last() }")
                    //val url = if (m.find()) m.group() else ""
                    val url: String = Regex(Patterns.WEB_URL.toString()).matchEntire(user.split('@').last())?.value ?: ""

                    preference.sharedPreferences.edit { put("custom_server" to url) }
                    Log.d(javaClass.simpleName, "custom_server: $url")
                } else {
                    preference.sharedPreferences.edit { put("custom_server" to "") }
                }

                true
            }
        }
    }
}
