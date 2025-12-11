package com.example.simplibus.ui.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.simplibus.ui.navigation.Screen
import com.example.simplibus.ui.theme.SimpliBusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    navController: NavHostController,
    viewModel: DriverViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking

    val logoutSuccess by viewModel.logoutSuccess.collectAsStateWithLifecycle()
    var showLocationDisabledAlert by remember { mutableStateOf(false) }

    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            navController.navigate("${Screen.RoleSelection.route}?showImmediately=true") {
                popUpTo(Screen.RoleSelection.route) { inclusive = true }
            }
            viewModel.resetLogoutState()
        }
    }
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (isLocationEnabled()) {
                    viewModel.startTracking(context, uiState.selectedBusId)
                } else {
                    showLocationDisabledAlert = true
                }
            }
        }
    )
    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } else {
                        viewModel.startTracking(context, uiState.selectedBusId)
                    }
                }
            }
        }
    )
    if (showLocationDisabledAlert) {
        AlertDialog(
            onDismissRequest = { showLocationDisabledAlert = false },
            icon = { Icon(Icons.Filled.LocationOff, contentDescription = null) },
            title = { Text("Location Disabled") },
            text = { Text("To track the bus, please enable GPS location services in your settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationDisabledAlert = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDisabledAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Mode") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        DriverScreenUI(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            isTracking = isTracking,
            isLoading = uiState.isLoading,
            availableBuses = uiState.availableBuses,
            selectedBusId = uiState.selectedBusId,
            currentLocation = uiState.currentLocation,
            onBusSelected = viewModel::onBusSelected,
            onStartTracking = {
                if (uiState.selectedBusId.isNotBlank()) {
                    locationPermissionsLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            onStopTracking = {
                viewModel.stopTracking(context)
            },
            onLogout = {
                viewModel.logout()
                navController.popBackStack("animatedRoleScreen", inclusive = false)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreenUI(
    modifier: Modifier = Modifier,
    isTracking: Boolean,
    isLoading: Boolean,
    availableBuses: List<String>,
    selectedBusId: String,
    currentLocation: String,
    onBusSelected: (String) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Bus ID: ${selectedBusId.ifBlank { "N/A" }}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isTracking) "Live Tracking ON" else "Live Tracking OFF",
            style = MaterialTheme.typography.titleMedium,
            color = if (isTracking) Color(0xFF4CAF50) else Color.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            var isExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = !isExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBusId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Bus") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                    enabled = !isTracking
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    availableBuses.forEach { busId ->
                        DropdownMenuItem(
                            text = { Text(busId) },
                            onClick = {
                                onBusSelected(busId)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        if (isTracking) {
            Button(
                onClick = onStopTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Stop Tracking (Log Out Temporarily)")
            }
        } else {
            Button(
                onClick = onStartTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = selectedBusId.isNotBlank()
            ) {
                Text(text = "Start Tracking")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tracking requires location access and runs in the background.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Log Out of Session")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DriverScreenPreview() {
    SimpliBusTheme {
        var isTracking by remember { mutableStateOf(false) }
        var selectedBus by remember { mutableStateOf("BUS-101") }
        val buses = listOf("BUS-101", "BUS-102")

        DriverScreenUI(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            isTracking = isTracking,
            isLoading = false,
            availableBuses = buses,
            selectedBusId = selectedBus,
            onBusSelected = { selectedBus = it },
            onStartTracking = { isTracking = true },
            onStopTracking = { isTracking = false },
            onLogout = {},
            currentLocation = "S"
        )
    }
}