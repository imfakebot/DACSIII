package com.tanh.datsan.data.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tanh.datsan.BuildConfig
import com.tanh.datsan.core.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    private var tokenManager : TokenManager? = null
    fun init(context: Context) {
        if (tokenManager == null) {
            tokenManager = TokenManager(context.applicationContext)
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = runBlocking { tokenManager?.getToken?.firstOrNull() }

        val newRequest = if(!token.isNullOrEmpty()){
            originalRequest.newBuilder()
                .header("Authorization","Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // Bắt in ra TẤT CẢ mọi thứ (Body, Header, URL)
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Thêm interceptor vào OkHttpClient
            .addInterceptor(authInterceptor) // Thêm interceptor vào OkHttpClient
            .build()
    }
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Sử dụng OkHttpClient đã cấu hình
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }
}