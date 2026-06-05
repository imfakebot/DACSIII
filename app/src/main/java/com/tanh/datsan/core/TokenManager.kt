package com.tanh.datsan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.authDataStore

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    // Biến cache trên RAM để truy cập tức thì (High Speed)
    @Volatile
    var cachedToken: String? = null
    var cachedRefreshToken: String? = null

    val tokenFlow: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val getRefreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    suspend fun getAccessToken(): String? {
        // Ưu tiên lấy từ RAM, nếu rỗng thì lấy từ Disk
        if (cachedToken.isNullOrBlank()) {
            cachedToken = tokenFlow.firstOrNull()
        }
        return if (cachedToken == "null" || cachedToken == "undefined") null else cachedToken
    }

    suspend fun getRefreshTokenSync(): String? {
        if (cachedRefreshToken.isNullOrBlank()) {
            cachedRefreshToken = getRefreshToken.firstOrNull()
        }
        return if (cachedRefreshToken == "null" || cachedRefreshToken == "undefined") null else cachedRefreshToken
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        // Cập nhật RAM trước để các thread khác thấy ngay
        cachedToken = accessToken
        cachedRefreshToken = refreshToken
        
        // Sau đó mới lưu vào Disk
        dataStore.edit {
            it[TOKEN_KEY] = accessToken
            it[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clearTokens() {
        cachedToken = null
        cachedRefreshToken = null
        dataStore.edit { it.clear() }
    }
}
