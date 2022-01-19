package me.codeenzyme.wally.home.repository

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.commons.preferencestore.*
import me.codeenzyme.wally.commons.utils.ALL
import me.codeenzyme.wally.commons.utils.POPULAR
import me.codeenzyme.wally.home.data.remote.HomeScreenWallpaperService
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class HomeWallpaperPagingSource constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val safeSearch: Boolean,
    private val orientation: String,
    private val imageType: String,
    private val order: String,
    private val searchTerm: String? = null
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
            val response = homeScreenWallpaperService.getHomeScreenWallpaper(
                nextPageNumber,
                query = searchTerm,
                safeSearch = safeSearch,
                orientation = orientation,
                imagetype = imageType,
                order = order
            )
            if (nextPageNumber == 1) {
                getHomeWallpaperDateWithNetworkFlow.value = WallpaperDataNetworkState.Success
            }
            Timber.d(response.hits.toString())
            LoadResult.Page(response.hits, null, nextPageNumber + 1)
        } catch (e: Exception) {
            if (e is HttpException) {
                return LoadResult.Page(emptyList(), null, null)
            }
            Timber.d(e)
            if (nextPageNumber == 1 && e is IOException) {
                getHomeWallpaperDateWithNetworkFlow.value = WallpaperDataNetworkState.Failure
            }
            LoadResult.Error(e)
        }
    }

}