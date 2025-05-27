package com.azterketa.multimediaproyect.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import com.azterketa.multimediaproyect.databinding.ActivityLoginBinding
import com.azterketa.multimediaproyect.ui.auth.register.RegisterActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya está logueado, ir a main
        if (authRepository.isLoggedIn()) {
            goToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Botón de login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            lifecycleScope.launch {
                val success = authRepository.login(email, password)
                showLoading(false)

                if (success) {
                    Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Error en login", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Link para registro
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Iniciando..." else "Iniciar Sesión"
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}