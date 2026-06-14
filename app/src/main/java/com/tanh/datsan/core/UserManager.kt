package com.tanh.datsan.core

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UserManager @Inject constructor() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName : StateFlow<String?> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatar.asStateFlow()

    fun setUserInfo(name: String?, avatarUrl: String?) {
        Log.d("UserManager", "Setting user info in memory: name=$name, avatarUrl=$avatarUrl")
        _userName.value = name
        _userAvatar.value = avatarUrl
    }

    fun clearUserInfo() {
        Log.d("UserManager", "Clearing user info from memory")
        _userName.value = null
        _userAvatar.value = null
    }
}