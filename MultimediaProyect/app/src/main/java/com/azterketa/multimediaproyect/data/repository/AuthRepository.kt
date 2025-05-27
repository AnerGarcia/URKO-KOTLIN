package com.azterketa.multimediaproyect.data.repository

import android.util.Log
import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

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

    // Registro simple
    suspend fun register(email: String, password: String, displayName: String): Boolean {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = mapOf("display_name" to displayName)
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
            supabase.auth.currentUserOrNull()?.userMetadata?.get("display_name")?.toString() ?: "Usuario"
        } catch (e: Exception) {
            "Usuario"
        }
    }
}