package com.example.opotracker.data

import kotlinx.coroutines.flow.Flow

/** Cuántas Unidades Didácticas se sortean a la vez para elegir cuál desarrollar. */
const val BOLAS_POR_SORTEO_UD = 3

class UnidadDidacticaRepository(private val db: AppDatabase) {

    val unidades: Flow<List<UnidadDidacticaEntity>> = db.unidadDidacticaDao().observeAll()
    val simulacros: Flow<List<SimulacroUdEntity>> = db.simulacroUdDao().observeAll()

    suspend fun crearUnidad(nombre: String) {
        db.unidadDidacticaDao().insert(UnidadDidacticaEntity(nombre = nombre))
    }

    suspend fun actualizarUnidad(unidad: UnidadDidacticaEntity) = db.unidadDidacticaDao().update(unidad)

    suspend fun eliminarUnidad(unidad: UnidadDidacticaEntity) = db.unidadDidacticaDao().delete(unidad)

    /**
     * Sortea hasta [BOLAS_POR_SORTEO_UD] ids distintos entre las unidades marcadas "en simulacro",
     * sin repetir ninguno hasta que todos hayan salido. Si no hay ninguna marcada, no hay universo del
     * que sortear y devuelve una lista vacía (a diferencia de los temas, aquí no hay un "todas" por defecto).
     */
    suspend fun sortearBolasUd(): List<Long> {
        val universo = db.unidadDidacticaDao().getIdsEnSimulacro()
        if (universo.isEmpty()) return emptyList()
        val numBolas = minOf(BOLAS_POR_SORTEO_UD, universo.size)

        var remaining = leerRestantes().filter { it in universo }
        if (remaining.size < numBolas) remaining = universo

        val elegidas = remaining.shuffled().take(numBolas)
        guardarRestantes(remaining - elegidas.toSet())
        return elegidas
    }

    /** Devuelve a la bolsa las unidades sorteadas pero no elegidas, para que sigan siendo elegibles. */
    suspend fun devolverAlBolsaUd(ids: List<Long>) {
        if (ids.isEmpty()) return
        val remaining = leerRestantes()
        guardarRestantes((remaining + ids).distinct())
    }

    suspend fun guardarSimulacro(simulacro: SimulacroUdEntity) = db.simulacroUdDao().insert(simulacro)

    suspend fun actualizarSimulacro(simulacro: SimulacroUdEntity) = db.simulacroUdDao().update(simulacro)

    suspend fun eliminarSimulacro(simulacro: SimulacroUdEntity) = db.simulacroUdDao().delete(simulacro)

    private suspend fun leerRestantes(): List<Long> =
        db.unidadSimulatorStateDao().get()?.remaining
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()

    private suspend fun guardarRestantes(remaining: List<Long>) {
        db.unidadSimulatorStateDao().set(UnidadSimulatorStateEntity(remaining = remaining.joinToString(",")))
    }
}
