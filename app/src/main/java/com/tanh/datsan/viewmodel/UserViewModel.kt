package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.core.UserManager
import com.tanh.datsan.data.repository.AuthRepository
import com.tanh.datsan.data.repository.NotificationRepository
import com.tanh.datsan.data.repository.UserRepository
import com.tanh.datsan.utils.JwtUtil
import com.tanh.datsan.utils.toFullImageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val userManager: UserManager
) : ViewModel() {

    val userName: StateFlow<String?> = userManager.userName
    val userAvatarUrl: StateFlow<String?> = userManager.userAvatarUrl
        .map { it?.toFullImageUrl() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
        Log.d("UserViewModel", "Initializing UserViewModel")
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                Log.d("UserViewModel", "isLoggedIn emission: $loggedIn")
                if (loggedIn) {
                    Log.d("UserViewModel", "User is logged in, fetching profile...")
                    fetchUserProfile()
                    fetchInitialUnreadCount()
                } else {
                    Log.d("UserViewModel", "User is not logged in")
                }
            }
        }
    }

    private fun fetchInitialUnreadCount() {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Fetching initial unread count")
                notificationRepository.fetchIntialUnreadCount()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Lỗi lấy thông báo: ${e.message}")
            }
        }
    }

    fun fetchUserProfile() {
        Log.d("UserViewModel", "fetchUserProfile() called")
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Calling userRepository.getUserProfile()")
                val account = userRepository.getUserProfile()
                Log.d("UserViewModel", "Fetched user profile successfully: ${account.userProfile.fullName}")
                userManager.setUserInfo(
                    name = account.userProfile.fullName,
                    avatarUrl = account.userProfile.avatarUrl
                )
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user profile: ${e.message}", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
