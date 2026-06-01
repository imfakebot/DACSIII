package com.tanh.datsan.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val locationApiService: com.tanh.datsan.data.network.LocationApiService
){
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCities() = locationApiService.getCities()

    suspend fun getWards(cityId: Int) = locationApiService.getWards(cityId)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<String,String>?{
        return try{
            val location = fusedLocationClient.lastLocation.await()
            if (location != null){
                Pair(location.latitude.toString(),location.longitude.toString())
            } else null
        } catch(e: Exception){
           null
        }
    }
}