package com.azterketa.multimediaproyect.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.model.User
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }
}