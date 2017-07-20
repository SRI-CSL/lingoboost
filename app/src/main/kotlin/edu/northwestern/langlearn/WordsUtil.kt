package edu.northwestern.langlearn

import android.util.Log

import org.json.JSONObject
import org.json.JSONException

inline fun JSONObject.getLongLogCatch(key: String, block: (value: Long) -> Unit) {
    val l: Long

    try {
        l = this.getLong(key)
        block(l)
    } catch (e: JSONException) {
        Log.w(javaClass.simpleName, e.message)
    }
}

//07-19 18:03:31.561 15498-15498/edu.northwestern.langlearn D/WordsProvider: 10
//07-19 18:03:31.561 15498-15498/edu.northwestern.langlearn D/WordsProvider: New extension get: 10000
//07-19 18:03:31.562 15498-15498/edu.northwestern.langlearn D/WordsProvider: fetchFromJSONObject long get: 10000
//07-19 18:03:31.562 15498-15498/edu.northwestern.langlearn D/WordsProvider: fetchFromJSONObject long get: 10000
//07-19 18:03:31.562 15498-15498/edu.northwestern.langlearn D/WordsProvider: fetchFromJSONObject boolean get: false
//07-19 18:03:31.562 15498-15498/edu.northwestern.langlearn D/WordsProvider: fetchIt extension: 10000
//07-19 18:03:31.562 15498-15498/edu.northwestern.langlearn I/WordsProvider: No value for sham
//07-19 18:03:31.563 15498-15498/edu.northwestern.langlearn I/WordsProvider: No value for error

inline fun <T> fetchFromJSONObjectByClass(clazz: Class<T>, jsonObj: JSONObject, key: String, block: (value: T) -> Unit) {
    try {
        when {
            clazz.isInstance(Long) -> { block(jsonObj.getLong(key) as T) }
        }
    } catch (e: JSONException) {
        Log.w("fetchFromJSONObject", e.message)
    }
}

inline fun <reified T> fetchFromJSONObject(jsonObj: JSONObject, key: String, block: (value: T) -> Unit) {
    try {
        when (T::class.java) {
            Long::class.java -> { block(jsonObj.getLong(key) as T) }
            Boolean::class.java -> { block(jsonObj.getBoolean(key) as T) }
        }
    } catch (e: JSONException) {
        Log.w("fetchFromJSONObject", e.message)
    }
}

inline fun <reified T> JSONObject.fetchIt(key: String, block: (value: T) -> Unit) {
    try {
        when (T::class.java) {
            Long::class.java -> { block(this.getLong(key) as T) }
            Boolean::class.java -> { block(this.getBoolean(key) as T) }
        }
    } catch (e: JSONException) {
        Log.w("fetchFromJSONObject", e.message)
    }
}

//try {
//    val error = jsonObj.getString("error")
//
//    jsonError = error
//} catch (e: JSONException) {
//    Log.i(javaClass.simpleName, e.message)
//}