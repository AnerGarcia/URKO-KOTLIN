package com.example.azterketa.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.azterketa.R
import com.example.azterketa.databinding.ActivityRegisterBinding
import com.example.azterketa.utils.LanguageHelper
import com.example.azterketa.viewmodels.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar idioma
        LanguageHelper.setLocale(this, LanguageHelper.getCurrentLanguage(this))

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                val useSupabase = binding.switchAuthProvider.isChecked
                authViewModel.registerWithEmail(email, password, useSupabase)
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }

        binding.switchAuthProvider.setOnCheckedChangeListener { _, isChecked ->
            binding.tvAuthProvider.text = if (isChecked) "Supabase" else "Firebase"
        }
    }

    private fun observeViewModel() {
        authViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        authViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }

        authViewModel.authSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_required)
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_required)
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.error_password_short)
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            return false
        }

        return true
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}