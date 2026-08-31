package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimulacionDao {
    @Query("SELECT * FROM simulaciones ORDER BY fechaMillis DESC")
    fun observeAll(): Flow<List<SimulacionEntity>>

    @Insert
    suspend fun insert(simulacion: SimulacionEntity)
}
