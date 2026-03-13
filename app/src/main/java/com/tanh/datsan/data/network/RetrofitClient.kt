package com.tanh.datsan.data.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tanh.datsan.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Bắt  in ra TẤT CẢ mọi thứ (Body, Header, URL)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Thêm interceptor vào OkHttpClient
        .build()
    val apiService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Sử dụng OkHttpClient đã cấu hình
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}