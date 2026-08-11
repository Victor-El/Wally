package com.turingheights.wally.commons.repositories.impls

import com.turingheights.wally.commons.data.local.PhotoDatabase
import com.turingheights.wally.commons.data.local.daos.PhotoDao
import com.turingheights.wally.commons.models.Photo
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultFavouritePhotoRepositoryTest {

    private val db: PhotoDatabase = mockk()
    private val dao: PhotoDao = mockk(relaxed = true)
    private lateinit var repository: DefaultFavouritePhotoRepository

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
        every { db.photoDao() } returns dao
        repository = DefaultFavouritePhotoRepository(db)
    }

    @Test
    fun `put calls dao insert`() = runTest {
        repository.put(mockPhoto)
        coVerify { dao.insertPhoto(any()) }
    }

    @Test
    fun `remove calls dao delete`() = runTest {
        repository.remove(mockPhoto)
        coVerify { dao.deletePhoto(any()) }
    }
}
