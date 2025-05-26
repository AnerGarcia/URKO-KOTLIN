package com.example.azterketa.repository

import android.content.Context
import com.example.azterketa.R
import com.example.azterketa.core.SupabaseConfig
import com.example.azterketa.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val supabaseAuth = SupabaseConfig.client.auth

    // Google Sign In
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    // Firebase Authentication
    suspend fun loginWithEmailFirebase(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Error en el login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmailFirebase(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Error en el registro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleFirebase(idToken: String): Result<User> {
        return try {
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
                Result.success(user)
            } else {
                Result.failure(Exception("Error en el login con Google"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Supabase Authentication
    suspend fun loginWithEmailSupabase(email: String, password: String): Result<User> {
        return try {
            supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val supabaseUser = supabaseAuth.currentUserOrNull()
            if (supabaseUser != null) {
                val user = User(
                    id = supabaseUser.id,
                    email = supabaseUser.email ?: "",
                    displayName = supabaseUser.userMetadata?.get("display_name")?.toString() ?: "",
                    photoUrl = supabaseUser.userMetadata?.get("avatar_url")?.toString() ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Error en el login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmailSupabase(email: String, password: String): Result<User> {
        return try {
            supabaseAuth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val supabaseUser = supabaseAuth.currentUserOrNull()
            if (supabaseUser != null) {
                val user = User(
                    id = supabaseUser.id,
                    email = supabaseUser.email ?: "",
                    displayName = supabaseUser.userMetadata?.get("display_name")?.toString() ?: "",
                    photoUrl = supabaseUser.userMetadata?.get("avatar_url")?.toString() ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Error en el registro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }

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
}