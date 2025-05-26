package com.example.azterketa

import android.app.Application
import androidx.room.Room
import com.example.azterketa.database.AppDatabase
import com.example.azterketa.utils.LanguageHelper

class AzterketaApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "personajes_database"
        )
            .fallbackToDestructiveMigration() // Para desarrollo, en producción usar migraciones
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Configurar idioma de la aplicación
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        LanguageHelper.setLocale(this, currentLanguage)
    }

    companion object {
        lateinit var instance: AzterketaApplication
            private set
    }
}