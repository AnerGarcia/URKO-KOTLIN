package com.azterketa.multimediaproyect.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import com.azterketa.multimediaproyect.databinding.ActivityRegisterBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Botón de registro
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val displayName = binding.etDisplayName.text.toString().trim()

            if (!validateInput(email, password, confirmPassword, displayName)) {
                return@setOnClickListener
            }

            showLoading(true)

            lifecycleScope.launch {
                val success = authRepository.register(email, password, displayName)
                showLoading(false)

                if (success) {
                    Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(this@RegisterActivity, "Error en registro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Link para login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String, displayName: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu email", Toast.LENGTH_SHORT).show()
                false
            }
            displayName.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "Registrando..." else "Registrarse"
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
        binding.etDisplayName.isEnabled = !isLoading
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}