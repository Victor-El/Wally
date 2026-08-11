package com.turingheights.wally.home.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import com.turingheights.wally.commons.data.local.entities.CacheMetadataEntity
import com.turingheights.wally.commons.data.local.entities.CachedPhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import retrofit2.HttpException
import timber.log.Timber

class HomeWallpaperPagingSource constructor(
    private val homeScreenWallpaperService: HomeScreenWallpaperService,
    private val cachedPhotoDao: CachedPhotoDao,
    private val safeSearch: Boolean,
    private val orientation: String,
    private val imageType: String,
    private val order: String,
    private val category: String? = null,
    private val searchTerm: String? = null,
    private val networkStateFlow: MutableStateFlow<WallpaperDataNetworkState>
): PagingSource<Int, Photo>(){

    private var hasLoadedFromCache = false

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition ?: return null)
            anchorPage?.nextKey?.plus(1) ?: anchorPage?.prevKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val nextPageNumber: Int = params.key ?: 1
        
        // 1. Try to load from cache first if it's the very first request
        if (nextPageNumber == 1 && !hasLoadedFromCache) {
            try {
                val cachedEntities = cachedPhotoDao.getCachedPhotos(
                    searchTerm, category, safeSearch, orientation, imageType, order
                )
                if (cachedEntities.isNotEmpty()) {
                    Timber.d("Loading from cache: ${cachedEntities.size} items")
                    hasLoadedFromCache = true
                    val photos = cachedEntities.map { CachedPhotoEntity.toPhoto(it) }
                    // Update network state to success since we have data to show
                    networkStateFlow.value = WallpaperDataNetworkState.Success
                    // Return cache, but next key should be 1 to trigger fresh network fetch of page 1?
                    // Actually, the prompt says "subsequent calls... gotten from the network".
                    // If we want to replace cache with network immediately, we'd need a different approach.
                    // But if we just want to show cache THEN network on scroll, we set nextKey = 2.
                    return LoadResult.Page(photos, null, 2)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading from cache")
            }
        }

        return try {
            if (nextPageNumber == 1) {
                Timber.d("Starting initial retrofit request")
                networkStateFlow.value = WallpaperDataNetworkState.Loading
            }
            val response = homeScreenWallpaperService.getHomeScreenWallpaper(
                nextPageNumber,
                query = searchTerm,
                category = category,
                safeSearch = safeSearch,
                orientation = orientation,
                imagetype = imageType,
                order = order
            )
            
            // 2. Save to cache if it's page 1
            if (nextPageNumber == 1) {
                networkStateFlow.value = WallpaperDataNetworkState.Success
                try {
                    val metadata = CacheMetadataEntity(
                        query = searchTerm,
                        category = category,
                        safeSearch = safeSearch,
                        orientation = orientation,
                        imageType = imageType,
                        order = order
                    )
                    cachedPhotoDao.saveCache(metadata, response.hits)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving to cache")
                }
            }
            
            Timber.d(response.hits.toString())
            LoadResult.Page(response.hits, null, nextPageNumber + 1)
        } catch (e: Exception) {
            if (e is HttpException) {
                return LoadResult.Page(emptyList(), null, null)
            }
            Timber.d(e)
            if (nextPageNumber == 1) {
                networkStateFlow.value = WallpaperDataNetworkState.Failure
            }
            LoadResult.Error(e)
        }
    }

}
