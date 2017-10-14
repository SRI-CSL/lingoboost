package edu.northwestern.langlearn

import android.net.Uri

/**
 * Created by bcooper on 10/6/17.
 */
fun buildRequestUrl(server: String, serverRootPath: String, username: String, service: String,
                    sessionId: String, appVersion: String?): Uri.Builder {
    var uriBuilder: Uri.Builder = Uri.parse("https://$server/$serverRootPath/user/").buildUpon()
            .appendEncodedPath(username)
            .appendEncodedPath(service)
            .appendQueryParameter("sessionId", sessionId)

            if (appVersion != null) {
                uriBuilder = uriBuilder.appendQueryParameter("version", appVersion)
            }

    return uriBuilder
}