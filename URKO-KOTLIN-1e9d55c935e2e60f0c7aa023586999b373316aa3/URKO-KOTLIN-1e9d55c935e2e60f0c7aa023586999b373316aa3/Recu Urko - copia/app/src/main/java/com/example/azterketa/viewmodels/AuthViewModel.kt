package com.example.azterketa.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.azterketa.models.User
import com.example.azterketa.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> get() = _authSuccess

    val googleSignInClient = authRepository.googleSignInClient

    init {
        _user.value = authRepository.getCurrentUser()
    }

    fun loginWithEmail(email: String, password: String, useSupabase: Boolean = false) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = if (useSupabase) {
                authRepository.loginWithEmailSupabase(email, password)
            } else {
                authRepository.loginWithEmailFirebase(email, password)
            }

            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _authSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
            _loading.value = false
        }
    }

    fun registerWithEmail(email: String, password: String, useSupabase: Boolean = false) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = if (useSupabase) {
                authRepository.registerWithEmailSupabase(email, password)
            } else {
                authRepository.registerWithEmailFirebase(email, password)
            }

            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _authSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
            _loading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authRepository.signInWithGoogleFirebase(idToken)
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _authSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
            _loading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
        _authSuccess.value = false
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun clearError() {
        _error.value = null
    }
}