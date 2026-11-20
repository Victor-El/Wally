package com.turingheights.wally.commons.utils

import android.content.Context
import android.os.StatFs
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import timber.log.Timber

@GlideModule
class WallyGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val minStorageRequired = 50L * 1024 * 1024 // 50MB
        if (!isStorageAvailable(context, minStorageRequired)) {
            Timber.w("Low storage detected. Disabling disk cache.")
            builder.setDiskCache { DiskCacheAdapter() }
        } else {
            builder.setDiskCache(InternalCacheDiskCacheFactory(context, 250L * 1024 * 1024)) // 250MB
        }

        builder.setMemoryCache(LruResourceCache(20L * 1024 * 1024)) // 20MB
    }

    private fun isStorageAvailable(context: Context, requiredBytes: Long): Boolean {
        return try {
            val path = context.cacheDir
            val stat = StatFs(path.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes > requiredBytes
        } catch (e: Exception) {
            false
        }
    }
}
