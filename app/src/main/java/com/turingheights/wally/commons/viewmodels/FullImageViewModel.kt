package com.turingheights.wally.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.utils.WallyDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullImageViewModel @Inject constructor(
    private val wallyDownloader: WallyDownloader,
    private val favouritesRepository: FavouritePhotosRepository
) : ViewModel() {

    fun startDownload(url: String): Boolean = wallyDownloader.startDownload(url)

    fun addToFavourites(photo: Photo) {
        viewModelScope.launch {
            favouritesRepository.put(photo)
        }
    }
}
