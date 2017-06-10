package edu.northwestern.langlearn

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

data class Word(
        val name: String
)

class WordsAdapter {
    fun ParseJson(): Boolean {
        val json: String = """
{
    "words": [
        {
            "name": "velvet"
        }
    ]
}
"""

        try {
            val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))
            val wordJson = jsonObj.getJSONArray("words") // JSONException ?

            for (i in 0..wordJson!!.length() - 1) {
                val name = wordJson.getJSONObject(i).getString("name")
                val word = Word(name = name)
                // word.name = name

                Log.d(javaClass.simpleName, word.name)
            }
        } catch (e: JSONException) {
            Log.e(javaClass.simpleName, e.message)
        }

        return true
    }
}
