package com.tanh.datsan.viewmodel

import com.tanh.datsan.data.model.AccountResponse
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.WardDto

data class UserStats(
    val bookingCount: Int = 0,
    val voucherCount: Int = 0,
    val points: Int = 0
)

data class ProfileUiState(
    val profile: AccountResponse? = null,
    val stats: UserStats = UserStats(),
    val isLoading: Boolean = false,
    val isLoadingCities: Boolean = false,
    val isLoadingWards: Boolean = false,
    val isEditing: Boolean = false,
    val toastMessage: String? = null,
    val avatarUrl: String? = null,
    val avatarUpdateTimestamp: Long = 0L,
    val email: String = "",
    val cities: List<CityDto> = emptyList(),
    val wards: List<WardDto> = emptyList(),
    // Read-only fields for display
    val displayCityName: String = "",
    val displayWardName: String = "",

    // Editable fields
    val fullName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val bio: String = "",
    val street: String = "",
    val selectedCityId: Int? = null,
    val selectedWardId: Int? = null
)
