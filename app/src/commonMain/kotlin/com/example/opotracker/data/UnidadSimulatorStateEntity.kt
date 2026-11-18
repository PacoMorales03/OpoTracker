package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Bolsa de ids de Unidad Didáctica no sorteados aún en el ciclo actual "sin repetición". Fila única. */
@Entity(tableName = "unidad_simulator_state")
data class UnidadSimulatorStateEntity(
    @PrimaryKey val id: Int = 0,
    val remaining: String,
)

@Dao
interface UnidadSimulatorStateDao {
    @Query("SELECT * FROM unidad_simulator_state WHERE id = 0")
    suspend fun get(): UnidadSimulatorStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(state: UnidadSimulatorStateEntity)
}
