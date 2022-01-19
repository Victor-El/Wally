package me.codeenzyme.wally.commons.data.local.daos

import androidx.room.*
import me.codeenzyme.wally.commons.data.local.entities.PhotoEntity

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photoentity")
    suspend fun getAll(): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photoEntity: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photoEntity: PhotoEntity)

}