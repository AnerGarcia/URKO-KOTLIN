package com.azterketa.multimediaproyect.ui.auth.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun login(email: String, password: String) {
        if (validateInput(email, password)) {
            _authResult.value = AuthResult.Loading

            viewModelScope.launch {
                try {
                    val result = authRepository.login(email, password)
                    _authResult.value = result
                } catch (e: Exception) {
                    _authResult.value = AuthResult.Error(e.message ?: "Error de conexión")
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch {
                try {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    _authResult.value = AuthResult.Success(
                        com.azterketa.multimediaproyect.data.model.User()
                    )
                } catch (e: Exception) {
                    _authResult.value = AuthResult.Error(e.message ?: "Error al enviar email")
                }
            }
        } else {
            _authResult.value = AuthResult.Error("Ingresa un email válido para recuperar tu contraseña")
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
    }
}