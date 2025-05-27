package com.azterketa.multimediaproyect.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azterketa.multimediaproyect.ui.auth.AuthManager

class LoginViewModel : ViewModel() {

    private val authManager = AuthManager()

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState> = _loginResult

    private val _resetPasswordResult = MutableLiveData<String?>()
    val resetPasswordResult: LiveData<String?> = _resetPasswordResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        _loginResult.value = LoginState.Loading

        authManager.login(email, password) { success, error ->
            _isLoading.value = false
            if (success) {
                val user = authManager.getCurrentUser()
                _loginResult.value = LoginState.Success(
                    uid = user?.uid ?: "",
                    email = user?.email ?: "",
                    displayName = user?.displayName
                )
            } else {
                _loginResult.value = LoginState.Error(error ?: "Error desconocido")
            }
        }
    }

    fun resetPassword(email: String) {
        authManager.sendPasswordResetEmail(email) { success, message ->
            _resetPasswordResult.value = message
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authManager.isLoggedIn()
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