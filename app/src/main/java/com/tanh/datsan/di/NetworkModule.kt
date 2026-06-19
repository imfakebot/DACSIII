package com.tanh.datsan.di

import android.content.Context
import android.util.Log
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import com.tanh.datsan.data.network.AuthApiService
import com.tanh.datsan.data.network.BookingApiService
import com.tanh.datsan.data.network.BranchApiService
import com.tanh.datsan.data.network.FieldApiService
import com.tanh.datsan.data.network.NotificationApiService
import com.tanh.datsan.data.network.PricingApiService
import com.tanh.datsan.data.network.ReviewApiService
import com.tanh.datsan.data.network.UserApiService
import com.tanh.datsan.data.network.VoucherApiService
import com.tanh.datsan.utils.ResponseHelper.responseCount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import java.util.concurrent.TimeUnit


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
            val orignalRequest = chain.request()
            val token = tokenManager.cachedToken
            val requestBuilder = orignalRequest.newBuilder()

            val path = orignalRequest.url.encodedPath
            if (!path.contains("refresh") && !path.contains("login")) {
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
            }
            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideAuthenticator(
        tokenManager: TokenManager,
        authApiService: dagger.Lazy<AuthApiService>
    ): Authenticator {
        return Authenticator { _, response ->
            if (responseCount(response) > 2) {
                return@Authenticator null
            }

            synchronized(this) {
                val currentToken = tokenManager.cachedToken
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                // Nếu token đã bị xóa bởi 1 thread khác trước đó, hoặc request gốc không có token -> Bỏ qua
                if (currentToken.isNullOrEmpty() || failedToken.isNullOrEmpty()) {
                    return@Authenticator null
                }

                val tokenToUse = if (currentToken != failedToken) {
                    // Một thread khác ĐÃ refresh thành công, dùng luôn token mới
                    currentToken
                } else {
                    // Thực hiện gọi API Refresh thực sự
                    try {
                        val refreshResponse = authApiService.get().refreshToken().execute()
                        if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                            val newToken = refreshResponse.body()!!.accessToken
                            Log.d("NetworkModule", "Refresh token thành công: $newToken")
                            runBlocking { tokenManager.saveToken(newToken) }
                            newToken
                        } else {
                            // Refresh thất bại (401/403) -> Token chết hẳn
                            Log.e("NetworkModule", "Refresh Token đã hết hạn. Đang đăng xuất...")
                            runBlocking { tokenManager.clearToken() }
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("NetworkModule", "Lỗi kết nối khi refresh token: ${e.message}")
                        null
                    }
                }

                return@Authenticator tokenToUse?.let {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $it")
                        .build()
                }
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
    fun provideCookieJar(@ApplicationContext context: Context): CookieJar {
        return PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
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
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): com.tanh.datsan.data.network.LocationApiService =
        retrofit.create(com.tanh.datsan.data.network.LocationApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApiService(retrofit: Retrofit): com.tanh.datsan.data.network.FeedbackApiService =
        retrofit.create(com.tanh.datsan.data.network.FeedbackApiService::class.java)

    @Provides
    @Singleton
    fun provideStatisticsApiService(retrofit: Retrofit): com.tanh.datsan.data.network.StatisticsApiService =
        retrofit.create(com.tanh.datsan.data.network.StatisticsApiService::class.java)

    @Provides
    @Singleton
    fun provideBranchApiService(retrofit: Retrofit): BranchApiService =
        retrofit.create(BranchApiService::class.java)
}