package com.ono.geofencingapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeofenceManager @Inject constructor(
    private val geofencingClient: GeofencingClient,
    @ApplicationContext private val context: Context
) {

    private val geofenceList = mutableListOf<Geofence>()

    suspend fun addGeofence(location: DtoLocation, previousLocation: DtoLocation? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            previousLocation?.let { removeGeofence(it.locationId) }

            val geofence = Geofence.Builder()
                .setRequestId(location.locationId)
                .setCircularRegion(location.latitude, location.longitude, 100f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val geofencePendingIntent = getGeofencePendingIntent()

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Location permission not granted")
            }

            val task = geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            val deferredResult = CompletableDeferred<Result<String>>()

            task.addOnSuccessListener {
                geofenceList.add(geofence)
                deferredResult.complete(Result.success("Geofence added for ${location.locationId}"))
            }.addOnFailureListener { e ->
                deferredResult.complete(Result.failure(e))
            }
            return@withContext deferredResult.await()

        } catch (e: SecurityException) {
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private suspend fun removeGeofence(geofenceId: String) = withContext(Dispatchers.IO) {
        try {
            /*val task: Task<Void> = geofencingClient.removeGeofences(listOf(geofenceId))
            val deferred = CompletableDeferred<Result<Void>>()
            task.addOnSuccessListener {
                deferred.complete(Result.success(it))
            }.addOnFailureListener { e ->
                deferred.complete(Result.failure(e))
            }
            deferred.await()*/

            geofencingClient.removeGeofences(listOf(geofenceId)).addOnSuccessListener {
                Log.d("GeofenceManager", "Successfully removed geofence $geofenceId: ")
            }.addOnFailureListener {
                Log.e("GeofenceManager", "Failed to remove geofence $geofenceId: ${it.message}")
            }

        } catch (e: Exception) {
            Log.e("GeofenceManager", "Failed to remove geofence $geofenceId: ${e.message}")
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // Use FLAG_IMMUTABLE
        )
    }

}
