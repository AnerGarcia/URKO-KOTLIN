package com.azterketa.multimediaproyect.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.azterketa.multimediaproyect.R
import com.azterketa.multimediaproyect.auth.AuthManager
import com.azterketa.multimediaproyect.databinding.ActivityMainBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authManager = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si hay usuario logueado
        if (!authManager.isLoggedIn()) {
            goToLogin()
            return
        }

        setupToolbar()
        showUserInfo()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Multimedia Project"
    }

    private fun showUserInfo() {
        val user = authManager.getCurrentUser()
        user?.let {
            binding.tvWelcome.text = "¡Bienvenido!"
            binding.tvUserEmail.text = it.email ?: "Sin email"
            // Mostrar más info si es necesario
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                authManager.signOut()
                goToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}