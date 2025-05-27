package com.azterketa.multimediaproyect.ui.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.azterketa.multimediaproyect.data.config.SupabaseConfig
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.delay

class AuthManager(private val context: Context? = null) {

    private val supabase = SupabaseConfig.client

    companion object {
        private const val TAG = "AuthManager"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    // Método para login con suspend
    suspend fun login(email: String, password: String): AuthResult {
        return executeWithRetry {
            performLogin(email, password)
        }
    }

    private suspend fun performLogin(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando iniciar sesión para: $email")

            if (!isNetworkAvailable()) {
                return AuthResult.Error("Sin conexión a internet. Verifica tu conexión")
            }

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo != null) {
                Log.d(TAG, "Login exitoso para usuario: ${userInfo.id}")

                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = userInfo.userMetadata?.get("display_name")?.toString(),
                    avatarUrl = userInfo.userMetadata?.get("avatar_url")?.toString(),
                    createdAt = userInfo.createdAt
                )
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del login")
                AuthResult.Error("Error de autenticación")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            handleException(e)
        }
    }

    // Método para login con callback (para compatibilidad)
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val result = login(email, password)
            when (result) {
                is AuthResult.Success -> callback(true, null)
                is AuthResult.Error -> callback(false, result.message)
                is AuthResult.Loading -> {} // No aplicable aquí
            }
        }
    }

    // Método para registro con suspend
    suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return executeWithRetry {
            performRegister(email, password, displayName)
        }
    }

    private suspend fun performRegister(email: String, password: String, displayName: String): AuthResult {
        return try {
            Log.d(TAG, "Intentando registrar usuario: $email")

            if (!isNetworkAvailable()) {
                return AuthResult.Error("Sin conexión a internet. Verifica tu conexión")
            }

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = mapOf(
                    "display_name" to displayName
                )
            }

            val userInfo = supabase.auth.currentUserOrNull()

            if (userInfo != null) {
                Log.d(TAG, "Usuario registrado exitosamente: ${userInfo.id}")

                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = displayName,
                    createdAt = userInfo.createdAt
                )

                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Usuario nulo después del registro")
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            handleException(e)
        }
    }

    // Método para registro con callback (para compatibilidad)
    fun register(email: String, password: String, displayName: String, callback: (Boolean, String?) -> Unit) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val result = register(email, password, displayName)
            when (result) {
                is AuthResult.Success -> callback(true, null)
                is AuthResult.Error -> callback(false, result.message)
                is AuthResult.Loading -> {} // No aplicable aquí
            }
        }
    }

    // Cerrar sesión
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
            Log.d(TAG, "Usuario desconectado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            throw e
        }
    }

    // Cerrar sesión con callback (para compatibilidad)
    fun signOut(callback: ((Boolean, String?) -> Unit)? = null) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                signOut()
                callback?.invoke(true, null)
            } catch (e: Exception) {
                callback?.invoke(false, e.message)
            }
        }
    }

    // Obtener usuario actual como UserInfo (compatibilidad Firebase)
    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    // Obtener usuario actual como User (modelo personalizado)
    suspend fun getCurrentUserAsModel(): User? {
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
            Log.e(TAG, "Error al obtener usuario actual", e)
            null
        }
    }

    // Verificar si hay usuario logueado
    fun isLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    // Enviar email de recuperación de contraseña
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            AuthResult.Success(User()) // Usuario vacío para indicar éxito
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de recuperación", e)
            AuthResult.Error("Error al enviar email: ${e.message}")
        }
    }

    // Enviar email de recuperación con callback
    fun sendPasswordResetEmail(email: String, callback: (Boolean, String?) -> Unit) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val result = sendPasswordResetEmail(email)
            when (result) {
                is AuthResult.Success -> callback(true, "Email enviado correctamente")
                is AuthResult.Error -> callback(false, result.message)
                is AuthResult.Loading -> {} // No aplicable aquí
            }
        }
    }

    // Métodos privados de utilidad
    private fun handleException(e: Exception): AuthResult {
        val errorMessage = when {
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
                    return handleException(e)
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
}