package com.tanh.datsan.core

import android.util.Log
import com.google.gson.Gson
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.data.model.ChatMessage
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var socket: Socket? = null
    private val gson = Gson()

    private val _messageFlow = MutableSharedFlow<ChatMessage>()
    val messageFlow = _messageFlow.asSharedFlow()

    private val _notificationFlow = MutableSharedFlow<String>()
    val notificationFlow = _notificationFlow.asSharedFlow()

    fun connect() {
        if (socket?.connected() == true) return

        val token = tokenManager.cachedToken
        if (token.isNullOrBlank()) {
            Log.e("SocketManager", "Cannot connect: Token is null")
            return
        }

        try {
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                forceNew = true
                reconnection = true
            }

            socket = IO.socket(BuildConfig.API_BACKEND, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected to server")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketManager", "Disconnected from server")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketManager", "Connection error: ${args.getOrNull(0)}")
            }

            // Lắng nghe tin nhắn mới
            socket?.on("receive_message") { args ->
                val data = args.getOrNull(0) as? JSONObject
                data?.let {
                    val message = gson.fromJson(it.toString(), ChatMessage::class.java)
                    CoroutineScope(Dispatchers.IO).launch {
                        _messageFlow.emit(message)
                    }
                }
            }

            // Lắng nghe thông báo hệ thống
            socket?.on("new_notification") { args ->
                val data = args.getOrNull(0).toString()
                CoroutineScope(Dispatchers.IO).launch {
                    _notificationFlow.emit(data)
                }
            }

            // Lắng nghe exception
            socket?.on("exception") { args ->
                Log.e("SocketManager", "Socket Exception: ${args.getOrNull(0)}")
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("SocketManager", "Socket Initialization Error", e)
        }
    }

    fun joinFeedbackRoom(feedbackId: String) {
        Log.d("SocketManager", "Joining room: feedback_$feedbackId")
        socket?.emit("join_feedback_room", feedbackId)
    }

    fun leaveFeedbackRoom(feedbackId: String) {
        Log.d("SocketManager", "Leaving room: feedback_$feedbackId")
        socket?.emit("leave_feedback_room", feedbackId)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
