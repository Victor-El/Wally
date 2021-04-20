package me.codeenzyme.wally.home.data.remote

import me.codeenzyme.wally.commons.models.PhotoSearchResult
import me.codeenzyme.wally.commons.utils.API_KEY
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeScreenWallpaperService {

    @GET("api/")
    suspend fun getHomeScreenWallpaper(
        @Query("page") page: Int,
        @Query("key") apiKey: String = API_KEY
    ): PhotoSearchResult
}