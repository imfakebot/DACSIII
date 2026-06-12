package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.WardDto
import retrofit2.http.GET
import retrofit2.http.Path

interface LocationApiService {
    @GET("locations/cities")
    suspend fun getCities(): List<CityDto>

    @GET("locations/wards/{cityId}")
    suspend fun getWards(@Path("cityId") cityId: String): List<WardDto>
}
