package com.example.practicasegundoparcial
// com.example.practicasegundoparcial = registrar el nombre en Firebase para manejar la base de datos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.practicasegundoparcial.utils.NetworkSensorUtils

class MainActivity : AppCompatActivity() {

    // COMPONENTES UI
    private lateinit var btnCrearAlumno: Button
    private lateinit var btnListaAlumnosSqlite: Button
    private lateinit var btnListaAlumnosFirebase: Button
    private lateinit var btnRutaDosAlumnos: Button
    private lateinit var btnSensorProximidad: Button
    private lateinit var tvEstadoConexion: TextView

    // SENSOR DE RED
    private lateinit var networkSensor: NetworkSensorUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // INICIALIZAR COMPONENTES UI
        btnCrearAlumno = findViewById(R.id.btnCrearAlumno)
        btnListaAlumnosSqlite = findViewById(R.id.btnListaAlumnosSqlite)
        btnListaAlumnosFirebase = findViewById(R.id.btnListaAlumnosFirebase)
        btnRutaDosAlumnos = findViewById(R.id.btnRutaDosAlumnos)
        btnSensorProximidad = findViewById(R.id.btnSensorProximidad)
        tvEstadoConexion = findViewById(R.id.tvEstadoConexion)

        // INICIALIZAR SENSOR DE RED
        networkSensor = NetworkSensorUtils(this) { estado ->
            runOnUiThread {
                tvEstadoConexion.text = estado
            }
        }

        btnCrearAlumno.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE CREAR ALUMNO
            val intent = Intent(this, RegistrarAlumno::class.java)
            startActivity(intent)
        }

        btnListaAlumnosSqlite.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE LISTA DE ALUMNOS DESDE SQLITE
            val intent = Intent(this, ListaAlumnosSQLiteActivity::class.java)
            startActivity(intent)
        }

        btnListaAlumnosFirebase.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE LISTA DE ALUMNOS DESDE FIREBASE
            val intent = Intent(this, ListaAlumnosFirebaseActivity::class.java)
            startActivity(intent)
        }

        btnRutaDosAlumnos.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE RUTA DOS ALUMNOS
            val intent = Intent(this, RutaDosAlumnosActivity::class.java)
            startActivity(intent)
        }

        btnSensorProximidad.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE SENSOR DE PROXIMIDAD
            val intent = Intent(this, SensorProximidad::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // INICIAR MONITOREO DEL SENSOR DE RED
        networkSensor.iniciarMonitoreo()
    }

    override fun onPause() {
        super.onPause()
        // DETENER MONITOREO DEL SENSOR DE RED
        networkSensor.detenerMonitoreo()
    }
}