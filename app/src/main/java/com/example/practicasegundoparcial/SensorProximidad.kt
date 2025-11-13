package com.example.practicasegundoparcial

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.practicasegundoparcial.database.RegistroSensorDAO
import com.example.practicasegundoparcial.database.BaseDatosSensorFirebase
import com.example.practicasegundoparcial.models.RegistroSensor
import com.example.practicasegundoparcial.utils.SensorProximidadUtils

class SensorProximidad : AppCompatActivity() {

    private lateinit var utils: SensorProximidadUtils
    private lateinit var dao: RegistroSensorDAO
    private lateinit var firebase: BaseDatosSensorFirebase

    private var permisosUbicacionConcedidos = false

    companion object {
        const val REQ_PERM_AUDIO = 4001
        const val REQ_PERM_LOCATION = 4002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_proximidad)

        try {
            utils = SensorProximidadUtils(this)
            dao = RegistroSensorDAO(this)
            firebase = BaseDatosSensorFirebase(this)

            val btnElegir = findViewById<Button>(R.id.btnElegirAudio)
            val btnLimpiar = findViewById<Button>(R.id.btnLimpiarRegistros)

            btnElegir.setOnClickListener {
                pedirPermisoYElegirAudio()
            }

            btnLimpiar.setOnClickListener {
                limpiarRegistros()
            }

            // Verificar permisos de ubicación al inicio
            verificarPermisos()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun verificarPermisos() {
        val permisosNecesarios = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permisosFaltantes = permisosNecesarios.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisosFaltantes.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permisosFaltantes.toTypedArray(),
                REQ_PERM_LOCATION
            )
        } else {
            permisosUbicacionConcedidos = true
        }
    }

    override fun onResume() {
        super.onResume()
        // Solo iniciar si tenemos permisos de ubicación
        if (permisosUbicacionConcedidos) {
            iniciarSensoresYCargarDatos()
        }
    }

    private fun iniciarSensoresYCargarDatos() {
        try {
            utils.iniciar()
            cargarListados()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar sensores: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            utils.detener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SensorProximidadUtils.REQ_AUDIO && resultCode == Activity.RESULT_OK) {
            utils.recibirAudio(data?.data)
        }
    }

    private fun pedirPermisoYElegirAudio() {
        val permisoNecesario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val tienePermiso = ContextCompat.checkSelfPermission(
            this,
            permisoNecesario
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permisoNecesario),
                REQ_PERM_AUDIO
            )
        } else {
            utils.elegirAudio(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQ_PERM_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    utils.elegirAudio(this)
                } else {
                    Toast.makeText(this, "Permiso denegado, se usará el audio por defecto", Toast.LENGTH_SHORT).show()
                }
            }
            REQ_PERM_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    permisosUbicacionConcedidos = true
                    Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
                    iniciarSensoresYCargarDatos()
                } else {
                    permisosUbicacionConcedidos = false
                    Toast.makeText(this, "Se requieren permisos de ubicación para usar esta funcionalidad", Toast.LENGTH_LONG).show()
                    finish() // Cerrar la actividad si no se conceden permisos
                }
            }
        }
    }

    private fun cargarListados() {
        try {
            cargarSqlite()
            cargarFirebase()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar listados: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun cargarSqlite() {
        try {
            val tvSqlite = findViewById<TextView>(R.id.tvRegistrosSqlite)
            val lista: List<RegistroSensor> = dao.obtenerTodos()
            if (lista.isEmpty()) {
                tvSqlite.text = "Sin registros en SQLite"
            } else {
                val texto = lista.joinToString("\n\n") { r ->
                    "Fecha: ${r.fecha}\n" +
                            "Prox: ${r.proximidad}\n" +
                            "Acc: X:${r.ax} Y:${r.ay} Z:${r.az}\n" +
                            "Lat:${r.latitud} Lon:${r.longitud}\n" +
                            "Canción: ${r.nombreCancion}"
                }
                tvSqlite.text = texto
            }
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tvRegistrosSqlite).text = "Error: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun cargarFirebase() {
        try {
            val tvFirebase = findViewById<TextView>(R.id.tvRegistrosFirebase)
            firebase.obtenerTodos { lista ->
                runOnUiThread {
                    if (lista.isEmpty()) {
                        tvFirebase.text = "Sin registros en Firebase"
                    } else {
                        val texto = lista.joinToString("\n\n") { r ->
                            "Fecha: ${r.fecha}\n" +
                                    "Prox: ${r.proximidad}\n" +
                                    "Acc: X:${r.ax} Y:${r.ay} Z:${r.az}\n" +
                                    "Lat:${r.latitud} Lon:${r.longitud}\n" +
                                    "Canción: ${r.nombreCancion}"
                        }
                        tvFirebase.text = texto
                    }
                }
            }
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tvRegistrosFirebase).text = "Error: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun limpiarRegistros() {
        try {
            dao.eliminarTodos()
            firebase.eliminarTodos {
                runOnUiThread {
                    cargarListados()
                    findViewById<TextView>(R.id.tvUltimosRegistros).text = "Último registro: (sin datos)"
                    Toast.makeText(this, "Registros eliminados en SQLite y Firebase", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al limpiar registros: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
