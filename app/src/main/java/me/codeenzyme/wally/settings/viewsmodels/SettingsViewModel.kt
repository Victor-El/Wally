package me.codeenzyme.wally.settings.viewsmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.codeenzyme.wally.commons.preferencestore.*
import java.util.*

class SettingsViewModel: ViewModel() {

    fun getSafeSearch(context: Context) = context.settingsPref.data.map {
        it[SAFE_SEARCH_PREF_KEY] ?: false
    }

    fun setSafeSearch(context: Context, boolean: Boolean) {
        viewModelScope.launch {
            context.settingsPref.edit {
                it[SAFE_SEARCH_PREF_KEY] = boolean
            }
        }
    }

    fun getOrientation(context: Context) = context.settingsPref.data.map {
        it[ORIENTATION_PREF_KEY]?.toUpperCase(Locale.ROOT) ?: "ALL"
    }

    fun getImageType(context: Context) = context.settingsPref.data.map {
        it[IMAGE_TYPE_PREF_KEY]?.toUpperCase(Locale.ROOT) ?: "ALL"
    }

    fun getOrder(context: Context) = context.settingsPref.data.map {
        it[ORDER_PREF_KEY]?.toUpperCase(Locale.ROOT) ?: "POPULAR"
    }
}