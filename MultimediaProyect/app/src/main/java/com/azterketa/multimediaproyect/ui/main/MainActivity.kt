package com.azterketa.multimediaproyect.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.azterketa.multimediaproyect.R
import com.azterketa.multimediaproyect.data.repository.AuthRepository
import com.azterketa.multimediaproyect.databinding.ActivityMainBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si hay usuario logueado
        if (!authRepository.isLoggedIn()) {
            goToLogin()
            return
        }

        setupToolbar()
        loadUserInfo()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Multimedia App"
    }

    private fun loadUserInfo() {
        val userName = authRepository.getCurrentUserName()
        val userEmail = authRepository.getCurrentUserEmail()

        binding.tvWelcome.text = "¡Bienvenido, $userName!"
        binding.tvUserEmail.text = userEmail

        // Mostrar layouts
        binding.layoutUserInfo.visibility = android.view.View.VISIBLE
        binding.layoutContent.visibility = android.view.View.VISIBLE
    }

    private fun setupListeners() {
        // Botón refresh
        binding.btnRefresh.setOnClickListener {
            loadUserInfo()
            Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            authRepository.signOut()
            Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}