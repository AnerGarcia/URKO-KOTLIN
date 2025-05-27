package com.azterketa.multimediaproyect.ui.auth.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()

    private val _registerResult = MutableLiveData<RegisterState>()
    val registerResult: LiveData<RegisterState> = _registerResult

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerResult.value = RegisterState.Loading

            val result = authRepository.register(email, password, displayName)
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