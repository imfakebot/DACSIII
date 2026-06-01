package com.tanh.datsan.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor() {

    // Dùng MutableStateFlow để giữ dữ liệu tạm thời trên RAM
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    private val _userPhone = MutableStateFlow<String?>(null)
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()

    private val _userAddress = MutableStateFlow<String?>(null)
    val userAddress: StateFlow<String?> = _userAddress.asStateFlow()

    private val _userGender = MutableStateFlow<String?>(null)
    val userGender: StateFlow<String?> = _userGender.asStateFlow()

    private val _userDob = MutableStateFlow<String?>(null)
    val userDob: StateFlow<String?> = _userDob.asStateFlow()

    private val _userBio = MutableStateFlow<String?>(null)
    val userBio: StateFlow<String?> = _userBio.asStateFlow()

    // Hàm này dùng để đổ dữ liệu từ API Backend vào sau khi call thành công
    fun setUserInfo(
        name: String?,
        avatarUrl: String?,
        phone: String? = null,
        address: String? = null,
        gender: String? = null,
        dob: String? = null,
        bio: String? = null
    ) {
        _userName.value = name
        _userAvatar.value = avatarUrl
        _userPhone.value = phone
        _userAddress.value = address
        _userGender.value = gender
        _userDob.value = dob
        _userBio.value = bio
    }

    // Xóa dữ liệu trên RAM khi người dùng Logout
    fun clearUserInfo() {
        _userName.value = null
        _userAvatar.value = null
        _userPhone.value = null
        _userAddress.value = null
        _userGender.value = null
        _userDob.value = null
        _userBio.value = null
    }
}