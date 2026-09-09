package com.example.opotracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TemaEntity::class,
        SimulacionEntity::class,
        SimulatorStateEntity::class,
        InsigniaVistaEntity::class,
        UnidadDidacticaEntity::class,
        SimulacroUdEntity::class,
        UnidadSimulatorStateEntity::class,
        PlanificacionDiariaEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun temaDao(): TemaDao
    abstract fun simulacionDao(): SimulacionDao
    abstract fun simulatorStateDao(): SimulatorStateDao
    abstract fun insigniaVistaDao(): InsigniaVistaDao
    abstract fun unidadDidacticaDao(): UnidadDidacticaDao
    abstract fun simulacroUdDao(): SimulacroUdDao
    abstract fun unidadSimulatorStateDao(): UnidadSimulatorStateDao
    abstract fun planificacionDiariaDao(): PlanificacionDiariaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "opotracker.db",
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}
