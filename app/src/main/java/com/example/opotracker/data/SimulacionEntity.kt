package com.example.opotracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulaciones")
data class SimulacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val temaNumero: Int,
    val fechaMillis: Long,
    val duracionSegundos: Long,
    val sensacion: Float,
    val observaciones: String,
)
