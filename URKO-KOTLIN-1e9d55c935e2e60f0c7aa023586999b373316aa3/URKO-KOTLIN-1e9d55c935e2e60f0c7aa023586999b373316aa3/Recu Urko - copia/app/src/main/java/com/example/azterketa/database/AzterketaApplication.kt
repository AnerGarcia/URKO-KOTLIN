
package com.example.azterketa

import android.app.Application
import androidx.room.Room
import com.example.azterketa.database.AppDatabase

class AzterketaApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "personajes_database"
        ).build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AzterketaApplication
    }
}