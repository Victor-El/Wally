package com.turingheights.wally.commons.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import com.turingheights.wally.commons.data.local.daos.PhotoDao
import com.turingheights.wally.commons.data.local.entities.CacheMetadataEntity
import com.turingheights.wally.commons.data.local.entities.CachedPhotoEntity
import com.turingheights.wally.commons.data.local.entities.PhotoEntity

@Database(
    entities = [
        PhotoEntity::class,
        CachedPhotoEntity::class,
        CacheMetadataEntity::class
    ],
    version = 2
)
abstract class PhotoDatabase: RoomDatabase() {

    abstract fun photoDao(): PhotoDao
    abstract fun cachedPhotoDao(): CachedPhotoDao

    companion object {
        private var INSTANCE: PhotoDatabase? = null
        fun getInstance(context: Context): PhotoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, PhotoDatabase::class.java, "photo.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }

    }

}
