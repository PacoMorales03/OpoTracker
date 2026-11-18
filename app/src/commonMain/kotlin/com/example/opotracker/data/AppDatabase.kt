package com.example.opotracker.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

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
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun temaDao(): TemaDao
    abstract fun simulacionDao(): SimulacionDao
    abstract fun simulatorStateDao(): SimulatorStateDao
    abstract fun insigniaVistaDao(): InsigniaVistaDao
    abstract fun unidadDidacticaDao(): UnidadDidacticaDao
    abstract fun simulacroUdDao(): SimulacroUdDao
    abstract fun unidadSimulatorStateDao(): UnidadSimulatorStateDao
    abstract fun planificacionDiariaDao(): PlanificacionDiariaDao
}

// Room's KSP compiler generates the `actual` implementation for each target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Database file name, shared by every platform's builder. */
const val NOMBRE_BASE_DATOS = "opotracker.db"

/** Finishes configuring a platform-provided builder and builds the singleton database. */
fun crearAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()

/** Holds the single [AppDatabase] instance for the process; each platform's entry point sets it once. */
object AppDatabaseHolder {
    private var instancia: AppDatabase? = null

    val database: AppDatabase
        get() = instancia ?: error("AppDatabaseHolder.inicializar() no se ha llamado todavía")

    fun inicializar(builder: () -> RoomDatabase.Builder<AppDatabase>) {
        if (instancia == null) {
            instancia = crearAppDatabase(builder())
        }
    }
}
