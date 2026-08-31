package com.example.opotracker.data

import kotlinx.coroutines.flow.Flow

/** How many balls the tribunal draws on exam day for the opositor to choose from. */
const val BOLAS_POR_SORTEO = 3

class OpoRepository(private val db: AppDatabase) {

    val temas: Flow<List<TemaEntity>> = db.temaDao().observeAll()
    val simulaciones: Flow<List<SimulacionEntity>> = db.simulacionDao().observeAll()

    suspend fun ensureSeeded() {
        if (db.temaDao().count() == 0) {
            db.temaDao().insertAll((1..TOTAL_TEMAS).map { TemaEntity(numero = it) })
        }
    }

    suspend fun updateTema(tema: TemaEntity) = db.temaDao().update(tema)

    /**
     * Draws [BOLAS_POR_SORTEO] distinct tema numbers, like the ball draw on exam day, without
     * repeating a number until every tema has come up once. The caller must eventually call
     * [devolverAlBolsa] with whichever of these were not chosen, so they stay eligible.
     */
    suspend fun sortearBolas(): List<Int> {
        var remaining = leerRestantes()
        if (remaining.size < BOLAS_POR_SORTEO) remaining = (1..TOTAL_TEMAS).toList()

        val elegidas = remaining.shuffled().take(BOLAS_POR_SORTEO)
        guardarRestantes(remaining - elegidas.toSet())
        return elegidas
    }

    /** Returns temas that were drawn but not chosen back into the pool for future draws. */
    suspend fun devolverAlBolsa(temas: List<Int>) {
        if (temas.isEmpty()) return
        val remaining = leerRestantes()
        guardarRestantes((remaining + temas).distinct())
    }

    suspend fun saveSimulacion(simulacion: SimulacionEntity) = db.simulacionDao().insert(simulacion)

    private suspend fun leerRestantes(): List<Int> =
        db.simulatorStateDao().get()?.remaining
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?: (1..TOTAL_TEMAS).toList()

    private suspend fun guardarRestantes(remaining: List<Int>) {
        db.simulatorStateDao().set(SimulatorStateEntity(remaining = remaining.joinToString(",")))
    }
}
