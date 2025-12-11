package com.example.simplibus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.simplibus.ui.driver.DriverScreen
import com.example.simplibus.ui.driver.LoginScreen
import com.example.simplibus.ui.driver.LoginViewModel
import com.example.simplibus.ui.driver.ResetPasswordScreen
import com.example.simplibus.ui.passenger.PassengerScreen
import com.example.simplibus.ui.passenger.RouteViewModel
import com.example.simplibus.ui.passenger.ScheduleScreen
import com.example.simplibus.ui.passenger.ScheduleViewModel
import com.example.simplibus.ui.role.AnimatedRoleScreen
import com.example.simplibus.ui.settings.SettingsScreen
import com.example.simplibus.ui.settings.SettingsViewModel

@Composable
fun AppNavigation(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        composable(
            route = "${Screen.RoleSelection.route}?showImmediately={showImmediately}",
            arguments = listOf(
                navArgument("showImmediately") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val showImmediately = backStackEntry.arguments?.getBoolean("showImmediately") ?: false

            AnimatedRoleScreen(
                onPassengerClick = { navController.navigate(Screen.Passenger.route) },
                onDriverClick = { navController.navigate(Screen.LoginScreen.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                showImmediately = showImmediately
            )
        }

        composable(route = Screen.Passenger.route) {
            val routeViewModel: RouteViewModel = viewModel()
            PassengerScreen(navController = navController, routeViewModel)
        }
        composable(Screen.LoginScreen.route) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                navController = navController,
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Driver.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(route = Screen.Driver.route) {
            DriverScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel
            )
        }

        composable(Screen.Schedule.route) {
            val scheduleViewModel: ScheduleViewModel = viewModel()
            ScheduleScreen(navController = navController, viewModel = scheduleViewModel)
        }

        composable(
            route = "${Screen.ResetPassword.route}/{driverId}",
            arguments = listOf(navArgument("driverId") { type = NavType.StringType })
        ) { backStackEntry ->
            val driverId = backStackEntry.arguments?.getString("driverId") ?: ""
            ResetPasswordScreen(
                navController = navController,
                viewModel = viewModel(),
                driverId = driverId
            )
        }
    }
}