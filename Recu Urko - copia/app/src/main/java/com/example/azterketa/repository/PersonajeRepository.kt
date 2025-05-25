
package com.example.azterketa.repository

import androidx.lifecycle.LiveData
import com.example.azterketa.AzterketaApplication
import com.example.azterketa.core.RetrofitClient
import com.example.azterketa.database.PersonajeEntity
import com.example.azterketa.models.Personaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonajeRepository {

    private val personajeDao = AzterketaApplication.instance.database.personajeDao()
    private val webService = RetrofitClient.webService

    fun getAllPersonajes(): LiveData<List<PersonajeEntity>> {
        return personajeDao.getAllPersonajes()
    }

    fun getFavoritos(): LiveData<List<PersonajeEntity>> {
        return personajeDao.getFavoritos()
    }

    suspend fun refreshPersonajes() {
        withContext(Dispatchers.IO) {
            try {
                val response = webService.obtenerPersonajes()
                if (response.isSuccessful) {
                    response.body()?.let { personajes ->
                        val entities = personajes.map { personaje ->
                            PersonajeEntity(
                                frase = personaje.frase,
                                personaje = personaje.personaje,
                                imagen = personaje.imagen,
                                direccionPersonaje = personaje.direccionPersonaje
                            )
                        }
                        personajeDao.deleteAll()
                        personajeDao.insertPersonajes(entities)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun buscarPersonaje(nombre: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = webService.obtenerPersonaje(nombre)
                if (response.isSuccessful) {
                    response.body()?.let { personajes ->
                        val entities = personajes.map { personaje ->
                            PersonajeEntity(
                                frase = personaje.frase,
                                personaje = personaje.personaje,
                                imagen = personaje.imagen,
                                direccionPersonaje = personaje.direccionPersonaje
                            )
                        }
                        personajeDao.deleteAll()
                        personajeDao.insertPersonajes(entities)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun toggleFavorito(personaje: PersonajeEntity) {
        withContext(Dispatchers.IO) {
            personajeDao.updatePersonaje(
                personaje.copy(esFavorito = !personaje.esFavorito)
            )
        }
    }
}