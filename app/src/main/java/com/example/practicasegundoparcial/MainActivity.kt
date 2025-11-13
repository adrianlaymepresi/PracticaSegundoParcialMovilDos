package com.example.practicasegundoparcial
// com.example.practicasegundoparcial = registrar el nombre en Firebase para manejar la base de datos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // COMPONENTES UI
    private lateinit var btnCrearAlumno: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // INICIALIZAR COMPONENTES UI
        btnCrearAlumno = findViewById(R.id.btnCrearAlumno)
        btnCrearAlumno.setOnClickListener {
            // NAVEGAR A LA ACTIVIDAD DE CREAR ALUMNO
            val intent = Intent(this, RegistrarAlumno::class.java)
            startActivity(intent)
        }

    }
}