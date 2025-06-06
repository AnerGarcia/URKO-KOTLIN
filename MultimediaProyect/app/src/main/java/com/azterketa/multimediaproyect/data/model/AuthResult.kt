package com.azterketa.multimediaproyect.data.model

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}