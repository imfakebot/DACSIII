package com.tanh.datsan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khởi tạo DataStore ở cấp độ top-level
val Context.dataStorage by preferencesDataStore(name = "user_prefs")

class TokenManager(context: Context) {
    // Luôn dùng applicationContext để tránh rò rỉ bộ nhớ (Memory Leak) trên Android
    private val dataStore = context.applicationContext.dataStorage

    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    // Lấy Access Token
    val getAccessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    // Lấy Refresh Token (Phục vụ cho việc gọi API /auth/refresh sau này)
    val getRefreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    // Lưu CẢ 2 token vào máy
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // Xóa sạch token khi người dùng bấm Đăng Xuất
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}