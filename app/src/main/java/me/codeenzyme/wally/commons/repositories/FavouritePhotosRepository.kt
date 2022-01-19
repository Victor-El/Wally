package me.codeenzyme.wally.commons.repositories

import me.codeenzyme.wally.commons.models.Photo

interface FavouritePhotosRepository {

    suspend fun fetch(): List<Photo>
    suspend fun put(photo: Photo)
    suspend fun remove(photo: Photo)

}