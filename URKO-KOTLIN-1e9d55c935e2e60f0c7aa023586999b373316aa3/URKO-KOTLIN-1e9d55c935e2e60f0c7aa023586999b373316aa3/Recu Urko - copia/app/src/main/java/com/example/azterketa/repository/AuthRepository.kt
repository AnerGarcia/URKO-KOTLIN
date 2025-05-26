package com.example.azterketa.repository

import android.content.Context
import android.util.Log
import com.example.azterketa.R
import com.example.azterketa.core.SupabaseConfig
import com.example.azterketa.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val supabaseAuth = SupabaseConfig.client.auth

    companion object {
        private const val TAG = "AuthRepository"
    }

    // Google Sign In Configuration
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    // Firebase Authentication Methods
    suspend fun loginWithEmailFirebase(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting Firebase login for: $email")
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                Log.d(TAG, "Firebase login successful for: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Firebase user is null after login")
                Result.failure(Exception("Error en el login: Usuario no encontrado"))
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase login error: ${e.errorCode} - ${e.message}")
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Email inválido"
                "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
                "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Inténtalo más tarde"
                else -> "Error de autenticación: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase login unexpected error", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun registerWithEmailFirebase(email: String, password: String, displayName: String = ""): Result<User> {
        return try {
            Log.d(TAG, "Attempting Firebase registration for: $email")
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Actualizar el nombre de usuario si se proporciona
                if (displayName.isNotEmpty()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                }

                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName.ifEmpty { firebaseUser.displayName ?: "" },
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                Log.d(TAG, "Firebase registration successful for: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Firebase user is null after registration")
                Result.failure(Exception("Error en el registro: No se pudo crear el usuario"))
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase registration error: ${e.errorCode} - ${e.message}")
            val errorMessage = when (e.errorCode) {
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil"
                "ERROR_INVALID_EMAIL" -> "Email inválido"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este email ya está registrado"
                else -> "Error de registro: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration unexpected error", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun signInWithGoogleFirebase(idToken: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting Google Sign In with Firebase")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                Log.d(TAG, "Google Sign In with Firebase successful for: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Firebase user is null after Google Sign In")
                Result.failure(Exception("Error en el login con Google"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign In with Firebase error", e)
            Result.failure(Exception("Error en Google Sign In: ${e.message}"))
        }
    }

    // Supabase Authentication Methods
    suspend fun loginWithEmailSupabase(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting Supabase login for: $email")
            supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val supabaseUser = supabaseAuth.currentUserOrNull()
            if (supabaseUser != null) {
                val user = mapSupabaseUserToUser(supabaseUser)
                Log.d(TAG, "Supabase login successful for: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Supabase user is null after login")
                Result.failure(Exception("Error en el login: Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase login error", e)
            val errorMessage = when {
                e.message?.contains("Invalid login credentials") == true -> "Credenciales inválidas"
                e.message?.contains("Email not confirmed") == true -> "Email no confirmado"
                else -> "Error de login: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun registerWithEmailSupabase(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting Supabase registration for: $email")
            supabaseAuth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val supabaseUser = supabaseAuth.currentUserOrNull()
            if (supabaseUser != null) {
                val user = mapSupabaseUserToUser(supabaseUser)
                Log.d(TAG, "Supabase registration successful for: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Supabase user is null after registration")
                Result.failure(Exception("Error en el registro"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase registration error", e)
            val errorMessage = when {
                e.message?.contains("User already registered") == true -> "El usuario ya está registrado"
                e.message?.contains("Password should be at least 6 characters") == true -> "La contraseña debe tener al menos 6 caracteres"
                else -> "Error de registro: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Helper method to map Supabase UserInfo to our User model
    private fun mapSupabaseUserToUser(supabaseUser: UserInfo): User {
        return User(
            id = supabaseUser.id,
            email = supabaseUser.email ?: "",
            displayName = supabaseUser.userMetadata?.get("display_name")?.toString() ?: "",
            photoUrl = supabaseUser.userMetadata?.get("avatar_url")?.toString() ?: ""
        )
    }

    // Password Reset
    suspend fun resetPassword(email: String): Result<String> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success("Email de recuperación enviado")
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error", e)
            Result.failure(Exception("Error al enviar email de recuperación: ${e.message}"))
        }
    }

    // Logout methods
    fun logout() {
        try {
            Log.d(TAG, "Logging out user")
            firebaseAuth.signOut()
            googleSignInClient.signOut()
            // Note: Supabase logout would be handled differently
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
        }
    }

    // Current user methods
    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
        } else {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Email verification
    suspend fun sendEmailVerification(): Result<String> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                user.sendEmailVerification().await()
                Result.success("Email de verificación enviado")
            } else {
                Result.failure(Exception("Usuario no encontrado o email ya verificado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send email verification error", e)
            Result.failure(Exception("Error al enviar verificación: ${e.message}"))
        }
    }

    fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified ?: false
    }
}