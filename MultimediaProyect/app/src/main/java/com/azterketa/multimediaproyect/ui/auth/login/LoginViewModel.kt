package com.azterketa.multimediaproyect.ui.auth.login

import android.app.Application
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

    private val _resetPasswordResult = MutableLiveData<String?>()
    val resetPasswordResult: LiveData<String?> = _resetPasswordResult

    fun login(email: String, password: String) {
        _authResult.value = AuthResult.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                _authResult.value = result
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String) {
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
    }
}