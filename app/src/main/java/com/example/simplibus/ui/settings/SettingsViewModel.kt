package com.example.simplibus.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.core.content.edit
import androidx.core.net.toUri

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // State for Dark Mode (default to system setting logic usually, here false for simplicity)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    init {
        loadTheme()
    }

    private fun loadTheme() {
        // Default to false (Light) or system default if you prefer
        val savedTheme = prefs.getBoolean("dark_theme", false)
        _isDarkTheme.value = savedTheme
    }

    fun toggleTheme(enabled: Boolean) {
        _isDarkTheme.update { enabled }
        prefs.edit { putBoolean("dark_theme", enabled) }
    }

    fun openEmailClient(context: Context, subject: String, body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri() // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("midankalahon@gmail.com")) // Replace with your
            // email
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no email app is found
        }
    }
}