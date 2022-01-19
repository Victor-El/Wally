package me.codeenzyme.wally.home.data.remote

import me.codeenzyme.wally.commons.models.PhotoSearchResult
import me.codeenzyme.wally.commons.utils.ALL
import me.codeenzyme.wally.commons.utils.API_KEY
import me.codeenzyme.wally.commons.utils.POPULAR
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeScreenWallpaperService {

    @GET("api/")
    suspend fun getHomeScreenWallpaper(
        @Query("page") page: Int,
        @Query("key") apiKey: String = API_KEY,
        @Query("q") query: String? = null,
        @Query("safesearch") safeSearch: Boolean = false,
        @Query("orientation") orientation: String = ALL,
        @Query("order") order: String = POPULAR,
        @Query("image_type") imagetype: String = ALL
    ): PhotoSearchResult
}