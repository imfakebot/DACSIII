package com.tanh.datsan.data.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tanh.datsan.BuildConfig

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    val apiService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}