package edu.northwestern.langlearn

import android.content.pm.PackageInfo
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment

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
        constructor():this("Unkown")

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            val pc: PreferenceCategory? = findPreference("pref_build_key") as PreferenceCategory

            pc?.setTitle(version)
        }
    }
}
