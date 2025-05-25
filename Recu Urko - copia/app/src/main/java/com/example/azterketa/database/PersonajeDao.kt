
package com.example.azterketa.database

import androidx.lifecycle.LiveData
import androidx.room.*
import retrofit2.http.Query

@Dao
interface PersonajeDao {
    @Query("SELECT * FROM personajes")
    fun getAllPersonajes(): LiveData<List<PersonajeEntity>>

    @Query("SELECT * FROM personajes WHERE esFavorito = 1")
    fun getFavoritos(): LiveData<List<PersonajeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonaje(personaje: PersonajeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonajes(personajes: List<PersonajeEntity>)

    @Update
    suspend fun updatePersonaje(personaje: PersonajeEntity)

    @Delete
    suspend fun deletePersonaje(personaje: PersonajeEntity)

    @Query("DELETE FROM personajes")
    suspend fun deleteAll()
}

