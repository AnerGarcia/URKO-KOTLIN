package com.azterketa.multimediaproyect.ui.auth.register

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val context = application.applicationContext

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _networkError = MutableLiveData<Boolean>()
    val networkError: LiveData<Boolean> = _networkError

    private val _passwordStrength = MutableLiveData<PasswordStrength>()
    val passwordStrength: LiveData<PasswordStrength> = _passwordStrength

    // Variables para tracking de datos no guardados
    private var currentName = ""
    private var currentEmail = ""
    private var currentPassword = ""
    private var currentConfirmPassword = ""

    companion object {
        private const val TAG = "RegisterViewModel"
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MIN_NAME_LENGTH = 2
    }

    enum class PasswordStrength {
        WEAK, REGULAR, GOOD, STRONG
    }

    fun register(email: String, password: String, confirmPassword: String, name: String) {
        // Actualizar datos actuales
        updateCurrentData(name, email, password, confirmPassword)

        // Verificar conexión de red
        if (!isNetworkAvailable()) {
            _networkError.value = true
            return
        }

        if (!validateInput(email, password, confirmPassword, name)) {
            return
        }

        _authResult.value = AuthResult.Loading

        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando registro para email: $email")
                val result = authRepository.register(email, password, name)
                _authResult.value = result

                // Limpiar datos si el registro es exitoso
                if (result is AuthResult.Success) {
                    clearCurrentData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en registro", e)
                val errorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Tiempo de espera agotado. Intenta nuevamente"
                    else -> "Error de conexión: ${e.message ?: "Error desconocido"}"
                }
                _authResult.value = AuthResult.Error(errorMessage)
            }
        }
    }

    private fun updateCurrentData(name: String, email: String, password: String, confirmPassword: String) {
        currentName = name
        currentEmail = email
        currentPassword = password
        currentConfirmPassword = confirmPassword
    }

    private fun clearCurrentData() {
        currentName = ""
        currentEmail = ""
        currentPassword = ""
        currentConfirmPassword = ""
    }

    fun hasUnsavedData(): Boolean {
        return currentName.isNotBlank() ||
                currentEmail.isNotBlank() ||
                currentPassword.isNotBlank() ||
                currentConfirmPassword.isNotBlank()
    }

    private fun validateInput(email: String, password: String, confirmPassword: String, name: String): Boolean {
        var isValid = true

        // Validar nombre
        if (name.isBlank()) {
            _nameError.value = "El nombre es requerido"
            isValid = false
        } else if (name.length < MIN_NAME_LENGTH) {
            _nameError.value = "El nombre debe tener al menos $MIN_NAME_LENGTH caracteres"
            isValid = false
        } else if (!isValidName(name)) {
            _nameError.value = "El nombre contiene caracteres no válidos"
            isValid = false
        } else {
            _nameError.value = null
        }

        // Validar email
        if (email.isBlank()) {
            _emailError.value = "El email es requerido"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Email inválido"
            isValid = false
        } else if (!isValidEmailDomain(email)) {
            _emailError.value = "Dominio de email no válido"
            isValid = false
        } else {
            _emailError.value = null
        }

        // Validar contraseña
        val passwordValidation = validatePassword(password)
        if (passwordValidation != null) {
            _passwordError.value = passwordValidation
            isValid = false
        } else {
            _passwordError.value = null
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isBlank()) {
            _confirmPasswordError.value = "Confirmar contraseña es requerido"
            isValid = false
        } else if (password != confirmPassword) {
            _confirmPasswordError.value = "Las contraseñas no coinciden"
            isValid = false
        } else {
            _confirmPasswordError.value = null
        }

        return isValid
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < MIN_PASSWORD_LENGTH ->
                "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            password.length > 128 -> "La contraseña es demasiado larga"
            isCommonPassword(password) -> "Esta contraseña es muy común, elige otra"
            else -> null
        }
    }

    private fun isValidName(name: String): Boolean {
        // Permitir letras, espacios, acentos y algunos caracteres especiales
        val namePattern = Regex("^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s'-]+$")
        return name.matches(namePattern) && !name.trim().contains("  ") // No espacios dobles
    }

    private fun isValidEmailDomain(email: String): Boolean {
        val domain = email.substringAfter("@").lowercase()
        val blockedDomains = listOf("tempmail.com", "10minutemail.com", "guerrillamail.com")
        return !blockedDomains.contains(domain)
    }

    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = listOf(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "1234567890", "qwerty", "abc123", "111111"
        )
        return commonPasswords.contains(password.lowercase())
    }

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        val strength = when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 8 -> PasswordStrength.REGULAR
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&].*$")) ->
                PasswordStrength.STRONG
            password.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d).*$")) ->
                PasswordStrength.GOOD
            else -> PasswordStrength.REGULAR
        }

        _passwordStrength.value = strength
        return strength
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            true // Asumir conectividad si hay error
        }
    }

    // Métodos para limpiar errores individualmente
    fun clearNameError() {
        _nameError.value = null
    }

    fun clearEmailError() {
        _emailError.value = null
    }

    fun clearPasswordError() {
        _passwordError.value = null
    }

    fun clearConfirmPasswordError() {
        _confirmPasswordError.value = null
    }

    fun clearNetworkError() {
        _networkError.value = false
    }

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _nameError.value = null
        _networkError.value = false
    }

    // Validación en tiempo real
    fun validateEmailRealTime(email: String) {
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Email inválido"
        } else {
            _emailError.value = null
        }
    }

    fun validatePasswordRealTime(password: String) {
        evaluatePasswordStrength(password)
        if (password.isNotEmpty() && password.length < MIN_PASSWORD_LENGTH) {
            _passwordError.value = "Mínimo $MIN_PASSWORD_LENGTH caracteres"
        } else {
            _passwordError.value = null
        }
    }

    fun validateConfirmPasswordRealTime(password: String, confirmPassword: String) {
        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            _confirmPasswordError.value = "Las contraseñas no coinciden"
        } else {
            _confirmPasswordError.value = null
        }
    }

    fun validateNameRealTime(name: String) {
        if (name.isNotEmpty() && name.length < MIN_NAME_LENGTH) {
            _nameError.value = "Mínimo $MIN_NAME_LENGTH caracteres"
        } else if (name.isNotEmpty() && !isValidName(name)) {
            _nameError.value = "Caracteres no válidos"
        } else {
            _nameError.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearCurrentData()
        Log.d(TAG, "RegisterViewModel cleared")
    }
}