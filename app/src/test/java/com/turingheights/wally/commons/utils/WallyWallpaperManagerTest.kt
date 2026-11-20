package com.turingheights.wally.commons.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class WallyWallpaperManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val androidWallpaperManager: WallpaperManager = mockk(relaxed = true)
    private val bitmap: Bitmap = mockk(relaxed = true)

    private lateinit var manager: WallyWallpaperManager

    @Before
    fun setup() {
        mockkStatic(WallpaperManager::class)
        every { WallpaperManager.getInstance(context) } returns androidWallpaperManager
        manager = WallyWallpaperManager(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(WallpaperManager::class)
    }

    @Test
    fun `setHomeScreen returns true when allowed and supported`() {
        every { androidWallpaperManager.isSetWallpaperAllowed } returns true
        every { androidWallpaperManager.isWallpaperSupported } returns true
        every { androidWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM) } returns 1

        assertThat(manager.setHomeScreen(bitmap)).isTrue()
    }

    @Test
    fun `setHomeScreen returns false instead of crashing on IOException`() {
        every { androidWallpaperManager.isSetWallpaperAllowed } returns true
        every { androidWallpaperManager.isWallpaperSupported } returns true
        every {
            androidWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        } throws IOException("Disk full")

        assertThat(manager.setHomeScreen(bitmap)).isFalse()
    }

    @Test
    fun `setLockScreen returns false when wallpaper is not supported`() {
        every { androidWallpaperManager.isSetWallpaperAllowed } returns true
        every { androidWallpaperManager.isWallpaperSupported } returns false

        assertThat(manager.setLockScreen(bitmap)).isFalse()
    }
}
