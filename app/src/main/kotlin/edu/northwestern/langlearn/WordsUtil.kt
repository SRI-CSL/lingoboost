package edu.northwestern.langlearn

import android.util.Log

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

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

fun JSONObject.returnItString(key: String): String {
    try {
        return this.getString(key)
    } catch (e: JSONException) {
        Log.w("${ javaClass.simpleName }KEx", e.message)
        return e.message ?: ""
    }
}


//inline fun <T: Any> fetchFromJSONObjectByClass(clazz: Class<T>, jsonObj: JSONObject, key: String, block: (value: T) -> Unit) {
//    Log.d("fetchFromJSONObject", "Here2")
//    Log.d("fetchFromJSONObject", clazz.toString())
//
//    try {
//        when {
//            clazz.isInstance(Long::class.javaObjectType) -> {
//                Log.d("fetchFromJSONObject", "Here3...")
//                block(jsonObj.getLong(key) as T)
//            }
//        }
//    } catch (e: JSONException) {
//        Log.w("fetchFromJSONObject", e.message)
//    }
//}
//
//inline fun <reified T> JSONObject.getIt(key: String, block: (value: T) -> Unit) {
//    Log.d("fetchFromJSONObject", "Here4")
//
//    try {
//        when {
//            Long is T -> { block(this.getLong(key) as T) }
//            // Boolean is T -> { block(this.getBoolean(key) as T) }
//            String is T -> { block(this.getString(key) as T) }
//        }
//
//        Log.d("fetchFromJSONObject", "Here5")
//
//        val clazz: T
//
//        if (T::class.simpleName == "Long") {
//            Log.d("fetchFromJSONObject", "Here6")
//            block(this.getLong(key) as T)
//        }
//
//
//    } catch (e: JSONException) {
//        Log.w("fetchFromJSONObject", e.message)
//    }
//}

//try {
//    val error = jsonObj.getString("error")
//
//    jsonError = error
//} catch (e: JSONException) {
//    Log.i(javaClass.simpleName, e.message)
//}