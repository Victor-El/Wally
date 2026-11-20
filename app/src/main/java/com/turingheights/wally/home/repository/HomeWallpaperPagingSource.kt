package com.turingheights.wally.home.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.turingheights.wally.commons.data.local.daos.CachedPhotoDao
import com.turingheights.wally.commons.data.local.entities.CacheMetadataEntity
import com.turingheights.wally.commons.data.local.entities.CachedPhotoEntity
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.models.WallpaperDataNetworkState
import com.turingheights.wally.commons.utils.HOME_WALLPAPER_PAGE_SIZE
import com.turingheights.wally.home.data.remote.HomeScreenWallpaperService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
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
) : PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val currentPage: Int = params.key ?: 1
        val prevKey = if (currentPage == 1) null else currentPage - 1

        return try {
            if (currentPage == 1) {
                Timber.d("Starting initial retrofit request")
                networkStateFlow.value = WallpaperDataNetworkState.Loading
            }
            val response = homeScreenWallpaperService.getHomeScreenWallpaper(
                page = currentPage,
                query = searchTerm,
                category = category,
                safeSearch = safeSearch,
                orientation = orientation,
                imagetype = imageType,
                order = order,
                perPage = HOME_WALLPAPER_PAGE_SIZE
            )

            if (currentPage == 1) {
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Error saving to cache")
                }
            }

            val nextKey = if (
                response.hits.isEmpty() ||
                currentPage * HOME_WALLPAPER_PAGE_SIZE >= response.totalHits
            ) {
                null
            } else {
                currentPage + 1
            }

            LoadResult.Page(
                data = response.hits,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Pixabay returns HTTP 400 when requesting a page beyond the available range
            if (e is HttpException && e.code() == 400 && currentPage > 1) {
                return LoadResult.Page(emptyList(), prevKey, null)
            }

            Timber.e(e, "Error loading wallpapers for page $currentPage")

            if (currentPage == 1) {
                try {
                    val cachedEntities = cachedPhotoDao.getCachedPhotos(
                        searchTerm, category, safeSearch, orientation, imageType, order
                    )
                    if (cachedEntities.isNotEmpty()) {
                        Timber.d("Network failed; falling back to cache (${cachedEntities.size} items)")
                        val photos = cachedEntities.map { CachedPhotoEntity.toPhoto(it) }
                        networkStateFlow.value = WallpaperDataNetworkState.Success
                        return LoadResult.Page(photos, null, 2)
                    }
                } catch (cacheError: CancellationException) {
                    throw cacheError
                } catch (cacheError: Exception) {
                    Timber.e(cacheError, "Error loading fallback cache")
                }
                networkStateFlow.value = WallpaperDataNetworkState.Failure
            }
            LoadResult.Error(e)
        }
    }
}
