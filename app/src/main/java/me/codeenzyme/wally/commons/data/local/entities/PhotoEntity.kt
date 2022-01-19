package me.codeenzyme.wally.commons.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.codeenzyme.wally.commons.models.Photo

@Entity
data class PhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: Int,
    val previewURL: String,
    val fullHDURL: String?,
    val imageURL: String?,
    val previewHeight: Int,
    val previewWidth: Int,
    val largeImageURL: String,
    val webformatURL: String,
    val webformatWidth: Int,
    val webformatHeight: Int
) {
    companion object {
        fun fromPhoto(photo: Photo) = PhotoEntity(
            photo.id,
            photo.previewURL,
            photo.fullHDURL,
            photo.imageURL,
            photo.previewHeight,
            photo.previewWidth,
            photo.largeImageURL,
            photo.webformatURL,
            photo.webformatWidth,
            photo.webformatHeight,
        )

        fun toPhoto(photo: PhotoEntity) = Photo(
            photo.id,
            photo.previewURL,
            photo.fullHDURL,
            photo.imageURL,
            photo.previewHeight,
            photo.previewWidth,
            photo.largeImageURL,
            photo.webformatURL,
            photo.webformatWidth,
            photo.webformatHeight,
        )
    }
}
