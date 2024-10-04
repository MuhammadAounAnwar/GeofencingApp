package com.ono.geofencingapp

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context

) : ViewModel() {

    val homeLocation = DtoLocation(31.5925, 74.3095, "lahore")
    val workLocation = DtoLocation(33.7296, 73.0368, "islamabad")
    val partyLocation = DtoLocation(48.8584, 2.2945, "party")

    var prevLocation: DtoLocation? = null

    fun initiateGeoFencing(dtoLocation: DtoLocation = homeLocation) {
        viewModelScope.launch {
            val result = geofenceManager.addGeofence(dtoLocation, prevLocation)
            handleGeofenceResult(result)
            prevLocation = dtoLocation
        }
    }
    private fun handleGeofenceResult(result: Result<String>) {
        result.fold(
            onSuccess = { message -> Log.d("Geofencing", message) },
            onFailure = { error -> Log.e("Geofencing", "Failed to add geofence: ${error.message}") }
        )
    }
}

fun Long.toMinutesSeconds(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Long.toDateTimeString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}