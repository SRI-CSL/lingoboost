package edu.northwestern.langlearn

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText

import org.jetbrains.anko.longToast
import kotlinx.android.synthetic.main.activity_words.*
import java.io.IOException
import java.io.OutputStreamWriter

//fun EditText.onTextChanged(onTextChanged: (CharSequence?, Int, Int, Int) -> Unit) {
//    this.addTextChangedListener(object : TextWatcher {
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            onTextChanged.invoke(s, start, before, count)
//        }
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
//        override fun afterTextChanged(editable: Editable?) { }
//    })
//}

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
    private lateinit  var words: List<Word>
    private var wordsIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)
        // words_edit_word.isFocusable = false
        // words_edit_word.isEnabled = false
        words_text_word.text = ""

        val sP = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val user = sP.getString(MainActivity.USER_PREF, "NA")

        wordsProvider = WordsProvider("https://cortical.csl.sri.com/langlearn/user/$user")
        wordsProvider.fetchJSONWords(this)

        // words_edit_word.isFocusable = true
        // words_edit_word.setOnFocusChangeListener { v, hasFocus ->
        //     Log.d(TAG, "hasFocus: $hasFocus")
        // }

//        words_edit_word.onTextChanged { s, start, before, count -> Log.d(TAG, "Wow!") }
        words_edit_word.afterTextChanged {
            if (it.isNotEmpty()) {
                if (it.last() == '\n') {
                    words_edit_word.setText(it)
                    words_edit_word.setSelection(it.length)
                    logTestResults { continueWordTesting() }
                }
            }
        }

//        words_edit_word.addTextChangedListener(object: TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if ((start >= s.length) or (start < 0) or s.isEmpty())
//                    return
//
//                if (s.subSequence(start, start + 1).last() == '\n') {// if (s.last() == '\n') {
//                    val s1 = if (start > 0) s.subSequence(0, start).toString() else ""
//                    val s2 = if (start < s.length) s.subSequence(start + 1, s.length).toString() else ""
//                    val text = "$s1$s2"
//
//                    words_edit_word.setText(text)
//                    words_edit_word.setSelection(text.length)
//                    logTestResults { continueWordTesting() }
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
//            override fun afterTextChanged(s: Editable) {
//                if (s.isEmpty())
//                    return
//
//                if (s.last() == '\n') {
//                    val text = s.toString()
//
//                    words_edit_word.setText(text)
//                    words_edit_word.setSelection(text.length)
//                    logTestResults { continueWordTesting() }
//                }
//            }
//        })
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

        continueWordTesting()
    }

    private fun continueWordTesting() {
        Log.d(TAG, "continueWordTesting")

        if (wordsIndex < words.size) {
            val word = words.get(wordsIndex).word

            words_text_word.text = word
        }
    }

    private fun logTestResults(next: () -> Unit) {
        val word = words.get(wordsIndex).word

        writeFileLog(word);
        wordsIndex++
        next();
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
