package com.example.azterketa.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.azterketa.R
import com.example.azterketa.databinding.FragmentSettingsBinding
import com.example.azterketa.utils.LanguageHelper
import com.example.azterketa.utils.NotificationHelper
import com.example.azterketa.viewmodels.AuthViewModel
import com.example.azterketa.views.LoginActivity
import com.example.azterketa.views.MainActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationHelper = NotificationHelper(requireContext())
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Mostrar información del usuario
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserEmail.text = it.email
                binding.tvUserName.text = it.displayName.ifEmpty {
                    getString(R.string.no_name)
                }
            }
        }

        // Configurar selector de idioma
        setupLanguageSpinner()

        // Botón de logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Botón para limpiar caché
        binding.btnClearCache.setOnClickListener {
            showClearCacheConfirmation()
        }

        // Botón de información de la app
        binding.btnAppInfo.setOnClickListener {
            showAppInfo()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = LanguageHelper.getAvailableLanguages()
        val languageNames = languages.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        // Seleccionar idioma actual
        val currentLanguage = LanguageHelper.getCurrentLanguage(requireContext())
        val currentIndex = languages.indexOfFirst { it.first == currentLanguage }
        if (currentIndex != -1) {
            binding.spinnerLanguage.setSelection(currentIndex)
        }

        // Listener para cambios de idioma
        binding.spinnerLanguage.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position].first
                if (selectedLanguage != currentLanguage) {
                    LanguageHelper.setLocale(requireContext(), selectedLanguage)
                    notificationHelper.showNotification(
                        getString(R.string.language_changed),
                        getString(R.string.language_changed_message)
                    )
                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun observeViewModel() {
        authViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                navigateToLogin()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                authViewModel.logout()
                notificationHelper.showNotification(
                    getString(R.string.logout),
                    getString(R.string.logout_success)
                )
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showClearCacheConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_cache))
            .setMessage(getString(R.string.clear_cache_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Aquí podrías implementar la limpieza de caché
                notificationHelper.showNotification(
                    getString(R.string.cache_cleared),
                    getString(R.string.cache_cleared_message)
                )
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showAppInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.app_info))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}