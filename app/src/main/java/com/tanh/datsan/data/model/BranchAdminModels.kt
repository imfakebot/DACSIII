package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// ---- Response Models ----

data class AvailableManagerDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("account") val account: AccountDto?
)

data class AccountDto(
    @SerializedName("email") val email: String
)

data class BranchManagerDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("account") val account: AccountDto?
)

data class BranchDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("open_time") val openTime: String,
    @SerializedName("close_time") val closeTime: String,
    @SerializedName("address") val address: Address?,
    @SerializedName("manager") val manager: BranchManagerDto?
)

// ---- Request Models ----

data class CreateBranchRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("description") val description: String?,
    @SerializedName("open_time") val openTime: String,
    @SerializedName("close_time") val closeTime: String,
    @SerializedName("street") val street: String,
    @SerializedName("cityId") val cityId: Int,
    @SerializedName("wardId") val wardId: Int,
    @SerializedName("manager_id") val managerId: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

typealias UpdateBranchRequest = CreateBranchRequest

data class CreateFieldRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("fieldTypeId") val fieldTypeId: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("utilityIds") val utilityIds: List<Int> = emptyList()
)

data class UpdateFieldRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("fieldTypeId") val fieldTypeId: String
)
