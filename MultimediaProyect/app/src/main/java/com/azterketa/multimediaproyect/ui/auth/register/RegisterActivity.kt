package com.azterketa.multimediaproyect.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.azterketa.multimediaproyect.R
import com.azterketa.multimediaproyect.data.model.AuthResult
import com.azterketa.multimediaproyect.databinding.ActivityRegisterBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import com.azterketa.multimediaproyect.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
        observeViewModel()
        setupBackPressedHandler()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Crear Cuenta"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            handleBackNavigation()
        }
    }

    private fun setupUI() {
        with(binding) {
            // Configurar listeners de botones
            btnRegister.setOnClickListener {
                if (validateNetworkAndProceed()) {
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString()
                    val confirmPassword = etConfirmPassword.text.toString()
                    val name = etName.text.toString().trim()

                    viewModel.register(email, password, confirmPassword, name)
                }
            }

            tvLoginLink.setOnClickListener {
                handleBackNavigation()
            }

            // Configurar visibilidad de contraseñas
            setupPasswordVisibilityToggles()

            // Limpiar errores al escribir con debounce
            etName.addTextChangedListener {
                tilName.error = null
                if (it?.length ?: 0 >= 2) viewModel.clearNameError()
            }

            etEmail.addTextChangedListener {
                tilEmail.error = null
                // Validación en tiempo real del email
                val email = it.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.clearEmailError()
                }
            }

            etPassword.addTextChangedListener {
                tilPassword.error = null
                // Mostrar fortaleza de contraseña
                updatePasswordStrength(it.toString())
                if (it?.length ?: 0 >= 6) viewModel.clearPasswordError()
            }

            etConfirmPassword.addTextChangedListener {
                tilConfirmPassword.error = null
                // Validar coincidencia en tiempo real
                val password = etPassword.text.toString()
                val confirmPassword = it.toString()
                if (confirmPassword.isNotEmpty() && password == confirmPassword) {
                    viewModel.clearConfirmPasswordError()
                }
            }

            // Mejorar UX con focus
            setupFocusListeners()
        }
    }

    private fun setupPasswordVisibilityToggles() {
        with(binding) {
            // Toggle para contraseña principal
            tilPassword.setEndIconOnClickListener {
                isPasswordVisible = !isPasswordVisible
                if (isPasswordVisible) {
                    etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
                } else {
                    etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    tilPassword.setEndIconDrawable(R.drawable.ic_visibility)
                }
                etPassword.setSelection(etPassword.text?.length ?: 0)
            }

            // Toggle para confirmar contraseña
            tilConfirmPassword.setEndIconOnClickListener {
                isConfirmPasswordVisible = !isConfirmPasswordVisible
                if (isConfirmPasswordVisible) {
                    etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    tilConfirmPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
                } else {
                    etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    tilConfirmPassword.setEndIconDrawable(R.drawable.ic_visibility)
                }
                etConfirmPassword.setSelection(etConfirmPassword.text?.length ?: 0)
            }
        }
    }

    private fun setupFocusListeners() {
        with(binding) {
            etName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // Validar nombre al perder el foco
                    val name = etName.text.toString().trim()
                    if (name.isNotEmpty() && name.length < 2) {
                        tilName.error = "El nombre debe tener al menos 2 caracteres"
                    }
                }
            }

            etEmail.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // Validar email al perder el foco
                    val email = etEmail.text.toString().trim()
                    if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        tilEmail.error = "Email inválido"
                    }
                }
            }

            etPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    tvPasswordHint.visibility = View.VISIBLE
                } else {
                    tvPasswordHint.visibility = View.GONE
                }
            }
        }
    }

    private fun updatePasswordStrength(password: String) {
        with(binding) {
            when {
                password.isEmpty() -> {
                    tvPasswordStrength.visibility = View.GONE
                }
                password.length < 6 -> {
                    tvPasswordStrength.visibility = View.VISIBLE
                    tvPasswordStrength.text = "Contraseña débil"
                    tvPasswordStrength.setTextColor(getColor(R.color.error))
                }
                password.length < 8 -> {
                    tvPasswordStrength.visibility = View.VISIBLE
                    tvPasswordStrength.text = "Contraseña regular"
                    tvPasswordStrength.setTextColor(getColor(R.color.warning))
                }
                password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&].*$")) -> {
                    tvPasswordStrength.visibility = View.VISIBLE
                    tvPasswordStrength.text = "Contraseña fuerte"
                    tvPasswordStrength.setTextColor(getColor(R.color.success))
                }
                else -> {
                    tvPasswordStrength.visibility = View.VISIBLE
                    tvPasswordStrength.text = "Contraseña buena"
                    tvPasswordStrength.setTextColor(getColor(R.color.primary))
                }
            }
        }
    }

    private fun validateNetworkAndProceed(): Boolean {
        if (!viewModel.isNetworkAvailable()) {
            showNetworkErrorDialog()
            return false
        }
        return true
    }

    private fun showNetworkErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sin conexión")
            .setMessage("Se requiere conexión a internet para crear una cuenta. Por favor, verifica tu conexión.")
            .setPositiveButton("Reintentar") { _, _ ->
                // El usuario puede intentar de nuevo
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
                    showSuccessDialog()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    handleRegistrationError(result.message)
                }
            }
        }

        // Observar errores de validación individuales
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

        // Observar estado de red
        viewModel.networkError.observe(this) { hasNetworkError ->
            if (hasNetworkError) {
                showNetworkErrorDialog()
            }
        }
    }

    private fun handleRegistrationError(message: String) {
        when {
            message.contains("email", ignoreCase = true) -> {
                binding.etEmail.requestFocus()
            }
            message.contains("contraseña", ignoreCase = true) -> {
                binding.etPassword.requestFocus()
            }
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("¡Registro exitoso!")
            .setMessage("Tu cuenta ha sido creada correctamente. ¡Bienvenido!")
            .setPositiveButton("Continuar") { _, _ ->
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            // Mostrar/ocultar progress
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Actualizar botón
            btnRegister.isEnabled = !isLoading
            btnRegister.text = if (isLoading) "Creando cuenta..." else "Crear Cuenta"

            // Deshabilitar todos los campos durante carga
            etName.isEnabled = !isLoading
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            etConfirmPassword.isEnabled = !isLoading
            tvLoginLink.isEnabled = !isLoading

            // Deshabilitar toggles de visibilidad
            tilPassword.isEndIconVisible = !isLoading
            tilConfirmPassword.isEndIconVisible = !isLoading
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })
    }

    private fun handleBackNavigation() {
        if (viewModel.hasUnsavedData()) {
            AlertDialog.Builder(this)
                .setTitle("¿Salir del registro?")
                .setMessage("Los datos ingresados se perderán. ¿Estás seguro?")
                .setPositiveButton("Salir") { _, _ ->
                    navigateToLogin()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearErrors()
    }

    override fun onSupportNavigateUp(): Boolean {
        handleBackNavigation()
        return true
    }
}