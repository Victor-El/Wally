package com.turingheights.wally.commons.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.turingheights.wally.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class WallyDownloader @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun startDownload(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val rawSegment = uri.lastPathSegment?.takeIf { it.isNotBlank() }
                ?: "${System.currentTimeMillis()}.jpg"
            val filename = "Wally-$rawSegment"
            val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadRequest = DownloadManager.Request(uri)
                .setDescription(ctx.getString(R.string.downloading_wallpaer))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            downloadManager.enqueue(downloadRequest)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to start download for $url")
            false
        }
    }
}
