package me.codeenzyme.wally.commons.repositories

import kotlinx.coroutines.flow.Flow
import me.codeenzyme.wally.commons.models.Photo

interface FavouritePhotosRepository {

    fun fetch(): Flow<List<Photo>>
    suspend fun put(photo: Photo)
    suspend fun remove(photo: Photo)

}