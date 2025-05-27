package com.azterketa.multimediaproyect.ui.auth.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.ui.auth.AuthManager
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application.applicationContext)

    private val _registerResult = MutableLiveData<RegisterState>()
    val registerResult: LiveData<RegisterState> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerResult.value = RegisterState.Loading
            _isLoading.value = true

            try {
                val result = authManager.register(email, password, displayName)
                when (result) {
                    is AuthResult.Success -> {
                        _registerResult.value = RegisterState.Success(
                            uid = result.user.id,
                            email = result.user.email,
                            displayName = result.user.displayName ?: displayName
                        )
                    }
                    is AuthResult.Error -> {
                        _registerResult.value = RegisterState.Error(result.message)
                    }
                    is AuthResult.Loading -> {
                        // Ya manejado arriba
                    }
                }
            } catch (e: Exception) {
                _registerResult.value = RegisterState.Error("Error inesperado: ${e.message}")
            } finally {
                _isLoading.value = false
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