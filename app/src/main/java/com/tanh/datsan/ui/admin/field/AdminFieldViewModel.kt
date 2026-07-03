package com.tanh.datsan.ui.admin.field

import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CreateFieldDto
import com.tanh.datsan.data.model.FieldResponse
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.model.UpdateFieldDto
import com.tanh.datsan.data.repository.BranchRepository
import com.tanh.datsan.data.repository.FieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject
@HiltViewModel
class AdminFieldViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _fields = MutableStateFlow<List<FieldResponse>>(emptyList())
    val fields: StateFlow<List<FieldResponse>> = _fields.asStateFlow()

    private val _fieldTypes = MutableStateFlow<List<FieldType>>(emptyList())
    val fieldTypes: StateFlow<List<FieldType>> = _fieldTypes.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _utilities = MutableStateFlow<List<com.tanh.datsan.data.model.Utility>>(emptyList())
    val utilities: StateFlow<List<com.tanh.datsan.data.model.Utility>> = _utilities.asStateFlow()

    private val _uiState = MutableStateFlow< AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _selectedField = MutableStateFlow<FieldResponse?>(null)
    val selectedField: StateFlow<FieldResponse?> = _selectedField.asStateFlow()

    // Pagination State
    private var currentPage = 1
    private val limit = 10
    var isLastPage = false
        private set
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val types = fieldRepository.getAllFieldTypes()
                _fieldTypes.value = types

                val branches = branchRepository.getBranches()
                _branches.value = branches

                val utils = fieldRepository.getAllUtilities()
                _utilities.value = utils

                fetchFields()
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi tải dữ liệu")
            }
        }
    }

    fun fetchFields(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            isLastPage = false
            _fields.value = emptyList()
        }
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = fieldRepository.getAllField(
                    lat = null, lon = null, page = currentPage, limit = limit
                )
                _fields.value = response.data
                isLastPage = response.data.size < limit
                _uiState.value = AdminUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi tải danh sách sân")
            }
        }
    }

    fun loadMoreFields() {
        if (isLastPage || _isLoadingMore.value || _uiState.value == AdminUiState.Loading) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            try {
                val response = fieldRepository.getAllField(
                    lat = null, lon = null, page = currentPage, limit = limit
                )
                if (response.data.isNotEmpty()) {
                    val currentList = _fields.value.toMutableList()
                    currentList.addAll(response.data)
                    _fields.value = currentList
                }
                isLastPage = response.data.size < limit
            } catch (e: Exception) {
               Log.d("AdminFieldViewModel", "Error loading more fields: ${e.message}")
                currentPage--
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun fetchFieldsSilently() {
        viewModelScope.launch {
            try {
                // Silently refetch the first page (or all items up to currentPage)
                // For simplicity, we just refetch everything up to the current page length
                val totalToFetch = currentPage * limit
                val response = fieldRepository.getAllField(
                    lat = null, lon = null, page = 1, limit = totalToFetch
                )
                _fields.value = response.data
            } catch (e: Exception) {
                // Do not update UI state on silent fetch failure
            }
        }
    }

    fun getFieldById(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val field = fieldRepository.getFieldDetail(id, null, null)
                _selectedField.value = field
                _uiState.value = AdminUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi tải chi tiết sân")
            }
        }
    }

    fun createField(dto: CreateFieldDto) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val response = fieldRepository.createField(dto)
                fetchFieldsSilently()
                _uiState.value = AdminUiState.Success("Tạo sân thành công", response.id)
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi tạo sân")
            }
        }
    }

    fun updateField(id: String, dto: UpdateFieldDto) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                fieldRepository.updateField(id, dto)
                fetchFieldsSilently()
                _uiState.value = AdminUiState.Success("Cập nhật sân thành công")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi cập nhật sân")
            }
        }
    }

    fun deleteField(id: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                fieldRepository.deleteField(id)
                fetchFieldsSilently()
                _uiState.value = AdminUiState.Success("Xóa sân thành công")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi xóa sân")
            }
        }
    }

    fun uploadImages(id: String, images: List<MultipartBody.Part>) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                fieldRepository.uploadImages(id, images)
                _uiState.value = AdminUiState.Success("Tải ảnh lên thành công")
            } catch (e: Exception) {
                android.util.Log.e("AdminFieldViewModel", "Lỗi khi tải ảnh lên", e)
                _uiState.value = AdminUiState.Error(e.message ?: "Lỗi khi tải ảnh lên")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AdminUiState.Idle
    }

    fun clearSelectedField() {
        _selectedField.value = null
    }
}
