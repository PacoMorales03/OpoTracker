package com.example.opotracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PlanificacionRepository(private val db: AppDatabase) {

    /** Racha actual de días consecutivos con horas reales de estudio registradas, contando hacia atrás desde hoy (o ayer si hoy aún no se ha rellenado). Se recalcula sola cada vez que cambian los datos. */
    val racha: Flow<Int> = db.planificacionDiariaDao().observeFechasConEstudio()
        .map { fechas -> calcularRachaDesde(fechas.toSet()) }

    suspend fun obtenerOCrear(fecha: String): PlanificacionDiariaEntity =
        db.planificacionDiariaDao().getPorFecha(fecha) ?: PlanificacionDiariaEntity(fecha = fecha)

    /** Inserta si es la primera vez que se guarda ese día, o actualiza si ya existía. Devuelve la entidad persistida (con id real). */
    suspend fun guardar(plan: PlanificacionDiariaEntity): PlanificacionDiariaEntity {
        return if (plan.id == 0L) {
            val nuevoId = db.planificacionDiariaDao().insertar(plan)
            plan.copy(id = nuevoId)
        } else {
            db.planificacionDiariaDao().actualizar(plan)
            plan
        }
    }

    private fun calcularRachaDesde(fechasConEstudio: Set<String>): Int {
        var dia = LocalDate.now()
        if (!fechasConEstudio.contains(dia.toString())) dia = dia.minusDays(1)

        var racha = 0
        while (fechasConEstudio.contains(dia.toString())) {
            racha++
            dia = dia.minusDays(1)
        }
        return racha
    }
}
