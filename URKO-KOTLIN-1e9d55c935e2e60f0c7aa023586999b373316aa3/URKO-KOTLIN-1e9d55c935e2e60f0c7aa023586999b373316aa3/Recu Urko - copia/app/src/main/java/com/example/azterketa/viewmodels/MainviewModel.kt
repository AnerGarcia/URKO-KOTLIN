package com.example.azterketa.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azterketa.database.PersonajeEntity
import com.example.azterketa.repository.PersonajeRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PersonajeRepository()

    val personajes: LiveData<List<PersonajeEntity>> = repository.getAllPersonajes()
    val favoritos: LiveData<List<PersonajeEntity>> = repository.getFavoritos()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        obtenerPersonajes()
    }

    fun obtenerPersonajes() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.refreshPersonajes()
            } catch (e: Exception) {
                _error.value = "Error al cargar personajes: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun buscarPersonaje(nombre: String) {
        if (nombre.isBlank()) {
            obtenerPersonajes()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.buscarPersonaje(nombre.trim())
            } catch (e: Exception) {
                _error.value = "Error al buscar personaje: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleFavorito(personaje: PersonajeEntity) {
        viewModelScope.launch {
            try {
                repository.toggleFavorito(personaje)
            } catch (e: Exception) {
                _error.value = "Error al actualizar favorito: ${e.message}"
            }
        }
    }

    fun refreshFavoritos() {
        // Este método es llamado desde FavoritosFragment
        // Los favoritos se actualizan automáticamente a través del LiveData
        // No necesita implementación adicional ya que Room maneja la observación automáticamente
    }

    fun clearError() {
        _error.value = null
    }
}