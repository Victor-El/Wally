package com.turingheights.wally.commons.repositories

import kotlinx.coroutines.flow.Flow
import com.turingheights.wally.commons.models.Photo

interface FavouritePhotosRepository {

    fun fetch(): Flow<List<Photo>>
    suspend fun put(photo: Photo)
    suspend fun remove(photo: Photo)

}