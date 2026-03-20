package com.tanh.datsan.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val _fieldList = MutableStateFlow<List<FieldModel>>(emptyList())
    val fieldList: StateFlow<List<FieldModel>> = _fieldList

    val isLoggedIn : StateFlow<Boolean> = tokenManager.getToken.map{ token -> !token.isNullOrEmpty()}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    fun fetchField(lat: String? = null, lng: String? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllFields(lat, lng)

                val mappedList = response.map { jsonItem ->
                    val rawUrl = jsonItem.images?.firstOrNull()?.imageUrl?:"Hình ảnh không xác định"

                    val fixedUrl = rawUrl.replace("localhost", BuildConfig.API_HOST)
                    Log.d("TEST_LINK_ANH", "Link sau khi sửa: $fixedUrl")
                    FieldModel(
                        name = jsonItem.name,
                        address = jsonItem.branch?.address?.street ?: "Địa chỉ không xác định",
                        rating = 5.0,
                        imageUrl = fixedUrl
                    )
                }

                // Cập nhật giao diện
                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi gọi API: ${e.message}")
            }
        }
    }
}