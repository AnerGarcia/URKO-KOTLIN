package com.azterketa.multimediaproyect.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azterketa.multimediaproyect.ui.auth.AuthManager

class MainViewModel : ViewModel() {

    private val authManager = AuthManager()

    private val _currentUser = MutableLiveData<UserInfo?>()
    val currentUser: LiveData<UserInfo?> = _currentUser

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> = _signOutResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = authManager.getCurrentUser()
        if (user != null) {
            _currentUser.value = UserInfo(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )
        } else {
            _currentUser.value = null
        }
    }

    fun signOut() {
        _isLoading.value = true
        try {
            authManager.signOut()
            _currentUser.value = null
            _signOutResult.value = true
        } catch (e: Exception) {
            _signOutResult.value = false
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }

    fun isUserLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }

    data class UserInfo(
        val uid: String,
        val email: String,
        val displayName: String?,
        val photoUrl: String?
    )
}