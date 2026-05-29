package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.ChatMessage
import com.tanh.datsan.data.model.FeedbackResponse
import com.tanh.datsan.data.repository.FeedbackRepository
import com.tanh.datsan.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _feedbackDetail = MutableStateFlow<FeedbackResponse?>(null)
    val feedbackDetail = _feedbackDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Lấy ID của user hiện tại để phân biệt tin nhắn bên trái/phải
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        // Kết nối socket khi ViewModel được khởi tạo
        feedbackRepository.connectSocket()
        
        // Lắng nghe tin nhắn real-time từ Repository
        viewModelScope.launch {
            feedbackRepository.realTimeMessages.collect { newMessage ->
                _messages.value = _messages.value + newMessage
            }
        }

        // Lấy thông tin user hiện tại (giả định bạn đã có hàm này trong UserRepository)
        // Nếu chưa có, ta có thể lấy từ profile state
        viewModelScope.launch {
            userRepository.getProfile().body()?.let {
                _currentUserId.value = it.id
            }
        }
    }

    fun loadChatHistory(feedbackId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = feedbackRepository.getFeedbackDetail(feedbackId)
                if (response.isSuccessful) {
                    val detail = response.body()
                    _feedbackDetail.value = detail
                    _messages.value = detail?.responses ?: emptyList()
                    
                    // Sau khi load history thành công thì join vào room để nhận tin mới
                    feedbackRepository.joinRoom(feedbackId)
                } else {
                    _error.value = "Không thể tải lịch sử trò chuyện"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(feedbackId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val response = feedbackRepository.replyFeedback(feedbackId, content)
                if (!response.isSuccessful) {
                    _error.value = "Gửi tin nhắn thất bại"
                }
                // Lưu ý: Chúng ta không tự add tin nhắn vào list ở đây, 
                // vì Backend sẽ emit tin nhắn đó qua Socket và ta nhận ở collect { ... } bên trên.
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Rời phòng và ngắt kết nối khi thoát màn hình chat
        _feedbackDetail.value?.id?.let {
            feedbackRepository.leaveRoom(it)
        }
        feedbackRepository.disconnectSocket()
    }
}
