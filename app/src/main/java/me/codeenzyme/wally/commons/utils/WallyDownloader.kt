package me.codeenzyme.wally.commons.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.qualifiers.ApplicationContext
import me.codeenzyme.wally.R
import java.io.File
import javax.inject.Inject

class WallyDownloader @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun startDownload(url: String) {
        var hasPermission = false
        Dexter.withContext(ctx)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    hasPermission = true
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    hasPermission = false
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    hasPermission = false
                }
            }).check()

        if (hasPermission) {
            val filename = "Wally-" + url.split("/").reversed()[0]
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + filename
            val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadRequest = DownloadManager.Request(Uri.parse(url))
                .setDescription(ctx.getString(R.string.downloading_wallpaer))
                //.setTitle(ctx.getString(R.string.download_in_progress))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(File(path).toUri())

            downloadManager.enqueue(downloadRequest)
        }
    }

    /*companion object {
        @JvmStatic
        var instance: WallyDownloader? = null
        @JvmStatic
        fun getInstance(context: Context): WallyDownloader {
            if (instance == null) {
                instance = WallyDownloader(context)
            }
            return instance!!
        }
    }*/

}