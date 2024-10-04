package com.ono.geofencingapp

import android.Manifest
import android.content.IntentSender
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.ono.geofencingapp.ui.MainActivityUI
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val LOCATION_SETTINGS_REQUEST_CODE = 1000
        private const val REQUEST_CODE_LOCATION_PERMISSION = 102
    }


    private val mainActivityViewModel: MainActivityViewModel by viewModels<MainActivityViewModel>()
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var settingsClient: SettingsClient


    private val requestLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityUI(mainActivityViewModel)
        }


        requestPermissions()
        settingsClient = LocationServices.getSettingsClient(this)
        createLocationSettingsRequest()
        checkLocationSettings()

    }

    private fun createLocationSettingsRequest() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL) // Set granularity
            .build()

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }



    private fun checkLocationSettings() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener { locationSettingsResponse ->

            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    val intentSender = exception.resolution.intentSender
                    try {
                        intentSender?.let {
                            startIntentSenderForResult(it, LOCATION_SETTINGS_REQUEST_CODE, null, 0, 0, 0, null)
                        }
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.e("MainActivity", "checkLocationSettings: ", sendEx)
                    }
                } else {
                    val statusCode = (exception as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // Location settings are inadequate, and cannot be fixed here

                        }
                    }
                }
            }
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            mainActivityViewModel.initiateGeoFencing()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
//                , Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                , Manifest.permission.POST_NOTIFICATIONS

            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        mainActivityViewModel.initiateGeoFencing()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

}
