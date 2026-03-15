package com.tanh.datsan // Giữ nguyên package của ông

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- 1. Khuôn mẫu nhận kết quả (Giữ nguyên vì JSON trả về giống hệt PHP) ---
data class LoginResponse(
    val status: String,
    val message: String,
    val account_id: String? = null
)

// --- 2. Khuôn mẫu gói dữ liệu gửi đi (MỚI: Dùng cho JSON Body) ---
// Tương đương với LoginDto bên NestJS
data class LoginRequest(
    val loginIdentifier: String, // Đổi tên cho khớp với DTO bên NestJS
    val password: String
)

// Tương đương với RegisterDto bên NestJS
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone_number: String,
    val gender: String,
    val password: String
)

// --- 3. Danh sách API ---
interface ApiService {
    // Đã bỏ @FormUrlEncoded, đổi đường dẫn và dùng @Body
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
}

// --- 4. Bộ máy kết nối mạng ---
object RetrofitClient {
    // THAY ĐỔI QUAN TRỌNG: Thêm cổn g :3000 củaNestJS
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Cái này sẽ tự động biến Data Class thành JSON
            .build()
            .create(ApiService::class.java)
    }
}