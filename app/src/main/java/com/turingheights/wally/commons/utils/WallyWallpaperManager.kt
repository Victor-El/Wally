package com.turingheights.wally.commons.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect

class WallyWallpaperManager(ctx: Context) {
    private val androidWallpaperManager: WallpaperManager = WallpaperManager.getInstance(ctx)

    fun setLockScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
            androidWallpaperManager.setBitmap(bitmap, cropRect, false, WallpaperManager.FLAG_LOCK)
        } else {
            return false
        }
        return true
    }

    fun setBothLockScreenAndHomeScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
            androidWallpaperManager.setBitmap(bitmap, cropRect, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
        } else {
            return false
        }
        return true
    }

    fun setHomeScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
            androidWallpaperManager.setBitmap(bitmap, cropRect, true, WallpaperManager.FLAG_SYSTEM)
        } else {
            return false
        }
        return true
    }

    companion object {
        private var wallyWallpaperManager: WallyWallpaperManager? = null

        fun getInstance(context: Context): WallyWallpaperManager {
            return wallyWallpaperManager ?: (WallyWallpaperManager(context).also { wallyWallpaperManager = it })
        }

    }

}