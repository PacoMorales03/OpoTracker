package com.example.opotracker.ui.unidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabase
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.data.UnidadDidacticaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnidadDidacticaRepository(AppDatabase.getInstance(application))

    val unidades: StateFlow<List<UnidadDidacticaEntity>> = repository.unidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun crear(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { repository.crearUnidad(limpio) }
    }

    fun actualizar(unidad: UnidadDidacticaEntity) {
        viewModelScope.launch { repository.actualizarUnidad(unidad) }
    }

    fun eliminar(unidad: UnidadDidacticaEntity) {
        viewModelScope.launch { repository.eliminarUnidad(unidad) }
    }
}
