package edu.northwestern.langlearn

import android.util.Log

import org.json.JSONObject

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import java.net.URL

class WordsProvider(val jsonUrl: String) {
    var jsonStartDelay: Long = SleepMode.DEFAULT_START_WORDS_DELAY_MILLIS
        private set
    var jsonWordDelay: Long = SleepMode.DEFAULT_BETWEEN_WORDS_DELAY_MILLIS
        private set
    var jsonSham: Boolean = SleepMode.PLAY_ONLY_WHITE_NOISE_SHAM
        private set
    var jsonError: String = SleepMode.JSON_ERROR_MESSAGE
        private set
    var jsonVolumeDampening: Float = SleepMode.DEFAULT_WHITENOISE_DAMPENING
        private set
    var jsonStimulationStopSeconds: Int = SleepMode.DEFAULT_SIMULATION_STOP_SECONDS
        private set
    var jsonPlayWhiteNoise: Boolean = SleepMode.PLAY_WHITE_NOISE
        private set
    var jsonFeedback: Boolean = false
        private set
    var jsonRepeatDelay: Long = 0L
        private set
    var jsonMaxLoops: Long = -1L
        private set
    var jsonMaxTime: Long = -1L
        private set
    var jsonSessionStartDelay: Long = SleepMode.DEFAULT_SESSION_START_DELAY_MILLIS
        private set
    var jsonPauseDelay: Long = SleepMode.DEFAULT_PAUSE_DELAY_MILLIS
        private set

    private val TAG = javaClass.simpleName

    fun fetchJSONWords(updateImpl: WordsProviderUpdate): Unit {
        doAsync {
            URL(jsonUrl).readText { text, error ->
                if (text.isNotEmpty()) {
                    Log.d(TAG, text.length.toString())
                    uiThread { updateImpl.updateJSONWords(text) }
                } else {
                    uiThread { updateImpl.openMessageActivity(error ?: "The exception message was null") }
                }
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
        val Words = mutableListOfWords()
        val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))

        jsonObj.unless { jsonStartDelay = getLong("start_delay") * 1000L }
        jsonObj.unless {
            jsonWordDelay = getLong("word_delay") * 1000L
            jsonFeedback = getBoolean("feedback")
        }
        // These could be missing, so do them one at a time due to missing exception
        jsonObj.unless { jsonVolumeDampening = getInt("volume_dampening") / 100f }
        jsonObj.unless { jsonSham = getBoolean("sham") }
        jsonObj.unless { jsonError = getString("error") }
        jsonObj.unless { jsonRepeatDelay = getLong("repeat_delay") }
        jsonObj.unless { jsonMaxLoops = getLong("max_loops") }
        jsonObj.unless { jsonMaxTime = getLong("max_time") }
        jsonObj.unless { jsonStimulationStopSeconds = getInt("stimulation_stop") }
        jsonObj.unless { jsonPlayWhiteNoise = getBoolean("white_noise") }
        jsonObj.unless {
            jsonSessionStartDelay = -1L // TODO: Remove when backend has this
            jsonPauseDelay = -1L // TODO: Remove when backend has this
            jsonSessionStartDelay = getLong("session_start_delay") * 1000L
            jsonPauseDelay = getLong("pause_delay") * 1000L
        }
        // TODO: Remove this when the backend starts supporting these values for now sleep does, so we need to set them to something reasonable
        jsonSessionStartDelay = if (jsonSessionStartDelay == -1L) jsonStartDelay else jsonSessionStartDelay
        jsonPauseDelay = if (jsonPauseDelay == -1L) jsonSessionStartDelay else jsonPauseDelay
        jsonObj.unless {
            val words = getJSONArray("words")
            var n: String = ""
            var url: String = ""
            var t: List<String> = listOf()
            var w: String = ""

            words.asSequence<JSONObject>().forEachIndexed { index, jObj ->
                jObj.unless {
                    n = getString("norm")
                    url = getString("audio_url")
                    w = getString("word")
                }
                jObj.unless { t = getJSONArray("translations").asListOf<String>() }

                val word = Word(n, url, t, w)

                Log.d(TAG, "$index $word")
                Words.add(word)
            }
        }

        Log.d(TAG, "Session Start Delay: $jsonSessionStartDelay")
        Log.d(TAG, "Pause Delay: $jsonPauseDelay")
        Log.d(TAG, "Word Delay: $jsonWordDelay")
        Log.d(TAG, "Feedback: $jsonFeedback")
        return Words
    }
}
