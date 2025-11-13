package com.example.practicasegundoparcial.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class BaseDatosFirebase(private val context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val COLECCION_ALUMNOS = "alumnos"
    private val TAG = "BaseDatosFirebase"

    fun agregarAlumno(
        alumno: AlumnoFirebase,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(COLECCION_ALUMNOS)
            .add(alumno)
            .addOnSuccessListener { referencia ->
                val msg = "Alumno guardado con ID: ${referencia.id}"
                Log.d(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                onSuccess()
            }
            .addOnFailureListener { error ->
                val msg = "Error al guardar alumno: ${error.message}"
                Log.e(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                onFailure(error)
            }
    }

    fun obtenerTodos(listener: (List<AlumnoFirebase>) -> Unit): ListenerRegistration {
        return db.collection(COLECCION_ALUMNOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error obteniendo alumnos", error)
                    return@addSnapshotListener
                }

                val lista = mutableListOf<AlumnoFirebase>()

                if (snapshot != null) {
                    for (documento in snapshot.documents) {
                        val alumno = documento.toObject(AlumnoFirebase::class.java)

                        if (alumno != null) {
                            alumno.id = documento.id // importante
                            lista.add(alumno)
                        }
                    }
                }

                listener(lista)
            }
    }
}
