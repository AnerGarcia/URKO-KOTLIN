package com.azterketa.multimediaproyect.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.User
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user

                if (user == null) {
                    _errorMessage.value = "No hay usuario autenticado"
                }
            } catch (e: Exception) {
                _currentUser.value = null
                _errorMessage.value = "Error al cargar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                authRepository.signOut()
                _currentUser.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al cerrar sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}