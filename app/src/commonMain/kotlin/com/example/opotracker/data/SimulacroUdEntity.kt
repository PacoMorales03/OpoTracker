package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "simulacros_ud")
data class SimulacroUdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    /** Copia del nombre en el momento del simulacro, para que el historial sobreviva si renombras o borras la unidad. */
    val unidadNombre: String,
    val fechaMillis: Long,
    val duracionSegundos: Long,
    val sensacion: Float,
    val comentarios: String,
)

@Dao
interface SimulacroUdDao {
    @Query("SELECT * FROM simulacros_ud ORDER BY fechaMillis DESC")
    fun observeAll(): Flow<List<SimulacroUdEntity>>

    @Insert
    suspend fun insert(simulacro: SimulacroUdEntity)

    @Update
    suspend fun update(simulacro: SimulacroUdEntity)

    @Delete
    suspend fun delete(simulacro: SimulacroUdEntity)
}
