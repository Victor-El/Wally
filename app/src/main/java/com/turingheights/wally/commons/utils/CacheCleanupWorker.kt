package com.turingheights.wally.commons.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CacheCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CacheCleanupEntryPoint {
        fun cachedPhotoDao(): CachedPhotoDao
    }

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                CacheCleanupEntryPoint::class.java
            )
            val cutoffTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(CACHE_TTL_DAYS)
            val deletedMetadataCount = entryPoint.cachedPhotoDao().deleteExpiredCache(cutoffTimestamp)
            Timber.d("Room cache cleanup completed. Deleted $deletedMetadataCount expired cache entries.")
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during cache cleanup")
            ListenableWorker.Result.failure()
        }
    }

    companion object {
        private const val CACHE_TTL_DAYS = 7L
    }
}
