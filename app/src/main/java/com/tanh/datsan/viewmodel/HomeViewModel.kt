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
    private val tokenManager: TokenManager,
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

    val userName: StateFlow<String?> = userRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userAvatarUrl: StateFlow<String?> = userRepository.userAvatarUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Luồng đếm số lượng thông báo chưa đọc từ Git
    val unreadNotification = notificationRepository.unreadCountFlow

    private var currentLat: String? = null
    private var currentLng: String? = null

    // Kiểm tra trạng thái đăng nhập dựa trên Token thực tế từ TokenManager
    val isLoggedIn: StateFlow<Boolean> = userRepository.isLoggedIn
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        fetchFieldNearMe()
        fetchFieldTypes()
        getUnreadCount()
    }

    // Tự động lắng nghe trạng thái Login để kéo thông tin Profile & Thông báo về
    fun getUnreadCount() {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    fetchUserProfileAfterLoggin()
                    try {
                        notificationRepository.fetchIntialUnreadCount()
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Lỗi lấy thông báo: ${e.message}")
                    }
                }
            }
        }
    }

    // Hàm lấy danh sách sân có tích hợp bộ lọc tìm kiếm nâng cao từ Git
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
                        status = jsonItem.status, // Ép kiểu Boolean đã đồng bộ từ bước trước
                        name = jsonItem.name,
                        address = jsonItem.branch.address?.street ?: "Địa chỉ k xác định",
                        rating = jsonItem.averageRating,
                        imageUrl = fixedUrl,
                        distance = jsonItem.distance,   // Thêm từ Git
                        fieldType = jsonItem.fieldType  // Thêm từ Git
                    )
                }
                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching fields: ${e.message}")
            }
        }
    }

    // Lấy vị trí hiện tại và lưu vào cache tọa độ cục bộ
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

    // Lấy danh sách các loại sân phục vụ cho thanh Tab Filter đầu trang
    private fun fetchFieldTypes() {
        viewModelScope.launch {
            try {
                _fieldTypes.value = fieldRepository.getAllFieldTypes()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching field types: ${e.message}")
            }
        }
    }

    // Xử lý khi người dùng ấn lọc theo loại sân (Sân 5, Sân 7...)
    fun onFieldTypeSelected(type: FieldType?) {
        _selectedType.value = type?.id
        fetchField(currentLat, currentLng, type?.id)
    }

    // Lấy thông tin cá nhân sau khi đăng nhập thành công
    private fun fetchUserProfileAfterLoggin() {
        viewModelScope.launch {
            try {
                val response = userRepository.getProfile()
                if (response.isSuccessful) {
                    val userMe = response.body()
                    userRepository.saveUserInfo(
                        avatarUrl = userMe?.userProfile?.avatarUrl,
                        userName = userMe?.userProfile?.fullName,
                        phone = userMe?.userProfile?.phoneNumber,
                        address = userMe?.userProfile?.address
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching user profile: ${e.message}")
            }
        }
    }
}