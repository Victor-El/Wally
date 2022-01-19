package me.codeenzyme.wally.commons.preferencestore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

const val SETTINGS = "settings"
//val Context.settingsPref: DataStore<Preferences> by preferencesDataStore(name = SETTINGS)
val Context.settingsPref: DataStore<Preferences> by preferencesDataStore(name = SETTINGS)

const val SAFE_SEARCH_PREF = "safe_search"
val SAFE_SEARCH_PREF_KEY = booleanPreferencesKey(SAFE_SEARCH_PREF)

const val ORIENTATION_PREF = "orientation"
val ORIENTATION_PREF_KEY = stringPreferencesKey(ORIENTATION_PREF)

const val IMAGE_TYPE_PREF = "image_type"
val IMAGE_TYPE_PREF_KEY = stringPreferencesKey(IMAGE_TYPE_PREF)

const val ORDER_PREF = "order"
val ORDER_PREF_KEY = stringPreferencesKey(ORDER_PREF)
