package edu.northwestern.langlearn

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

import java.text.SimpleDateFormat
import java.io.IOException

import edu.northwestern.langlearn.log.CSVEventLogger
import edu.northwestern.langlearn.log.LogEventAction

import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

import kotlinx.android.synthetic.main.activity_words.*
import java.util.*

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

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    imm.hideSoftInputFromWindow(windowToken, 0)
}

class TestActivity : WordsProviderUpdate, AppCompatActivity() {
    override val wordsProviderUpdateActivity: AppCompatActivity
        get() = this

    private val TAG = javaClass.simpleName
    private lateinit var wordsProvider: WordsProvider
    private lateinit var words: ListOfWords
    private var wordsIndex = -1
    private var numCorrectGuesses = 0
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

        Math.round((sysStreamVolume / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()) * 100f)
    }
    private val prefsServer: String by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        prefs.getString(MainActivity.CUSTOM_SERVER, "")
    }
    private val prefsUser: String by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        prefs.getString(MainActivity.SERVER_USER, MainActivity.NA_PREF)
    }
    private val server: String by lazy {
        if (prefsServer.isEmpty()) "cortical.csl.sri.com" else prefsServer
    }
    private var eventLogger: CSVEventLogger? = null
    private lateinit var submitClickListener: View.OnClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_words)
        words_text_word.text = ""
        words_edit_word.hint = "Words Updating..."
        eventLogger = CSVEventLogger("test", baseContext)
        eventLogger?.writeHeader(listOf("timestamp", "word", "entry",
                "system_volume", "words_volume"))

        logEvent(LogEventAction.SYSTEM_EVENT_ONCREATE)

        Log.d(TAG, "Test server user is: $prefsUser");
        Log.d(TAG, "Test server is: $server");
        wordsProvider = WordsProvider("https://$server/${ getString(R.string.server_root_path) }/user/$prefsUser?purpose=test")
        wordsProvider.fetchJSONWords(this)
        submitClickListener = View.OnClickListener {
            Log.d(TAG, "submit OnClickListener")

            if (IsSubmitEnabled && words_edit_word.text.isNotEmpty()) {
                Log.d(TAG, "Submitted: ${words_edit_word.text}. Actual answer(s): ${ words[wordsIndex].translations }")

                // TODO: Temporarily(?) removed until correctness algorithm is finalized
//                val correctMatches = words[wordsIndex].getMatches(words_edit_word.text.toString())
//
//                if (correctMatches.isNotEmpty()) {
//                    Log.d(TAG, "Correct matches: $correctMatches")
//                    numCorrectGuesses++
//                }
//                else {
//                    Log.d(TAG, "User submitted incorrect guess")
//                }

                destroyPlayer()
                logTestResults(words_edit_word.asString()) { showTranslations() }
            }
        }
        submit.setOnClickListener(submitClickListener)
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
        logEvent(LogEventAction.USER_EVENT_BACK_BUTTON)
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
        logEvent(LogEventAction.SYSTEM_EVENT_ONSTOP)
        uploadLog()
        super.onStop()
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
        words_edit_word.showKeyboard()
        continueWordTesting()
    }

    private fun showTranslations() {
        if (wordsProvider.jsonFeedback) {
            val translations = words[wordsIndex].translations

            words_text_list_of_translations.setText(translations.toString())
            submit.setText(R.string.continue_button)
            words_text_translations.visibility = View.VISIBLE
            words_text_list_of_translations.visibility = View.VISIBLE

            // TODO: Temporarily(?) removed until correctness algorithm is finalized
//            test_score_text.visibility = View.VISIBLE
//            test_score_text.text = "Score: $numCorrectGuesses out of ${ wordsIndex + 1 } correct"

            words_edit_word.hideKeyboard()
            runOnUiThread { words_edit_word.isEnabled = false }
            submit.setOnClickListener(View.OnClickListener {
                submit.setText(R.string.submit_button)
                words_text_translations.visibility = View.GONE
                words_text_list_of_translations.visibility = View.GONE
                submit.setOnClickListener(submitClickListener)
                words_edit_word.showKeyboard()
                runOnUiThread { words_edit_word.isEnabled = true }
                continueWordTesting()
            })
        } else {
            continueWordTesting()
        }
    }

    private fun continueWordTesting() {
        Log.d(TAG, "continueWordTesting")
        wordsIndex++

        if (wordsIndex < words.size) {
            val word = words[wordsIndex].word

            playAudioUrl()
            words_text_word.text = "$word (${ wordsIndex + 1} of ${ words.size })"
            words_edit_word.text.clear()
        } else {
            uploadLog()
            words_edit_word.hideKeyboard()
            words_edit_word.text.clear()
            words_edit_word.hint = "Great Job! Your data is sent"
            words_edit_word.isFocusable = false
            IsSubmitEnabled = false
        }
    }

    private fun uploadLog() {
        if (IsLogUploaded) return

        val timeout = 60000 // 1 min
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val packageVersion = packageInfo.versionName
        val sessionId = (application as LanglearnApplication).sessionId

        eventLogger?.tryUploadLog(server, getString(R.string.server_root_path), prefsUser,
                sessionId, packageVersion, "test", timeout)
    }

    private fun logTestResults(entry:String, next: () -> Unit) {
        val word = words[wordsIndex].word
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        val dateToStr = format.format(Date())

        // Sleep headers:  timestamp,word,activity,audio_url,system_volume,white_noise_volume,words_volume,orientation,acceleration
        eventLogger?.logRow("$dateToStr,$word,$entry,$sysStreamVolumeProgress,${ Math.round(wordsVolume * 100f) }")
        next()
    }

    private fun logEvent(event: LogEventAction) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        val dateToStr = format.format(Date())
        eventLogger?.logRow("$dateToStr,${ event.eventString },,,")
    }

    private fun playAudioUrl() {
        Log.d(TAG, "playAudioUrl")

        try {
            val url = words[wordsIndex].audio_url

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
