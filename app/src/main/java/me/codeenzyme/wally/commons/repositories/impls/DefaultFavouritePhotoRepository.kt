package me.codeenzyme.wally.commons.repositories.impls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.codeenzyme.wally.commons.data.local.PhotoDatabase
import me.codeenzyme.wally.commons.data.local.entities.PhotoEntity
import me.codeenzyme.wally.commons.models.Photo
import me.codeenzyme.wally.commons.repositories.FavouritePhotosRepository
import javax.inject.Inject

class DefaultFavouritePhotoRepository @Inject constructor(
    private val photoDatabase: PhotoDatabase
): FavouritePhotosRepository {
    override fun fetch(): Flow<List<Photo>> {
        return photoDatabase.photoDao().getAll().map {
            it.map {
                PhotoEntity.toPhoto(it)
            }
        }
    }

    override suspend fun put(photo: Photo) {
        photoDatabase.photoDao().insertPhoto(PhotoEntity.fromPhoto(photo))
    }

    override suspend fun remove(photo: Photo) {
        photoDatabase.photoDao().deletePhoto(PhotoEntity.fromPhoto(photo))
    }

}