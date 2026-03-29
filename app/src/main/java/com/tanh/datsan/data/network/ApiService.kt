package com.tanh.datsan

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- 1. Khuôn mẫu nhận kết quả ---
data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val account_id: String? = null,

    // Ép Android phải đọc đúng tên biến accessToken từ Backend trả về
    @SerializedName("accessToken")
    val accessToken: String? = null
)

// --- 2. Khuôn mẫu gói dữ liệu gửi đi ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone_number: String,
    val gender: String,
    val password: String
)

// MỚI: Dành cho bước 2 (Gửi mã OTP lên để hoàn tất)
data class OtpRequest(
    val email: String,
    val verificationCode: String // Tên biến khớp với Backend của bạn
)

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(
  val token: String,
    var newPassword:String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class ForgotRequest(
    val email: String
)

// --- 3. Danh sách API ---
interface ApiService {
    // BƯỚC 1: Gọi cửa (Initiate)
    @POST("auth/login/initiate")
    suspend fun loginInitiate(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register/initiate")
    suspend fun registerInitiate(@Body request: RegisterRequest): Response<LoginResponse>

    // BƯỚC 2: Nhập OTP (Complete)
    @POST("auth/login/complete")
    suspend fun loginComplete(@Body request: OtpRequest): Response<LoginResponse>

    // Lưu ý: Tùy Backend của bạn cấu hình là register/complete hay verify-email. Thường là register/complete
    @POST("auth/register/complete")
    suspend fun registerComplete(@Body request: OtpRequest): Response<LoginResponse>

    // Quên mật khẩu
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<LoginResponse>

    // API gửi Token Google lên Backend (Tên đường dẫn có thể đổi sau khi Backend viết xong)
    @POST("auth/google/mobile")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST("auth/forgot-password") // Đổi lại đường dẫn này cho khớp với Backend của bạn nếu cần
    suspend fun forgotPassword(@Body request: ForgotRequest): Response<Any>

}

// --- 4. Bộ máy kết nối mạng ---
object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}