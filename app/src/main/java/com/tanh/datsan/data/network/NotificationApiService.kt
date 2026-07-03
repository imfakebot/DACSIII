package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.NotificationModel
import com.tanh.datsan.data.model.NotificationResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {
    @GET("notification")
    suspend fun getNotification(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): NotificationResponse

    @PATCH("notification/read-all")
    suspend fun markAllAsRead()

    @DELETE("notification/clear-all")
    suspend fun clearAllNotifications()

    @DELETE("notification/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String
    )

    @PATCH("notification/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    )
}