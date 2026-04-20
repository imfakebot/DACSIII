package com.tanh.datsan.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getCurrentLocation(
        onLocationFetched: (lat: String?, lon: String?) -> Unit
    ) {
        val fusedLocalClient = LocationServices.getFusedLocationProviderClient(context)

        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocationPermission || hasCoarseLocationPermission) {
            fusedLocalClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationFetched(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    } else {
                        onLocationFetched(null, null)
                    }
                }
                .addOnFailureListener {
                    onLocationFetched(null, null)
                }
        }else{
            onLocationFetched(null,null)
        }
    }
}