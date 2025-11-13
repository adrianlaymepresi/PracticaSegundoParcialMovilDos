package com.example.practicasegundoparcial.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.practicasegundoparcial.models.RegistroSensor

class RegistroSensorDAO(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val TAG = "RegistroSensorDAO"

    fun insertar(reg: RegistroSensor): Long {
        var db: SQLiteDatabase? = null
        return try {
            db = dbHelper.writableDatabase
            val valores = ContentValues().apply {
                put(DatabaseHelper.SEN_FECHA, reg.fecha)
                put(DatabaseHelper.SEN_PROX, reg.proximidad)
                put(DatabaseHelper.SEN_AX, reg.ax)
                put(DatabaseHelper.SEN_AY, reg.ay)
                put(DatabaseHelper.SEN_AZ, reg.az)
                put(DatabaseHelper.SEN_LAT, reg.latitud)
                put(DatabaseHelper.SEN_LON, reg.longitud)
                put(DatabaseHelper.SEN_AUDIO, reg.nombreCancion)
            }
            val id = db.insert(DatabaseHelper.TABLA_SENSOR, null, valores)
            Log.d(TAG, "Registro insertado en SQLite con ID: $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar registro en SQLite: ${e.message}")
            -1L
        } finally {
            db?.close()
        }
    }

    fun obtenerTodos(): List<RegistroSensor> {
        val lista = mutableListOf<RegistroSensor>()
        var db: SQLiteDatabase? = null
        return try {
            db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM ${DatabaseHelper.TABLA_SENSOR} ORDER BY ${DatabaseHelper.SEN_ID} DESC",
                null
            )

            cursor.use {
                while (it.moveToNext()) {
                    try {
                        val reg = RegistroSensor(
                            id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.SEN_ID)),
                            fecha = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.SEN_FECHA)),
                            proximidad = it.getFloat(it.getColumnIndexOrThrow(DatabaseHelper.SEN_PROX)),
                            ax = it.getFloat(it.getColumnIndexOrThrow(DatabaseHelper.SEN_AX)),
                            ay = it.getFloat(it.getColumnIndexOrThrow(DatabaseHelper.SEN_AY)),
                            az = it.getFloat(it.getColumnIndexOrThrow(DatabaseHelper.SEN_AZ)),
                            latitud = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.SEN_LAT)),
                            longitud = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.SEN_LON)),
                            nombreCancion = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.SEN_AUDIO))
                        )
                        lista.add(reg)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al leer registro de SQLite: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Total registros obtenidos de SQLite: ${lista.size}")
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener registros de SQLite: ${e.message}")
            emptyList()
        } finally {
            db?.close()
        }
    }

    fun eliminarTodos() {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            val count = db.delete(DatabaseHelper.TABLA_SENSOR, null, null)
            Log.d(TAG, "Registros eliminados de SQLite: $count")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar registros de SQLite: ${e.message}")
        } finally {
            db?.close()
        }
    }
}
