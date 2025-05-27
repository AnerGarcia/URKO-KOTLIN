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

    private val authRepository = AuthRepository(application)

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun register(email: String, password: String, name: String) {
        _authResult.value = AuthResult.Loading

        viewModelScope.launch {
            try {
                val result = authRepository.register(email, password, name)
                _authResult.value = result
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Error al crear cuenta: ${e.message}")
            }
        }
    }
}