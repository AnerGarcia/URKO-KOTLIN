package com.azterketa.multimediaproyect.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.databinding.ActivityLoginBinding
import com.azterketa.multimediaproyect.ui.auth.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si ya hay un usuario logueado
        if (FirebaseAuth.getInstance().currentUser != null) {
            navigateToMain()
            return
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    showLoading(true)
                }
                is AuthResult.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.emailError.observe(this) { error ->
            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.tilPassword.error = error
        }

        viewModel.resetPasswordResult.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                // Limpiar el mensaje después de mostrarlo
                viewModel.clearErrors()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.resetPassword(email)
            } else {
                Toast.makeText(this, "Ingresa tu email para recuperar la contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Limpiar errores cuando el usuario empiece a escribir
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearErrors()
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearErrors()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Iniciando sesión..." else "Iniciar Sesión"
        binding.tvRegister.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading

        // Deshabilitar campos durante la carga
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar errores al destruir la actividad
        viewModel.clearErrors()
    }
}