package com.example.practicasegundoparcial.utils

import android.content.Context
import android.widget.Toast
import com.example.practicasegundoparcial.database.AlumnoDAO
import com.example.practicasegundoparcial.firebase.AlumnoFirebase
import com.example.practicasegundoparcial.firebase.BaseDatosFirebase

class SyncManager(private val context: Context) {

    private val alumnoDAO = AlumnoDAO(context)
    private val firebaseDB = BaseDatosFirebase(context)

    fun sincronizarPendientes(onComplete: (Int) -> Unit) {
        val pendientes = alumnoDAO.obtenerPendientesSync()

        if (pendientes.isEmpty()) {
            onComplete(0)
            return
        }

        var sincronizados = 0
        val total = pendientes.size

        pendientes.forEach { alumno ->
            val alumnoFB = AlumnoFirebase(
                id = null,
                ci = alumno.ci,
                nombres = alumno.nombres,
                apellidos = alumno.apellidos,
                fechaNacimiento = alumno.fechaNacimiento,
                latitud = alumno.latitud,
                longitud = alumno.longitud
            )

            firebaseDB.agregarAlumno(alumnoFB,
                onSuccess = {
                    // Marcar como sincronizado en SQLite
                    alumnoDAO.marcarComoSincronizado(alumno.ci)
                    sincronizados++

                    if (sincronizados == total) {
                        onComplete(sincronizados)
                    }
                },
                onFailure = {
                    if (sincronizados == total) {
                        onComplete(sincronizados)
                    }
                }
            )
        }
    }
}

