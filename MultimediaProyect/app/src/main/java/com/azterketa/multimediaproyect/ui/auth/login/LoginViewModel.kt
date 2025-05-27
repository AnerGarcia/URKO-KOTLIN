package com.azterketa.multimediaproyect.ui.auth.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _resetPasswordResult = MutableLiveData<String?>()
    val resetPasswordResult: LiveData<String?> = _resetPasswordResult

    fun login(email: String, password: String) {
        if (validateInput(email, password)) {
            _authResult.value = AuthResult.Loading

            viewModelScope.launch {
                try {
                    val result = authRepository.login(email, password)
                    _authResult.value = result
                } catch (e: Exception) {
                    _authResult.value = AuthResult.Error(
                        "Error de conexión: ${e.message ?: "Error desconocido"}"
                    )
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch {
                try {
                    val result = authRepository.sendPasswordResetEmail(email)
                    when (result) {
                        is AuthResult.Success -> {
                            _resetPasswordResult.value = "Se ha enviado un email de recuperación a $email"
                        }
                        is AuthResult.Error -> {
                            _resetPasswordResult.value = result.message
                        }
                        else -> {
                            _resetPasswordResult.value = "Error al enviar email de recuperación"
                        }
                    }
                } catch (e: Exception) {
                    _resetPasswordResult.value = "Error al enviar email: ${e.message}"
                }
            }
        } else {
            _resetPasswordResult.value = "Ingresa un email válido para recuperar tu contraseña"
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validar email
        when {
            email.isEmpty() -> {
                _emailError.value = "Email requerido"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailError.value = "Email inválido"
                isValid = false
            }
            else -> {
                _emailError.value = null
            }
        }

        // Validar contraseña
        when {
            password.isEmpty() -> {
                _passwordError.value = "Contraseña requerida"
                isValid = false
            }
            password.length < 6 -> {
                _passwordError.value = "La contraseña debe tener al menos 6 caracteres"
                isValid = false
            }
            else -> {
                _passwordError.value = null
            }
        }

        return isValid
    }

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _resetPasswordResult.value = null
    }
}