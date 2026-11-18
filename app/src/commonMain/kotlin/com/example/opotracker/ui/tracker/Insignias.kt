package com.example.opotracker.ui.tracker

import com.example.opotracker.data.SimulacionEntity
import com.example.opotracker.data.SimulacroUdEntity
import com.example.opotracker.data.TOTAL_ITEMS_POR_TEMA
import com.example.opotracker.data.TOTAL_ITEMS_POR_UD
import com.example.opotracker.data.TemaEntity
import com.example.opotracker.data.UnidadDidacticaEntity
import com.example.opotracker.data.completadas
import com.example.opotracker.data.completados

/** Todo lo que puede hacer falta para decidir si una insignia está conseguida. */
data class ContextoInsignias(
    val temas: List<TemaEntity>,
    val simulaciones: List<SimulacionEntity>,
    val unidades: List<UnidadDidacticaEntity>,
    val simulacrosUd: List<SimulacroUdEntity>,
) {
    val totalSimulacros: Int get() = simulaciones.size + simulacrosUd.size
    val unidadesDominadas: Int get() = unidades.count { it.completadas() == TOTAL_ITEMS_POR_UD }
}

data class Insignia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val conseguida: (ContextoInsignias) -> Boolean,
)

private fun temasCompletos(temas: List<TemaEntity>, minimo: Int): Boolean =
    temas.count { it.completados() == TOTAL_ITEMS_POR_TEMA } >= minimo

val INSIGNIAS: List<Insignia> = listOf(
    Insignia(
        id = "primer_paso",
        titulo = "Primer paso",
        descripcion = "Marca tu primera casilla",
        conseguida = { ctx -> ctx.temas.any { it.completados() > 0 } },
    ),
    Insignia(
        id = "tema_dominado",
        titulo = "Tema dominado",
        descripcion = "Completa un tema entero",
        conseguida = { ctx -> temasCompletos(ctx.temas, 1) },
    ),
    Insignia(
        id = "cuarto_temario",
        titulo = "Un cuarto del temario",
        descripcion = "Completa 7 temas enteros",
        conseguida = { ctx -> temasCompletos(ctx.temas, 7) },
    ),
    Insignia(
        id = "mitad_temario",
        titulo = "La mitad del temario",
        descripcion = "Completa 13 temas enteros",
        conseguida = { ctx -> temasCompletos(ctx.temas, 13) },
    ),
    Insignia(
        id = "temario_completo",
        titulo = "¡Temario completo!",
        descripcion = "Completa los 25 temas",
        conseguida = { ctx -> temasCompletos(ctx.temas, 25) },
    ),
    Insignia(
        id = "repasando_fuerte",
        titulo = "Repasando fuerte",
        descripcion = "Acumula 25 repasos en total",
        conseguida = { ctx -> ctx.temas.sumOf { it.repasos } >= 25 },
    ),
    Insignia(
        id = "maquina_repasos",
        titulo = "Máquina de repasos",
        descripcion = "Acumula 100 repasos en total",
        conseguida = { ctx -> ctx.temas.sumOf { it.repasos } >= 100 },
    ),
    Insignia(
        id = "primer_simulacro",
        titulo = "Primer simulacro",
        descripcion = "Termina tu primer simulacro (de tema o de unidad didáctica)",
        conseguida = { ctx -> ctx.totalSimulacros >= 1 },
    ),
    Insignia(
        id = "diez_simulacros",
        titulo = "10 simulacros",
        descripcion = "Termina 10 simulacros entre temas y unidades didácticas",
        conseguida = { ctx -> ctx.totalSimulacros >= 10 },
    ),
    Insignia(
        id = "veinticinco_simulacros",
        titulo = "25 simulacros",
        descripcion = "Termina 25 simulacros entre temas y unidades didácticas",
        conseguida = { ctx -> ctx.totalSimulacros >= 25 },
    ),
    Insignia(
        id = "cincuenta_simulacros",
        titulo = "50 simulacros",
        descripcion = "Termina 50 simulacros entre temas y unidades didácticas",
        conseguida = { ctx -> ctx.totalSimulacros >= 50 },
    ),
    Insignia(
        id = "cien_simulacros",
        titulo = "100 simulacros",
        descripcion = "Termina 100 simulacros entre temas y unidades didácticas",
        conseguida = { ctx -> ctx.totalSimulacros >= 100 },
    ),
    Insignia(
        id = "primera_ud",
        titulo = "Primera unidad didáctica",
        descripcion = "Crea tu primera unidad didáctica",
        conseguida = { ctx -> ctx.unidades.isNotEmpty() },
    ),
    Insignia(
        id = "cinco_ud",
        titulo = "5 unidades didácticas",
        descripcion = "Crea 5 unidades didácticas",
        conseguida = { ctx -> ctx.unidades.size >= 5 },
    ),
    Insignia(
        id = "diez_ud",
        titulo = "10 unidades didácticas",
        descripcion = "Crea 10 unidades didácticas",
        conseguida = { ctx -> ctx.unidades.size >= 10 },
    ),
    Insignia(
        id = "ud_dominada",
        titulo = "Unidad dominada",
        descripcion = "Completa una unidad entera (hecha, revisada, guión, presentación y lista)",
        conseguida = { ctx -> ctx.unidadesDominadas >= 1 },
    ),
    Insignia(
        id = "cinco_ud_dominadas",
        titulo = "5 unidades dominadas",
        descripcion = "Completa 5 unidades didácticas enteras",
        conseguida = { ctx -> ctx.unidadesDominadas >= 5 },
    ),
    Insignia(
        id = "diez_ud_dominadas",
        titulo = "10 unidades dominadas",
        descripcion = "Completa 10 unidades didácticas enteras",
        conseguida = { ctx -> ctx.unidadesDominadas >= 10 },
    ),
)
