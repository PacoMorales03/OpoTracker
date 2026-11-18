package com.example.opotracker.ui.update

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.opotracker.BuildConfig
import com.example.opotracker.data.InfoActualizacion
import com.example.opotracker.data.descargarApk
import com.example.opotracker.data.obtenerUltimaActualizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface EstadoActualizacion {
    data object SinComprobar : EstadoActualizacion
    data object Comprobando : EstadoActualizacion
    data object AlDia : EstadoActualizacion
    data class Disponible(val info: InfoActualizacion) : EstadoActualizacion
    data class Descargando(val progreso: Float) : EstadoActualizacion
    data class ListaParaInstalar(val uriApk: Uri) : EstadoActualizacion
    data object Error : EstadoActualizacion
}

class ActualizacionViewModel(application: Application) : AndroidViewModel(application) {

    private val _estado = MutableStateFlow<EstadoActualizacion>(EstadoActualizacion.SinComprobar)
    val estado: StateFlow<EstadoActualizacion> = _estado

    fun comprobarActualizacion() {
        if (BuildConfig.GIT_SHA == "dev") return
        viewModelScope.launch {
            _estado.value = EstadoActualizacion.Comprobando
            val info = obtenerUltimaActualizacion()
            _estado.value = when {
                info == null -> EstadoActualizacion.Error
                info.commitSha == BuildConfig.GIT_SHA -> EstadoActualizacion.AlDia
                else -> EstadoActualizacion.Disponible(info)
            }
        }
    }

    fun descargarEInstalar(info: InfoActualizacion) {
        viewModelScope.launch {
            _estado.value = EstadoActualizacion.Descargando(0f)
            val contexto = getApplication<Application>()
            val destino = File(contexto.getExternalFilesDir(null), "actualizacion.apk")
            runCatching {
                descargarApk(info.urlDescargaApk, destino) { progreso ->
                    _estado.value = EstadoActualizacion.Descargando(progreso)
                }
            }.onFailure {
                _estado.value = EstadoActualizacion.Error
                return@launch
            }

            val uri = FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                destino,
            )
            _estado.value = EstadoActualizacion.ListaParaInstalar(uri)
        }
    }

}

/** Comprueba si esta app tiene permiso para instalar APKs descargados. */
fun puedeInstalarApks(contexto: Context): Boolean =
    contexto.packageManager.canRequestPackageInstalls()

fun intentAjustesInstalarDesconocidos(paquete: String): Intent {
    val uri = Uri.parse("package:$paquete")
    return Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
}

fun intentInstalarApk(uriApk: Uri): Intent =
    Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uriApk, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
