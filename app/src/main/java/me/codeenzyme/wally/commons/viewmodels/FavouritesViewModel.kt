package me.codeenzyme.wally.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.repositories.FavouritePhotosRepository
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