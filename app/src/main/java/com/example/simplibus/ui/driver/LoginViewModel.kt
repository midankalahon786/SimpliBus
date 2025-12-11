package com.example.simplibus.ui.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplibus.data.driver.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val driverBusId: String? = null,
    val driverName: String? = null,
    val isResetLoading: Boolean = false,
    val resetMessage: String? = null,
    val resetError: String? = null
)
data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "LoginViewModel"
    private val okHttpClient = OkHttpClient()
    private val authRepository = AuthRepository(okHttpClient)
    private val driverDataStore = DriverDataStore(application.applicationContext)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _driverId = MutableStateFlow("")
    val driverId = _driverId.asStateFlow()
    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    private val _otpState = MutableStateFlow(ResetPasswordUiState())
    val otpState = _otpState.asStateFlow()

    init {
        if (driverDataStore.isLoggedIn()) {
            val busId = driverDataStore.getBusId()
            val name = driverDataStore.getDriverName()
            if (busId != null) {
                _uiState.update {
                    it.copy(
                        isLoginSuccessful = true,
                        driverBusId = busId,
                        driverName = name
                    )
                }
                Log.i(TAG, "Restored session for driver: $name (Bus: $busId)")
            } else {
                driverDataStore.clearSession()
            }
        }
    }

    fun onDriverIdChange(newId: String) {
        _driverId.value = newId
    }
    fun onPasswordChange(newPass: String) {
        _password.value = newPass
    }
    fun login() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val response = authRepository.loginDriver(driverId.value, password.value)
                driverDataStore.saveDriverSession(driverId.value, response.busId, response.name)
                Log.i(TAG, "Login successful. Session saved for ${response.name}.")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        driverBusId = response.busId,
                        driverName = response.name
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unknown error occurred"
                    )
                }
                Log.e(TAG, "Login failed: ${e.message}")
            }
        }
    }
    fun triggerForgotPassword(driverId: String) {
        if (driverId.isBlank()) {
            _uiState.update { it.copy(resetError = "Please enter your Driver ID first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isResetLoading = true, resetError = null, resetMessage = null)
            }
            try {
                val msg = authRepository.requestPasswordReset(driverId)
                _uiState.update {
                    it.copy(isResetLoading = false, resetMessage = msg)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isResetLoading = false, resetError = e.message ?: "Reset failed")
                }
            }
        }
    }

    fun submitResetPassword(driverId: String, otp: String, newPass: String) {
        if (otp.length != 6) {
            _otpState.update { it.copy(error = "OTP must be 6 digits") }
            return
        }
        if (newPass.length < 4) {
            _otpState.update { it.copy(error = "Password is too short") }
            return
        }

        viewModelScope.launch {
            _otpState.update { it.copy(isLoading = true, error = null, success = null) }
            try {
                val msg = authRepository.verifyOtpAndResetPassword(driverId, otp, newPass)
                _otpState.update { it.copy(isLoading = false, success = msg) }
            } catch (e: Exception) {
                _otpState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun clearResetState() {
        _uiState.update { it.copy(resetMessage = null, resetError = null) }
    }

    fun logout() {
        driverDataStore.clearSession()
        _uiState.update { LoginUiState(isLoginSuccessful = false) }
        _driverId.value = ""
        _password.value = ""
        Log.i(TAG, "Driver session cleared (Logged out).")
    }
}