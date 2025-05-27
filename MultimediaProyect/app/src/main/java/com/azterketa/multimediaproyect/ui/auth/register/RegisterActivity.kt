package com.azterketa.multimediaproyect.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.databinding.ActivityRegisterBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            // Configurar listeners de botones
            btnRegister.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()
                val name = etName.text.toString().trim()

                viewModel.register(email, password, confirmPassword, name)
            }

            tvLoginLink.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }

            // Limpiar errores al escribir
            etName.addTextChangedListener { viewModel.clearErrors() }
            etEmail.addTextChangedListener { viewModel.clearErrors() }
            etPassword.addTextChangedListener { viewModel.clearErrors() }
            etConfirmPassword.addTextChangedListener { viewModel.clearErrors() }
        }
    }

    private fun observeViewModel() {
        // Observar resultado de autenticación
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    showLoading(true)
                }
                is AuthResult.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observar errores de validación
        viewModel.nameError.observe(this) { error ->
            binding.tilName.error = error
        }

        viewModel.emailError.observe(this) { error ->
            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.tilPassword.error = error
        }

        viewModel.confirmPasswordError.observe(this) { error ->
            binding.tilConfirmPassword.error = error
        }
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            btnRegister.text = if (isLoading) "Registrando..." else "Registrarse"

            // Deshabilitar campos durante carga
            etName.isEnabled = !isLoading
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            etConfirmPassword.isEnabled = !isLoading
            tvLoginLink.isEnabled = !isLoading
        }
    }
}