package com.example.opotracker.ui.planificacion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabaseHolder
import com.example.opotracker.data.PlanificacionDiariaEntity
import com.example.opotracker.data.PlanificacionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlanificacionViewModel : ViewModel() {

    private val repository = PlanificacionRepository(AppDatabaseHolder.database)

    var fechaSeleccionada by mutableStateOf(LocalDate.now())
        private set
    var plan by mutableStateOf<PlanificacionDiariaEntity?>(null)
        private set

    val racha: StateFlow<Int> = repository.racha
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        cargarPlan()
    }

    private fun cargarPlan() {
        val fecha = fechaSeleccionada
        viewModelScope.launch {
            plan = repository.obtenerOCrear(fecha.toString())
        }
    }

    fun irDiaAnterior() {
        fechaSeleccionada = fechaSeleccionada.minusDays(1)
        cargarPlan()
    }

    fun irDiaSiguiente() {
        fechaSeleccionada = fechaSeleccionada.plusDays(1)
        cargarPlan()
    }

    fun irHoy() {
        if (fechaSeleccionada == LocalDate.now()) return
        fechaSeleccionada = LocalDate.now()
        cargarPlan()
    }

    fun irAFecha(fecha: LocalDate) {
        if (fecha == fechaSeleccionada) return
        fechaSeleccionada = fecha
        cargarPlan()
    }

    fun actualizar(nuevo: PlanificacionDiariaEntity) {
        plan = nuevo
        viewModelScope.launch { plan = repository.guardar(nuevo) }
    }
}
