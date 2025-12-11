package com.example.simplibus.ui.role

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simplibus.ui.navigation.AppNavigation
import com.example.simplibus.ui.settings.SettingsViewModel
import com.example.simplibus.ui.theme.SimpliBusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SettingsViewModel::class.java]

        setContent {
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            SimpliBusTheme(
                darkTheme = isDarkTheme
            ) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}