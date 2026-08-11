package com.turingheights.wally.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.utils.ALL
import com.turingheights.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import com.turingheights.wally.commons.utils.POPULAR
import com.turingheights.wally.commons.utils.WallyDownloader
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import com.turingheights.wally.home.repository.HomeWallpaperPagingSource
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val wallyDownloader: WallyDownloader,
    private val favouritesService: FavouritePhotosRepository
): ViewModel() {

    data class SearchParams(
        val query: String? = null,
        val category: String? = null,
        val safeSearch: Boolean = true,
        val orientation: String = ALL,
        val imageType: String = ALL,
        val order: String = POPULAR
    )

    private val _searchParams = MutableStateFlow(SearchParams())

    private val _wallpaperDataNetworkState = MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)

    val homeWallpaperFlow: Flow<PagingData<Photo>> = _searchParams.flatMapLatest { params ->
        Pager(PagingConfig(HOME_WALLPAPER_PAGE_SIZE)) {
            HomeWallpaperPagingSource(
                homeScreenWallpaperService,
                params.safeSearch,
                params.orientation,
                params.imageType,
                params.order,
                params.category,
                params.query,
                _wallpaperDataNetworkState
            )
        }.flow
    }.cachedIn(viewModelScope)

    fun updateSearchParams(
        query: String? = _searchParams.value.query,
        category: String? = _searchParams.value.category,
        safeSearch: Boolean = _searchParams.value.safeSearch,
        orientation: String = _searchParams.value.orientation,
        imageType: String = _searchParams.value.imageType,
        order: String = _searchParams.value.order
    ) {
        val newParams = SearchParams(query, category, safeSearch, orientation, imageType, order)
        if (_searchParams.value != newParams) {
            _searchParams.value = newParams
        }
    }

    fun getWallPaperNetworkState(): StateFlow<WallpaperDataNetworkState> {
        return _wallpaperDataNetworkState
    }

    fun startDownload(url: String) = wallyDownloader.startDownload(url)

    fun addToFavourites(photo: Photo) {
        viewModelScope.launch {
            favouritesService.put(photo)
        }
    }

}