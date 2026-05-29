package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.NotificationModel
import com.tanh.datsan.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchNotification()
    }

    fun fetchNotification() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = notificationRepository.getNotification(page = 1, limit = 20)
                _notifications.value = response.data
                _unreadCount.value=response.meta.unreadCount
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi tải thông báo: ${e.message}")
            } finally {
                _isLoading.value=true
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead()
                fetchNotification()
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi markAllAsRead: ${e.message}")
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(id)
                fetchNotification()
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi markAsRead: ${e.message}")
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                notificationRepository.clearAllNotifications()
                fetchNotification()
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi clearAllNotifications: ${e.message}")
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(id)
                fetchNotification()
            } catch (e: Exception) {
                Log.e("NotificationVM", "Lỗi deleteNotification: ${e.message}")
            }
        }
    }
}