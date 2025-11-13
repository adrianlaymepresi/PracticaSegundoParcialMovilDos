package com.example.practicasegundoparcial.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.practicasegundoparcial.models.RegistroSensor

class RegistroSensorDAO(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun insertar(reg: RegistroSensor): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues()
        valores.put(DatabaseHelper.SEN_FECHA, reg.fecha)
        valores.put(DatabaseHelper.SEN_PROX, reg.proximidad)
        valores.put(DatabaseHelper.SEN_AX, reg.ax)
        valores.put(DatabaseHelper.SEN_AY, reg.ay)
        valores.put(DatabaseHelper.SEN_AZ, reg.az)
        valores.put(DatabaseHelper.SEN_LAT, reg.latitud)
        valores.put(DatabaseHelper.SEN_LON, reg.longitud)
        valores.put(DatabaseHelper.SEN_AUDIO, reg.nombreCancion)
        val id = db.insert(DatabaseHelper.TABLA_SENSOR, null, valores)
        db.close()
        return id
    }

    fun obtenerTodos(): List<RegistroSensor> {
        val lista = mutableListOf<RegistroSensor>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_SENSOR} ORDER BY ${DatabaseHelper.SEN_ID} DESC",
            null
        )
        while (cursor.moveToNext()) {
            val reg = RegistroSensor(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_ID)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_FECHA)),
                proximidad = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_PROX)),
                ax = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_AX)),
                ay = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_AY)),
                az = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_AZ)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_LAT)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_LON)),
                nombreCancion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SEN_AUDIO))
            )
            lista.add(reg)
        }
        cursor.close()
        db.close()
        return lista
    }

    fun eliminarTodos() {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLA_SENSOR, null, null)
        db.close()
    }
}
