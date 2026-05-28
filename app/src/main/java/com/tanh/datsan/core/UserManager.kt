package com.tanh.datsan.core

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UserManager @Inject constructor() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName : StateFlow<String?> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    fun setUserInfo(name: String?, avatarUrl: String?) {
        _userName.value = name
        _userAvatar.value = avatarUrl
    }

    fun clearUserInfo() {
        _userName.value = null
        _userAvatar.value = null
    }
}