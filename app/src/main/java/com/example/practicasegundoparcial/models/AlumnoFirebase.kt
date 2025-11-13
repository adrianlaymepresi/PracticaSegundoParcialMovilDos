package com.example.practicasegundoparcial.firebase

import com.google.firebase.firestore.PropertyName

class AlumnoFirebase(

    var id: String? = null,

    @get:PropertyName("ci") @set:PropertyName("ci")
    var ci: Int = 0,

    @get:PropertyName("nombres") @set:PropertyName("nombres")
    var nombres: String = "",

    @get:PropertyName("apellidos") @set:PropertyName("apellidos")
    var apellidos: String = "",

    @get:PropertyName("fechaNacimiento") @set:PropertyName("fechaNacimiento")
    var fechaNacimiento: String = "",

    @get:PropertyName("latitud") @set:PropertyName("latitud")
    var latitud: Double = 0.0,

    @get:PropertyName("longitud") @set:PropertyName("longitud")
    var longitud: Double = 0.0
) {
    constructor() : this(null,0,"","","",0.0,0.0)
}
