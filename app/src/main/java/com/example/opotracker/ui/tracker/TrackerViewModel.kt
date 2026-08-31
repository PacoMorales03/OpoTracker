package com.example.opotracker.ui.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabase
import com.example.opotracker.data.OpoRepository
import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.data.TemaEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OpoRepository(AppDatabase.getInstance(application))

    val temas: StateFlow<List<TemaEntity>> = repository.temas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val simulaciones: StateFlow<List<SimulacionEntity>> = repository.simulaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _insigniaGanada = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val insigniaGanada: SharedFlow<String> = _insigniaGanada

    private val insigniasVistas = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            insigniasVistas += repository.insigniasVistas()

            combine(temas, simulaciones) { t, s -> t to s }.collect { (t, s) ->
                for (insignia in INSIGNIAS) {
                    if (insignia.id !in insigniasVistas && insignia.conseguida(t, s)) {
                        insigniasVistas += insignia.id
                        repository.marcarInsigniaVista(insignia.id)
                        _insigniaGanada.emit(insignia.titulo)
                    }
                }
            }
        }
    }

    fun onTemaChanged(tema: TemaEntity) {
        viewModelScope.launch { repository.updateTema(tema) }
    }
}
