package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.repository.NotificationRepository
import com.tanh.datsan.data.repository.UserRepository
import com.tanh.datsan.utils.JwtUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl.asStateFlow()

    val unreadNotification = notificationRepository.unreadCountFlow

    val isLoggedIn: StateFlow<Boolean> = tokenManager.token
        .map { !it.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val userRole: StateFlow<String> = tokenManager.token
        .map { JwtUtil.getRoleFromToken(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "user"
        )

    init {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    fetchUserProfile()
                    fetchInitialUnreadCount()
                } else {
                    _userName.value = null
                    _userAvatarUrl.value = null
                }
            }
        }
    }

    private fun fetchInitialUnreadCount() {
        viewModelScope.launch {
            try {
                notificationRepository.fetchIntialUnreadCount()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Lỗi lấy thông báo: ${e.message}")
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userProfile = userRepository.getProfileLogginedIn()
                _userName.value = userProfile.fullName
                _userAvatarUrl.value = userProfile.avatarUrl
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user profile: ${e.message}")
            }
        }
    }
}
