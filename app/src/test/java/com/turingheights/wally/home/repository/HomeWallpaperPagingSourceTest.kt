package com.turingheights.wally.home.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
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
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class HomeWallpaperPagingSourceTest {

    private val service: HomeScreenWallpaperService = mockk()
    private val cachedPhotoDao: CachedPhotoDao = mockk(relaxed = true)
    private val networkStateFlow =
        MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)

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
    fun `load page 1 fetches from network and updates cache`() = runTest {
        val expectedResponse = PhotoSearchResult(total = 100, totalHits = 100, hits = listOf(mockPhoto))
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns expectedResponse

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
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
        coVerify(exactly = 1) { cachedPhotoDao.saveCache(any(), listOf(mockPhoto)) }
    }

    @Test
    fun `load page 1 sets nextKey to null when totalHits reached`() = runTest {
        val expectedResponse = PhotoSearchResult(total = 1, totalHits = 1, hits = listOf(mockPhoto))
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns expectedResponse

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isEqualTo(
            PagingSource.LoadResult.Page(
                data = listOf(mockPhoto),
                prevKey = null,
                nextKey = null
            )
        )
    }

    @Test
    fun `load page 1 falls back to cache when network throws exception`() = runTest {
        val cachedEntity = CachedPhotoEntity.fromPhoto(mockPhoto, 1L)
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws RuntimeException("Offline")
        coEvery {
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any())
        } returns listOf(cachedEntity)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isEqualTo(
            PagingSource.LoadResult.Page(
                data = listOf(mockPhoto),
                prevKey = null,
                nextKey = 2
            )
        )
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Success)
    }

    @Test
    fun `load returns error and Failure state when service throws exception and cache is empty`() = runTest {
        coEvery {
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any())
        } returns emptyList()

        val exception = RuntimeException("Network Error")
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        assertThat((result as PagingSource.LoadResult.Error).throwable).isEqualTo(exception)
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Failure)
    }

    @Test
    fun `load returns error on non-400 HttpException instead of empty page`() = runTest {
        coEvery {
            cachedPhotoDao.getCachedPhotos(any(), any(), any(), any(), any(), any())
        } returns emptyList()

        val httpException = HttpException(Response.error<Any>(429, "Rate limit".toResponseBody()))
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws httpException

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        assertThat((result as PagingSource.LoadResult.Error).throwable).isEqualTo(httpException)
        assertThat(networkStateFlow.value).isEqualTo(WallpaperDataNetworkState.Failure)
    }

    @Test
    fun `load treats HTTP 400 on page greater than 1 as end of pagination`() = runTest {
        val httpException = HttpException(Response.error<Any>(400, "Out of range".toResponseBody()))
        coEvery {
            service.getHomeScreenWallpaper(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws httpException

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isEqualTo(
            PagingSource.LoadResult.Page(
                data = emptyList(),
                prevKey = 1,
                nextKey = null
            )
        )
    }

    @Test
    fun `getRefreshKey returns null to reload from page 1`() {
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(mockPhoto),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )

        assertThat(pagingSource.getRefreshKey(state)).isNull()
    }
}
