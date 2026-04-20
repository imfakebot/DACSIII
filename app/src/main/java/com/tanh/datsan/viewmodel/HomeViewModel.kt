package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.utils.LocationHelper
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
    tokenManager: TokenManager,
    private val fieldRepository: FieldRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _fieldList = MutableStateFlow<List<FieldModel>>(emptyList())
    val fieldList: StateFlow<List<FieldModel>> = _fieldList

    var isLoggedIn: StateFlow<Boolean> = tokenManager.token
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun fetchField(lat: String? = null, lng: String? = null) {
        viewModelScope.launch {
            try {
                val response = fieldRepository.getAllField(lat, lng)
                val mappedList = response.map { jsonItem ->
                    val rawUrl = jsonItem.images?.firstOrNull()?.imageUrl ?: ""
                    val fixedUrl = rawUrl.replace(
                        "localhost",
                        BuildConfig.API_BASE_URL.removePrefix("http://").removeSuffix("/")
                    )

                    FieldModel(
                        id = jsonItem.id,
                        status = jsonItem.status,
                        name = jsonItem.name,
                        address = jsonItem.branch.address?.street ?: "Địa chỉ k xác định",
                        rating = jsonItem.averageRating,
                        imageUrl = fixedUrl
                    )
                }
                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching fields: ${e.message}")
            }
        }
    }

    fun fetchFieldNearMe(){
        locationHelper.getCurrentLocation { lat, lon ->
            if(lat!=null && lon!=null){
                Log.d("HomeViewModel", "Current location: lat=$lat, lon=$lon")
                fetchField(lat, lon)
            }else{
                Log.w("HomeViewModel", "Unable to get current location")
            }

            fetchField(lat, lon)
        }
    }
}