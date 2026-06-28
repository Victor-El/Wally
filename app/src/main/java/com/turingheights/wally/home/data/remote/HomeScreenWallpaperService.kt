package com.turingheights.wally.home.data.remote

import com.turingheights.wally.commons.models.PhotoSearchResult
import com.turingheights.wally.commons.utils.ALL
import com.turingheights.wally.commons.utils.API_KEY
import com.turingheights.wally.commons.utils.POPULAR
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