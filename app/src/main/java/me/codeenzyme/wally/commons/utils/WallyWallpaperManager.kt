package me.codeenzyme.wally.commons.utils

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import me.codeenzyme.wally.R
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

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (androidWallpaperManager.isSetWallpaperAllowed && androidWallpaperManager.isWallpaperSupported) {
            val lockBitmapDrawable= androidWallpaperManager.drawable as BitmapDrawable
            val lockBitmap = lockBitmapDrawable.bitmap
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

        fun wallpaperPermission(context: Context) {
            val listener = DialogOnDeniedPermissionListener.Builder
                .withContext(context)
                .withTitle("External storage permission")
                .withMessage("External storage permission is needed to set wallpaper to only home screen.")
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build()
            Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(listener)
                .check()
        }

    }

}