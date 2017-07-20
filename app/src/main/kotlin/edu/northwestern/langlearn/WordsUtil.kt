package edu.northwestern.langlearn

import android.util.Log
import java.net.URL

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.FileNotFoundException
import java.net.UnknownHostException

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
