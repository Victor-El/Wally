package com.turingheights.wally.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import com.turingheights.wally.commons.utils.WallyDownloader
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import com.turingheights.wally.home.repository.HomeWallpaperPagingSource
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val wallyDownloader: WallyDownloader,
    private val favouritesService: FavouritePhotosRepository
): ViewModel() {

    private val _wallpaperDataNetworkState = MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)

    fun homeWallpaperFlow(query: String?, safeSearch: Boolean, orientation: String, imageType: String, order: String): Flow<PagingData<Photo>> {
        return Pager(PagingConfig(HOME_WALLPAPER_PAGE_SIZE)) {
            HomeWallpaperPagingSource(
                homeScreenWallpaperService,
                safeSearch,
                orientation,
                imageType,
                order,
                query,
                _wallpaperDataNetworkState
            )
        }.flow.cachedIn(viewModelScope)
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