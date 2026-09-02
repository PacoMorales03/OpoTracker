package com.example.opotracker.data

import kotlinx.coroutines.flow.Flow

/** How many balls the tribunal draws on exam day for the opositor to choose from. */
const val BOLAS_POR_SORTEO = 3

class OpoRepository(private val db: AppDatabase) {

    val temas: Flow<List<TemaEntity>> = db.temaDao().observeAll()
    val simulaciones: Flow<List<SimulacionEntity>> = db.simulacionDao().observeAll()
    val temasEnSimulacro: Flow<List<Int>> = db.temaDao().observeNumerosEnSimulacro()

    suspend fun ensureSeeded() {
        if (db.temaDao().count() == 0) {
            db.temaDao().insertAll((1..TOTAL_TEMAS).map { TemaEntity(numero = it) })
        }
    }

    suspend fun updateTema(tema: TemaEntity) = db.temaDao().update(tema)

    /**
     * Draws up to [BOLAS_POR_SORTEO] distinct tema numbers, like the ball draw on exam day, without
     * repeating a number until every eligible tema has come up once. Only temas marked "en
     * simulacro" are eligible; if none are marked, all 25 are used instead. There's no minimum:
     * with 3 or fewer eligible temas, every one of them is drawn. The caller must eventually call
     * [devolverAlBolsa] with whichever of these were not chosen, so they stay eligible.
     */
    suspend fun sortearBolas(): List<Int> {
        val marcados = db.temaDao().getNumerosEnSimulacro()
        val universo = marcados.ifEmpty { (1..TOTAL_TEMAS).toList() }
        val numBolas = minOf(BOLAS_POR_SORTEO, universo.size)

        var remaining = leerRestantes().filter { it in universo }
        if (remaining.size < numBolas) remaining = universo

        val elegidas = remaining.shuffled().take(numBolas)
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

    suspend fun updateSimulacion(simulacion: SimulacionEntity) = db.simulacionDao().update(simulacion)

    suspend fun deleteSimulacion(simulacion: SimulacionEntity) = db.simulacionDao().delete(simulacion)

    suspend fun insigniasVistas(): Set<String> = db.insigniaVistaDao().getAll().toSet()

    suspend fun marcarInsigniaVista(id: String) = db.insigniaVistaDao().insert(InsigniaVistaEntity(id))

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
