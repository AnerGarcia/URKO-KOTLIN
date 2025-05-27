package com.azterketa.multimediaproyect.data.repository

import android.util.Log
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException

class AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
        private const val USERS_COLLECTION = "users"
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando iniciar sesión para: $email")

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                Log.d(TAG, "Login exitoso para usuario: ${firebaseUser.uid}")

                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del login")
                AuthResult.Error("Error de autenticación")
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Error de Firebase Auth: ${e.errorCode}", e)
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Email inválido"
                "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
                "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Revisa tu internet"
                else -> "Error de autenticación: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Error de red: Sin conexión a internet", e)
            AuthResult.Error("Sin conexión a internet. Revisa tu conexión")
        } catch (e: Exception) {
            Log.e(TAG, "Error general en login", e)
            AuthResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando registrar usuario: $email")

            // Crear usuario con email y contraseña
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                Log.d(TAG, "Usuario creado exitosamente: ${firebaseUser.uid}")

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

                // Guardar usuario en Firestore (opcional)
                try {
                    saveUserToFirestore(user)
                } catch (e: Exception) {
                    Log.w(TAG, "Error al guardar en Firestore, pero registro exitoso", e)
                }

                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del registro")
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Error de Firebase Auth en registro: ${e.errorCode}", e)
            val errorMessage = when (e.errorCode) {
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil"
                "ERROR_INVALID_EMAIL" -> "Email inválido"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este email ya está en uso"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Revisa tu internet"
                else -> "Error al crear cuenta: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Error de red en registro: Sin conexión a internet", e)
            AuthResult.Error("Sin conexión a internet. Revisa tu conexión")
        } catch (e: Exception) {
            Log.e(TAG, "Error general en registro", e)
            AuthResult.Error("Error de conexión: ${e.message}")
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user)
                .await()
            Log.d(TAG, "Usuario guardado en Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar en Firestore", e)
            throw e
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
            Log.e(TAG, "Error al obtener usuario actual", e)
            null
        }
    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
            Log.d(TAG, "Usuario desconectado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            throw e
        }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}