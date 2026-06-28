package com.turingheights.wally.commons.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import com.turingheights.wally.commons.repositories.impls.DefaultFavouritePhotoRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingDataModule {

    @Binds
    abstract fun bindsFavouritePhotoRepository(defaultFavouritePhotoRepository: DefaultFavouritePhotoRepository): FavouritePhotosRepository

}