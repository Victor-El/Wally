package me.codeenzyme.wally.commons.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.codeenzyme.wally.commons.data.local.PhotoDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun providesPhotoDatabase(@ApplicationContext context: Context): PhotoDatabase {
        return PhotoDatabase.getInstance(context)
    }

}