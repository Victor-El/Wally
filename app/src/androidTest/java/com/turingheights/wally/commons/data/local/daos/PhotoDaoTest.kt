package com.turingheights.wally.commons.data.local.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.turingheights.wally.commons.data.local.PhotoDatabase
import com.turingheights.wally.commons.data.local.entities.PhotoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PhotoDaoTest {

    private lateinit var database: PhotoDatabase
    private lateinit var dao: PhotoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PhotoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.photoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAllPhotos() = runTest {
        val photo = PhotoEntity(
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
        dao.insertPhoto(photo)

        val allPhotos = dao.getAll().first()
        assertThat(allPhotos).contains(photo)
    }

    @Test
    fun deletePhoto() = runTest {
        val photo = PhotoEntity(
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
        dao.insertPhoto(photo)
        dao.deletePhoto(photo)

        val allPhotos = dao.getAll().first()
        assertThat(allPhotos).isEmpty()
    }
}
