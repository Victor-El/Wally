package me.codeenzyme.wally.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import me.codeenzyme.wally.commons.models.WallpaperDataNetworkState
import me.codeenzyme.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import me.codeenzyme.wally.home.repository.HomeWallpaperPagingSource
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pagingSource: HomeWallpaperPagingSource
): ViewModel() {

    val homeWallpaperFlow = Pager(PagingConfig(HOME_WALLPAPER_PAGE_SIZE)) {
        pagingSource
    }.flow.cachedIn(viewModelScope)

    private val _wallpaperDataNetworkState = MutableStateFlow<WallpaperDataNetworkState>(WallpaperDataNetworkState.Loading)
    private val wallpaperDataNetworkState: StateFlow<WallpaperDataNetworkState> = _wallpaperDataNetworkState

    suspend fun getWallPaperNetworkState(): StateFlow<WallpaperDataNetworkState> {
        /*pagingSource.wallpaperDataNetworkLoadingState.collect {
            Timber.d("viewmodel retrofit state $it")
            _wallpaperDataNetworkState.value = it
        }*/
        return pagingSource.wallpaperDataNetworkLoadingState
    }


}