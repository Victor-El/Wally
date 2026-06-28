package com.turingheights.wally.commons.models

sealed class WallpaperDataNetworkState {
    object Loading: WallpaperDataNetworkState()
    object Success: WallpaperDataNetworkState()
    object Failure: WallpaperDataNetworkState()
}
