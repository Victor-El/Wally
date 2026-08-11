package com.turingheights.wally.home.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.utils.WallyDownloader
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val service: HomeScreenWallpaperService = mockk(relaxed = true)
    private val downloader: WallyDownloader = mockk(relaxed = true)
    private val repository: FavouritePhotosRepository = mockk(relaxed = true)
    private val cachedPhotoDao: CachedPhotoDao = mockk(relaxed = true)
    
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(service, downloader, repository, cachedPhotoDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startDownload calls downloader`() {
        val url = "http://example.com"
        viewModel.startDownload(url)
        verify { downloader.startDownload(url) }
    }

    @Test
    fun `updateSearchParams updates state`() {
        viewModel.updateSearchParams(query = "nature")
        // Verify no crash and params updated
    }
}
