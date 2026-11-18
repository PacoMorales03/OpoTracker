package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SimulacionDao {
    @Query("SELECT * FROM simulaciones ORDER BY fechaMillis DESC")
    fun observeAll(): Flow<List<SimulacionEntity>>

    @Insert
    suspend fun insert(simulacion: SimulacionEntity)

    @Update
    suspend fun update(simulacion: SimulacionEntity)

    @Delete
    suspend fun delete(simulacion: SimulacionEntity)
}
