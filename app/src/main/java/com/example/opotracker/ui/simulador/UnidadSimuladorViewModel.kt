package com.example.opotracker.ui.simulador

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabase
import com.example.opotracker.data.SimulacroUdEntity
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.data.UnidadDidacticaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UnidadSimuladorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnidadDidacticaRepository(AppDatabase.getInstance(application))

    val unidades: StateFlow<List<UnidadDidacticaEntity>> = repository.unidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historial: StateFlow<List<SimulacroUdEntity>> = repository.simulacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var fase by mutableStateOf(SimFase.INACTIVO)
        private set
    var opciones by mutableStateOf<List<Long>>(emptyList())
        private set
    var unidadActualId by mutableStateOf<Long?>(null)
        private set
    var elapsedSeconds by mutableStateOf(0L)
        private set

    private var tickerJob: Job? = null

    fun nombreDe(id: Long): String = unidades.value.find { it.id == id }?.nombre ?: "?"

    /** Sortea hasta 3 unidades entre las marcadas; el opositor elige después cuál desarrollar. */
    fun iniciarSimulacion() {
        if (fase != SimFase.INACTIVO || unidades.value.none { it.enSimulacro }) return
        viewModelScope.launch {
            opciones = repository.sortearBolasUd()
            fase = SimFase.SELECCIONANDO
        }
    }

    /** Elige una unidad directamente, sin pasar por el sorteo. No afecta a la bolsa de "sin repetición". */
    fun seleccionarUnidadManual(id: Long) {
        if (fase != SimFase.INACTIVO) return
        unidadActualId = id
        elapsedSeconds = 0L
        fase = SimFase.LISTO
    }

    fun seleccionarUnidad(id: Long) {
        if (fase != SimFase.SELECCIONANDO) return
        val descartadas = opciones.filter { it != id }
        viewModelScope.launch {
            repository.devolverAlBolsaUd(descartadas)
            opciones = emptyList()
            unidadActualId = id
            elapsedSeconds = 0L
            fase = SimFase.LISTO
        }
    }

    fun cancelarSeleccion() {
        if (fase != SimFase.SELECCIONANDO) return
        val paraDevolver = opciones
        viewModelScope.launch {
            repository.devolverAlBolsaUd(paraDevolver)
            opciones = emptyList()
            fase = SimFase.INACTIVO
        }
    }

    fun iniciarCronometro() {
        if (fase != SimFase.LISTO && fase != SimFase.PAUSADO) return
        fase = SimFase.CORRIENDO
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                elapsedSeconds++
            }
        }
    }

    fun pausar() {
        if (fase != SimFase.CORRIENDO) return
        tickerJob?.cancel()
        fase = SimFase.PAUSADO
    }

    fun finalizar() {
        if (fase != SimFase.CORRIENDO && fase != SimFase.PAUSADO) return
        tickerJob?.cancel()
        fase = SimFase.FINALIZANDO
    }

    fun cancelarEnCurso() {
        if (fase != SimFase.LISTO && fase != SimFase.CORRIENDO && fase != SimFase.PAUSADO) return
        tickerJob?.cancel()
        val id = unidadActualId
        viewModelScope.launch {
            if (id != null) repository.devolverAlBolsaUd(listOf(id))
            unidadActualId = null
            elapsedSeconds = 0L
            fase = SimFase.INACTIVO
        }
    }

    fun cancelarFinalizacion() {
        if (fase != SimFase.FINALIZANDO) return
        fase = SimFase.PAUSADO
    }

    fun guardarResultado(sensacion: Float, comentarios: String) {
        val id = unidadActualId ?: return
        val nombre = nombreDe(id)
        val duracion = elapsedSeconds
        viewModelScope.launch {
            repository.guardarSimulacro(
                SimulacroUdEntity(
                    unidadId = id,
                    unidadNombre = nombre,
                    fechaMillis = System.currentTimeMillis(),
                    duracionSegundos = duracion,
                    sensacion = sensacion,
                    comentarios = comentarios,
                ),
            )
            unidadActualId = null
            elapsedSeconds = 0L
            fase = SimFase.INACTIVO
        }
    }

    /** Registra un simulacro hecho fuera de la app (sin usar el cronómetro), con el tiempo puesto a mano. */
    fun crearSimulacroManual(unidadId: Long, duracionSegundos: Long, sensacion: Float, comentarios: String) {
        val nombre = nombreDe(unidadId)
        viewModelScope.launch {
            repository.guardarSimulacro(
                SimulacroUdEntity(
                    unidadId = unidadId,
                    unidadNombre = nombre,
                    fechaMillis = System.currentTimeMillis(),
                    duracionSegundos = duracionSegundos,
                    sensacion = sensacion,
                    comentarios = comentarios,
                ),
            )
        }
    }

    fun editarSimulacro(simulacro: SimulacroUdEntity, duracionSegundos: Long, sensacion: Float, comentarios: String) {
        viewModelScope.launch {
            repository.actualizarSimulacro(
                simulacro.copy(
                    duracionSegundos = duracionSegundos,
                    sensacion = sensacion,
                    comentarios = comentarios,
                ),
            )
        }
    }

    fun eliminarSimulacro(simulacro: SimulacroUdEntity) {
        viewModelScope.launch { repository.eliminarSimulacro(simulacro) }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
