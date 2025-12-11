package com.example.simplibus.ui.role

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.simplibus.R
import com.example.simplibus.data.driver.service.LocationService
import com.example.simplibus.ui.driver.DriverDataStore
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AnimatedRoleScreen(
    onPassengerClick: () -> Unit,
    onDriverClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showImmediately: Boolean = false
) {
    var isContentVisible by rememberSaveable { mutableStateOf(showImmediately) }
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    fun checkDriverAndNavigateToPassenger() {
        if (!isConnected) {
            isConnected = isInternetAvailable(context)
            if (!isConnected) return
        }
        if (LocationService.isRunning.value) {
            Toast.makeText(context, "Active Driver Session: Please stop tracking first.", Toast.LENGTH_LONG).show()
            return
        }
        val dataStore = DriverDataStore(context)
        if (dataStore.isLoggedIn()) {
            Toast.makeText(context, "You are logged in as a Driver.\nPlease logout to access Passenger mode.", Toast.LENGTH_LONG).show()
        } else {
            onPassengerClick()
        }
    }
    LaunchedEffect(Unit) {
        isConnected = isInternetAvailable(context)
        if (!isContentVisible) {
            delay(1500L)
            isContentVisible = true
        }
    }

    val verticalBias by animateFloatAsState(
        targetValue = if (isContentVisible) -0.4f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "VerticalBias"
    )

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val taglineOffsetX by animateDpAsState(
        targetValue = if (isContentVisible) 0.dp else -screenWidth,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "TaglineOffset"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        AnimatedVisibility(
            visible = !isConnected,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 60.dp)
                .padding(horizontal = 16.dp)
                .zIndex(10f)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No Internet Connection",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.weight(1f + verticalBias))
            Image(
                painter = painterResource(id = R.drawable.simplibus_icon),
                contentDescription = "SimpliBus Logo",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Personal Bus Companion",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(x = taglineOffsetX)
            )

            Spacer(Modifier.weight(1f - verticalBias))
        }
        AnimatedVisibility(
            visible = isContentVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(animationSpec = tween(delayMillis = 200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Welcome to SimpliBus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please select your role:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { checkDriverAndNavigateToPassenger() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD98E4C)
                    )
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("I'm a Passenger")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isConnected) onDriverClick() else isConnected = isInternetAvailable(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6E7B8B)
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_directions_bus_24),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("I'm a Driver")
                }
            }
        }
    }
}
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}
@Preview(showBackground = true, heightDp = 640, widthDp = 360)
@Composable
fun AnimatedRoleScreenPreviewSmall() {
    AnimatedRoleScreen(
        onPassengerClick = {}, onDriverClick = {},
        onSettingsClick = {}
    )
}