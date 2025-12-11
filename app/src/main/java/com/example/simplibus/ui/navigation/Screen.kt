package com.example.simplibus.ui.navigation
sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection_screen")
    object Passenger : Screen("passenger_screen")
    object Driver : Screen("driver_screen")
    object LoginScreen : Screen("login_screen")
    object Settings : Screen("settings_screen")
    object Schedule : Screen("schedule_screen")
    object ResetPassword: Screen("reset_password_screen")
}