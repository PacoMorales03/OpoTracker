package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * Holds the "bag" of tema numbers not yet drawn in the current no-repeat cycle
 * of the simulador. Single-row table.
 */
@Entity(tableName = "simulator_state")
data class SimulatorStateEntity(
    @PrimaryKey val id: Int = 0,
    val remaining: String,
)

@Dao
interface SimulatorStateDao {
    @Query("SELECT * FROM simulator_state WHERE id = 0")
    suspend fun get(): SimulatorStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(state: SimulatorStateEntity)
}
