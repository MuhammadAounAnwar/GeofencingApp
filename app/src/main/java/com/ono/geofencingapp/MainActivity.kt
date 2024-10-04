package com.ono.geofencingapp

import android.Manifest
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.google.android.gms.location.LocationServices
import com.ono.geofencingapp.ui.MainActivityUI
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {

    private val REQUEST_CODE_LOCATION_PERMISSION = 102
    private val mainActivityViewModel: MainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityUI(mainActivityViewModel)
        }
        requestPermissions()



    }

    private fun getLocationCords(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this  )

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d("CurrentLocation", "Lat: ${location.latitude}, Lng: ${location.longitude}")
            } else {
                Log.d("CurrentLocation", "Location is null")
            }
        }
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            mainActivityViewModel.initiateGeoFencing()
            getLocationCords()
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
        getLocationCords()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

}
