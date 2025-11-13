package com.example.practicasegundoparcial.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.practicasegundoparcial.models.RegistroSensorFirebase

class BaseDatosSensorFirebase(private val context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val COLECCION = "registrosSensor"
    private val TAG = "BaseDatosSensorFirebase"

    fun agregar(
        registro: RegistroSensorFirebase
    ) {
        db.collection(COLECCION)
            .add(registro)
            .addOnSuccessListener { ref ->
                Log.d(TAG, "Registro guardado: ${ref.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar registro: ${e.message}")
            }
    }

    fun obtenerTodos(listener: (List<RegistroSensorFirebase>) -> Unit): ListenerRegistration {
        return db.collection(COLECCION)
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error obteniendo registros", error)
                    return@addSnapshotListener
                }
                val lista = mutableListOf<RegistroSensorFirebase>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val reg = doc.toObject(RegistroSensorFirebase::class.java)
                        if (reg != null) {
                            reg.id = doc.id
                            lista.add(reg)
                        }
                    }
                }
                listener(lista)
            }
    }

    fun eliminarTodos(onFinish: () -> Unit) {
        db.collection(COLECCION)
            .get()
            .addOnSuccessListener { snap ->
                val batch = db.batch()
                for (doc in snap.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    Toast.makeText(context, "Firebase: registros eliminados", Toast.LENGTH_SHORT).show()
                    onFinish()
                }
            }
            .addOnFailureListener {
                onFinish()
            }
    }
}
