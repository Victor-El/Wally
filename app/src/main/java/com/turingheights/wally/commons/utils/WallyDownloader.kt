package com.turingheights.wally.commons.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import com.turingheights.wally.R
import javax.inject.Inject

class WallyDownloader @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun startDownload(url: String) {
        val filename = "Wally-" + url.split("/").reversed()[0]
        val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadRequest = DownloadManager.Request(Uri.parse(url))
            .setDescription(ctx.getString(R.string.downloading_wallpaer))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

        downloadManager.enqueue(downloadRequest)
    }
}