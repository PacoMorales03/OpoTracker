package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDidacticaDao {
    @Query("SELECT * FROM unidades_didacticas ORDER BY id ASC")
    fun observeAll(): Flow<List<UnidadDidacticaEntity>>

    @Insert
    suspend fun insert(unidad: UnidadDidacticaEntity): Long

    @Update
    suspend fun update(unidad: UnidadDidacticaEntity)

    @Delete
    suspend fun delete(unidad: UnidadDidacticaEntity)

    @Query("SELECT id FROM unidades_didacticas WHERE enSimulacro = 1 ORDER BY id ASC")
    suspend fun getIdsEnSimulacro(): List<Long>
}
