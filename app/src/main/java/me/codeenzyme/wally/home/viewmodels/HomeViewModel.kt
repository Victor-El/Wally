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
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import me.codeenzyme.wally.commons.utils.WallyDownloader
import me.codeenzyme.wally.home.data.remote.HomeScreenWallpaperService
import me.codeenzyme.wally.home.repository.HomeWallpaperPagingSource
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // private val pagingSource: HomeWallpaperPagingSource,
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val wallyDownloader: WallyDownloader,
): ViewModel() {

    private lateinit var pagingSource: HomeWallpaperPagingSource

    /*val homeWallpaperFlow = Pager(PagingConfig(HOME_WALLPAPER_PAGE_SIZE)) {
        pagingSource
    }.flow.cachedIn(viewModelScope)*/

    fun homeWallpaperFlow(query: String?, safeSearch: Boolean, orientation: String, imageType: String, order: String): Flow<PagingData<Photo>> {
        return Pager(PagingConfig(HOME_WALLPAPER_PAGE_SIZE)) {
            pagingSource = HomeWallpaperPagingSource(
                homeScreenWallpaperService,
                safeSearch,
                orientation,
                imageType,
                order,
                query
            )
            pagingSource
        }.flow.cachedIn(viewModelScope)
    }

    private val _wallpaperDataNetworkState = MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)
    private val wallpaperDataNetworkState: StateFlow<WallpaperDataNetworkState> = _wallpaperDataNetworkState

    suspend fun getWallPaperNetworkState(): StateFlow<WallpaperDataNetworkState> {
        /*pagingSource.wallpaperDataNetworkLoadingState.collect {
            Timber.d("viewmodel retrofit state $it")
            _wallpaperDataNetworkState.value = it
        }*/
        return pagingSource.wallpaperDataNetworkLoadingState
    }

    fun startDownload(url: String) = wallyDownloader.startDownload(url)

}