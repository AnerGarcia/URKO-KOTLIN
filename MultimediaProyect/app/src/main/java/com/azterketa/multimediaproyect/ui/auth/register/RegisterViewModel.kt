package com.azterketa.multimediaproyect.ui.auth.register

import android.app.Application
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

    fun register(email: String, password: String, confirmPassword: String, name: String) {
        if (!validateInput(email, password, confirmPassword, name)) {
            return
        }

        _authResult.value = AuthResult.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.register(email, password, name)
                _authResult.value = result
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error(
                    "Error de conexión: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String, name: String): Boolean {
        var isValid = true

        // Validar nombre
        if (name.isBlank()) {
            _nameError.value = "El nombre es requerido"
            isValid = false
        } else if (name.length < 2) {
            _nameError.value = "El nombre debe tener al menos 2 caracteres"
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
        } else {
            _emailError.value = null
        }

        // Validar contraseña
        if (password.isBlank()) {
            _passwordError.value = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "La contraseña debe tener al menos 6 caracteres"
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

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _nameError.value = null
    }
}