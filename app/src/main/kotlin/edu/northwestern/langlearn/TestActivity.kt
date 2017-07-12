package edu.northwestern.langlearn

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
// import android.support.v7.widget.Toolbar
// import android.widget.TextView

import org.jetbrains.anko.longToast
import kotlinx.android.synthetic.main.activity_words.*
import java.io.IOException
import java.io.OutputStreamWriter

class TestActivity : WordsProviderUpdate, AppCompatActivity() {
    override val wordsProviderUpdateActivity: AppCompatActivity
        get() = this

    private val TAG = javaClass.simpleName
    private lateinit var wordsProvider: WordsProvider
    private lateinit  var words: List<Word>
    private var wordsIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)
        words_edit_word.isFocusable = false
        // words_edit_word.isEnabled = false

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val user = sP.getString(MainActivity.USER_PREF, "NA")

        wordsProvider = WordsProvider("https://cortical.csl.sri.com/langlearn/user/$user")
        wordsProvider.fetchJSONWords(this)
    }

    override fun updateJSONWords(json: String) {
        Log.d(TAG, "updateJSONWords")
        words = wordsProvider.parseJSONWords(json)

        longToast("Words Updated")
        Log.d(TAG, "words.size: $words.size")

        if (wordsProvider.jsonError.isNotEmpty()) {
            openMessageActivity(wordsProvider.jsonError)
            return
        }

        startWordTesting()
    }

    private fun startWordTesting() {
        Log.d(TAG, "startWordTesting")

        if (wordsIndex < words.size) {
            longToast("Playing ${ words.get(wordsIndex).word }")

            writeFileLog(words.get(wordsIndex).word);
        }
    }

    private fun writeFileLog(toLog: String) {
        try {
            val outputStreamWriter = OutputStreamWriter(baseContext.openFileOutput("log-test.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(toLog)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e.toString()")
        }
    }
}
