package com.ono.geofencingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var entryTime: Long = 0
    private var exitTime: Long = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val geofencingEvent = GeofencingEvent.fromIntent(it)
            if (geofencingEvent?.hasError() == true) {
                return
            }

            val geofenceTransition = geofencingEvent?.geofenceTransition ?: return

            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    entryTime = System.currentTimeMillis()
                    Log.d("Geofencing", "Entered geofence at: $entryTime")
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    exitTime = System.currentTimeMillis()
                    val dwellTime = exitTime - entryTime

                    Log.d("Geofencing", "Exited geofence at: $exitTime")
                    Log.d("Geofencing", "Dwell time in milliseconds: $dwellTime")
                }

                else -> {

                }
            }
        } ?: Log.e("Geofencing", "Intent is null")
    }
}
