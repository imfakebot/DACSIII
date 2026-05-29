package com.tanh.datsan.di

import android.util.Log
import com.tanh.datsan.BuildConfig

import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.AuthApiService         // Giữ từ Local
import com.tanh.datsan.data.network.BookingApiService
import com.tanh.datsan.data.network.FeedbackApiService     // Giữ từ Local
import com.tanh.datsan.data.network.FieldApiService
import com.tanh.datsan.data.network.NotificationApiService  // Thêm từ Git
import com.tanh.datsan.data.network.PricingApiService       // Thêm từ Git
import com.tanh.datsan.data.network.ReviewApiService
import com.tanh.datsan.data.network.UserApiService
import com.tanh.datsan.data.network.VoucherApiService       // Thêm từ Git
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton                             // Sử dụng cấu trúc thư viện mới từ Git
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // Thử lấy token từ cache trước
            var token = tokenManager.cachedToken
            
            // Nếu cache trống (có thể do app mới restart), lấy trực tiếp từ DataStore
            if (token.isNullOrEmpty()) {
                token = runBlocking {
                    tokenManager.token.firstOrNull()
                }
                Log.d("NETWORK_DEBUG", "Token retrieved from DataStore: ${token?.take(10)}...")
            }

            Log.d("NETWORK_DEBUG", "--- Request: ${originalRequest.method} ${originalRequest.url} ---")
            val tokenInfo = if (token.isNullOrEmpty()) "EMPTY" else "${token.take(5)}...${token.takeLast(5)}"
            Log.d("NETWORK_DEBUG", "Final Token used: $tokenInfo")

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty() && token != "null" && token != "undefined") {
                Log.d("NETWORK_DEBUG", "Adding Authorization Header: Bearer ${token.take(10)}...")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                Log.w("NETWORK_DEBUG", "Token is empty or invalid, skip adding Authorization header")
            }

            val response = chain.proceed(requestBuilder.build())
//...
            
            if (response.code == 401) {
                Log.e("NETWORK_DEBUG", "401 Unauthorized response for: ${originalRequest.url}")
            }
            
            response
        }
    }

    @Provides
    @Singleton
    fun provideAuthenticator(): Authenticator {
        return Authenticator { route, response ->
            Log.e(
                "NETWORK_AUTH",
                "Phát hiện lỗi 401 - Token hết hạn hoặc không hợp lệ! Url: ${response.request.url}"
            )
            // Nếu muốn xử lý Refresh Token thì code ở đây
            null
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authenticator: Authenticator,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
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
}