package me.codeenzyme.wally.home.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.codeenzyme.wally.home.data.remote.HomeScreenWallpaperService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HomeModule {

    @Provides
    @Singleton
    @Inject
    fun providesHomeScreenWallpaperService(retrofit: Retrofit): HomeScreenWallpaperService {
        return retrofit.create(HomeScreenWallpaperService::class.java)
    }
}