package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class FieldType(
    val id: String, val name: String, val description: String?
)

data class FieldImage(
    val id: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("isCover") val isCover: Boolean = false
)

data class Utility(
    val id: Int,
    val name: String,
    @SerializedName("iconUrl") val iconUrl: String?,
    val price: Int?,
    val type: String
)

data class City(
    val id: Int,
    val name: String,
    val type: String?
)

data class Ward(
    val id: Int,
    val name: String,
    val type: String?,
    val city: City?
)

data class Address(
    val id: String? = null,
    val street: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: City? = null,
    val ward: Ward? = null
)

data class Branch(
    val id: String,
    val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val description: String,
    val status: Boolean,
    @SerializedName("open_time") val openTime: String = "00:00",
    @SerializedName("close_time") val closeTime: String = "23:59",
    val address: Address
)

// ==========================================
// 1. MODEL DÀNH CHO UI (HomeViewModel gọi cái này)
// ==========================================
data class FieldModel(
    val id: String,
    val name: String,
    val status: String,
    val address: String?,
    @SerializedName("averageRating") val rating: Float?,
    val imageUrl: String = "",
    val utilities: List<Utility>? = emptyList(),
    @SerializedName("fieldType") val fieldType: FieldType? = null
)

// ==========================================
// 2. MODEL DÀNH CHO NETWORK (Hứng dữ liệu từ API)
// ==========================================
data class FieldResponse(
    val id: String,//val id: String? = null
    val name: String, //val name: String? = null
    val description: String, //val description: String? = null
    val status: String, // Giữ kiểu String để linh hoạt xử lý
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("fieldType") val fieldType: FieldType, // Bổ sung cho DetailScreen
    val branch: Branch,
    val images: List<FieldImage>?,
    val utilities: List<Utility>?,
    @SerializedName("averageRating") val averageRating: Float?,
    @SerializedName("reviewCount") val reviewCount: Int?, // Chuyển thành Int vì reviewCount thường là số nguyên
    val reviews: List<Review>? = null,
    val distance: Double?
)

data class Review(
    val id: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
    @SerializedName("user") val user: ReviewUser?
)

data class ReviewUser(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)

data class ReviewPaginateResponse(
    val data: List<Review>? = null
)