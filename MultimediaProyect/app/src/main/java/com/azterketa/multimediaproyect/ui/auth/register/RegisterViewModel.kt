package com.azterketa.multimediaproyect.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            val result = authRepository.register(email, password, displayName)
            _registerState.value = when (result) {
                is AuthResult.Success -> RegisterState.Success
                is AuthResult.Error -> RegisterState.Error(result.message)
                is AuthResult.Loading -> RegisterState.Loading
            }
        }
    }

    sealed class RegisterState {
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}