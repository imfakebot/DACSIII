package com.tanh.datsan.di

import android.util.Log
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dagger.Lazy
import com.tanh.datsan.core.GlobalEventBus

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val refreshMutex = Mutex()

    @Provides
    @Singleton
    fun provideLogginInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        authService: Lazy<AuthApiService>,
        globalEventBus: GlobalEventBus
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // 1. Lấy token một cách an toàn
            val token = runBlocking { tokenManager.getAccessToken() }

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrBlank()) {
                Log.d("NETWORK_DEBUG", "Adding Token to Request: ${originalRequest.url}")
                requestBuilder.header("Authorization", "Bearer $token")
            } else {
                Log.w("NETWORK_DEBUG", "NO TOKEN FOUND for Request: ${originalRequest.url}")
            }

            var response = chain.proceed(requestBuilder.build())
            
            // 2. Xử lý lỗi 401
            if (response.code == 401 && !originalRequest.url.toString().contains("auth/refresh")) {
                Log.e("NETWORK_DEBUG", "401 detected at: ${originalRequest.url}")
                
                val newAccessToken = runBlocking {
                    refreshMutex.withLock {
                        // Kiểm tra xem đã có ai refresh thành công chưa
                        val currentToken = tokenManager.getAccessToken()
                        val requestToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
                        
                        if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                            Log.d("NETWORK_DEBUG", "Token refreshed by another thread.")
                            return@withLock currentToken
                        }

                        Log.d("NETWORK_DEBUG", "Initiating Token Refresh...")
                        val refreshToken = tokenManager.getRefreshTokenSync()

                        if (refreshToken.isNullOrBlank()) {
                            Log.e("NETWORK_DEBUG", "Refresh Token is missing.")
                            return@withLock null
                        }

                        try {
                            val refreshResponse = authService.get().refreshToken(RefreshTokenRequest(refreshToken))
                            if (refreshResponse.isSuccessful) {
                                val body = refreshResponse.body()
                                val newAccess = body?.accessToken
                                val newRefresh = body?.refreshToken ?: refreshToken
                                
                                if (!newAccess.isNullOrBlank()) {
                                    Log.d("NETWORK_DEBUG", "Refresh Success! New Token: ${newAccess.take(8)}...")
                                    tokenManager.saveTokens(newAccess, newRefresh)
                                    return@withLock newAccess
                                }
                            } else {
                                Log.e("NETWORK_DEBUG", "Refresh API failed: ${refreshResponse.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("NETWORK_DEBUG", "Refresh Exception: ${e.message}")
                        }
                        null
                    }
                }

                if (!newAccessToken.isNullOrBlank()) {
                    // Tránh vòng lặp vô tận: Nếu token mới giống hệt token cũ vừa bị 401
                    val oldToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
                    if (newAccessToken == oldToken) {
                        Log.e("NETWORK_DEBUG", "New token is identical to failed token. Stopping loop.")
                        runBlocking {
                            tokenManager.clearTokens()
                            globalEventBus.emit(com.tanh.datsan.core.GlobalEvent.Logout)
                        }
                        return@Interceptor response
                    }

                    response.close()
                    Log.d("NETWORK_DEBUG", "Retrying request with new token...")
                    return@Interceptor chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    )
                } else {
                    Log.e("NETWORK_DEBUG", "Refresh failed. Forcing Logout.")
                    runBlocking {
                        tokenManager.clearTokens()
                        globalEventBus.emit(com.tanh.datsan.core.GlobalEvent.Logout)
                    }
                }
            }
            
            response
        }
    }

    @Provides
    @Singleton
    fun provideAuthenticator(): Authenticator {
        return Authenticator { _, response -> null }
    }

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) { cookieStore[url.host] = cookies }
            override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host] ?: listOf()
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
    fun provideAuthService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideFieldService(retrofit: Retrofit): FieldApiService = retrofit.create(FieldApiService::class.java)

    @Provides
    @Singleton
    fun provideBookingService(retrofit: Retrofit): BookingApiService = retrofit.create(BookingApiService::class.java)

    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): ReviewApiService = retrofit.create(ReviewApiService::class.java)

    @Provides
    @Singleton
    fun provideVoucherApi(retrofit: Retrofit): VoucherApiService = retrofit.create(VoucherApiService::class.java)

    @Provides
    @Singleton
    fun providePricingApi(retrofit: Retrofit): PricingApiService = retrofit.create(PricingApiService::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserApiService = retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): FeedbackApiService = retrofit.create(FeedbackApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService = retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): LocationApiService = retrofit.create(LocationApiService::class.java)

    @Provides
    @Singleton
    fun provideAdminAnalyticsApi(retrofit: Retrofit): AdminAnalyticsApi = retrofit.create(AdminAnalyticsApi::class.java)
}
