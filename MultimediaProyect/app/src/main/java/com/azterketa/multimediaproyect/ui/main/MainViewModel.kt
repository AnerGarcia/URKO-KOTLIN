package com.azterketa.multimediaproyect.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _currentUser = MutableLiveData<UserInfo?>()
    val currentUser: LiveData<UserInfo?> = _currentUser

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> = _signOutResult

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _currentUser.value = UserInfo(
                    uid = user.id,
                    email = user.email,
                    displayName = user.displayName
                )
            } else {
                _currentUser.value = null
            }
        }
    }

    suspend fun signOut() {
        try {
            authRepository.signOut()
            _currentUser.value = null
            _signOutResult.value = true
        } catch (e: Exception) {
            _signOutResult.value = false
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    data class UserInfo(
        val uid: String,
        val email: String,
        val displayName: String?
    )
}