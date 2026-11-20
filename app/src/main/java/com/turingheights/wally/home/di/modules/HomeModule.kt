package com.turingheights.wally.home.di.modules

import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HomeModule {

    @Provides
    @Singleton
    fun providesHomeScreenWallpaperService(retrofit: Retrofit): HomeScreenWallpaperService {
        return retrofit.create(HomeScreenWallpaperService::class.java)
    }
}
