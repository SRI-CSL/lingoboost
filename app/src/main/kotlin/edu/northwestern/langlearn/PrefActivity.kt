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
        // val ( versionName, versionCode ) = packageInfo // doesn't work with non-kotlin object? (Needs to implement component1(), ... positional destructure)

        "$versionName.$versionCode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frag =  PrefFragment(ver)

        fragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
    }

    class PrefFragment(val version: String) : PreferenceFragment() {
        private val TAG: String = javaClass.simpleName

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
                    val splitUser: List<String> = user.split('@')
                    val url: String = Regex(Patterns.WEB_URL.toString()).matchEntire(splitUser.last())?.value ?: ""

                    preference.sharedPreferences.edit {
                        put(MainActivity.CUSTOM_SERVER to url)
                        put(MainActivity.SERVER_USER to splitUser.first())
                    }
                    Log.d(TAG, "custom_server: $url")
                } else {
                    preference.sharedPreferences.edit {
                        put(MainActivity.CUSTOM_SERVER to getString(R.string.default_server))
                        put(MainActivity.SERVER_USER to user)
                    }
                }

                true
            }
        }
    }
}
