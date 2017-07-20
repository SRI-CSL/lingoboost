package edu.northwestern.langlearn

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import java.net.URL
import java.net.UnknownHostException
import java.io.FileNotFoundException

data class Word(val norm: String, val audio_url: String, val word: String)

interface WordsProviderUpdate {
    val wordsProviderUpdateActivity: AppCompatActivity

    fun updateJSONWords(json: String)
    fun openMessageActivity(errorMsg: String) {
        val msgIntent = Intent(wordsProviderUpdateActivity, MessageActivity::class.java)
        val MESSAGE_INTENT_EXTRA = "message"

        msgIntent.putExtra(MESSAGE_INTENT_EXTRA, errorMsg)
        wordsProviderUpdateActivity.startActivity(msgIntent)
    }
}

class WordsProvider(val jsonUrl: String) {
    var jsonStartDelay: Long = SleepMode.DEFAULT_START_WORDS_DELAY_MILLIS
        private set
    var jsonWordDelay: Long = SleepMode.DEFAULT_BETWEEN_WORDS_DELAY_MILLIS
        private set
    var jsonSham: Boolean = SleepMode.PLAY_ONLY_WHITE_NOISE_SHAM
        private set
    var jsonError: String = SleepMode.JSON_ERROR_MESSAGE
        private set

    private val TAG = javaClass.simpleName

    fun fetchJSONWords(updateImpl: WordsProviderUpdate): Unit {
        doAsync {
            var errorMsg: String? = ""

            try {
                val json = URL(jsonUrl).readText()

                Log.d(javaClass.simpleName, json.length.toString())
                uiThread { updateImpl.updateJSONWords(json) } // if (!sleepModeActivity.isFinishing) uiThread does this since it is used by an Activity
            } catch (e: UnknownHostException) {
                Log.e(javaClass.simpleName, e.message)
                errorMsg = e.message
            } catch (e: FileNotFoundException) {
                Log.e(javaClass.simpleName, e.message)
                errorMsg = e.message
            }

            if (errorMsg?.isNotEmpty() ?: true) {
                uiThread { updateImpl.openMessageActivity(errorMsg ?: "The exception message was null") }
            }
        }
    }

    fun parseJSONWords(wordsJSON: String?): List<Word> {
        val json = wordsJSON ?: """
        {
            "words": [
                {
                    "norm": "velvet",
                    "audio_url": "http://someplace.cool",
                    "word": "velvet"
                }
            ]
        }
        """
        val Words: MutableList<Word> = mutableListOf()
        val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))



        jsonObj.getItLong("start_delay") { jsonStartDelay = it * 1000 }
        jsonObj.getItLong("word_delay") { jsonWordDelay = it * 1000 }
        jsonObj.getItBoolean("sham") { jsonSham = it }
        jsonObj.getItString("error") { jsonError = it }

        jsonObj.getItJSONArray("words") {
            for (i in 0..it.length() - 1) {
                val n = it.getJSONObject(i).returnItString("norm")         // getString("norm")
                val url = it.getJSONObject(i).returnItString("audio_url")  // getString("audio_url")
                val w = it.getJSONObject(i).returnItString("word")         // getString("word")
                val word = Word(n, url, w)

                Log.d(TAG, "$i $word")
                Words.add(word)
            }
        }

        return Words
    }
}
