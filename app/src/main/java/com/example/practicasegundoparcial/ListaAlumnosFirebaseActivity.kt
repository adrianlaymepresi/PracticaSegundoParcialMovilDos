package com.example.practicasegundoparcial

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicasegundoparcial.adapters.AlumnoAdapter
import com.example.practicasegundoparcial.database.AlumnoDAO
import com.example.practicasegundoparcial.firebase.AlumnoFirebase
import com.example.practicasegundoparcial.firebase.BaseDatosFirebase
import com.example.practicasegundoparcial.models.Alumno
import com.example.practicasegundoparcial.utils.NetworkSensorUtils
import com.example.practicasegundoparcial.utils.SyncManager

class ListaAlumnosFirebaseActivity : AppCompatActivity() {

    private lateinit var firebase: BaseDatosFirebase
    private lateinit var dao: AlumnoDAO
    private lateinit var adapter: AlumnoAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var networkSensor: NetworkSensorUtils
    private lateinit var syncManager: SyncManager

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var tvEstadoConexion: TextView

    private var listaFirebase = listOf<AlumnoFirebase>()
    private var tieneConexion: Boolean = false
    private var yaSeIntentoSincronizar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos_firebase)

        firebase = BaseDatosFirebase(this)
        dao = AlumnoDAO(this)
        syncManager = SyncManager(this)

        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        recycler = findViewById(R.id.recyclerAlumnos)
        tvEstadoConexion = findViewById(R.id.tvEstadoConexion)

        // INICIALIZAR SENSOR DE RED
        networkSensor = NetworkSensorUtils(this) { estado ->
            runOnUiThread {
                tvEstadoConexion.text = estado
                val nuevaConexion = !estado.contains("Sin conexión")

                // Detectar cambio de SIN conexión a CON conexión
                if (!tieneConexion && nuevaConexion && !yaSeIntentoSincronizar) {
                    Toast.makeText(this, "🔄 Conectado. Sincronizando datos pendientes...", Toast.LENGTH_SHORT).show()
                    sincronizarPendientes()
                    yaSeIntentoSincronizar = true
                }

                tieneConexion = nuevaConexion

                // Cargar Firebase si hay conexión
                if (tieneConexion) {
                    cargarFirebase()
                }
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = AlumnoAdapter(emptyList()) { alumno ->
            eliminarFirebase(alumno)
        }

        recycler.adapter = adapter

        btnBuscar.setOnClickListener { buscar() }

        cargarFirebase()
    }

    override fun onResume() {
        super.onResume()
        yaSeIntentoSincronizar = false
        networkSensor.iniciarMonitoreo()
        cargarFirebase()
    }

    override fun onPause() {
        super.onPause()
        networkSensor.detenerMonitoreo()
    }

    private fun sincronizarPendientes() {
        syncManager.sincronizarPendientes { cantidad ->
            if (cantidad > 0) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "✅ $cantidad registro(s) sincronizado(s) con Firebase",
                        Toast.LENGTH_LONG
                    ).show()
                    // Esperar un momento y recargar
                    recycler.postDelayed({
                        cargarFirebase()
                    }, 1000)
                }
            }
        }
    }

    private fun cargarFirebase() {
        if (!tieneConexion) {
            Toast.makeText(this, "⚠️ Sin conexión. No se pueden cargar datos de Firebase", Toast.LENGTH_SHORT).show()
            adapter.actualizar(emptyList())
            return
        }

        firebase.obtenerTodos { lista ->
            listaFirebase = lista

            val convertidos = lista.map {
                Alumno(
                    id = 0,
                    ci = it.ci,
                    nombres = it.nombres,
                    apellidos = it.apellidos,
                    fechaNacimiento = it.fechaNacimiento,
                    latitud = it.latitud,
                    longitud = it.longitud,
                    pendienteSync = false
                )
            }.sortedByDescending { it.ci }

            adapter.actualizar(convertidos)
        }
    }

    private fun buscar() {
        val texto = etBuscar.text.toString().trim()

        if (!tieneConexion) {
            Toast.makeText(this, "⚠️ Sin conexión a Internet", Toast.LENGTH_SHORT).show()
            return
        }

        val filtrados = listaFirebase.filter {
            it.ci.toString().contains(texto) ||
                    "${it.nombres} ${it.apellidos}".lowercase().contains(texto.lowercase())
        }.map {
            Alumno(0, it.ci, it.nombres, it.apellidos, it.fechaNacimiento, it.latitud, it.longitud)
        }.sortedByDescending { it.ci }

        adapter.actualizar(filtrados)
    }

    private fun eliminarFirebase(alumno: Alumno) {
        if (!tieneConexion) {
            Toast.makeText(this, "⚠️ Sin conexión. No se puede eliminar de Firebase", Toast.LENGTH_SHORT).show()
            return
        }

        val obj = listaFirebase.firstOrNull { it.ci == alumno.ci } ?: return
        val id = obj.id ?: return

        // ELIMINAR DE FIREBASE
        firebase.eliminarAlumnoPorId(id) {
            // TAMBIÉN ELIMINAR DE SQLITE
            dao.eliminarPorCI(alumno.ci)
            Toast.makeText(this, "🗑️ Eliminado de Firebase y SQLite", Toast.LENGTH_SHORT).show()
            cargarFirebase()
        }
    }
}
