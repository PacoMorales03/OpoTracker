package com.example.opotracker.ui.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.data.AppDatabase
import com.example.opotracker.data.OpoRepository
import com.example.opotracker.data.TemaEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OpoRepository(AppDatabase.getInstance(application))

    val temas: StateFlow<List<TemaEntity>> = repository.temas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    fun onTemaChanged(tema: TemaEntity) {
        viewModelScope.launch { repository.updateTema(tema) }
    }
}
