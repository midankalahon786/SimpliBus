package com.example.simplibus.ui.passenger

import android.Manifest
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.simplibus.R
import com.example.simplibus.data.passenger.model.Station
import com.example.simplibus.ui.navigation.Screen
import com.example.simplibus.ui.theme.SimpliBusTheme
import com.example.simplibus.utils.NotificationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun PassengerScreen(
    navController: NavHostController,
    viewModel: RouteViewModel = viewModel(),
){
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Permission result handled silently */ }
    )
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    if (uiState.selectedBusId == null) {
        BusSelectionScreen(
            uiState = uiState,
            onBusSelected = { busId -> viewModel.selectBusAndStartTracking(busId) },
            onBackClicked = { navController.popBackStack() },
            onRefresh = { },
            onScheduleClick = { navController.navigate(Screen.Schedule.route) }
        )
    } else {
        RouteScreen(
            uiState = uiState,
            onBackClicked = { viewModel.deselectBus() },
            onRefresh = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusSelectionScreen(
    uiState: RouteUiState,
    onBusSelected: (String) -> Unit,
    onBackClicked: () -> Unit,
    onRefresh: () -> Unit,
    onScheduleClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Route", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onScheduleClick) {
                        Icon(Icons.Default.DateRange, "Schedule")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            if (uiState.isLoadingBuses) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = "Live Buses",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.availableBuses) { busId ->
                        BusListItem(busId = busId, onBusSelected = onBusSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun BusListItem(busId: String, onBusSelected: (String) -> Unit) {
    Surface(
        onClick = { onBusSelected(busId) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_bus_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = busId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to track live location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    uiState: RouteUiState,
    onBackClicked: () -> Unit,
    onRefresh: () -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 130.dp, // Height when minimized
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                Text(
                    text = "Route Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                StationTimelineList(
                    stations = uiState.stations,
                    estimates = uiState.stationEstimates,
                    currentSegmentIndex = uiState.currentSegmentIndex
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.stations.isNotEmpty()) {
                BusMap(
                    uiState = uiState,
                    stations = uiState.stations,
                    busLat = uiState.busLat,
                    busLng = uiState.busLng
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            FloatingStatusCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                uiState = uiState,
                onBackClicked = onBackClicked
            )
        }
    }
}
@Composable
fun FloatingStatusCard(
    modifier: Modifier = Modifier,
    uiState: RouteUiState,
    onBackClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClicked, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = uiState.selectedBusId ?: "Tracking Bus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Circle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when (uiState.seatStatus?.lowercase()) {
                        "seats available" -> Color(0xFFE8F5E9)
                        "full" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF8E1)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EventSeat, null, Modifier.size(14.dp),
                            tint = when (uiState.seatStatus?.lowercase()) {
                                "seats available" -> Color(0xFF2E7D32)
                                "full" -> Color(0xFFC62828)
                                else -> Color(0xFFF9A825)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.seatStatus ?: "Unknown",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (uiState.seatStatus?.lowercase()) {
                                "seats available" -> Color(0xFF2E7D32)
                                "full" -> Color(0xFFC62828)
                                else -> Color(0xFFF9A825)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (uiState.eta != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ETA: ${uiState.eta}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun StationTimelineList(
    stations: List<Station>,
    estimates: Map<Int, Pair<String, String>>,
    currentSegmentIndex: Int,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        val startIndex = (currentSegmentIndex + 1).coerceAtLeast(0)

        if (startIndex < stations.size) {
            val futureStations = stations.subList(startIndex, stations.size)

            itemsIndexed(futureStations) { index, station ->
                val absoluteIndex = startIndex + index
                val estimate = estimates[absoluteIndex]
                val isFirst = index == 0

                TimelineItem(
                    stationName = station.name,
                    distance = estimate?.first ?: "--",
                    time = estimate?.second ?: "--",
                    isFirst = isFirst,
                    isLast = index == futureStations.lastIndex
                )
            }
        } else {
            item {
                Text("Route completed or no upcoming stops.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun TimelineItem(
    stationName: String,
    distance: String,
    time: String,
    isFirst: Boolean,
    isLast: Boolean
) {
    IntrinsicHeightRow(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(60.dp).padding(vertical = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if(isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = distance,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFirst) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Outlined.Circle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp).padding(top = 4.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal,
                color = if (isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun IntrinsicHeightRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
        content = content
    )
}

@Composable
fun BusMap(
    uiState: RouteUiState,
    stations: List<Station>,
    busLat: Double,
    busLng: Double,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) { Text("Map Preview") }
        return
    }
    val initialLocation = if (stations.isNotEmpty()) LatLng(stations.first().lat, stations.first().lng) else LatLng(busLat, busLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }

    LaunchedEffect(busLat, busLng) {
        if (busLat != 0.0 && busLng != 0.0) {
            val newBusPos = LatLng(busLat, busLng)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(newBusPos, 16f)
                ),
                durationMs = 1000
            )
        }
    }

    val context = LocalContext.current
    val busIcon = remember { bitmapDescriptorFromVector(context, R.drawable.baseline_directions_bus_24, Color(0xFF4285F4)) }
    val stationIcon = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false)
    ) {
        stations.forEach { station ->
            Marker(
                state = MarkerState(position = LatLng(station.lat, station.lng)),
                title = station.name,
                icon = stationIcon,
                anchor = Offset(0.5f, 0.5f),
                flat = true
            )
        }

        if (busLat != 0.0 && busLng != 0.0) {
            Marker(
                state = MarkerState(position = LatLng(busLat, busLng)),
                title = uiState.selectedBusId ?: "Bus",
                snippet = uiState.statusMessage,
                icon = busIcon,
                zIndex = 1.0f,
                anchor = Offset(0.5f, 0.5f)
            )
        }
    }
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, tintColor: Color): BitmapDescriptor? {
    try {
        com.google.android.gms.maps.MapsInitializer.initialize(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setTint(tintColor.toArgb())
    val width = (vectorDrawable.intrinsicWidth * 1.5).toInt()
    val height = (vectorDrawable.intrinsicHeight * 1.5).toInt()
    vectorDrawable.setBounds(0, 0, width, height)
    val bitmap = try {
        createBitmap(width, height)
    } catch (e: Exception) {
        return null
    }
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return try {
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true, name = "Bus Selection Preview")
@Composable
fun BusSelectionScreenPreview() {
    SimpliBusTheme {
        BusSelectionScreen(
            uiState = RouteUiState(
                availableBuses = listOf("BUS-101: Downtown", "BUS-205: North Route"),
                isLoadingBuses = false,
                statusMessage = "Select a route to track"
            ),
            onBusSelected = {},
            onBackClicked = {},
            onRefresh = {},
            onScheduleClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Route Screen Preview")
@Composable
fun RouteScreenPreview() {
    SimpliBusTheme {
        RouteScreen(
            uiState = RouteUiState(
                selectedBusId = "BUS-101",
                statusMessage = "En route to Panbazar: 500m",
                seatStatus = "Seats Available",
                eta = "5 mins",
                stations = listOf(
                    Station("Panbazar", 0.0, 0.0),
                    Station("Fancy Bazar", 0.0, 0.0)
                ),
                stationEstimates = mapOf(
                    0 to Pair("500m", "5 mins"),
                    1 to Pair("1.2km", "12 mins")
                )
            ),
            onBackClicked = {},
            onRefresh = {}
        )
    }
}