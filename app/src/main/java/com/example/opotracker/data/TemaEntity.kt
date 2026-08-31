package com.example.opotracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TOTAL_TEMAS = 25

@Entity(tableName = "temas")
data class TemaEntity(
    @PrimaryKey val numero: Int,
    val leido1: Boolean = false,
    val leido2: Boolean = false,
    val leido3: Boolean = false,
    val resumen: Boolean = false,
    val personalizacion: Boolean = false,
    val estudio: Boolean = false,
    val repasos: Int = 0,
)

/** Leído x3 + Resumen + Personalización + Estudio. Repasos is an open-ended counter, so it doesn't count toward completion. */
const val TOTAL_ITEMS_POR_TEMA = 6

fun TemaEntity.completados(): Int {
    var total = 0
    if (leido1) total++
    if (leido2) total++
    if (leido3) total++
    if (resumen) total++
    if (personalizacion) total++
    if (estudio) total++
    return total
}
