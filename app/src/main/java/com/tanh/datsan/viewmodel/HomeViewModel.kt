package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.repository.FieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _fieldList = MutableStateFlow<List<FieldModel>>(emptyList())
    val fieldList: StateFlow<List<FieldModel>> = _fieldList

    val isLoggedIn: StateFlow<Boolean> = tokenManager.getAccessToken
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun fetchFieldNearMe(lat: String? = null, lng: String? = null) {
        viewModelScope.launch {
            try {
                // Sửa thành getAllField (không có s) cho khớp FieldRepository
                val response = fieldRepository.getAllField(lat, lng)

                val mappedList = response.map { jsonItem ->
                    val rawUrl = jsonItem.images?.firstOrNull()?.imageUrl ?: ""

                    // Dùng API_BASE_URL từ BuildConfig, cắt bỏ http:// và / cuối nếu cần (tùy config của bạn)
                    val host = BuildConfig.API_BASE_URL.removePrefix("http://").removeSuffix("/")
                    val fixedUrl = rawUrl.replace("localhost", host)

                    FieldModel(
                        id = jsonItem.id ?: "",
                        status = jsonItem.status ?: "",
                        name = jsonItem.name ?: "Chưa có tên",
                        address = jsonItem.branch?.address?.street ?: "Địa chỉ không xác định",
                        rating = jsonItem.averageRating ?: 0.0,
                        imageUrl = fixedUrl
                    )
                }

                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi gọi API: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
        }
    }
}