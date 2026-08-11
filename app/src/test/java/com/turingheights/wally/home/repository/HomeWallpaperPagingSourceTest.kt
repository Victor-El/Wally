package com.turingheights.wally.home.repository

import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import com.turingheights.wally.commons.data.local.entities.CachedPhotoEntity
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.PhotoSearchResult
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeWallpaperPagingSourceTest {

    private val service: HomeScreenWallpaperService = mockk()
    private val cachedPhotoDao: CachedPhotoDao = mockk(relaxed = true)
    private val networkStateFlow = MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)
    
    private lateinit var pagingSource: HomeWallpaperPagingSource

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
        pagingSource = HomeWallpaperPagingSource(
            homeScreenWallpaperService = service,
            cachedPhotoDao = cachedPhotoDao,
            safeSearch = true,
            orientation = "all",
            imageType = "all",
            order = "popular",
            networkStateFlow = networkStateFlow
        )
    }

    @Test
    fun `load returns data from cache on initial load if available`() = runTest {
        val cachedEntity = CachedPhotoEntity.fromPhoto(mockPhoto, 1L)
        coEvery { 
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any()) 
        } returns listOf(cachedEntity)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )

        val expectedResult = PagingSource.LoadResult.Page(
            data = listOf(mockPhoto),
            prevKey = null,
            nextKey = 2
        )

        assertThat(result).isEqualTo(expectedResult)
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Success)
        coVerify(exactly = 0) { service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `load returns success from network when cache is empty`() = runTest {
        coEvery { 
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any()) 
        } returns emptyList()
        
        val expectedResponse = PhotoSearchResult(1, 1, listOf(mockPhoto))
        coEvery { 
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any()) 
        } returns expectedResponse

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )

        val expectedResult = PagingSource.LoadResult.Page(
            data = listOf(mockPhoto),
            prevKey = null,
            nextKey = 2
        )

        assertThat(result).isEqualTo(expectedResult)
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Success)
        coVerify { cachedPhotoDao.saveCache(any(), any()) }
    }

    @Test
    fun `load returns error when service throws exception and cache is empty`() = runTest {
        coEvery { 
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any()) 
        } returns emptyList()

        val exception = RuntimeException("Network Error")
        coEvery { 
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any()) 
        } throws exception

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        assertThat((result as PagingSource.LoadResult.Error).throwable).isEqualTo(exception)
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Failure)
    }
}
