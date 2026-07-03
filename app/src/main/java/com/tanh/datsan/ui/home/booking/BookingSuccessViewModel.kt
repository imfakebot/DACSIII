package com.tanh.datsan.ui.home.booking

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.repository.BookingRepository
import com.tanh.datsan.utils.DownloadHelper
import com.tanh.datsan.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSuccessViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<BookingReceiptUiState>(BookingReceiptUiState.Loading)
    val uiState: StateFlow<BookingReceiptUiState> = _uiState.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun fetchBookingReceipt(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = BookingReceiptUiState.Loading
            try {
                val receipt = bookingRepository.getBookingById(bookingId)
                _uiState.value = BookingReceiptUiState.Success(receipt)
            } catch (e: Exception) {
                _uiState.value = BookingReceiptUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun downloadTicket(context: Context, bookingId: String, bookingCode: String) {
        if (_isDownloading.value) return
        viewModelScope.launch {
            _isDownloading.value = true
            try {
                val response = bookingRepository.downloadTicket(bookingId)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val fileUri = DownloadHelper.saveTicketPdf(context, body, bookingCode)

                        if (fileUri != null) {
                            Toast.makeText(
                                context,
                                "Tải vé thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                            NotificationHelper.showDownloadCompleteNotification(
                                context,
                                fileUri,
                                bookingCode
                            )
                        } else {
                            Toast.makeText(context, "Không thể lưu vé vào máy!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } ?: run {
                        Toast.makeText(
                            context,
                            "Dữ liệu vé trống hoặc không hợp lệ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Lỗi tải vé từ server: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Lỗi kết nối mạng: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _isDownloading.value = false
            }
        }
    }
}