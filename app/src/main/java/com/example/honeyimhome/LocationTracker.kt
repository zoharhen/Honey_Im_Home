package com.example.honeyimhome

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson


data class LocationInfo(val latitude: Double, val longitude: Double, val accuracy: Float? = null)

class LocationTracker(private val context: Context, var fusedLocationClient: FusedLocationProviderClient, sp : SharedPreferences) {

    var isTrackingOn: Boolean = false
    var currentLocation: LocationInfo? = null

    private fun sendBroadcast(message: String) {
        val intent = Intent()
        intent.action = message
        context.sendBroadcast(intent)
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        // record the location information in runtime (avoid location == null in the following cases:
        // 1. turn off the location and again turn on 2. user never turned on location before using the app (previousLocation == null)
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000

        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        sendBroadcast("started")
        isTrackingOn = true
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        isTrackingOn = false
        sendBroadcast("stopped")
    }

    val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location = locationResult.lastLocation
            currentLocation = LocationInfo(location.latitude, location.longitude, location.accuracy)
            sp.edit().putString("current", Gson().toJson(currentLocation)).apply() // save to SP
            sendBroadcast("new_location")
        }
    }

}