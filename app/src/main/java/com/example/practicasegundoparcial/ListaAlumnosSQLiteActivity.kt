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
import com.example.practicasegundoparcial.firebase.BaseDatosFirebase
import com.example.practicasegundoparcial.models.Alumno
import com.example.practicasegundoparcial.utils.NetworkSensorUtils

class ListaAlumnosSQLiteActivity : AppCompatActivity() {

    private lateinit var dao: AlumnoDAO
    private lateinit var firebase: BaseDatosFirebase
    private lateinit var adapter: AlumnoAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var networkSensor: NetworkSensorUtils

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var tvEstadoConexion: TextView

    private var tieneConexion: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos_sqlite)

        dao = AlumnoDAO(this)
        firebase = BaseDatosFirebase(this)

        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        recycler = findViewById(R.id.recyclerAlumnos)
        tvEstadoConexion = findViewById(R.id.tvEstadoConexion)

        // INICIALIZAR SENSOR DE RED
        networkSensor = NetworkSensorUtils(this) { estado ->
            runOnUiThread {
                tvEstadoConexion.text = estado
                tieneConexion = !estado.contains("Sin conexión")
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = AlumnoAdapter(emptyList()) { alumno ->
            eliminarAlumno(alumno)
        }

        recycler.adapter = adapter

        btnBuscar.setOnClickListener { buscar() }

        cargarLista()
    }

    override fun onResume() {
        super.onResume()
        networkSensor.iniciarMonitoreo()
        cargarLista()
    }

    override fun onPause() {
        super.onPause()
        networkSensor.detenerMonitoreo()
    }

    private fun eliminarAlumno(alumno: Alumno) {
        // ELIMINAR DE SQLITE SIEMPRE
        dao.eliminar(alumno.id)

        // SI HAY CONEXIÓN, INTENTAR ELIMINAR DE FIREBASE TAMBIÉN
        if (tieneConexion) {
            firebase.eliminarAlumnoPorCI(alumno.ci) {
                Toast.makeText(this, "🗑️ Eliminado de SQLite y Firebase", Toast.LENGTH_SHORT).show()
                cargarLista()
            }
        } else {
            Toast.makeText(this, "🗑️ Eliminado de SQLite", Toast.LENGTH_SHORT).show()
            cargarLista()
        }
    }

    private fun cargarLista() {
        // SIEMPRE MOSTRAR TODOS LOS DATOS DE SQLITE
        val lista = dao.obtenerTodos().sortedByDescending { it.id }
        adapter.actualizar(lista)
    }

    private fun buscar() {
        val texto = etBuscar.text.toString().trim()
        if (texto.isEmpty()) {
            cargarLista()
            return
        }

        val lista = dao.obtenerTodos()

        val filtrada = lista.filter {
            it.ci.toString().contains(texto) ||
                    "${it.nombres} ${it.apellidos}".lowercase().contains(texto.lowercase())
        }.sortedByDescending { it.id }

        adapter.actualizar(filtrada)
    }
}
