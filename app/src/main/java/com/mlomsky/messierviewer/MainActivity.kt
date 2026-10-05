package com.mlomsky.messierviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mlomsky.messierviewer.model.CatalogTarget
import com.mlomsky.messierviewer.ui.AboutDialog
import com.mlomsky.messierviewer.ui.DisplayFiltersDialog
import com.mlomsky.messierviewer.ui.FilterEditDialog
import com.mlomsky.messierviewer.ui.HelpDialog
import com.mlomsky.messierviewer.ui.LocationSearchDialog
import com.mlomsky.messierviewer.ui.MainScreen
import com.mlomsky.messierviewer.ui.SearchDialog
import com.mlomsky.messierviewer.ui.WelcomeScreen
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
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestLocationPermission.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val currentTime by viewModel.currentTime.collectAsState()
            var showLocationDialog by remember { mutableStateOf(false) }
            var showAboutDialog by remember { mutableStateOf(false) }
            var showHelpDialog by remember { mutableStateOf(false) }
            var showDisplayFiltersDialog by remember { mutableStateOf(false) }
            var showSearchDialog by remember { mutableStateOf(false) }
            var searchError by remember { mutableStateOf<String?>(null) }
            var isSearching by remember { mutableStateOf(false) }
            var showWelcome by remember { mutableStateOf(true) }
            var filterEditTarget by remember { mutableStateOf<CatalogTarget?>(null) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(uiState.isLoading) {
                if (!uiState.isLoading) showWelcome = false
            }

            MessierViewerTheme(nightMode = uiState.nightMode) {
                if (showWelcome) {
                    WelcomeScreen()
                } else {
                    MainScreen(
                        state = uiState,
                        currentTime = currentTime,
                        onSortSelected = { viewModel.setSortMode(it) },
                        onNightModeToggle = { viewModel.toggleNightMode() },
                        onSetLocationClick = { showLocationDialog = true },
                        onAboutClick = { showAboutDialog = true },
                        onHelpClick = { showHelpDialog = true },
                        onFilterClick = { filterEditTarget = it },
                        onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                        onCatalogToggle = { source, enabled -> viewModel.setCatalogEnabled(source, enabled) },
                        onDisplayFiltersClick = { showDisplayFiltersDialog = true },
                        onSearchClick = { showSearchDialog = true }
                    )

                    if (showAboutDialog) {
                        AboutDialog(onDismiss = { showAboutDialog = false })
                    }

                    if (showHelpDialog) {
                        HelpDialog(onDismiss = { showHelpDialog = false })
                    }

                    if (showDisplayFiltersDialog) {
                        DisplayFiltersDialog(
                            currentNgcMagnitudeLimit = uiState.ngcMagnitudeLimit,
                            currentHideBelowHorizon = uiState.hideBelowHorizon,
                            onSave = { limit, hideBelowHorizon ->
                                viewModel.setNgcMagnitudeLimit(limit)
                                viewModel.setHideBelowHorizon(hideBelowHorizon)
                                showDisplayFiltersDialog = false
                            },
                            onDismiss = { showDisplayFiltersDialog = false }
                        )
                    }

                    if (showSearchDialog) {
                        SearchDialog(
                            current = uiState.searchFilter,
                            onApply = { filter ->
                                viewModel.setSearchFilter(filter)
                                showSearchDialog = false
                            },
                            onDismiss = { showSearchDialog = false }
                        )
                    }

                    filterEditTarget?.let { target ->
                        FilterEditDialog(
                            objectName = target.displayName,
                            currentFilterText = uiState.filters[target.id].orEmpty(),
                            onSave = { text ->
                                viewModel.setObjectFilter(target.id, text)
                                filterEditTarget = null
                            },
                            onDismiss = { filterEditTarget = null }
                        )
                    }

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
}
