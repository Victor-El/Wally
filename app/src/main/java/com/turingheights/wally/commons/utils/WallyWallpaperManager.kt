package com.turingheights.wally.commons.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallyWallpaperManager @Inject constructor(
    @ApplicationContext ctx: Context
) {
    private val androidWallpaperManager: WallpaperManager = WallpaperManager.getInstance(ctx)

    fun setLockScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        return setWallpaperInternal(bitmap, cropRect, allowBackup = false, flags = WallpaperManager.FLAG_LOCK)
    }

    fun setBothLockScreenAndHomeScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        return setWallpaperInternal(
            bitmap,
            cropRect,
            allowBackup = true,
            flags = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        )
    }

    fun setHomeScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        return setWallpaperInternal(bitmap, cropRect, allowBackup = true, flags = WallpaperManager.FLAG_SYSTEM)
    }

    private fun setWallpaperInternal(
        bitmap: Bitmap,
        cropRect: Rect?,
        allowBackup: Boolean,
        flags: Int
    ): Boolean {
        return try {
            if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
                androidWallpaperManager.setBitmap(bitmap, cropRect, allowBackup, flags)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set wallpaper (flags=$flags)")
            false
        }
    }
}
