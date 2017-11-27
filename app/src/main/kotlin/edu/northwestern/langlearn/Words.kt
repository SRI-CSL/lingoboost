package edu.northwestern.langlearn

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.net.UnknownHostException
import java.net.ConnectException

typealias ListOfWords = List<Word>
typealias MutableListOfWords = MutableList<Word>

fun mutableListOfWords(): MutableListOfWords = mutableListOf()

class Word(val norm: String, val audio_url: String, val translations: List<String>, val word: String) {
    fun getMatches(guess: String, cutoff: Double = 0.8): List<String> {
        val preprocessed = preprocessGuess(guess)

        return translations.filter {
            realQuickRatio(preprocessed, it) >= cutoff
        }
    }

    override fun toString(): String = """ { "norm": "$norm", "audio_url": "$audio_url", "translations": $translations, "word": "$word" } """

    private fun preprocessGuess(guess: String): String = guess.toLowerCase().trim()

    private fun realQuickRatio(guess: String, word: String): Double {
        val guessLen = guess.length
        val wordLen = word.length

        if (guessLen == 0 && wordLen == 0) {
            return 0.0
        }

        return calculateRatio(minOf(guessLen, wordLen), guessLen + wordLen)
    }

    private fun calculateRatio(matches: Int, length: Int): Double = 2.0 * matches / length
}

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

inline fun JSONObject.unless(func: JSONObject.() -> Unit) {
    try {
        this.func()
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun URL.readText(block: (text: String, error: String?) -> Unit) {
    try {
        block(this.readText(), "")
    } catch (e: UnknownHostException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", "No Internet Available. Please connect to the internet and try again")
    } catch (e: FileNotFoundException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", e.message)
    } catch (e: ConnectException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", e.message)
    } catch (e: Exception) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", e.message)
    }
}

// The values are evaluated lazily
inline fun <reified T> JSONArray.asSequence(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator() = object : Iterator<T> {
            val it = (0 until length()).iterator() //this@asSequence.length()

            override fun next(): T {
                val i = it.next()

                return get(i).apply { check(this is T) } as T
            }

            override fun hasNext() = it.hasNext()
        }
    }
}

inline fun <reified T> JSONArray.asListOf(): List<T> = asSequence<T>().toList()

// to eager ...
inline fun <reified T> JSONArray.asSequenceOf(): Sequence<T> = (0 until length()).asSequence().map {
    get(it).apply { check(this is T) } as T
}

inline fun <reified T> JSONArray.toListOf(): List<T> = (0 until length()).asSequence().map {
    get(it).apply { check(this is T) } as T
}.toList()

inline fun <reified T> JSONArray.asFilterIsInstanceListOf(): List<T> = (0 until length()).asSequence().toList().filterIsInstance<T>()

fun JSONArray.asSequenceOfObjects(): Sequence<JSONObject> = (0 until length()).asSequence().map {
    get(it) as JSONObject
}

fun JSONArray.asListOfObjects(): List<JSONObject> = (0 until length()).asSequence().map {
    get(it) as JSONObject
}.toList()

fun <T> JSONArray.asExpressedSequenceOf(expr: (a: Int) -> T): Sequence<T> = (0 until length()).asSequence().map(expr)

fun <T> JSONArray.asExpressedListOf(expr: (a: Int) -> T): List<T> = (0 until length()).asSequence().map(expr).toList()

operator fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map {
    get(it) as JSONObject
}.iterator()
