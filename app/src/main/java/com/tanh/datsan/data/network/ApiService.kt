package com.tanh.datsan // Nhớ sửa đúng tên package của bạn

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// --- 1. Khuôn mẫu để nhận dữ liệu từ PHP trả về ---
data class LoginResponse(
    val status: String,
    val message: String,
    val account_id: String? = null
)

// --- 2. Danh sách các "món ăn" (API) có thể gọi ---
interface ApiService {
    @FormUrlEncoded
    @POST("datsan_api/login.php")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("datsan_api/register.php")
    suspend fun register(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone_number") phone: String, // Khớp với $_POST['phone_number']
        @Field("gender") gender: String,      // Khớp với $_POST['gender']
        @Field("password") password: String
    ): Response<LoginResponse>
}

// --- 3. Bộ máy kết nối mạng ---
object RetrofitClient {
    // 10.0.2.2 là địa chỉ "đặc biệt" để máy ảo Android hiểu là "máy tính đang chạy XAMPP"
    private const val BASE_URL = "http://10.0.2.2/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}