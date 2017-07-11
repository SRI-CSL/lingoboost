package edu.northwestern.langlearn

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)

        // val toolbar = findViewById(R.id.toolbar) as Toolbar
        // val textMessage = findViewById(R.id.errorMessage) as TextView
        // val msg = intent.getStringExtra(SleepMode.MESSAGE_INTENT_EXTRA)

        // setSupportActionBar(toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // supportActionBar?.setDisplayShowHomeEnabled(true)
        // textMessage.setText(msg)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
