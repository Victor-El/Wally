package me.codeenzyme.wally.commons.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import timber.log.Timber

class WallyWallpaperManager(private val ctx: Context) {
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
            androidWallpaperManager.setBitmap(bitmap, cropRect, true, WallpaperManager.FLAG_SYSTEM)
        } else {
            return false
        }
        return true
    }

    fun setHomeScreen(bitmap: Bitmap, cropRect: Rect? = null): Boolean {
        if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
            var lockBitmapDrawable: BitmapDrawable? = null
            try {
                lockBitmapDrawable = androidWallpaperManager.drawable as BitmapDrawable
            } catch (e: Exception) {
                Timber.e(e)
            }
            val lockBitmap = lockBitmapDrawable?.bitmap
            androidWallpaperManager.setBitmap(bitmap, cropRect, true, WallpaperManager.FLAG_SYSTEM)
            androidWallpaperManager.setBitmap(lockBitmap, cropRect, false, WallpaperManager.FLAG_LOCK)
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