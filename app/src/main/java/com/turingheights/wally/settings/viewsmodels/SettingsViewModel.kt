package com.turingheights.wally.settings.viewsmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.turingheights.wally.commons.preferencestore.IMAGE_TYPE_PREF_KEY
import com.turingheights.wally.commons.preferencestore.ORIENTATION_PREF_KEY
import com.turingheights.wally.commons.preferencestore.settingsPref
import kotlinx.coroutines.flow.map

class SettingsViewModel : ViewModel() {

    fun getOrientation(context: Context) = context.settingsPref.data.map {
        it[ORIENTATION_PREF_KEY]?.uppercase() ?: "ALL"
    }

    fun getImageType(context: Context) = context.settingsPref.data.map {
        it[IMAGE_TYPE_PREF_KEY]?.uppercase() ?: "ALL"
    }
}
