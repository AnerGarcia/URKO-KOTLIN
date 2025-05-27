package com.azterketa.multimediaproyect.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.azterketa.multimediaproyect.R
import com.azterketa.multimediaproyect.databinding.ActivityMainBinding
import com.azterketa.multimediaproyect.ui.auth.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Verificar si hay usuario logueado
        if (!viewModel.isUserLoggedIn()) {
            goToLogin()
            return
        }

        setupToolbar()
        setupObservers()
        viewModel.loadCurrentUser()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Multimedia Project"
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            user?.let {
                binding.tvWelcome.text = if (!it.displayName.isNullOrEmpty()) {
                    "¡Bienvenido, ${it.displayName}!"
                } else {
                    "¡Bienvenido!"
                }
                binding.tvUserEmail.text = it.email
            }
        }

        viewModel.signOutResult.observe(this) { success ->
            if (success) {
                goToLogin()
            } else {
                Toast.makeText(this, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                lifecycleScope.launch {
                    viewModel.signOut()
                }
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