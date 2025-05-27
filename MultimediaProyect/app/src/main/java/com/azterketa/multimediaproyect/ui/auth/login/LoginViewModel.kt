package com.azterketa.multimediaproyect.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _resetPasswordMessage = MutableLiveData<String?>()
    val resetPasswordMessage: LiveData<String?> = _resetPasswordMessage

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // authRepository.login() devuelve Boolean, no AuthResult
            val success = authRepository.login(email, password)
            _loginState.value = if (success) {
                LoginState.Success
            } else {
                LoginState.Error("Error en el login")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            _resetPasswordMessage.value = when (result) {
                is com.azterketa.multimediaproyect.data.model.AuthResult.Success -> "Email de recuperación enviado"
                is com.azterketa.multimediaproyect.data.model.AuthResult.Error -> result.message
                is com.azterketa.multimediaproyect.data.model.AuthResult.Loading -> null
            }
        }
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}