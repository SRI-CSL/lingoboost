package edu.northwestern.langlearn

import android.util.Log
import java.net.URL

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.FileNotFoundException
import java.net.UnknownHostException

inline fun <reified T> JSONObject.getIt(key: String, block: (value: T) -> Unit) {
    try {
        when (T::class) {
            kotlin.Long::class.java -> block(this.getLong(key) as T) // as Long
            kotlin.String::class.java -> block(this.getString(key) as T) // as Stirng
            kotlin.Boolean::class.java -> block(this.getBoolean(key) as T) // as Boolean
            else -> {
                Log.w("${ javaClass.simpleName }KEx", "Missing type T in JSONObject.getIt extension function")
            }
        }
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun <T> JSONObject.getIt(key: String, accessor: JSONObject.(String) -> T, block: (T) -> Unit) {
    try {
        accessor(key).let(block)
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun JSONObject.getItLong(key: String, block: (value: Long) -> Unit) {
    try {
        block(this.getLong(key))
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun JSONObject.getItBoolean(key: String, block: (value: Boolean) -> Unit) {
    try {
        block(this.getBoolean(key))
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun JSONObject.getItString(key: String, block: (value: String) -> Unit) {

    try {
        block(this.getString(key))
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun JSONObject.getItJSONArray(key: String, block: (value: JSONArray) -> Unit) {
    try {
        block(this.getJSONArray("words"))
    } catch (e: JSONException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
    }
}

inline fun URL.readItText(block: (text: String, error: String?) -> Unit) {
    try {
        block(this.readText(), "")
    } catch (e: UnknownHostException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", e.message)
    } catch (e: FileNotFoundException) {
        Log.e("${ javaClass.simpleName }KEx", e.message)
        block("", e.message)
    }
}

fun JSONObject.returnItString(key: String): String {
    try {
        return this.getString(key)
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
        return e.message ?: ""
    }
}
