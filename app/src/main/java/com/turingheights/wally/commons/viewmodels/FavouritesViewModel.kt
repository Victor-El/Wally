package com.turingheights.wally.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouritePhotosRepository: FavouritePhotosRepository
): ViewModel() {

    fun getFavourites() = favouritePhotosRepository.fetch()

    fun removeFavourite(photo: Photo) {
        viewModelScope.launch {
            favouritePhotosRepository.remove(photo)
        }
    }

}