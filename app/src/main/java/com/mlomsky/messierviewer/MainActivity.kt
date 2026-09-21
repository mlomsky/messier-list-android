package com.mlomsky.messierviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mlomsky.messierviewer.ui.LocationSearchDialog
import com.mlomsky.messierviewer.ui.MainScreen
import com.mlomsky.messierviewer.ui.theme.MessierViewerTheme
import com.mlomsky.messierviewer.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val currentTime by viewModel.currentTime.collectAsState()
            var showLocationDialog by remember { mutableStateOf(false) }
            var searchError by remember { mutableStateOf<String?>(null) }
            var isSearching by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            MessierViewerTheme(nightMode = uiState.nightMode) {
                MainScreen(
                    state = uiState,
                    currentTime = currentTime,
                    onSortSelected = { viewModel.setSortMode(it) },
                    onNightModeToggle = { viewModel.toggleNightMode() },
                    onSetLocationClick = { showLocationDialog = true }
                )

                if (showLocationDialog) {
                    LocationSearchDialog(
                        isSearching = isSearching,
                        errorMessage = searchError,
                        onSearch = { query ->
                            isSearching = true
                            searchError = null
                            coroutineScope.launch {
                                val result = viewModel.searchLocation(query)
                                isSearching = false
                                if (result != null) {
                                    viewModel.setCustomLocation(result)
                                    showLocationDialog = false
                                } else {
                                    searchError = "Location not found"
                                }
                            }
                        },
                        onUseGps = {
                            viewModel.useGpsLocation()
                            showLocationDialog = false
                        },
                        onDismiss = { showLocationDialog = false }
                    )
                }
            }
        }
    }
}
