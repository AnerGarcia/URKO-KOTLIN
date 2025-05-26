package com.example.azterketa.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.azterketa.R
import com.example.azterketa.databinding.ActivityLoginBinding
import com.example.azterketa.utils.LanguageHelper
import com.example.azterketa.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                authViewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Error en Google Sign In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar idioma
        LanguageHelper.setLocale(this, LanguageHelper.getCurrentLanguage(this))

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (authViewModel.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                val useSupabase = binding.switchAuthProvider.isChecked
                authViewModel.loginWithEmail(email, password, useSupabase)
            }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = authViewModel.googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.switchAuthProvider.setOnCheckedChangeListener { _, isChecked ->
            binding.tvAuthProvider.text = if (isChecked) "Supabase" else "Firebase"
        }

        // Selector de idioma
        binding.spinnerLanguage.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val languages = LanguageHelper.getAvailableLanguages()
                val selectedLanguage = languages[position].first
                if (selectedLanguage != LanguageHelper.getCurrentLanguage(this@LoginActivity)) {
                    LanguageHelper.setLocale(this@LoginActivity, selectedLanguage)
                    recreate()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        val languages = LanguageHelper.getAvailableLanguages()
        val languageNames = languages.map { it.second }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        // Seleccionar idioma actual
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        val currentIndex = languages.indexOfFirst { it.first == currentLanguage }
        if (currentIndex != -1) {
            binding.spinnerLanguage.setSelection(currentIndex)
        }
    }

    private fun observeViewModel() {
        authViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnGoogleSignIn.isEnabled = !isLoading
        }

        authViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }

        authViewModel.authSuccess.observe(this) { success ->
            if (success) {
                navigateToMain()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
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

        return true
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}