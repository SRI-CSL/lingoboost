package edu.northwestern.langlearn

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log

import org.json.JSONObject
import org.json.JSONException
import org.json.JSONArray

import java.io.FileNotFoundException
import java.net.URL
import java.net.UnknownHostException

typealias ListOfWords = List<Word>
typealias MutableListOfWords = MutableList<Word>

data class Word(val norm: String, val audio_url: String, val translations: Array<String>, val word: String)

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

fun mutableListOfWords(): MutableListOfWords = ArrayList()

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
    }
}

public fun JSONArray.asSequence(): Sequence<Any> {
    return object : Sequence<Any> {

        override fun iterator() = object : Iterator<Any> {

            //val it = (0..this@asSequence.length() - 1).iterator()
            val it = (0 until this@asSequence.length()).iterator()

            override fun next(): Any {
                val i = it.next()

                return this@asSequence.get(i)
            }

            override fun hasNext() = it.hasNext()
        }
    }
}

//jsonObj.getIt<Int>("word_delay") { jsonWordDelay = it.toLong() * 1000 }
//jsonObj.getIt<Boolean>("sham") { jsonSham = it }
//jsonObj.getIt<String>("error") { jsonError = it }
//inline fun <reified T> JSONObject.getIt(key: String, block: (value: T) -> Unit) {
//    try {
//        block(this.get(key) as T)
//    } catch (e: JSONException) {
//        Log.w("${ javaClass.simpleName }KEx", e.message)
//    }
//}

// NOTE: Cannot inline currently an optional function parameter (see below = null)
//fun <T> JSONObject.getEx(key: String, block: ((value: T) -> T?)? = null): T? {
//    var r: T? = null
//
//    try {
//        r = if (block != null) block(this.get(key) as T) else this.get(key) as T
//    } catch (e: JSONException) {
//        Log.w("${ javaClass.simpleName }KEx", e.message)
//    }
//
//    return r
//}

//jsonObj.getIt<Boolean>("sham", JSONObject::getBoolean) { jsonSham = it }
//jsonObj.getIt<String>("error", JSONObject::getString) { jsonError = it }
//inline fun <T> JSONObject.getIt(key: String, accessor: JSONObject.(String) -> T, block: (T) -> Unit) {
//    try {
//        accessor(key).let(block)
//    } catch (e: JSONException) {
//        Log.w("${ javaClass.simpleName }KEx", e.message)
//    }
//}

//inline fun JSONObject.getItLong(key: String, block: (value: Long) -> Unit) {
//    try {
//        block(this.getLong(key))
//    } catch (e: JSONException) {
//        Log.w("${ javaClass.simpleName }KEx", e.message)
//    }
//}
