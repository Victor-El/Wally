package me.codeenzyme.wally.commons.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Photo(
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
): Parcelable
