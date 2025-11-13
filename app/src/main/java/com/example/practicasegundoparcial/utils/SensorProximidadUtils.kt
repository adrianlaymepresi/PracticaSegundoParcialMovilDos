package com.example.practicasegundoparcial.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.practicasegundoparcial.R
import com.example.practicasegundoparcial.database.RegistroSensorDAO
import com.example.practicasegundoparcial.database.BaseDatosSensorFirebase
import com.example.practicasegundoparcial.models.RegistroSensor
import com.example.practicasegundoparcial.models.RegistroSensorFirebase
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class SensorProximidadUtils(private val context: Context) : SensorEventListener {

    private val TAG = "SensorProximidadUtils"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorProx: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val sensorAcel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var ax = 0f
    private var ay = 0f
    private var az = 0f

    private var audioUri: Uri? = null
    private var nombreAudio = "sonido por defecto"

    private var mediaPlayer: MediaPlayer? = null

    private val dao = RegistroSensorDAO(context)
    private val firebase = BaseDatosSensorFirebase(context)
    private val gps = LocationServices.getFusedLocationProviderClient(context)

    private var ultimoRegistro = 0L // Para evitar registros duplicados

    // Callback para notificar cuando se guarda un registro
    var onRegistroGuardado: (() -> Unit)? = null

    companion object {
        const val REQ_AUDIO = 3000
    }

    fun iniciar() {
        sensorProx?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        sensorAcel?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        try {
            mediaPlayer = if (audioUri != null) {
                MediaPlayer.create(context, audioUri)
            } else {
                MediaPlayer.create(context, R.raw.sonido)
            }

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.sonido)
            }

            mediaPlayer?.isLooping = true
        } catch (_: Exception) {
            mediaPlayer = MediaPlayer.create(context, R.raw.sonido)
            mediaPlayer?.isLooping = true
        }
    }

    fun detener() {
        sensorManager.unregisterListener(this)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun elegirAudio(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        activity.startActivityForResult(intent, REQ_AUDIO)
    }

    fun recibirAudio(uri: Uri?) {
        if (uri != null) {
            audioUri = uri
            nombreAudio = obtenerNombreArchivo(uri)
            detener()
            iniciar()
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        var nombre = "audioDesconocido"
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    nombre = it.getString(idx)
                }
            }
        }
        return nombre
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0]
            ay = event.values[1]
            az = event.values[2]
        }

        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val prox = event.values[0]
            val max = sensorProx?.maximumRange ?: return

            if (prox < max) {
                if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()

                // Evitar registros duplicados (solo guardar cada 3 segundos)
                val ahora = System.currentTimeMillis()
                if (ahora - ultimoRegistro > 3000) {
                    ultimoRegistro = ahora
                    guardarRegistro(prox)
                }
            } else {
                if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
            }
        }
    }

    private fun guardarRegistro(prox: Float) {
        // Verificar permisos antes de acceder a la ubicación
        val tienePermisos = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermisos) {
            Log.w(TAG, "No hay permisos de ubicación, no se guardará el registro")
            return
        }

        try {
            gps.lastLocation.addOnSuccessListener { loc ->
                val lat = loc?.latitude ?: 0.0
                val lon = loc?.longitude ?: 0.0
                val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val reg = RegistroSensor(
                    id = 0,
                    fecha = fecha,
                    proximidad = prox,
                    ax = ax,
                    ay = ay,
                    az = az,
                    latitud = lat,
                    longitud = lon,
                    nombreCancion = nombreAudio
                )

                // Guardar en SQLite
                val idInsertado = dao.insertar(reg)
                Log.d(TAG, "Registro guardado en SQLite con ID: $idInsertado")

                // Guardar en Firebase
                val regFb = RegistroSensorFirebase(
                    id = null,
                    fecha = fecha,
                    proximidad = prox,
                    ax = ax,
                    ay = ay,
                    az = az,
                    latitud = lat,
                    longitud = lon,
                    nombreCancion = nombreAudio
                )

                firebase.agregar(regFb)

                // Mostrar el último registro en pantalla
                mostrarRegistroEnPantalla(reg)

                // Notificar a la actividad que se guardó un registro para que actualice la lista
                (context as? Activity)?.runOnUiThread {
                    onRegistroGuardado?.invoke()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener ubicación: ${e.message}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al acceder a ubicación: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar registro: ${e.message}")
        }
    }

    private fun mostrarRegistroEnPantalla(reg: RegistroSensor) {
        try {
            val vista = (context as Activity).findViewById<TextView>(R.id.tvUltimosRegistros)
            vista?.text =
                "Último Registro:\n" +
                        "Fecha: ${reg.fecha}\n" +
                        "Proximidad: ${reg.proximidad}\n" +
                        "Acc: X:${reg.ax} Y:${reg.ay} Z:${reg.az}\n" +
                        "Lat:${reg.latitud} Lon:${reg.longitud}\n" +
                        "Canción: ${reg.nombreCancion}"
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar registro en pantalla: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
