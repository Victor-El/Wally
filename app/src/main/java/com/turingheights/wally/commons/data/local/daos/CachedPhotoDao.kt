package com.turingheights.wally.commons.data.local.daos

import androidx.room.*
import com.turingheights.wally.commons.data.local.entities.CacheMetadataEntity
import com.turingheights.wally.commons.data.local.entities.CachedPhotoEntity
import com.turingheights.wally.commons.models.Photo

@Dao
interface CachedPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: CacheMetadataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPhotos(photos: List<CachedPhotoEntity>)

    @Query("""
        SELECT * FROM cache_metadata 
        WHERE `query` IS :query 
        AND category IS :category 
        AND safeSearch = :safeSearch 
        AND orientation = :orientation 
        AND imageType = :imageType 
        AND `order` = :order 
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getLatestMetadata(
        query: String?,
        category: String?,
        safeSearch: Boolean,
        orientation: String,
        imageType: String,
        order: String
    ): CacheMetadataEntity?

    @Query("SELECT * FROM cached_photos WHERE cacheMetadataId = :metadataId")
    suspend fun getPhotosForMetadata(metadataId: Long): List<CachedPhotoEntity>

    @Transaction
    suspend fun saveCache(metadata: CacheMetadataEntity, photos: List<Photo>) {
        // Optional: clear old cache for these params first
        deleteOldCache(metadata.query, metadata.category, metadata.safeSearch, metadata.orientation, metadata.imageType, metadata.order)
        
        val id = insertMetadata(metadata)
        val entities = photos.map { CachedPhotoEntity.fromPhoto(it, id) }
        insertCachedPhotos(entities)
    }

    @Query("""
        DELETE FROM cache_metadata 
        WHERE `query` IS :query 
        AND category IS :category 
        AND safeSearch = :safeSearch 
        AND orientation = :orientation 
        AND imageType = :imageType 
        AND `order` = :order
    """)
    suspend fun deleteOldCache(
        query: String?,
        category: String?,
        safeSearch: Boolean,
        orientation: String,
        imageType: String,
        order: String
    )
    
    // Helper to get photos directly if cache exists
    @Transaction
    suspend fun getCachedPhotos(
        query: String?,
        category: String?,
        safeSearch: Boolean,
        orientation: String,
        imageType: String,
        order: String
    ): List<CachedPhotoEntity> {
        val metadata = getLatestMetadata(query, category, safeSearch, orientation, imageType, order)
        return metadata?.let { getPhotosForMetadata(it.id) } ?: emptyList()
    }
}
