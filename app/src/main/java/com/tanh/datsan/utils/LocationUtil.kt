package com.tanh.datsan.utils

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

object LocationUtil{
    fun checkRequestLocationSetting(
        context: Context,
        onEnabled: ()-> Unit,
        onDisabled:(IntentSenderRequest)->Unit
    ){
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(context)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                onEnabled()
            }
            .addOnFailureListener { exception ->
                if(exception is ResolvableApiException){
                    try {
                         val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        onDisabled(intentSenderRequest)
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
    }
}