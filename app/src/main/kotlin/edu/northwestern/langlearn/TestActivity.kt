package edu.northwestern.langlearn

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText

import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

//import com.github.kittinunf.fuel.Fuel
//import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload

//import org.jetbrains.anko.longToast
import kotlinx.android.synthetic.main.activity_words.*
import java.io.File

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

class TestActivity : WordsProviderUpdate, AppCompatActivity() {
    override val wordsProviderUpdateActivity: AppCompatActivity
        get() = this

    private val TAG = javaClass.simpleName
    private lateinit var wordsProvider: WordsProvider
    private lateinit var words: List<Word>
    private var wordsIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private val logDateToStr by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)
        // words_edit_word.isFocusable = false
        words_text_word.text = ""
        words_edit_word.hint = "Words Updating..."
        writeCSVHeader()

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val user = sP.getString(MainActivity.USER_PREF, "NA")

        wordsProvider = WordsProvider("https://cortical.csl.sri.com/langlearn/user/$user")
        wordsProvider.fetchJSONWords(this)

        words_edit_word.afterTextChanged {
            Log.d(TAG, "afterTextChanged")

            if (it.isNotEmpty()) {

                if (it.last() == '\n') {
                    val text = it.toString().replace("\n", "")

                    destroyPlayer()
                    logTestResults(text) { continueWordTesting() }
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        destroyPlayer()
        super.onDestroy()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val user = sP.getString(MainActivity.USER_PREF, "NA")
        val timeout = 60000 // 1 min

        "https://cortical.csl.sri.com/langlearn/user/$user/upload"
            .httpUpload()
            .timeout(timeout)
            .source { request, url -> File(filesDir, "log-test-$logDateToStr.txt") }
            .name { "app_log_file" }
            .progress { writtenBytes, totalBytes -> Log.d(TAG, "Upload: ${ writtenBytes.toFloat().toString() } Total: ${ totalBytes.toFloat().toString() }") }
            .responseString { request, response, result ->
                Log.d(TAG, request.cUrlString())
                val (data, error) = result

                if (error != null) {
                    Log.e(TAG, response.toString())
                    Log.e(TAG, error.toString())
                } else {
                    Log.d(TAG, "https://cortical.csl.sri.com/langlearn/user/$user/upload ${ response.httpStatusCode.toString() }:${ response.httpResponseMessage }")
                }
            }
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
            words_text_word.text = word
            words_edit_word.text.clear()
        } else {
            finish()
        }
    }

    private fun logTestResults(entry:String, next: () -> Unit) {
        val word = words.get(wordsIndex).word
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        val dateToStr = format.format(Date())

        writeFileLog("$dateToStr,$word,$entry\n");
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
        writeFileLog("timestamp,word,entry\n", false)
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
        if (mediaPlayer != null) {
            if (mediaPlayer?.isPlaying() ?: false) {
                mediaPlayer?.stop()
            }

            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
