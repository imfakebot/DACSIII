package com.tanh.datsan.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.CreateFieldRequest
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldRequest
import com.tanh.datsan.data.repository.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AdminFieldUiState(
    val isLoading: Boolean = false,
    val fields: List<FieldResponse> = emptyList(),
    val fieldTypes: List<FieldType> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false,
    val branchId: String = "",
    val branchName: String = ""
)

@HiltViewModel
class AdminFieldViewModel @Inject constructor(
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFieldUiState())
    val uiState: StateFlow<AdminFieldUiState> = _uiState.asStateFlow()

    fun init(branchId: String, branchName: String) {
        _uiState.update { it.copy(branchId = branchId, branchName = branchName) }
        fetchFields(branchId)
        fetchFieldTypes()
    }

    fun fetchFields(branchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = branchRepository.getFieldsByBranch(branchId)
                _uiState.update { it.copy(isLoading = false, fields = response.data) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    private fun fetchFieldTypes() {
        viewModelScope.launch {
            try {
                val types = branchRepository.getAllFieldTypes()
                _uiState.update { it.copy(fieldTypes = types) }
            } catch (e: Exception) {
                // Silently ignore
            }
        }
    }

    fun createField(context: Context, request: CreateFieldRequest, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                // 1. Gọi API tạo thông tin sân
                val response = branchRepository.createField(request)
                if (response.isSuccessful && response.body() != null) {
                    val newFieldId = response.body()!!.id

                    // 2. Nếu người dùng có chọn ảnh, tiến hành up ảnh
                    if (imageUri != null) {
                        val multipartBody = uriToMultipart(context, imageUri, "file")
                        if (multipartBody != null) {
                            branchRepository.uploadFieldImage(newFieldId, multipartBody)
                        }
                    }

                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Tạo sân thành công!") }
                    // Load lại danh sách sân để hiển thị kèm ảnh mới
                    fetchFields(request.branchId)
                    onSuccess()
                } else {
                    // Lấy thông báo lỗi thực tế từ server trả về
                    val errorBody = response.errorBody()?.string() ?: "Lỗi không xác định"

                    // In ra Logcat màu đỏ với tag "API_ERROR"
                    android.util.Log.e("API_ERROR", "Lỗi tạo sân 400: $errorBody")

                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi dữ liệu, check Logcat!") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun updateField(context: Context, id: String, request: UpdateFieldRequest, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                // Cập nhật thông tin sân
                val response = branchRepository.updateField(id, request)
                if (response.isSuccessful) {

                    // Nếu người dùng đổi ảnh mới, tiến hành up ảnh
                    if (imageUri != null) {
                        val multipartBody = uriToMultipart(context, imageUri, "file")
                        if (multipartBody != null) {
                            branchRepository.uploadFieldImage(id, multipartBody)
                        }
                    }

                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Cập nhật sân thành công!") }
                    fetchFields(_uiState.value.branchId)
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = "Không thể cập nhật sân") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun deleteField(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            try {
                val response = branchRepository.deleteField(id)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            fields = it.fields.filter { f -> f.id != id },
                            successMessage = "Đã xóa sân!"
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Không thể xóa sân") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Lỗi kết nối: ${e.message}") }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    // --- HÀM CHUYỂN ĐỔI URI THÀNH MULTIPART ---
    private fun uriToMultipart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // Tạo file tạm thời để chứa ảnh
            val tempFile = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            // Ép kiểu ảnh và đưa vào MultipartBody
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}