package me.codeenzyme.wally.commons.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.codeenzyme.wally.commons.repositories.FavouritePhotosRepository
import me.codeenzyme.wally.commons.repositories.impls.DefaultFavouritePhotoRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingDataModule {

    @Binds
    abstract fun bindsFavouritePhotoRepository(defaultFavouritePhotoRepository: DefaultFavouritePhotoRepository): FavouritePhotosRepository

}