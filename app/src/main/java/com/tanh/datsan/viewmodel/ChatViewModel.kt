
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

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val feedbackDetail: FeedbackResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val isConnected: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Kết nối socket
        feedbackRepository.connectSocket()
        
        // Theo dõi tin nhắn real-time
        viewModelScope.launch {
            feedbackRepository.realTimeMessages.collect { newMessage ->
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + newMessage
                )
            }
        }

        // Lấy Profile ID để phân biệt tin nhắn (Backend dùng Profile ID cho responder.id)
        viewModelScope.launch {
            try {
                val response = userRepository.getUserProfile()
                val profileId = response.userProfile.id
                _uiState.value = _uiState.value.copy(currentUserId = profileId)
            } catch (e: Exception) {
                // Ignore profile fetch error for chat
            }
        }
    }

    fun loadChatHistory(feedbackId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = feedbackRepository.getFeedbackDetail(feedbackId)
                if (response.isSuccessful) {
                    val detail = response.body()
                    _uiState.value = _uiState.value.copy(
                        feedbackDetail = detail,
                        messages = detail?.responses ?: emptyList(),
                        isLoading = false
                    )
                    // Tham gia room sau khi có history
                    feedbackRepository.joinRoom(feedbackId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Không thể tải lịch sử trò chuyện",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi kết nối: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(feedbackId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val response = feedbackRepository.replyFeedback(feedbackId, content)
                if (!response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(error = "Gửi tin nhắn thất bại")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Lỗi: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.feedbackDetail?.id?.let {
            feedbackRepository.leaveRoom(it)
        }
        feedbackRepository.disconnectSocket()
    }
}
