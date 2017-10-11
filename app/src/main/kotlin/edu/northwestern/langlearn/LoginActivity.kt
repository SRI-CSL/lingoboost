package edu.northwestern.langlearn

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by bcooper on 10/6/17.
 */
class LoginActivity : AppCompatActivity() {
    private val TAG: String = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            if (username_edit.text.isBlank()) {
                Toast.makeText(applicationContext, R.string.username_empty_msg, Toast.LENGTH_SHORT).show()

            } else {
                val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
                val user: String = username_edit.text.toString().trim()

                if (user.indexOf('@') != -1) {
                    val splitUser: List<String> = user.split('@')
                    val url: String = Regex(Patterns.WEB_URL.toString()).matchEntire(splitUser.last())?.value ?: ""
                    val username: String = splitUser.first()

                    sharedPrefs.edit {
                        put(MainActivity.CUSTOM_SERVER to url)
                        put(MainActivity.SERVER_USER to username)
                        put(MainActivity.USER_PREF to username)
                    }.apply {  }
                    Log.d(TAG, "custom_server: $url")
                } else {
                    sharedPrefs.edit {
                        put(MainActivity.CUSTOM_SERVER to "lingoboost-dev.csl.sri.com")
                        put(MainActivity.SERVER_USER to user)
                        put(MainActivity.USER_PREF to user)
                    }.apply {  }
                }

                finish()
            }
        }
    }

    override fun onBackPressed() {
        // Prevent user from backing out of login screen to main screen
    }
}