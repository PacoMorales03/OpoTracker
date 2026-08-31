package com.example.opotracker.ui.tracker

import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.data.TOTAL_ITEMS_POR_TEMA
import com.example.opotracker.data.TemaEntity
import com.example.opotracker.data.completados

data class Insignia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val conseguida: (temas: List<TemaEntity>, simulaciones: List<SimulacionEntity>) -> Boolean,
)

private fun temasCompletos(temas: List<TemaEntity>, minimo: Int): Boolean =
    temas.count { it.completados() == TOTAL_ITEMS_POR_TEMA } >= minimo

val INSIGNIAS: List<Insignia> = listOf(
    Insignia(
        id = "primer_paso",
        titulo = "Primer paso",
        descripcion = "Marca tu primera casilla",
        conseguida = { temas, _ -> temas.any { it.completados() > 0 } },
    ),
    Insignia(
        id = "tema_dominado",
        titulo = "Tema dominado",
        descripcion = "Completa un tema entero",
        conseguida = { temas, _ -> temasCompletos(temas, 1) },
    ),
    Insignia(
        id = "cuarto_temario",
        titulo = "Un cuarto del temario",
        descripcion = "Completa 7 temas enteros",
        conseguida = { temas, _ -> temasCompletos(temas, 7) },
    ),
    Insignia(
        id = "mitad_temario",
        titulo = "La mitad del temario",
        descripcion = "Completa 13 temas enteros",
        conseguida = { temas, _ -> temasCompletos(temas, 13) },
    ),
    Insignia(
        id = "temario_completo",
        titulo = "¡Temario completo!",
        descripcion = "Completa los 25 temas",
        conseguida = { temas, _ -> temasCompletos(temas, 25) },
    ),
    Insignia(
        id = "primer_simulacro",
        titulo = "Primer simulacro",
        descripcion = "Termina tu primer simulacro",
        conseguida = { _, sims -> sims.isNotEmpty() },
    ),
    Insignia(
        id = "diez_simulacros",
        titulo = "10 simulacros",
        descripcion = "Termina 10 simulacros",
        conseguida = { _, sims -> sims.size >= 10 },
    ),
    Insignia(
        id = "veinticinco_simulacros",
        titulo = "25 simulacros",
        descripcion = "Termina 25 simulacros",
        conseguida = { _, sims -> sims.size >= 25 },
    ),
    Insignia(
        id = "repasando_fuerte",
        titulo = "Repasando fuerte",
        descripcion = "Acumula 25 repasos en total",
        conseguida = { temas, _ -> temas.sumOf { it.repasos } >= 25 },
    ),
    Insignia(
        id = "maquina_repasos",
        titulo = "Máquina de repasos",
        descripcion = "Acumula 100 repasos en total",
        conseguida = { temas, _ -> temas.sumOf { it.repasos } >= 100 },
    ),
)
