
package com.example.azterketa.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personajes")
data class PersonajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val frase: String,
    val personaje: String,
    val imagen: String,
    val direccionPersonaje: String,
    val esFavorito: Boolean = false
)
