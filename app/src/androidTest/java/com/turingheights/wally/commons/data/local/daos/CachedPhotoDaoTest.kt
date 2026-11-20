package com.turingheights.wally.commons.data.local.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.turingheights.wally.commons.data.local.PhotoDatabase
import com.turingheights.wally.commons.data.local.entities.CacheMetadataEntity
import com.turingheights.wally.commons.models.Photo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CachedPhotoDaoTest {

    private lateinit var database: PhotoDatabase
    private lateinit var dao: CachedPhotoDao

    private val mockPhoto = Photo(
        id = 1,
        previewURL = "preview",
        fullHDURL = "full",
        imageURL = "image",
        previewHeight = 100,
        previewWidth = 100,
        largeImageURL = "large",
        webformatURL = "web",
        webformatWidth = 100,
        webformatHeight = 100
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PhotoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.cachedPhotoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveAndGetCachedPhotos() = runTest {
        val metadata = CacheMetadataEntity(
            query = "nature",
            category = "all",
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular"
        )
        
        dao.saveCache(metadata, listOf(mockPhoto))

        val cachedPhotos = dao.getCachedPhotos(
            query = "nature",
            category = "all",
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular"
        )

        assertThat(cachedPhotos).hasSize(1)
        assertThat(cachedPhotos[0].id).isEqualTo(mockPhoto.id)
    }

    @Test
    fun getCachedPhotos_returnsEmpty_whenNoMatch() = runTest {
        val metadata = CacheMetadataEntity(
            query = "nature",
            category = "all",
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular"
        )
        
        dao.saveCache(metadata, listOf(mockPhoto))

        val cachedPhotos = dao.getCachedPhotos(
            query = "different",
            category = "all",
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular"
        )

        assertThat(cachedPhotos).isEmpty()
    }

    @Test
    fun deleteExpiredCache_removesOldEntriesAndKeepsFreshOnes() = runTest {
        val now = System.currentTimeMillis()
        val expiredMetadata = CacheMetadataEntity(
            query = "old",
            category = null,
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular",
            timestamp = now - 10_000L
        )
        val freshMetadata = CacheMetadataEntity(
            query = "fresh",
            category = null,
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular",
            timestamp = now
        )

        dao.saveCache(expiredMetadata, listOf(mockPhoto))
        dao.saveCache(freshMetadata, listOf(mockPhoto))

        val deletedCount = dao.deleteExpiredCache(now - 5_000L)

        assertThat(deletedCount).isEqualTo(1)
        assertThat(dao.getCachedPhotos("old", null, true, "all", "all", "popular")).isEmpty()
        assertThat(dao.getCachedPhotos("fresh", null, true, "all", "all", "popular")).hasSize(1)
    }
}
