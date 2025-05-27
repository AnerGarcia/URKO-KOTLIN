package com.azterketa.multimediaproyect.data.repository

import android.util.Log
import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.delay

class AuthRepository {

    private val supabase = SupabaseConfig.client

    companion object {
        private const val TAG = "AuthRepository"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    // Login con reintentos automáticos
    suspend fun login(email: String, password: String): AuthResult {
        return executeWithRetry {
            performLogin(email, password)
        }
    }

    private suspend fun performLogin(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando login para: $email")

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = userInfo.userMetadata?.get("display_name")?.toString(),
                    avatarUrl = userInfo.userMetadata?.get("avatar_url")?.toString(),
                    createdAt = userInfo.createdAt
                )
                Log.d(TAG, "Login exitoso")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error de autenticación")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            AuthResult.Error(getErrorMessage(e))
        }
    }

    // Registro con reintentos automáticos
    suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return executeWithRetry {
            performRegister(email, password, displayName)
        }
    }

    private suspend fun performRegister(email: String, password: String, displayName: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando registro para: $email")

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = mapOf("display_name" to displayName)
            }

            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = displayName,
                    avatarUrl = userInfo.userMetadata?.get("avatar_url")?.toString(),
                    createdAt = userInfo.createdAt
                )
                Log.d(TAG, "Registro exitoso")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro: ${e.message}")
            AuthResult.Error(getErrorMessage(e))
        }
    }

    // Cerrar sesión
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
            Log.d(TAG, "Sesión cerrada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión: ${e.message}")
            throw e
        }
    }

    // Obtener usuario actual
    suspend fun getCurrentUser(): User? {
        return try {
            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = userInfo.userMetadata?.get("display_name")?.toString(),
                    avatarUrl = userInfo.userMetadata?.get("avatar_url")?.toString(),
                    createdAt = userInfo.createdAt
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario: ${e.message}")
            null
        }
    }

    // Verificar si hay usuario logueado
    fun isUserLoggedIn(): Boolean {
        return try {
            supabase.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar login: ${e.message}")
            false
        }
    }

    // Recuperar contraseña
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            AuthResult.Success(User()) // Usuario vacío indica éxito
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de recuperación: ${e.message}")
            AuthResult.Error("Error al enviar email: ${e.message}")
        }
    }

    // Métodos privados de utilidad
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                "Email o contraseña incorrectos"
            e.message?.contains("User already registered", ignoreCase = true) == true ->
                "Este email ya está registrado"
            e.message?.contains("Password should be at least", ignoreCase = true) == true ->
                "La contraseña debe tener al menos 6 caracteres"
            e.message?.contains("Invalid email", ignoreCase = true) == true ->
                "Email inválido"
            e.message?.contains("network", ignoreCase = true) == true ->
                "Error de conexión. Verifica tu internet"
            e.message?.contains("timeout", ignoreCase = true) == true ->
                "Tiempo de espera agotado. Intenta nuevamente"
            else -> "Error: ${e.message ?: "Desconocido"}"
        }
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
                    return AuthResult.Error(getErrorMessage(e))
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
                    msg.contains("connection", ignoreCase = true)
        } ?: false
    }
}