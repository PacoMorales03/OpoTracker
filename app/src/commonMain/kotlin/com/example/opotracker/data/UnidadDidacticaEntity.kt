package com.example.opotracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades_didacticas")
data class UnidadDidacticaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val hecha: Boolean = false,
    val revisada: Boolean = false,
    val guion: Boolean = false,
    val presentacion: Boolean = false,
    val lista: Boolean = false,
    val revision: Int = 0,
    val practica: Int = 0,
    val enSimulacro: Boolean = false,
)

/** Hecha + Revisada + Guión + Presentación + Lista. Revisión/Práctica son contadores abiertos. */
const val TOTAL_ITEMS_POR_UD = 5

fun UnidadDidacticaEntity.completadas(): Int {
    var total = 0
    if (hecha) total++
    if (revisada) total++
    if (guion) total++
    if (presentacion) total++
    if (lista) total++
    return total
}
