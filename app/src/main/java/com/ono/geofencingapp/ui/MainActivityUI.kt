package com.ono.geofencingapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ono.geofencingapp.MainActivityViewModel
import com.ono.geofencingapp.ui.theme.GeofencingAppTheme

@Composable
fun MainActivityUI(mainActivityViewModel: MainActivityViewModel = hiltViewModel()) {
    GeofencingAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Button(onClick = {
                    mainActivityViewModel.initiateGeoFencing(mainActivityViewModel.homeLocation)
                }) {
                    Text(text = "Home")
                }
                Button(onClick = {
                    mainActivityViewModel.initiateGeoFencing(mainActivityViewModel.workLocation)
                }) {
                    Text(text = "Work")
                }
                Button(onClick = {
                    mainActivityViewModel.initiateGeoFencing(mainActivityViewModel.partyLocation)
                }) {
                    Text(text = "Party")
                }
            }
        }
    }
}