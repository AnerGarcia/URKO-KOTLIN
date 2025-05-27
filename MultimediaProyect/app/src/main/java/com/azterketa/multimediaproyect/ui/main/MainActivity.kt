package com.azterketa.multimediaproyect.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.azterketa.multimediaproyect.R
import com.azterketa.multimediaproyect.databinding.ActivityMainBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Multimedia Project"
    }

    private fun setupUI() {
        // Configurar listeners de botones
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshUser()
        }
    }

    private fun observeViewModel() {
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRefresh.isEnabled = !isLoading
        }

        // Observar usuario actual
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                // Usuario autenticado - mostrar información
                showUserInfo(user)
            } else {
                // Usuario no autenticado - redirigir a login
                redirectToLogin()
            }
        }
    }

    private fun showUserInfo(user: com.azterketa.multimediaproyect.data.model.User) {
        with(binding) {
            // Mostrar información del usuario
            tvWelcome.text = "¡Bienvenido!"
            tvUserName.text = user.displayName ?: "Usuario"
            tvUserEmail.text = user.email

            // Cargar foto de perfil si existe
            if (!user.photoUrl.isNullOrEmpty()) {
                Glide.with(this@MainActivity)
                    .load(user.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfilePhoto)
            } else {
                ivProfilePhoto.setImageResource(R.drawable.ic_person)
            }

            // Mostrar contenido principal
            layoutUserInfo.visibility = View.VISIBLE
            layoutContent.visibility = View.VISIBLE
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                viewModel.signOut()
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}