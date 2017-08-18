package edu.northwestern.langlearn

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.content_message.errorMessage
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.clearTask

class MessageActivity : AppCompatActivity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val msg = intent.getStringExtra(SleepMode.MESSAGE_INTENT_EXTRA)

        debug("This never appears in the Device Monitor")
        debug("Msg: $msg") // ./adb shell setprop log.tag.LangLearn DEBUG
        errorMessage.text = msg
    }

    override fun onBackPressed() {
        startActivity(intentFor<MainActivity>().newTask().clearTask())
        finish()
    }
}
