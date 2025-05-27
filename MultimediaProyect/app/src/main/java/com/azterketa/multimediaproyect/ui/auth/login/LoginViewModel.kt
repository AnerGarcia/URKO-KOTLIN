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

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState> = _loginResult

    private val _resetPasswordResult = MutableLiveData<String?>()
    val resetPasswordResult: LiveData<String?> = _resetPasswordResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = LoginState.Loading

            val result = authRepository.login(email, password)
            when (result) {
                is AuthResult.Success -> {
                    _loginResult.value = LoginState.Success(
                        uid = result.user.id,
                        email = result.user.email,
                        displayName = result.user.displayName
                    )
                }
                is AuthResult.Error -> {
                    _loginResult.value = LoginState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            when (result) {
                is AuthResult.Success -> {
                    _resetPasswordResult.value = "Email de recuperación enviado"
                }
                is AuthResult.Error -> {
                    _resetPasswordResult.value = result.message
                }
                is AuthResult.Loading -> {
                    // No aplicable aquí
                }
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(
            val uid: String,
            val email: String,
            val displayName: String?
        ) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}