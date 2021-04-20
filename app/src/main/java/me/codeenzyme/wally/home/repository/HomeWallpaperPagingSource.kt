package me.codeenzyme.wally.home.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.home.data.remote.HomeScreenWallpaperService
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class HomeWallpaperPagingSource @Inject constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService
): PagingSource<Int, Photo>(){

    private val getHomeWallpaperDateWithNetworkFlow: MutableStateFlow<WallpaperDataNetworkState> = MutableStateFlow(WallpaperDataNetworkState.Loading)
    val wallpaperDataNetworkLoadingState: StateFlow<WallpaperDataNetworkState> = getHomeWallpaperDateWithNetworkFlow

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition!!)
            anchorPage?.nextKey?.plus(1) ?: anchorPage?.prevKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val nextPageNumber: Int = params.key ?: 1
        return try {
            if (nextPageNumber == 1) {
                Timber.d("Starting initial retrofit request")
                getHomeWallpaperDateWithNetworkFlow.value = WallpaperDataNetworkState.Loading
            }
            val response = homeScreenWallpaperService.getHomeScreenWallpaper(nextPageNumber)
            if (nextPageNumber == 1) {
                getHomeWallpaperDateWithNetworkFlow.value = WallpaperDataNetworkState.Success
            }
            Timber.d(response.hits.toString())
            LoadResult.Page(response.hits, null, nextPageNumber + 1)
        } catch (e: Exception) {
            Timber.d(e)
            if (nextPageNumber == 1 && e is IOException) {
                getHomeWallpaperDateWithNetworkFlow.value = WallpaperDataNetworkState.Failure
            }
            LoadResult.Error(e)
        }
    }

}