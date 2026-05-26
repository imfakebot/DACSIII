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

val Context.dataStorage by preferencesDataStore(name = "user_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.applicationContext.dataStorage

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    var cachedToken: String? = null
        private set

    init {
        CoroutineScope(Dispatchers.IO).launch {
            token.collect { currentToken->
                cachedToken = currentToken
            }
        }
    }

    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]?:"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFuaHR0LjI0aXRAdmt1LnVkbi52biIsInN1YiI6IjhhOTE5NmEzLTU1MDktMTFmMS1hY2RmLTZhNDU1NDM5OTg2NCIsInJvbGUiOiJ1c2VyIiwidXNlclByb2ZpbGVJZCI6IjRmZmRhYTNjLTMzZWMtNGEzNS05YmRlLWFiMjIwZTQwMDUyZSIsImlhdCI6MTc3OTgwNzc1NiwiZXhwIjoxNzc5ODA4NjU2fQ.XnkB9_8Kg3tjDfozEMGK0cmmB43D3TQqayx5OKyUvFQ"
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
        cachedToken = null
    }
}