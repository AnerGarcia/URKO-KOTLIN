package com.example.azterketa.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
            account.idToken?.let { authViewModel.signInWithGoogle(it) }
        } catch (e: ApiException) {
            Toast.makeText(
                this,
                getString(R.string.error_google_signin, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Idioma
        LanguageHelper.setLocale(this, LanguageHelper.getCurrentLanguage(this))

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Saltar si ya logueado
        if (authViewModel.isUserLoggedIn()) {
            goToMain()
            return
        }

        setupLanguageSpinner()
        setupAuthProviderSwitch()
        setupButtons()
        observeViewModel()
    }

    private fun setupLanguageSpinner() {
        val langs = LanguageHelper.getAvailableLanguages()
        val names = langs.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter
        val current = LanguageHelper.getCurrentLanguage(this)
        binding.spinnerLanguage.setSelection(langs.indexOfFirst { it.first == current })
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val sel = langs[pos].first
                if (sel != LanguageHelper.getCurrentLanguage(this@LoginActivity)) {
                    LanguageHelper.setLocale(this@LoginActivity, sel)
                    recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun setupAuthProviderSwitch() {
        binding.switchAuthProvider.setOnCheckedChangeListener { _, isSupabase ->
            binding.tvAuthProvider.text = if (isSupabase) "Supabase" else "Firebase"
        }
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()
            if (!validateInput(email, pass)) return@setOnClickListener
            authViewModel.loginWithEmail(email, pass, binding.switchAuthProvider.isChecked)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnGoogleSignIn.setOnClickListener {
            googleSignInLauncher.launch(authViewModel.googleSignInClient.signInIntent)
        }

        binding.tvForgotPassword.setOnClickListener {
            // Implementa reset de contraseña si quieres
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        authViewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
            binding.btnGoogleSignIn.isEnabled = !loading
        }

        authViewModel.error.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }

        authViewModel.authSuccess.observe(this) { success ->
            if (success) goToMain()
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        var ok = true
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_required)
            ok = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            ok = false
        }
        if (pass.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_required)
            ok = false
        } else if (pass.length < 6) {
            binding.etPassword.error = getString(R.string.error_password_short)
            ok = false
        }
        return ok
    }

    private fun goToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }
}
