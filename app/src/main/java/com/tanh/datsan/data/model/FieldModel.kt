package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

// ==========================================
// 1. API RESPONSE WRAPPERS (Phân trang, Metadata)
// ==========================================
data class Metadata(
    val total: Int,
    val page: Int,
    val limit: Int,
    val lastPage: Int,
    val isSuggestion: Boolean,
    val suggestionMessage: String?
)

data class ApiFieldResponse<T>(
    val data: T,
    val metadata: Metadata
)

// ==========================================
// 2. MODEL DÀNH CHO NETWORK (Hứng dữ liệu từ API)
// ==========================================
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
    val id: String,
    val name: String,
    @SerializedName("iconUrl") val iconUrl: String?,
    val price: Double?, // Dùng Double theo Git để tránh crash Gson
    val type: String
)

data class City(
    val id: String,
    val name: String,
    val type: String?
)

data class Ward(
    val id: String,
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
    val ward: Ward?,
    @SerializedName("ward_name") val wardName: String?,
    @SerializedName("city_name") val cityName: String?
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

data class FieldResponse(
    val id: String,
    val name: String,
    val description: String?, // Cập nhật kiểu Nullable
    val status: Boolean,      // Trạng thái đã chuyển sang Boolean
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("fieldType") val fieldType: FieldType,
    val branch: Branch,
    val images: List<FieldImage>?,
    val utilities: List<Utility>?,
    @SerializedName("averageRating") val averageRating: Float?,
    @SerializedName("reviewCount") val reviewCount: Int?,
    val reviews : List<Review>? = null,
    val distance: Double?
)

// ==========================================
// 3. MODEL ĐÁNH GIÁ (REVIEW)
// ==========================================
data class ReviewUser(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("avatar_url") val avatarUrl : String?
)

data class Review(
    val id: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
    @SerializedName("userProfile") val user: ReviewUser? // Key Backend đổi thành userProfile
)

data class ReviewMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val lastPage: Int,
    val averageRating: Float?
)

data class ReviewPaginateResponse(
    val data: List<Review>,
    val meta : ReviewMeta
)

// ==========================================
// 4. MODEL DÀNH CHO UI (Hiển thị danh sách, RecyclerView/LazyColumn)
// ==========================================
data class FieldModel(
    val id: String,
    val name: String,
    val status: Boolean,
    val address: String?,
    @SerializedName("averageRating") val rating: Float?,
    val imageUrl: String = "",
    val utilities: List<Utility>? = emptyList(),
    @SerializedName("fieldType") val fieldType: FieldType? = null,
    val distance: Double?
)