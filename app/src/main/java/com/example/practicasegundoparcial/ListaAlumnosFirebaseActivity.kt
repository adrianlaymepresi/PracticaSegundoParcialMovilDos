package com.example.practicasegundoparcial

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicasegundoparcial.adapters.AlumnoAdapter
import com.example.practicasegundoparcial.firebase.AlumnoFirebase
import com.example.practicasegundoparcial.firebase.BaseDatosFirebase
import com.example.practicasegundoparcial.models.Alumno

class ListaAlumnosFirebaseActivity : AppCompatActivity() {

    private lateinit var firebase: BaseDatosFirebase
    private lateinit var adapter: AlumnoAdapter
    private lateinit var recycler: RecyclerView

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button

    private var listaFirebase = listOf<AlumnoFirebase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos_firebase)

        firebase = BaseDatosFirebase(this)

        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        recycler = findViewById(R.id.recyclerAlumnos)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = AlumnoAdapter(emptyList()) { alumno ->
            eliminarFirebase(alumno)
        }

        recycler.adapter = adapter

        btnBuscar.setOnClickListener { buscar() }

        cargarFirebase()
    }

    private fun cargarFirebase() {
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
                    longitud = it.longitud
                )
            }

            adapter.actualizar(convertidos)
        }
    }

    private fun buscar() {
        val texto = etBuscar.text.toString().trim()

        val filtrados = listaFirebase.filter {
            it.ci.toString().contains(texto) ||
                    "${it.nombres} ${it.apellidos}".lowercase().contains(texto.lowercase())
        }.map {
            Alumno(0, it.ci, it.nombres, it.apellidos, it.fechaNacimiento, it.latitud, it.longitud)
        }

        adapter.actualizar(filtrados)
    }

    private fun eliminarFirebase(alumno: Alumno) {
        val obj = listaFirebase.firstOrNull { it.ci == alumno.ci } ?: return
        val id = obj.id ?: return

        firebase.eliminarAlumnoPorId(id) {
            cargarFirebase()
        }
    }
}
