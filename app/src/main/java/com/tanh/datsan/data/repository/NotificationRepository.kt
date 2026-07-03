package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.NotificationResponse
import com.tanh.datsan.data.network.NotificationApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {
    private val _unreadCountFlow = MutableStateFlow(0)
    val unreadCountFlow = _unreadCountFlow.asStateFlow()

    suspend fun fetchIntialUnreadCount(){
        try{
            val response = notificationApiService.getNotification(1,1)
            _unreadCountFlow.value=response.meta.unreadCount
        }catch(e:Exception){
            _unreadCountFlow.value = 0
        }
    }

    suspend fun getNotification(page: Int = 1, limit: Int = 10): NotificationResponse {
        val response = notificationApiService.getNotification(page, limit)
        _unreadCountFlow.value = response.meta.unreadCount
        return response
    }

    suspend fun markAllAsRead() {
        notificationApiService.markAllAsRead()
    }

    suspend fun markAsRead(id: String) {
        notificationApiService.markAsRead(id)
    }


    suspend fun clearAllNotifications() {
        notificationApiService.clearAllNotifications()
    }


    suspend fun deleteNotification(id: String) {
        notificationApiService.deleteNotification(id)
    }
}
