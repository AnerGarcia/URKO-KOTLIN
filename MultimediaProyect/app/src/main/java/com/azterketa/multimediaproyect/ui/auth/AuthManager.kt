package com.azterketa.multimediaproyect.auth

import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthManager {

    private val supabase = SupabaseConfig.client

    // Registrar usuario
    fun register(email: String, password: String, displayName: String, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = mapOf(
                        "display_name" to displayName
                    )
                }
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message ?: "Error desconocido")
            }
        }
    }

    // Iniciar sesión
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message ?: "Error desconocido")
            }
        }
    }

    // Cerrar sesión
    fun signOut(callback: ((Boolean, String?) -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                supabase.auth.signOut()
                callback?.invoke(true, null)
            } catch (e: Exception) {
                callback?.invoke(false, e.message)
            }
        }
    }

    // Obtener usuario actual
    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    // Verificar si hay usuario logueado
    fun isLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
}