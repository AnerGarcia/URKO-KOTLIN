package com.azterketa.multimediaproyect.data.repository

import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error de autenticación")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return try {
            // Crear usuario con email y contraseña
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Actualizar el perfil con el nombre
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()

                // Crear objeto User
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )

                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            throw e
        }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}