package com.azterketa.multimediaproyect.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.azterketa.multimediaproyect.BuildConfig
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException

class AuthRepository(private val context: Context? = null) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
        private const val USERS_COLLECTION = "users"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    init {
        configureForDevelopment()
    }

    private fun configureForDevelopment() {
        if (BuildConfig.DEBUG) {
            try {
                // Deshabilitar verificación de app para testing en desarrollo
                firebaseAuth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
                Log.d(TAG, "App verification disabled for testing")
            } catch (e: Exception) {
                Log.w(TAG, "Could not disable app verification: ${e.message}")
            }
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return executeWithRetry {
            performLogin(email, password)
        }
    }

    private suspend fun performLogin(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando iniciar sesión para: $email")

            // Verificar conectividad
            if (!isNetworkAvailable()) {
                return AuthResult.Error("Sin conexión a internet. Verifica tu conexión")
            }

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
            handleFirebaseAuthException(e)
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Error de red Firebase", e)
            AuthResult.Error("Error de conexión. Verifica tu internet y vuelve a intentar")
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Error de red: Sin conexión a internet", e)
            AuthResult.Error("Sin conexión a internet. Revisa tu conexión")
        } catch (e: Exception) {
            Log.e(TAG, "Error general en login", e)
            when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    AuthResult.Error("Error de conexión. Verifica tu internet")
                e.message?.contains("recaptcha", ignoreCase = true) == true ->
                    AuthResult.Error("Error de verificación. Intenta nuevamente en unos minutos")
                else -> AuthResult.Error("Error de conexión: ${e.message}")
            }
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return executeWithRetry {
            performRegister(email, password, displayName)
        }
    }

    private suspend fun performRegister(email: String, password: String, displayName: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando registrar usuario: $email")

            // Verificar conectividad
            if (!isNetworkAvailable()) {
                return AuthResult.Error("Sin conexión a internet. Verifica tu conexión")
            }

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
            handleFirebaseAuthException(e)
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Error de red Firebase en registro", e)
            AuthResult.Error("Error de conexión. Verifica tu internet y vuelve a intentar")
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Error de red en registro: Sin conexión a internet", e)
            AuthResult.Error("Sin conexión a internet. Revisa tu conexión")
        } catch (e: Exception) {
            Log.e(TAG, "Error general en registro", e)
            when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    AuthResult.Error("Error de conexión. Verifica tu internet")
                e.message?.contains("recaptcha", ignoreCase = true) == true ->
                    AuthResult.Error("Error de verificación. Intenta nuevamente en unos minutos")
                else -> AuthResult.Error("Error al crear cuenta: ${e.message}")
            }
        }
    }

    private fun handleFirebaseAuthException(e: FirebaseAuthException): AuthResult {
        val errorMessage = when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Email inválido"
            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
            "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
            "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Revisa tu internet"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este email ya está en uso"
            else -> "Error de autenticación: ${e.message}"
        }
        return AuthResult.Error(errorMessage)
    }

    private suspend fun executeWithRetry(operation: suspend () -> AuthResult): AuthResult {
        var lastException: Exception? = null

        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val result = operation()
                if (result !is AuthResult.Error || !isRetriableError(result.message)) {
                    return result
                }
                lastException = Exception(result.message)
            } catch (e: Exception) {
                lastException = e
                if (!isRetriableError(e.message)) {
                    throw e
                }
            }

            if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                Log.d(TAG, "Reintentando operación en ${RETRY_DELAY_MS}ms (intento ${attempt + 1})")
                delay(RETRY_DELAY_MS)
            }
        }

        return AuthResult.Error(
            lastException?.message ?: "Error después de $MAX_RETRY_ATTEMPTS intentos"
        )
    }

    private fun isRetriableError(message: String?): Boolean {
        return message?.let { msg ->
            msg.contains("network", ignoreCase = true) ||
                    msg.contains("timeout", ignoreCase = true) ||
                    msg.contains("connection", ignoreCase = true) ||
                    msg.contains("recaptcha", ignoreCase = true)
        } ?: false
    }

    private fun isNetworkAvailable(): Boolean {
        context?.let { ctx ->
            val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.let { cm ->
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        return true // Asumir conectividad si no se puede verificar
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

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(User()) // Usuario vacío para indicar éxito
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Error al enviar email de recuperación: ${e.errorCode}", e)
            handleFirebaseAuthException(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error general al enviar email de recuperación", e)
            AuthResult.Error("Error al enviar email: ${e.message}")
        }
    }
}