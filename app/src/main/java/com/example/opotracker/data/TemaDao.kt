package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemaDao {
    @Query("SELECT * FROM temas ORDER BY numero ASC")
    fun observeAll(): Flow<List<TemaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(temas: List<TemaEntity>)

    @Update
    suspend fun update(tema: TemaEntity)

    @Query("SELECT COUNT(*) FROM temas")
    suspend fun count(): Int
}
