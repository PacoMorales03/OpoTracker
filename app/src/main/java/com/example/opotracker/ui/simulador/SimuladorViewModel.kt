package com.example.opotracker.ui.simulador

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabase
import com.example.opotracker.data.OpoRepository
import com.example.opotracker.data.SimulacionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SimFase { INACTIVO, SELECCIONANDO, LISTO, CORRIENDO, PAUSADO, FINALIZANDO }

class SimuladorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OpoRepository(AppDatabase.getInstance(application))

    val historial: StateFlow<List<SimulacionEntity>> = repository.simulaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var fase by mutableStateOf(SimFase.INACTIVO)
        private set
    var opciones by mutableStateOf<List<Int>>(emptyList())
        private set
    var temaActual by mutableStateOf<Int?>(null)
        private set
    var elapsedSeconds by mutableStateOf(0L)
        private set

    private var tickerJob: Job? = null

    /** Sortea las 3 bolas, como el día del examen; el opositor elige después cuál desarrollar. */
    fun iniciarSimulacion() {
        if (fase != SimFase.INACTIVO) return
        viewModelScope.launch {
            opciones = repository.sortearBolas()
            fase = SimFase.SELECCIONANDO
        }
    }

    /** Elige un tema directamente, sin pasar por el sorteo de bolas. No afecta a la bolsa de "sin repetición". */
    fun seleccionarTemaManual(tema: Int) {
        if (fase != SimFase.INACTIVO) return
        temaActual = tema
        elapsedSeconds = 0L
        fase = SimFase.LISTO
    }

    fun seleccionarTema(tema: Int) {
        if (fase != SimFase.SELECCIONANDO) return
        val descartadas = opciones.filter { it != tema }
        viewModelScope.launch {
            repository.devolverAlBolsa(descartadas)
            opciones = emptyList()
            temaActual = tema
            elapsedSeconds = 0L
            fase = SimFase.LISTO
        }
    }

    /** Cancela la selección tras el sorteo: las 3 bolas vuelven a la bolsa y se vuelve a la pantalla inicial. */
    fun cancelarSeleccion() {
        if (fase != SimFase.SELECCIONANDO) return
        val paraDevolver = opciones
        viewModelScope.launch {
            repository.devolverAlBolsa(paraDevolver)
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

    /**
     * Cancela el simulacro ya en marcha (elegido a mano o por sorteo) sin guardar nada.
     * Si el tema venía del sorteo, vuelve a la bolsa; si venía de la selección manual,
     * [OpoRepository.devolverAlBolsa] no hace nada porque nunca se sacó de ahí.
     */
    fun cancelarEnCurso() {
        if (fase != SimFase.LISTO && fase != SimFase.CORRIENDO && fase != SimFase.PAUSADO) return
        tickerJob?.cancel()
        val tema = temaActual
        viewModelScope.launch {
            if (tema != null) repository.devolverAlBolsa(listOf(tema))
            temaActual = null
            elapsedSeconds = 0L
            fase = SimFase.INACTIVO
        }
    }

    fun cancelarFinalizacion() {
        if (fase != SimFase.FINALIZANDO) return
        fase = SimFase.PAUSADO
    }

    fun guardarResultado(sensacion: Float, observaciones: String) {
        val tema = temaActual ?: return
        val duracion = elapsedSeconds
        viewModelScope.launch {
            repository.saveSimulacion(
                SimulacionEntity(
                    temaNumero = tema,
                    fechaMillis = System.currentTimeMillis(),
                    duracionSegundos = duracion,
                    sensacion = sensacion,
                    observaciones = observaciones,
                ),
            )
            temaActual = null
            elapsedSeconds = 0L
            fase = SimFase.INACTIVO
        }
    }

    fun editarSimulacion(simulacion: SimulacionEntity, sensacion: Float, observaciones: String) {
        viewModelScope.launch {
            repository.updateSimulacion(simulacion.copy(sensacion = sensacion, observaciones = observaciones))
        }
    }

    fun eliminarSimulacion(simulacion: SimulacionEntity) {
        viewModelScope.launch { repository.deleteSimulacion(simulacion) }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
