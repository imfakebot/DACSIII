package com.tanh.datsan.di

import android.util.Log
import com.tanh.datsan.BuildConfig

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.AuthApiService
import com.tanh.datsan.data.network.RefreshTokenRequest
import com.tanh.datsan.data.network.BookingApiService
import com.tanh.datsan.data.network.FeedbackApiService
import com.tanh.datsan.data.network.FieldApiService
import com.tanh.datsan.data.network.LocationApiService
import com.tanh.datsan.data.network.NotificationApiService
import com.tanh.datsan.data.network.PricingApiService
import com.tanh.datsan.data.network.ReviewApiService
import com.tanh.datsan.data.network.UserApiService
import com.tanh.datsan.data.network.VoucherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val refreshMutex = Mutex()

    @Provides
    @Singleton
    fun provideLogginInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        authService: dagger.Lazy<AuthApiService>,
        globalEventBus: com.tanh.datsan.core.GlobalEventBus
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // 1. Lấy token hiện tại
            var token = tokenManager.cachedToken ?: runBlocking {
                tokenManager.token.firstOrNull()
            }

            // Xử lý các trường hợp token rác
            val isTokenValid = !token.isNullOrBlank() && 
                               token != "null" && 
                               token != "undefined" && 
                               token.length > 10

            val requestBuilder = originalRequest.newBuilder()
            if (isTokenValid) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            var response = chain.proceed(requestBuilder.build())
            
            // 2. Nếu bị 401, thử Refresh Token ngay trong Interceptor
            if (response.code == 401 && !originalRequest.url.toString().contains("auth/refresh")) {
                Log.e("NETWORK_DEBUG", "Phát hiện 401 tại: ${originalRequest.url}")
                
                val newAccessToken = runBlocking {
                    refreshMutex.withLock {
                        // Kiểm tra lại xem token đã được refresh bởi thread khác chưa
                        val currentToken = tokenManager.cachedToken ?: tokenManager.token.firstOrNull()
                        val requestToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
                        
                        if (currentToken != requestToken && !currentToken.isNullOrBlank()) {
                            Log.d("NETWORK_DEBUG", "Token đã được thread khác refresh, dùng token mới.")
                            return@withLock currentToken
                        }

                        Log.d("NETWORK_DEBUG", "Đang tiến hành gọi API Refresh...")
                        val refreshToken = tokenManager.cachedRefreshToken ?: tokenManager.getRefreshToken.firstOrNull()

                        if (refreshToken.isNullOrBlank()) {
                            Log.e("NETWORK_DEBUG", "Refresh Token trống, không thể refresh.")
                            return@withLock null
                        }

                        try {
                            val refreshResponse = authService.get().refreshToken(RefreshTokenRequest(refreshToken))
                            if (refreshResponse.isSuccessful) {
                                val body = refreshResponse.body()
                                val newAccess = body?.accessToken
                                val newRefresh = body?.refreshToken ?: refreshToken
                                
                                if (!newAccess.isNullOrBlank()) {
                                    Log.d("NETWORK_DEBUG", "Refresh thành công!")
                                    tokenManager.saveTokens(newAccess, newRefresh)
                                    return@withLock newAccess
                                }
                            } else {
                                Log.e("NETWORK_DEBUG", "API Refresh trả về lỗi: ${refreshResponse.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("NETWORK_DEBUG", "Lỗi exception khi refresh: ${e.message}")
                        }
                        null
                    }
                }

                if (!newAccessToken.isNullOrBlank()) {
                    response.close()
                    return@Interceptor chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    )
                } else {
                    Log.e("NETWORK_DEBUG", "Refresh thất bại hoàn toàn, yêu cầu đăng nhập lại.")
                    // Nếu refresh thất bại hoàn toàn, xóa token và báo sự kiện logout toàn cục
                    runBlocking {
                        tokenManager.clearTokens()
                        globalEventBus.emit(com.tanh.datsan.core.GlobalEvent.Logout)
                    }
                }
            }
            
            response
        }
    }

    // Authenticator vẫn giữ lại như một lớp bảo vệ thứ 2
    @Provides
    @Singleton
    fun provideAuthenticator(): Authenticator {
        return Authenticator { _, response ->
            // Authenticator thường chỉ chạy nếu Server trả về WWW-Authenticate header
            Log.w("NETWORK_AUTH", "Authenticator triggered for: ${response.request.url}")
            null
        }
    }

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return object : CookieJar {
            // Sử dụng một HashMap đơn giản để lưu trữ Cookie trong phiên làm việc
            private val cookieStore = mutableMapOf<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                Log.d("NETWORK_COOKIE", "Lưu ${cookies.size} cookies từ ${url.host}")
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host] ?: listOf()
                if (cookies.isNotEmpty()) {
                    Log.d("NETWORK_COOKIE", "Gửi ${cookies.size} cookies tới ${url.host}")
                }
                return cookies
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authenticator: Authenticator,
        authInterceptor: Interceptor,
        cookieJar: CookieJar
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideFieldService(retrofit: Retrofit): FieldApiService =
        retrofit.create(FieldApiService::class.java)

    @Provides
    @Singleton
    fun provideBookingService(retrofit: Retrofit): BookingApiService =
        retrofit.create(BookingApiService::class.java)

    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): ReviewApiService =
        retrofit.create(ReviewApiService::class.java)

    @Provides
    @Singleton
    fun provideVoucherApi(retrofit: Retrofit): VoucherApiService =
        retrofit.create(VoucherApiService::class.java)

    @Provides
    @Singleton
    fun providePricingApi(retrofit: Retrofit): PricingApiService =
        retrofit.create(PricingApiService::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApiService =
        retrofit.create(FeedbackApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): LocationApiService =
        retrofit.create(LocationApiService::class.java)
}
