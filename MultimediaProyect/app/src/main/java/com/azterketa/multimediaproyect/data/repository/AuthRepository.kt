package com.azterketa.multimediaproyect.data.repository

import android.util.Log
import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository {
    private val supabase = SupabaseConfig.client

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(email: String, password: String): AuthResult {
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
                    createdAt = userInfo.createdAt
                )
                Log.d(TAG, "Login exitoso para: ${user.email}")
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del login")
                AuthResult.Error("Error de autenticación")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            val errorMessage = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                    "Email o contraseña incorrectos"
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Error de conexión. Verifica tu internet"
                else -> "Error de autenticación: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AuthResult {
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
                    createdAt = userInfo.createdAt
                )
                Log.d(TAG, "Registro exitoso para: ${user.email}")
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del registro")
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            val errorMessage = when {
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    "Este email ya está registrado"
                e.message?.contains("Password should be at least", ignoreCase = true) == true ->
                    "La contraseña debe tener al menos 6 caracteres"
                e.message?.contains("Invalid email", ignoreCase = true) == true ->
                    "Email inválido"
                else -> "Error al crear la cuenta: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = userInfo.userMetadata?.get("display_name")?.toString(),
                    createdAt = userInfo.createdAt
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
            supabase.auth.signOut()
            Log.d(TAG, "Usuario desconectado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            throw e
        }
    }

    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            AuthResult.Success(User()) // Usuario vacío indica éxito
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de recuperación", e)
            AuthResult.Error("Error al enviar email: ${e.message}")
        }
    }
}