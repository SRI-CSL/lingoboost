package edu.northwestern.langlearn.log

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpUpload
import edu.northwestern.langlearn.buildRequestUrl
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by bcooper on 10/19/17.
 */

enum class LogEventAction(val eventString: String) {
    USER_EVENT_BACK_BUTTON("event.user.BackButton"),
    USER_EVENT_QUIT("event.user.Quit"),
    USER_EVENT_CANCEL_QUIT("event.user.CancelQuit"),
    USER_EVENT_PAUSE("event.user.Pause"),
    USER_EVENT_RESUME("event.user.Resume"),

    SYSTEM_EVENT_ONCREATE("event.system.onCreate"),
    SYSTEM_EVENT_ONSTART("event.system.onStart"),
    SYSTEM_EVENT_ONRESUME("event.system.onResume"),
    SYSTEM_EVENT_ONRESTART("event.system.onRestart"),
    SYSTEM_EVENT_ONPAUSE("event.system.onPause"),
    SYSTEM_EVENT_ONSTOP("event.system.onStop"),
    SYSTEM_EVENT_ONDESTROY("event.system.onDestroy")
}

class CSVEventLogger(private val filePrefix: String, private val context: Context) {
    private val TAG: String = "CSVEventLogger"

    private val logDateToStr: String by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
    }

    private val fileName = "log-$filePrefix-$logDateToStr.txt"
    private var isLogUploaded: Boolean = false
    private lateinit var headers: List<String>

    fun writeHeader(columns: List<String>) {
        headers = columns
        logRow(columns.joinToString(","), false)
    }

    // TODO: refactor to take in map of keys to values to allow better error checking / sanitization
    fun logRow(toLog: String, append: Boolean = true) {
        Log.d(TAG, "logRow")
        isLogUploaded = false

        var outputStreamWriter: OutputStreamWriter? = null

        try {
            val sanitizedRow: String = toLog.replace("\n", "\\n")

            if (append) {
                outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_APPEND))
                outputStreamWriter.append("$sanitizedRow\n")
            } else {
                outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE))
                outputStreamWriter.write("$sanitizedRow\n")
            }

        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.close()
            }
        }
    }

    fun tryUploadLog(server: String,
                     serverRootPath: String,
                     username: String,
                     sessionId: String,
                     packageVersion: String,
                     purpose: String,
                     timeout: Int = 60000,
                     onSuccess: ((String?) -> Void)? = null,
                     onError: ((FuelError) -> Void)? = null) {
        if (!isLogUploaded) {
            val requestUrl: String = buildRequestUrl(server, serverRootPath, username, "upload", sessionId, packageVersion)
                    .appendQueryParameter("purpose", purpose)
                    .toString()

            requestUrl.httpUpload()
                    .timeout(timeout)
                    .source { request, url -> File(context.filesDir, fileName) }
                    .name { "app_log_file" }
                    .progress { writtenBytes, totalBytes -> Log.d(TAG, "Upload: ${writtenBytes.toFloat()} Total: ${totalBytes.toFloat()}") }
                    .responseString { request, response, result ->
                        Log.d(TAG, request.cUrlString())
                        val (data: String?, error: FuelError?) = result

                        if (error == null) {
                            Log.d(TAG, "https://$server/$serverRootPath/user/$username/upload " +
                                    "${response.httpStatusCode}:${response.httpResponseMessage}")

                            if (onSuccess != null) {
                                onSuccess(data)
                            }
                        } else {
                            isLogUploaded = true

                            if (onError != null) {
                                onError(error)
                            }
                        }
                    }
        }
    }
}