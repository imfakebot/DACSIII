package com.tanh.datsan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Tách riêng file lưu trữ cài đặt
private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@Singleton
class AppSettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    val getTheme: Flow<String> = dataStore.data.map { it[THEME_KEY] ?: "system" }
    val getLanguage: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "vi" }

    suspend fun saveTheme(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveLanguage(language: String) {
        dataStore.edit { it[LANGUAGE_KEY] = language }
    }
}