package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.data.repository.NotificationRepository
import com.tanh.datsan.data.repository.UserRepository
import com.tanh.datsan.utils.JwtUtil
import com.tanh.datsan.utils.LocationHelper
import com.tanh.datsan.utils.toFullImageUrl
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
    private val locationHelper: LocationHelper,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _fieldList = MutableStateFlow<List<FieldModel>>(emptyList())
    val fieldList: StateFlow<List<FieldModel>> = _fieldList

    private val _fieldTypes = MutableStateFlow<List<FieldType>>(emptyList())
    val fieldTypes: StateFlow<List<FieldType>> = _fieldTypes

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userAvatarUrl = MutableStateFlow<String?>(null)
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl

    val unreadNotification = notificationRepository.unreadCountFlow

    private var currentLat: String? = null
    private var currentLng: String? = null

    var isLoggedIn: StateFlow<Boolean> = tokenManager.token
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val userRole: StateFlow<String> = tokenManager.token
        .map{token->
            JwtUtil.getRoleFromToken(token)
        }
        .stateIn(
            scope=viewModelScope,
            started= SharingStarted.WhileSubscribed(5000),
            initialValue = "user"
        )

    init {
        fetchFieldNearMe()
        fetchFieldTypes()
        getUnreadCount()
    }

    fun getUnreadCount() {
        viewModelScope.launch {
            isLoggedIn.collect { isLoggedIn->
                if(isLoggedIn){
                    fetchUserProfileAfterLoggin()

                    try{
                        notificationRepository.fetchIntialUnreadCount()
                    } catch(e:Exception){
                        Log.e("HomeViewModel", "Lỗi lấy thông báo: ${e.message}")
                    }
                } else{
                    _userName.value=null
                    _userAvatarUrl.value=null
                }
            }
        }
    }

    fun fetchField(
        lat: String? = currentLat,
        lng: String? = currentLng,
        typeId: String? = _selectedType.value,
        name: String? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(
                    "HomeViewModel",
                    "Fetching fields with lat=$lat, lng=$lng, typeId=$typeId, name=$name"
                )
                val response =
                    fieldRepository.getAllField(lat = lat, lon = lng, typeId = typeId, name = name)
                val mappedList = response.map { jsonItem ->
                    val rawUrl = jsonItem.images?.firstOrNull()?.imageUrl
                    val fixedUrl = rawUrl.toFullImageUrl()
                    Log.d("HomeViewModel", "Link gốc: $rawUrl --- Link ĐÃ SỬA: $fixedUrl")
                    FieldModel(
                        id = jsonItem.id,
                        status = jsonItem.status,
                        name = jsonItem.name,
                        address = jsonItem.branch.address?.street ?: "Địa chỉ k xác định",
                        rating = jsonItem.averageRating,
                        imageUrl = fixedUrl,
                        distance = jsonItem.distance,
                        fieldType = jsonItem.fieldType
                    )
                }
                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching fields: ${e.message}")
            }
        }
    }

    fun fetchFieldNearMe() {
        locationHelper.getCurrentLocation { lat, lon ->
            currentLat = lat
            currentLng = lon
            if (lat != null && lon != null) {
                Log.d("HomeViewModel", "Current location: lat=$lat, lon=$lon")
                fetchField(lat, lon)
            } else {
                fetchField(lat, lon)
                Log.w("HomeViewModel", "Unable to get current location")
            }
        }
    }

    private fun fetchFieldTypes() {
        viewModelScope.launch {
            try {
                _fieldTypes.value = fieldRepository.getAllFieldTypes()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching field types: ${e.message}")
            }
        }
    }

    fun onFieldTypeSelected(type: FieldType?) {
        _selectedType.value = type?.id
        fetchField(currentLat, currentLng, type?.id)
    }

    private fun fetchUserProfileAfterLoggin() {
        viewModelScope.launch {
            try {
                val userProfile = userRepository.getProfileLogginedIn()
                _userName.value = userProfile.fullName
                _userAvatarUrl.value = userProfile.avatarUrl
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching user profile: ${e.message}")
            }
        }
    }
}