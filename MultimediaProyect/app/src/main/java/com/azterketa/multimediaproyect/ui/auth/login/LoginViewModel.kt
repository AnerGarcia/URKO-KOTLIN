package com.azterketa.multimediaproyect.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
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

            val result = authRepository.login(email, password)
            _loginState.value = when (result) {
                is AuthResult.Success -> LoginState.Success
                is AuthResult.Error -> LoginState.Error(result.message)
                is AuthResult.Loading -> LoginState.Loading
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            _resetPasswordMessage.value = when (result) {
                is AuthResult.Success -> "Email de recuperación enviado"
                is AuthResult.Error -> result.message
                is AuthResult.Loading -> null
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