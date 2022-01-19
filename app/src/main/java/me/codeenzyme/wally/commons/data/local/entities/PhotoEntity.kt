package me.codeenzyme.wally.commons.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PhotoEntity(
    @PrimaryKey
    val id: Int,
    val previewURL: String,
    val fullHDURL: String,
    val imageURL: String,
    val previewHeight: Int,
    val previewWidth: Int,
    val largeImageURL: String,
    val webformatURL: String,
    val webformatWidth: Int,
    val webformatHeight: Int
)
