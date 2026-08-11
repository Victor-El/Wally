package com.turingheights.wally.commons.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import timber.log.Timber
import java.io.File

class CacheCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val cacheDir = File(applicationContext.cacheDir, "image_manager_disk_cache")
            if (cacheDir.exists() && cacheDir.isDirectory) {
                val currentTime = System.currentTimeMillis()
                val expiryTime = 30L * 24 * 60 * 60 * 1000 // 30 days in ms
                
                val files = cacheDir.listFiles()
                var deletedCount = 0
                files?.forEach { file ->
                    if (currentTime - file.lastModified() > expiryTime) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                }
                Timber.d("Cache cleanup completed. Deleted $deletedCount files.")
            }
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during cache cleanup")
            ListenableWorker.Result.failure()
        }
    }
}
