package com.tanh.datsan.data.model

data class NotificationResponse(
   val data: List<NotificationModel>,
    val meta : NotificationMeta
)

data class NotificationModel(
    val id:String,
    val title:String,
    val content:String,
    val isRead: Boolean,
    val createdAt:String
)

data class NotificationMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val lastPage: Int,
    val unreadCount: Int
)
