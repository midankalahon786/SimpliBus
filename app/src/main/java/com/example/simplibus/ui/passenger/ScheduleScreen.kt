package com.example.simplibus.ui.passenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.simplibus.data.passenger.model.BusRouteInfo
import com.example.simplibus.data.passenger.model.Station
import com.example.simplibus.ui.theme.SimpliBusTheme

@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScheduleContent(
        uiState = uiState,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    uiState: ScheduleUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.routes) { route ->
                        RouteScheduleCard(route)
                    }
                }
            }
        }
    }
}

@Composable
fun RouteScheduleCard(route: BusRouteInfo) {
    var expanded by remember { mutableStateOf(false) }
    var showReturn by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = !showReturn,
                            onClick = { showReturn = false },
                            label = { Text("Forward") }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        FilterChip(
                            selected = showReturn,
                            onClick = { showReturn = true },
                            label = { Text("Return") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    val currentStops = if (showReturn) route.stopsReturn else route.stops
                    if (currentStops.isEmpty()) {
                        Text("No stops data available.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        currentStops.forEachIndexed { index, station ->
                            StopItem(index + 1, station)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopItem(number: Int, station: Station) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = station.name, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, name = "Schedule Screen Content")
@Composable
fun ScheduleScreenPreview() {
    SimpliBusTheme {
        ScheduleContent(
            uiState = ScheduleUiState(
                isLoading = false,
                routes = listOf(
                    BusRouteInfo(
                        name = "Route 1: High Court - Dharapur",
                        stops = listOf(
                            Station("High Court", 0.0, 0.0),
                            Station("Panbazar", 0.0, 0.0),
                            Station("Fancy Bazar", 0.0, 0.0)
                        ),
                        stopsReturn = listOf(
                            Station("Dharapur", 0.0, 0.0),
                            Station("Maligaon", 0.0, 0.0)
                        )
                    ),
                    BusRouteInfo(
                        name = "Route 2: Basistha - University",
                        stops = listOf(
                            Station("Basistha Chariali", 0.0, 0.0),
                            Station("Lokhora", 0.0, 0.0)
                        ),
                        stopsReturn = emptyList()
                    )
                )
            ),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Route Card")
@Composable
fun RouteCardPreview() {
    SimpliBusTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RouteScheduleCard(
                route = BusRouteInfo(
                    name = "Route 1: High Court - Dharapur",
                    stops = listOf(
                        Station("High Court", 0.0, 0.0),
                        Station("Panbazar", 0.0, 0.0),
                        Station("Fancy Bazar", 0.0, 0.0)
                    ),
                    stopsReturn = listOf()
                )
            )
        }
    }
}