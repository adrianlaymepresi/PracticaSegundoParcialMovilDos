package com.example.practicasegundoparcial.models

data class Alumno(
    var id: Int = 0,
    var ci: Int,
    var nombres: String,
    var apellidos: String,
    var fechaNacimiento: String,
    var latitud: Double,
    var longitud: Double,
    var pendienteSync: Boolean = false  // Indica si está pendiente de sincronizar con Firebase
)