package edu.northwestern.langlearn

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

import java.net.URL

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException

//import org.jetbrains.anko.longToast // can't do this, not an activity

data class Word(val norm: String, val audio_url: String, val word: String)

class WordsProvider(val jsonUrl: String) {
    fun fetchJSONWords(sleepModeActivity: SleepMode): Unit {
        doAsync {
            try {
                val json = URL(jsonUrl).readText()

                Log.d(javaClass.simpleName, json.length.toString())
                uiThread { sleepModeActivity.updateJSONWords(json) } // if (!sleepModeActivity.isFinishing) uiThread does this since it is used by an Activity
            } catch (e: UnknownHostException) {
                Log.e(javaClass.simpleName, e.message)
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

        try {
            val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))
            val wordJson = jsonObj.getJSONArray("words") // JSONException ?

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
