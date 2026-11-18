package com.example.opotracker.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Tracks which insignia ids have already triggered their "earned" notification. */
@Entity(tableName = "insignias_vistas")
data class InsigniaVistaEntity(@PrimaryKey val id: String)

@Dao
interface InsigniaVistaDao {
    @Query("SELECT id FROM insignias_vistas")
    suspend fun getAll(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vista: InsigniaVistaEntity)
}
