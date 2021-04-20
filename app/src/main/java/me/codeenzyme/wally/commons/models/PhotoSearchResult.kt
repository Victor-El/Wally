package me.codeenzyme.wally.commons.models

data class PhotoSearchResult(
    val total: Int,
    val totalHits: Int,
    val hits: List<Photo>,
)
