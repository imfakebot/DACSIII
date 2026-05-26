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

// Khởi tạo DataStore ở cấp độ top-level
val Context.dataStorage by preferencesDataStore(name = "user_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    // Luôn dùng applicationContext để tránh rò rỉ bộ nhớ (Memory Leak)
    private val dataStore = context.applicationContext.dataStorage

    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // Cache để lấy token đồng bộ (Dùng cho Retrofit Interceptor)
    var cachedAccessToken: String? = null
        private set
    var cachedRefreshToken: String? = null
        private set

    // 1. KHAI BÁO CÁC FLOW TRƯỚC (RẤT QUAN TRỌNG ĐỂ TRÁNH NULL POINTER EXCEPTION)
    // Lấy Access Token (Flow để View/ViewModel observe)
    val getAccessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    // Lấy Refresh Token
    val getRefreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val getUserAvatar: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY]
    }

    val getUserName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    // 2. CHẠY INIT BLOCK SAU KHI CÁC FLOW ĐÃ ĐƯỢC KHỞI TẠO
    init {
        // Tự động cập nhật cache mỗi khi token trong DataStore thay đổi
        CoroutineScope(Dispatchers.IO).launch {
            getAccessToken.collect { token ->
                cachedAccessToken = token
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            getRefreshToken.collect { token ->
                cachedRefreshToken = token
            }
        }
    }

    // Lưu CẢ 2 token vào máy
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
        // Cập nhật ngay vào cache để có thể dùng ngay lập tức
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
    }

    suspend fun saveUserInfo(avatarUrl: String?, userName: String?) {
        dataStore.edit { preferences ->
            avatarUrl?.let { preferences[USER_AVATAR_KEY] = it }
            userName?.let { preferences[USER_NAME_KEY] = it }

        }
    }

    // Xóa sạch token khi người dùng bấm Đăng Xuất
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_AVATAR_KEY) // 🌟 Xóa thêm cái này
            preferences.remove(USER_NAME_KEY)   // 🌟 Xóa thêm cái này
        }
        // Xóa cả cache
        cachedAccessToken = null
        cachedRefreshToken = null
    }
}