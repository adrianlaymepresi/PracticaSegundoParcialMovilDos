package com.example.practicasegundoparcial.models

import com.google.firebase.firestore.PropertyName

class RegistroSensorFirebase(

    var id: String? = null,

    @get:PropertyName("fecha") @set:PropertyName("fecha")
    var fecha: String = "",

    @get:PropertyName("proximidad") @set:PropertyName("proximidad")
    var proximidad: Float = 0f,

    @get:PropertyName("ax") @set:PropertyName("ax")
    var ax: Float = 0f,

    @get:PropertyName("ay") @set:PropertyName("ay")
    var ay: Float = 0f,

    @get:PropertyName("az") @set:PropertyName("az")
    var az: Float = 0f,

    @get:PropertyName("latitud") @set:PropertyName("latitud")
    var latitud: Double = 0.0,

    @get:PropertyName("longitud") @set:PropertyName("longitud")
    var longitud: Double = 0.0,

    @get:PropertyName("nombreCancion") @set:PropertyName("nombreCancion")
    var nombreCancion: String = ""
) {
    constructor() : this(null, "", 0f, 0f, 0f, 0f, 0.0, 0.0, "")
}
