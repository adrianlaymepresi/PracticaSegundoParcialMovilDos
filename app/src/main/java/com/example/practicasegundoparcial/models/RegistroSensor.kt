package com.example.practicasegundoparcial.models

data class RegistroSensor(
    var id: Int = 0,
    var fecha: String,
    var proximidad: Float,
    var ax: Float,
    var ay: Float,
    var az: Float,
    var latitud: Double,
    var longitud: Double,
    var nombreCancion: String
)
