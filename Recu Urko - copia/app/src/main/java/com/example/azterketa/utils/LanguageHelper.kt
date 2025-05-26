package com.example.azterketa.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LanguageHelper {

    private const val PREF_LANGUAGE = "app_language"

    fun setLocale(context: Context, language: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, language).apply()

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, "es") ?: "es"
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "es" to "Español",
            "en" to "English",
            "eu" to "Euskera"
        )
    }
}