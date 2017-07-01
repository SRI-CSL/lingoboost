package edu.northwestern.langlearn

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

import java.net.URL

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException
import java.io.FileNotFoundException

data class Word(val norm: String, val audio_url: String, val word: String)

class WordsProvider(val jsonUrl: String) {
    var jsonStartDelay: Long = SleepMode.DEFAULT_START_WORDS_DELAY_MILLIS
        private set
    var jsonWordDelay: Long = SleepMode.DEFAULT_BETWEEN_WORDS_DELAY_MILLIS
        private set
    var jsonSham: Boolean = SleepMode.PLAY_ONLY_WHITE_NOISE_SHAM
        private set
    var jsonError: String = SleepMode.JSON_ERROR_MESSAGE
        private set

    fun fetchJSONWords(sleepModeActivity: SleepMode): Unit {
        doAsync {
            var errorMsg: String? = ""

            try {
                val json = URL(jsonUrl).readText()

                Log.d(javaClass.simpleName, json.length.toString())
                uiThread { sleepModeActivity.updateJSONWords(json) } // if (!sleepModeActivity.isFinishing) uiThread does this since it is used by an Activity
            } catch (e: UnknownHostException) {
                Log.e(javaClass.simpleName, e.message)
                errorMsg = e.message
            } catch (e: FileNotFoundException) {
                Log.e(javaClass.simpleName, e.message)
                errorMsg = e.message
            }

            if (errorMsg?.isNotEmpty() ?: true) {
                uiThread { sleepModeActivity.openMessageActivity(errorMsg ?: "The exception message was null") }
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
        // val Words: Array<Word> = Array(0) { idx -> Word(name = "$idx") } // Array(0, { idx -> Word(name = "$idx") })
        // val Words: Array<Word> = Array(0) { Word(name = "$it") }
        val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))

        try {
            val startDelay = jsonObj.getLong("start_delay")

            jsonStartDelay = startDelay * 1000
        } catch (e: JSONException) {
            Log.w(javaClass.simpleName, e.message)
        }

        try {
            val wordDelay = jsonObj.getLong("word_delay")

            jsonWordDelay = wordDelay * 1000
        } catch (e: JSONException) {
            Log.w(javaClass.simpleName, e.message)
        }

        try {
            val sham = jsonObj.getBoolean("sham")

            jsonSham = sham
        } catch (e: JSONException) {
            Log.i(javaClass.simpleName, e.message)
        }

        try {
            val error = jsonObj.getString("error")

            jsonError = error
        } catch (e: JSONException) {
            Log.i(javaClass.simpleName, e.message)
        }

        try {
            val wordJson = jsonObj.getJSONArray("words")

            for (i in 0..wordJson!!.length() - 1) {
                val n = wordJson.getJSONObject(i).getString("norm")
                val url = wordJson.getJSONObject(i).getString("audio_url")
                val w = wordJson.getJSONObject(i).getString("word")
                val word = Word(n, url, w)

                Log.d(javaClass.simpleName, "$i $word")
                Words.add(word)
            }
        } catch (e: JSONException) {
            Log.e(javaClass.simpleName, e.message)
        }

        return Words
    }
}
