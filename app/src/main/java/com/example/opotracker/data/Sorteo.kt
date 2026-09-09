package com.example.opotracker.data

/**
 * Elige hasta [cantidad] elementos distintos de [universo] sin repetir ninguno hasta que todos
 * hayan salido una vez. [restantePrevio] es lo que quedaba en la bolsa la última vez, ya filtrado
 * para contener solo elementos que siguen en [universo]; si no alcanza para completar [cantidad],
 * se rellena con [universo] entero (nuevo ciclo). Es pura: no lee ni escribe nada, cada repositorio
 * se encarga de persistir el resultado con el tipo de id que le corresponda (Int, Long...).
 */
fun <T> sortearSinRepeticion(
    universo: List<T>,
    restantePrevio: List<T>,
    cantidad: Int,
): Pair<List<T>, List<T>> {
    if (universo.isEmpty()) return emptyList<T>() to emptyList()

    val numero = minOf(cantidad, universo.size)
    val restante = if (restantePrevio.size < numero) universo else restantePrevio

    val elegidos = restante.shuffled().take(numero)
    val nuevoRestante = restante - elegidos.toSet()
    return elegidos to nuevoRestante
}
