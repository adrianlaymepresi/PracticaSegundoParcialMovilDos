package com.example.practicasegundoparcial.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NOMBRE, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NOMBRE = "segundoparcial.db"
        const val DATABASE_VERSION = 2  // Incrementado de 1 a 2 para forzar recreación

        // TABLA ALUMNO
        const val TABLA_ALUMNO = "Alumno"

        const val ALU_ID = "id"
        const val ALU_CI = "ci"
        const val ALU_NOMBRES = "nombres"
        const val ALU_APELLIDOS = "apellidos"
        const val ALU_FECHA_NAC = "fechaNacimiento"
        const val ALU_LATITUD = "latitud"
        const val ALU_LONGITUD = "longitud"

        const val TABLA_SENSOR = "RegistroSensor"
        const val SEN_ID = "id"
        const val SEN_FECHA = "fecha"
        const val SEN_PROX = "proximidad"
        const val SEN_AX = "ax"
        const val SEN_AY = "ay"
        const val SEN_AZ = "az"
        const val SEN_LAT = "latitud"
        const val SEN_LON = "longitud"
        const val SEN_AUDIO = "nombreCancion"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val crearTablaAlumno = """
            CREATE TABLE $TABLA_ALUMNO(
                $ALU_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ALU_CI INTEGER NOT NULL,
                $ALU_NOMBRES TEXT NOT NULL,
                $ALU_APELLIDOS TEXT NOT NULL,
                $ALU_FECHA_NAC TEXT NOT NULL,
                $ALU_LATITUD REAL NOT NULL,
                $ALU_LONGITUD REAL NOT NULL
            );
        """.trimIndent()

        db?.execSQL(crearTablaAlumno)

        val crearTablaSensor = """
            CREATE TABLE $TABLA_SENSOR(
                $SEN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $SEN_FECHA TEXT NOT NULL,
                $SEN_PROX REAL NOT NULL,
                $SEN_AX REAL NOT NULL,
                $SEN_AY REAL NOT NULL,
                $SEN_AZ REAL NOT NULL,
                $SEN_LAT REAL NOT NULL,
                $SEN_LON REAL NOT NULL,
                $SEN_AUDIO TEXT NOT NULL
            );
        """.trimIndent()

        db?.execSQL(crearTablaSensor)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_ALUMNO")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_SENSOR")
        onCreate(db)
    }
}
