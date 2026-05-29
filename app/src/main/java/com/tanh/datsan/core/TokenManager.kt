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
        // Giữ đồng bộ chuỗi định danh "jwt_token" từ Git cho Access Token
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_ADDRESS_KEY = stringPreferencesKey("user_address")
        
        // Settings keys
        val THEME_KEY = stringPreferencesKey("app_theme") // "light", "dark", "system"
        val LANGUAGE_KEY = stringPreferencesKey("app_language") // "vi", "en"
    }

    // Biến tĩnh dùng đồng bộ trực tiếp trong NetworkModule của Git
    var cachedToken: String? = null
        private set
    var cachedRefreshToken: String? = null
        private set

    // Flow luồng dữ liệu thay đổi của Token chính (Tương thích cả Git và Local)
    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Khai báo Flow lấy Access Token (Trỏ trực tiếp về TOKEN_KEY để không lệch cấu trúc)
    val getAccessToken: Flow<String?> = token

    val getRefreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val getUserAvatar: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY]
    }

    val getUserName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val getUserPhone: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_PHONE_KEY]
    }

    val getUserAddress: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ADDRESS_KEY]
    }

    // Settings Flows
    val getTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    val getLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "vi"
    }

    init {
        // Lắng nghe và gán tự động vào biến cache tĩnh theo kiến trúc tối ưu của Git
        CoroutineScope(Dispatchers.IO).launch {
            token.collect { currentToken ->
                cachedToken = currentToken
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            getRefreshToken.collect { token ->
                cachedRefreshToken = token
            }
        }
    }

    // Hàm lưu token đơn của Git (Dành cho các tiến trình cơ bản)
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
        cachedToken = token
    }

    // Hàm lưu cặp Token đôi cao cấp của Local (Giúp MVVM chạy cơ chế Refresh Token tự động)
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
        cachedToken = accessToken
        cachedRefreshToken = refreshToken
    }

    // Giữ nguyên hàm lưu thông tin User của Local để hiển thị lên MainScreen
    suspend fun saveUserInfo(
        avatarUrl: String?,
        userName: String?,
        phone: String? = null,
        address: String? = null
    ) {
        dataStore.edit { preferences ->
            avatarUrl?.let { preferences[USER_AVATAR_KEY] = it }
            userName?.let { preferences[USER_NAME_KEY] = it }
            phone?.let { preferences[USER_PHONE_KEY] = it }
            address?.let { preferences[USER_ADDRESS_KEY] = it }
        }
    }

    // Settings Save Methods
    suspend fun saveTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    // Xóa sạch dấu vết bộ nhớ khi người dùng thực hiện Log Out
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_AVATAR_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_PHONE_KEY)
            preferences.remove(USER_ADDRESS_KEY)
        }
        cachedToken = null
        cachedRefreshToken = null
    }

    // Map hàm cũ của bản Local về hàm mới để tránh báo lỗi đỏ ở các file Repository
    suspend fun clearTokens() = clearToken()
}