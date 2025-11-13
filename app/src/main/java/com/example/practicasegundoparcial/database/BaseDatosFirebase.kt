package com.example.practicasegundoparcial.firebase

import android.content.Context
import android.util.Log
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
                Log.d(TAG, "Alumno guardado con ID: ${referencia.id}")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error al guardar alumno: ${error.message}")
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
    fun eliminarAlumnoPorId(id: String, onSuccess: () -> Unit) {
        db.collection(COLECCION_ALUMNOS)
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }

    fun eliminarAlumnoPorCI(ci: Int, onSuccess: () -> Unit) {
        db.collection(COLECCION_ALUMNOS)
            .whereEqualTo("ci", ci)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (documento in querySnapshot.documents) {
                    documento.reference.delete()
                }
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error al eliminar por CI: ${error.message}")
            }
    }
}
