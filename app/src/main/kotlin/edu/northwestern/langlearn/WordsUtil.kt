package edu.northwestern.langlearn

import android.util.Log
import java.net.URL

import org.json.JSONObject
import org.json.JSONException
import java.io.FileNotFoundException
import java.net.UnknownHostException

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
