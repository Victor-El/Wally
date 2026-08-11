package com.turingheights.wally.commons.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.turingheights.wally.commons.models.Photo

@Entity(
    tableName = "cached_photos",
    foreignKeys = [
        ForeignKey(
            entity = CacheMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["cacheMetadataId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cacheMetadataId"])]
)
data class CachedPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val cacheEntryId: Long = 0,
    val cacheMetadataId: Long,
    val id: Int, // The original photo ID from API
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
        fun fromPhoto(photo: Photo, cacheMetadataId: Long) = CachedPhotoEntity(
            cacheMetadataId = cacheMetadataId,
            id = photo.id,
            previewURL = photo.previewURL,
            fullHDURL = photo.fullHDURL,
            imageURL = photo.imageURL,
            previewHeight = photo.previewHeight,
            previewWidth = photo.previewWidth,
            largeImageURL = photo.largeImageURL,
            webformatURL = photo.webformatURL,
            webformatWidth = photo.webformatWidth,
            webformatHeight = photo.webformatHeight,
        )

        fun toPhoto(entity: CachedPhotoEntity) = Photo(
            id = entity.id,
            previewURL = entity.previewURL,
            fullHDURL = entity.fullHDURL,
            imageURL = entity.imageURL,
            previewHeight = entity.previewHeight,
            previewWidth = entity.previewWidth,
            largeImageURL = entity.largeImageURL,
            webformatURL = entity.webformatURL,
            webformatWidth = entity.webformatWidth,
            webformatHeight = entity.webformatHeight,
        )
    }
}
