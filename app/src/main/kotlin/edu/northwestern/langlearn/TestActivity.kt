package edu.northwestern.langlearn

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.icu.text.MessagePattern
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText

import java.text.SimpleDateFormat
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.File
import java.util.Locale
import java.util.Date

import com.github.kittinunf.fuel.httpUpload
import com.github.kittinunf.fuel.core.FuelError

import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

import kotlinx.android.synthetic.main.activity_words.*

inline fun EditText.afterTextChanged(crossinline afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged(editable.toString())
        }
    })
}

fun EditText.asString(): String = text.toString()

class TestActivity : WordsProviderUpdate, AppCompatActivity() {
    override val wordsProviderUpdateActivity: AppCompatActivity
        get() = this

    private val TAG = javaClass.simpleName
    private lateinit var wordsProvider: WordsProvider
    private lateinit var words: ListOfWords
    private var wordsIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var IsSubmitEnabled = true
    private var IsLogUploaded = false
    private val logDateToStr by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
    }
    private val wordsVolume: Float by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        prefs.getInt(MainActivity.VOLUME_WORDS_PREF, MainActivity.WORDS_VOLUME_PREF_DEFAULT) / 100f
    }
    private val sysStreamVolumeProgress: Int by lazy {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val sysStreamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC) // 0 .. 15

        Math.round((sysStreamVolume / 15f) * 100f)
    }
    private val prefsServer: String by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        prefs.getString("custom_server", "");
    }
    private val prefsUser: String by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        if (prefsServer.isEmpty()) prefs.getString(MainActivity.USER_PREF, "NA") else prefs.getString(MainActivity.USER_PREF, "NA").split("@").first()
    }
    private val server: String by lazy {
        if (prefsServer.isEmpty()) "cortical.csl.sri.com" else prefsServer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_words)
        words_text_word.text = ""
        words_edit_word.hint = "Words Updating..."
        writeCSVHeader()

        //val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        //val customServer = prefs.getString("custom_server", "");
        //val user = if (customServer.isEmpty()) {
        //    prefs.getString(MainActivity.USER_PREF, "NA")
        //} else {
        //    prefs.getString(MainActivity.USER_PREF, "NA").split("@").first()
        //}
        //val server = if (customServer.isEmpty()) "cortical.csl.sri.com" else customServer

        Log.d(TAG, "Test server user is: $prefsUser");
        Log.d(TAG, "Test server is: $server");
        wordsProvider = WordsProvider("https://$server/langlearn/user/$prefsUser?purpose=test")
        wordsProvider.fetchJSONWords(this)

        submit.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "submit OnClickListener")

            if (IsSubmitEnabled && words_edit_word.text.isNotEmpty()) {
                destroyPlayer()
                logTestResults(words_edit_word.asString()) { continueWordTesting() }
            }
        })

        words_edit_word.afterTextChanged {
            Log.d(TAG, "afterTextChanged")

            if (it.isNotEmpty()) {
                Log.d(TAG, "afterTextChanged ${ it.toString() }")

                if (it.last() == '\n') {
                    words_edit_word.setText(it.toString().replace("\n", ""))
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(intentFor<MainActivity>().newTask().clearTask())
        finish()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        destroyPlayer()
        super.onDestroy()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
        uploadLog()
    }

    override fun updateJSONWords(json: String) {
        Log.d(TAG, "updateJSONWords")
        words = wordsProvider.parseJSONWords(json)

        // longToast("Words Updated")
        Log.d(TAG, "words.size: ${ words.size }")

        if (wordsProvider.jsonError.isNotEmpty()) {
            openMessageActivity(wordsProvider.jsonError)
            return
        }

        words_edit_word.hint = "Translate this word to English"
        continueWordTesting()
    }

    private fun continueWordTesting() {
        Log.d(TAG, "continueWordTesting")

        if (wordsIndex < words.size) {
            val word = words.get(wordsIndex).word

            playAudioUrl()
            words_text_word.text = "$word (${ wordsIndex + 1} of ${ words.size })"
            words_edit_word.text.clear()
        } else {
            uploadLog()
            words_edit_word.text.clear()
            words_edit_word.hint = "Great Job! Your data is sent"
            words_edit_word.isFocusable = false
            IsSubmitEnabled = false
        }
    }

    private fun uploadLog() {
        if (IsLogUploaded) return

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val user = sP.getString(MainActivity.USER_PREF, "NA")
        val timeout = 60000 // 1 min

        "https://cortical.csl.sri.com/langlearn/user/$user/upload?purpose=test"
                .httpUpload()
                .timeout(timeout)
                .source { request, url -> File(filesDir, "log-test-$logDateToStr.txt") }
                .name { "app_log_file" }
                .progress { writtenBytes, totalBytes -> Log.d(TAG, "Upload: ${ writtenBytes.toFloat().toString() } Total: ${ totalBytes.toFloat().toString() }") }
                .responseString { request, response, result ->
                    Log.d(TAG, request.cUrlString())
                    val (data: String?, err: FuelError?) = result

                    if (err != null) {
                        Log.e(TAG, response.toString())
                        Log.e(TAG, (err as FuelError).toString())
                    } else {
                        IsLogUploaded = true
                        Log.d(TAG, "https://cortical.csl.sri.com/langlearn/user/$user/upload ${ response.httpStatusCode.toString() }:${ response.httpResponseMessage }")
                    }
                }
    }

    private fun logTestResults(entry:String, next: () -> Unit) {
        val word = words.get(wordsIndex).word
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        val dateToStr = format.format(Date())

        writeFileLog("$dateToStr,$word,$entry,$sysStreamVolumeProgress,${ Math.round(wordsVolume * 100f) }\n");
        wordsIndex++
        next();
    }

    private fun writeFileLog(toLog: String, append: Boolean = true) {
        Log.d(TAG, "writeFileLog")

        try {
            val outputStreamWriter: OutputStreamWriter

            if (append) {
                outputStreamWriter = OutputStreamWriter(baseContext.openFileOutput("log-test-$logDateToStr.txt", Context.MODE_APPEND))
                outputStreamWriter.append(toLog)
            } else {
                outputStreamWriter = OutputStreamWriter(baseContext.openFileOutput("log-test-$logDateToStr.txt", Context.MODE_PRIVATE))
                outputStreamWriter.write(toLog)
            }

            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: ${ e.toString() }")
        }
    }

    private fun writeCSVHeader() {
        writeFileLog("timestamp,word,entry,system_volume,words_volume\n", false)
    }

    private fun playAudioUrl() {
        Log.d(TAG, "playAudioUrl")

        try {
            val url = words.get(wordsIndex).audio_url

            Log.d(TAG, words[wordsIndex].audio_url)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepare()
            mediaPlayer?.setVolume(wordsVolume, wordsVolume)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                Log.d(TAG, "onCompletion")
                destroyPlayer()
            }
        } catch (ex: IOException) {
            Log.e("Exception", "File write failed: $ex.toString()")
        }
    }

    private fun destroyPlayer() {
        if (mediaPlayer?.isPlaying() ?: false) {
            mediaPlayer?.stop()
        }

        mediaPlayer?.release()
        mediaPlayer = null
    }
}
