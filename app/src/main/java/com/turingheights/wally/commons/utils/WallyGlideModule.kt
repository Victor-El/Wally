package com.turingheights.wally.commons.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import timber.log.Timber

@GlideModule
class WallyGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val minStorageRequired = 50 * 1024 * 1024 // 50MB
        if (!isStorageAvailable(context, minStorageRequired)) {
            Timber.w("Low storage detected. Disabling disk cache.")
            // Effectively disable disk cache by setting size to 0
            builder.setDiskCache(InternalCacheDiskCacheFactory(context, 0))
        } else {
            // Default behavior or custom size
            builder.setDiskCache(InternalCacheDiskCacheFactory(context, 250 * 1024 * 1024)) // 250MB
        }
        
        // Increase memory cache slightly for better performance
        builder.setMemoryCache(LruResourceCache(20 * 1024 * 1024)) // 20MB
    }

    private fun isStorageAvailable(context: Context, requiredBytes: Int): Boolean {
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
