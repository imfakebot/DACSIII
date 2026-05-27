
package com.tanh.datsan.di

import android.util.Log
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.AuthApiService
import com.tanh.datsan.data.network.BookingApiService
import com.tanh.datsan.data.network.FieldApiService
import com.tanh.datsan.data.network.ReviewApiService
import com.tanh.datsan.data.network.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

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

            // Dùng runBlocking để lấy token mới nhất từ DataStore
            // Điều này đảm bảo token không bao giờ bị null do quá trình khởi tạo bất đồng bộ
            val token = runBlocking {
                tokenManager.getAccessToken.first()
            }

            Log.d("NETWORK_DEBUG", "Sending Token: $token")

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideAuthenticator(): Authenticator {
        return Authenticator { _, response ->
            Log.e(
                "NETWORK_AUTH",
                "Phát hiện lỗi 401 - Token hết hạn! Cần xử lý Refresh Token hoặc Logout."
            )
            //TODO: gọi Refresh Token API ở đây, nếu thành công trả về request mới với token mới, nếu thất bại trả về null
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
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    // MỚI: Cung cấp AuthApiService để Hilt có thể tiêm vào AuthRepository
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
    fun provideUserApi(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)
}