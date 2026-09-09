package com.example.opotracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val URL_ULTIMA_RELEASE =
    "https://api.github.com/repos/PacoMorales03/OpoTracker/releases/latest"
private val REGEX_SHA = Regex("\\b[0-9a-f]{40}\\b")

/** Datos de la última versión publicada en GitHub Releases. */
data class InfoActualizacion(
    val commitSha: String,
    val urlDescargaApk: String,
    val tamanoBytes: Long,
)

/** Consulta la última release en GitHub y extrae el commit y el APK a descargar. */
suspend fun obtenerUltimaActualizacion(): InfoActualizacion? = withContext(Dispatchers.IO) {
    runCatching {
        val conexion = URL(URL_ULTIMA_RELEASE).openConnection() as HttpURLConnection
        conexion.requestMethod = "GET"
        conexion.setRequestProperty("Accept", "application/vnd.github+json")
        conexion.connectTimeout = 10_000
        conexion.readTimeout = 10_000

        val texto = conexion.inputStream.bufferedReader().use { it.readText() }
        conexion.disconnect()

        val json = JSONObject(texto)
        val cuerpo = json.optString("body")
        val sha = REGEX_SHA.find(cuerpo)?.value ?: return@withContext null

        val assets = json.optJSONArray("assets") ?: return@withContext null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val nombre = asset.optString("name")
            if (nombre.endsWith(".apk")) {
                return@withContext InfoActualizacion(
                    commitSha = sha,
                    urlDescargaApk = asset.getString("browser_download_url"),
                    tamanoBytes = asset.optLong("size"),
                )
            }
        }
        null
    }.getOrNull()
}

/** Descarga el APK indicado a [destino], informando el progreso (0f..1f) por [onProgreso]. */
suspend fun descargarApk(
    urlDescarga: String,
    destino: java.io.File,
    onProgreso: (Float) -> Unit,
) = withContext(Dispatchers.IO) {
    val conexion = URL(urlDescarga).openConnection() as HttpURLConnection
    conexion.instanceFollowRedirects = true
    conexion.connectTimeout = 10_000
    conexion.readTimeout = 15_000
    conexion.connect()

    val total = conexion.contentLengthLong
    var leidos = 0L

    conexion.inputStream.use { entrada ->
        destino.outputStream().use { salida ->
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val n = entrada.read(buffer)
                if (n <= 0) break
                salida.write(buffer, 0, n)
                leidos += n
                if (total > 0) onProgreso(leidos.toFloat() / total.toFloat())
            }
        }
    }
    conexion.disconnect()
    onProgreso(1f)
}
