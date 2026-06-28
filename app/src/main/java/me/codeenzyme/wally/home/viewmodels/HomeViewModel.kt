package me.codeenzyme.wally.home.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.commons.repositories.FavouritePhotosRepository
import me.codeenzyme.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import me.codeenzyme.wally.commons.utils.WallyDownloader
import me.codeenzyme.wally.home.data.remote.HomeScreenWallpaperService
import me.codeenzyme.wally.home.repository.HomeWallpaperPagingSource
import timber.log.Timber
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