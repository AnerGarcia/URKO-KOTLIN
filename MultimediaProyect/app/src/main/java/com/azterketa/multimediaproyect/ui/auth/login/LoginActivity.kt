package com.azterketa.multimediaproyect.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azterketa.multimediaproyect.auth.AuthManager
import com.azterketa.multimediaproyect.databinding.ActivityLoginBinding
import com.azterketa.multimediaproyect.ui.auth.register.RegisterActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authManager = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya está logueado, ir a main
        if (authManager.isLoggedIn()) {
            goToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            authManager.login(email, password) { success, error ->
                showLoading(false)
                if (success) {
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    val errorMessage = when {
                        error?.contains("Invalid login credentials", ignoreCase = true) == true ->
                            "Email o contraseña incorrectos"
                        error?.contains("Invalid email", ignoreCase = true) == true ->
                            "Email inválido"
                        else -> "Error: $error"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Iniciando..." else "Iniciar Sesión"
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}