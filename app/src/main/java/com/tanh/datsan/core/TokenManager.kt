package com.tanh.datsan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// Tách riêng file lưu trữ auth
private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.authDataStore

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    var cachedToken: String? = null
        private set
    var cachedRefreshToken: String? = null
        private set

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val getAccessToken: Flow<String?> = token
    val getRefreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            token.collect { cachedToken = it }
        }
        CoroutineScope(Dispatchers.IO).launch {
            getRefreshToken.collect { cachedRefreshToken = it }
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
        cachedToken = token
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit {
            it[TOKEN_KEY] = accessToken
            it[REFRESH_TOKEN_KEY] = refreshToken
        }
        cachedToken = accessToken
        cachedRefreshToken = refreshToken
    }

    suspend fun clearTokens() {
        dataStore.edit { it.clear() } // Xóa toàn bộ token nhanh gọn
        cachedToken = null
        cachedRefreshToken = null
    }
}