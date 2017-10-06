package edu.northwestern.langlearn

import android.net.Uri

/**
 * Created by bcooper on 10/6/17.
 */
fun buildRequestUrl(server: String, username: String, service: String,
                    sessionId: String, appVersion: String): Uri.Builder {
    return Uri.parse("https://$server/langlearn/user/").buildUpon()
            .appendEncodedPath(username)
            .appendEncodedPath(service)
            .appendQueryParameter("sessionId", sessionId)
            .appendQueryParameter("version", appVersion)
}