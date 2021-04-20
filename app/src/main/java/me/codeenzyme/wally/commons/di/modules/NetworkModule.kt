package me.codeenzyme.wally.commons.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.codeenzyme.wally.commons.utils.BASE_API_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_API_URL)
            .client(OkHttpClient.Builder().also {
                it.connectTimeout(30, TimeUnit.SECONDS)
                it.readTimeout(30, TimeUnit.SECONDS)
                it.writeTimeout(30, TimeUnit.SECONDS)
                it.addInterceptor(HttpLoggingInterceptor())
            }.build())
            .build()
    }

}