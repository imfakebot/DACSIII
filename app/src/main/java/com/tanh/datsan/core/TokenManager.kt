package com.tanh.datsan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStorage by preferencesDataStore(name = "user_prefs")

class TokenManager (context: Context) {
    private val dataStore = context.applicationContext.dataStorage
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    val token : Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

        suspend fun saveToken(token: String) {
            dataStore.edit { preferences ->
                preferences[TOKEN_KEY] = token
            }
        }

        suspend fun clearToken() {
            dataStore.edit { preferences ->
                preferences.remove(TOKEN_KEY)
            }
        }
}