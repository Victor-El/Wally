package com.turingheights.wally.commons.viewmodels

import com.google.common.truth.Truth.assertThat
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.utils.WallyDownloader
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FullImageViewModelTest {

    private val downloader: WallyDownloader = mockk()
    private val repository: FavouritePhotosRepository = mockk(relaxed = true)
    private lateinit var viewModel: FullImageViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

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
        Dispatchers.setMain(testDispatcher)
        viewModel = FullImageViewModel(downloader, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startDownload delegates to WallyDownloader and returns result`() {
        val url = "https://example.com/photo.jpg"
        every { downloader.startDownload(url) } returns true

        val started = viewModel.startDownload(url)

        assertThat(started).isTrue()
        verify(exactly = 1) { downloader.startDownload(url) }
    }

    @Test
    fun `addToFavourites inserts photo via repository`() = runTest {
        viewModel.addToFavourites(mockPhoto)
        coVerify(exactly = 1) { repository.put(mockPhoto) }
    }
}
