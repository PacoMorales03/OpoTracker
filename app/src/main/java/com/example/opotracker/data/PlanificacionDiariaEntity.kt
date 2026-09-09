package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

val ASPECTOS_PLANIFICACION = listOf(
    "Tema",
    "Supuesto",
    "Programación escrita",
    "Unidades Didácticas (SdA) escritas",
    "Programación oral",
    "Unidad oral",
)
const val MAX_ASPECTOS_PLANIFICACION = 2

/** Una ficha de planificación diaria, una por fecha (formato ISO "yyyy-MM-dd"). */
@Entity(tableName = "planificaciones_diarias", indices = [Index(value = ["fecha"], unique = true)])
data class PlanificacionDiariaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val horasPrevistas: Float = 0f,
    val aspectosMask: Int = 0,
    val objetivo1: String = "",
    val objetivo2: String = "",
    val observaciones: String = "",
    val horasReales: Float = 0f,
)

fun PlanificacionDiariaEntity.aspectoActivo(indice: Int): Boolean = (aspectosMask shr indice) and 1 == 1

fun PlanificacionDiariaEntity.numeroAspectosActivos(): Int = Integer.bitCount(aspectosMask)

fun PlanificacionDiariaEntity.conAspectoAlternado(indice: Int): PlanificacionDiariaEntity {
    val activo = aspectoActivo(indice)
    if (!activo && numeroAspectosActivos() >= MAX_ASPECTOS_PLANIFICACION) return this
    return copy(aspectosMask = aspectosMask xor (1 shl indice))
}

@Dao
interface PlanificacionDiariaDao {
    @Query("SELECT * FROM planificaciones_diarias WHERE fecha = :fecha LIMIT 1")
    suspend fun getPorFecha(fecha: String): PlanificacionDiariaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(plan: PlanificacionDiariaEntity): Long

    @Update
    suspend fun actualizar(plan: PlanificacionDiariaEntity)

    @Query("SELECT fecha FROM planificaciones_diarias WHERE horasReales > 0")
    fun observeFechasConEstudio(): Flow<List<String>>
}
