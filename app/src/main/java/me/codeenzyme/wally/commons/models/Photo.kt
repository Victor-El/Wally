package me.codeenzyme.wally.commons.models

data class Photo(
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
