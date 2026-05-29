package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class FieldType(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)

data class FieldImage(
    val id: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("isCover") val isCover: Boolean
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
    val id: String,
    val street: String,
    val latitude: Double?,
    val longitude: Double?,
    val city: City?,
    val ward: Ward?
)

data class Branch(
    val id: String,
    val name: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    val description: String?,
    val status: Boolean,
    @SerializedName("open_time") val openTime: String,
    @SerializedName("close_time") val closeTime: String,
    val address: Address?
)

// ==========================================
// 1. MODEL DÀNH CHO UI (HomeViewModel gọi cái này)
// ==========================================
data class FieldModel(
    val id: String,
    val name: String,
    val status: Boolean, // Cập nhật sang Boolean theo Git để đồng bộ hệ thống
    val address: String?,
    @SerializedName("averageRating") val rating: Float?,
    val imageUrl: String = "",
    val utilities: List<Utility>? = emptyList(),
    @SerializedName("fieldType") val fieldType: FieldType? = null,
    val distance: Double? // Thêm trường khoảng cách từ bản Git để hiển thị ở danh sách UI
)

// ==========================================
// 2. MODEL DÀNH CHO NETWORK (Hứng dữ liệu từ API)
// ==========================================
data class FieldResponse(
    val id: String,
    val name: String,
    val description: String?, // Cập nhật kiểu Nullable từ Git
    val status: Boolean, // QUAN TRỌNG: Backend đổi thành Boolean, phải theo Git để tránh crash
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("fieldType") val fieldType: FieldType,
    val branch: Branch,
    val images: List<FieldImage>?,
    val utilities: List<Utility>?,
    @SerializedName("averageRating") val averageRating: Float?,
    @SerializedName("reviewCount") val reviewCount: Int?,
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
    val data: List<Review>
)