package me.codeenzyme.wally.commons.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.codeenzyme.wally.commons.data.local.daos.PhotoDao
import me.codeenzyme.wally.commons.data.local.entities.PhotoEntity

@Database(entities = [PhotoEntity::class], version = 1)
abstract class PhotoDatabase: RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    companion object {
        private var INSTANCE: PhotoDatabase? = null
        fun getInstance(context: Context): PhotoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, PhotoDatabase::class.java, "photo.db").build()
            }
            return INSTANCE!!
        }

    }

}