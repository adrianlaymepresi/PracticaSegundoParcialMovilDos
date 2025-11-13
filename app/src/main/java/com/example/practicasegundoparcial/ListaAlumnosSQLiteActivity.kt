package com.example.practicasegundoparcial

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicasegundoparcial.adapters.AlumnoAdapter
import com.example.practicasegundoparcial.database.AlumnoDAO
import com.example.practicasegundoparcial.models.Alumno

class ListaAlumnosSQLiteActivity : AppCompatActivity() {

    private lateinit var dao: AlumnoDAO
    private lateinit var adapter: AlumnoAdapter
    private lateinit var recycler: RecyclerView

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos_sqlite)

        dao = AlumnoDAO(this)

        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        recycler = findViewById(R.id.recyclerAlumnos)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = AlumnoAdapter(emptyList()) { alumno ->
            dao.eliminar(alumno.id)
            cargarLista()
        }

        recycler.adapter = adapter

        btnBuscar.setOnClickListener { buscar() }

        cargarLista()
    }

    private fun cargarLista() {
        val lista = dao.obtenerTodos()
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
        }

        adapter.actualizar(filtrada)
    }
}
