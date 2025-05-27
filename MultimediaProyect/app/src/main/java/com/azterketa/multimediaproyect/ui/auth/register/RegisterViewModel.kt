package com.azterketa.multimediaproyect.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azterketa.multimediaproyect.ui.auth.AuthManager

class RegisterViewModel : ViewModel() {

    private val authManager = AuthManager()

    private val _registerResult = MutableLiveData<RegisterState>()
    val registerResult: LiveData<RegisterState> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(email: String, password: String, name: String) {
        _isLoading.value = true
        _registerResult.value = RegisterState.Loading

        authManager.register(email, password, name) { success, error ->
            _isLoading.value = false
            if (success) {
                val user = authManager.getCurrentUser()
                _registerResult.value = RegisterState.Success(
                    uid = user?.uid ?: "",
                    email = user?.email ?: "",
                    displayName = user?.displayName ?: name
                )
            } else {
                _registerResult.value = RegisterState.Error(error ?: "Error desconocido")
            }
        }
    }

    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(
            val uid: String,
            val email: String,
            val displayName: String
        ) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}