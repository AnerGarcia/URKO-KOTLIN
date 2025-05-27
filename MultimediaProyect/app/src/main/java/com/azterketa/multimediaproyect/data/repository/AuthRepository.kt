package com.azterketa.multimediaproyect.data.repository

import android.util.Log
import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val supabase = SupabaseConfig.client

    // Login simple
    suspend fun login(email: String, password: String): Boolean {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error login: ${e.message}")
            false
        }
    }

    // Registro simple - CORREGIDO
    suspend fun register(email: String, password: String, displayName: String): Boolean {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Cambio: usar buildJsonObject en lugar de mapOf
                data = buildJsonObject {
                    put("display_name", displayName)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registro: ${e.message}")
            false
        }
    }

    // Cerrar sesión
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error cerrar sesión: ${e.message}")
        }
    }

    // Verificar si está logueado
    fun isLoggedIn(): Boolean {
        return try {
            supabase.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    // Para compatibilidad con ViewModels
    fun isUserLoggedIn(): Boolean = isLoggedIn()

    // Obtener email del usuario actual
    fun getCurrentUserEmail(): String {
        return try {
            supabase.auth.currentUserOrNull()?.email ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Obtener nombre del usuario actual
    fun getCurrentUserName(): String {
        return try {
            supabase.auth.currentUserOrNull()?.userMetadata?.get("display_name")?.toString()?.replace("\"", "") ?: "Usuario"
        } catch (e: Exception) {
            "Usuario"
        }
    }

    // Obtener usuario actual como objeto User
    suspend fun getCurrentUser(): User? {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            currentUser?.let {
                User(
                    id = it.id,
                    email = it.email ?: "",
                    displayName = it.userMetadata?.get("display_name")?.toString()?.replace("\"", ""),
                    avatarUrl = it.userMetadata?.get("avatar_url")?.toString()?.replace("\"", ""),
                    createdAt = it.createdAt.toString()
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error obteniendo usuario: ${e.message}")
            null
        }
    }

    // Método placeholder para reset password (si lo necesitas más adelante)
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            // Por ahora solo retornamos éxito
            // Más adelante puedes implementar: supabase.auth.resetPasswordForEmail(email)
            AuthResult.Success(User()) // Usuario vacío como placeholder
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }
}