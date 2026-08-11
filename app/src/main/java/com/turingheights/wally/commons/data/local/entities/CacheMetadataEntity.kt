package com.turingheights.wally.commons.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String?,
    val category: String?,
    val safeSearch: Boolean,
    val orientation: String,
    val imageType: String,
    val order: String,
    val timestamp: Long = System.currentTimeMillis()
)
