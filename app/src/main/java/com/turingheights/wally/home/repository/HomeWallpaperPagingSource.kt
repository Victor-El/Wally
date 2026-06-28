package com.turingheights.wally.home.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.MutableStateFlow
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import retrofit2.HttpException
import timber.log.Timber

class HomeWallpaperPagingSource constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val safeSearch: Boolean,
    private val orientation: String,
    private val imageType: String,
    private val order: String,
    private val searchTerm: String? = null,
    private val networkStateFlow: MutableStateFlow<WallpaperDataNetworkState>
): PagingSource<Int, Photo>(){

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition ?: return null)
            anchorPage?.nextKey?.plus(1) ?: anchorPage?.prevKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val nextPageNumber: Int = params.key ?: 1
        return try {
            if (nextPageNumber == 1) {
                Timber.d("Starting initial retrofit request")
                networkStateFlow.value = WallpaperDataNetworkState.Loading
            }
            val response = homeScreenWallpaperService.getHomeScreenWallpaper(
                nextPageNumber,
                query = searchTerm,
                safeSearch = safeSearch,
                orientation = orientation,
                imagetype = imageType,
                order = order
            )
            if (nextPageNumber == 1) {
                networkStateFlow.value = WallpaperDataNetworkState.Success
            }
            Timber.d(response.hits.toString())
            LoadResult.Page(response.hits, null, nextPageNumber + 1)
        } catch (e: Exception) {
            if (e is HttpException) {
                return LoadResult.Page(emptyList(), null, null)
            }
            Timber.d(e)
            if (nextPageNumber == 1) {
                networkStateFlow.value = WallpaperDataNetworkState.Failure
            }
            LoadResult.Error(e)
        }
    }

}