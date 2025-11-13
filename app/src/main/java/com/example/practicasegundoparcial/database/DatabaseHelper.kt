package com.example.practicasegundoparcial.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NOMBRE, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NOMBRE = "segundoparcial.db"
        const val DATABASE_VERSION = 1

        // TABLA ALUMNO
        const val TABLA_ALUMNO = "Alumno"

        const val ALU_ID = "id"
        const val ALU_CI = "ci"
        const val ALU_NOMBRES = "nombres"
        const val ALU_APELLIDOS = "apellidos"
        const val ALU_FECHA_NAC = "fechaNacimiento"
        const val ALU_LATITUD = "latitud"
        const val ALU_LONGITUD = "longitud"
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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_ALUMNO")
        onCreate(db)
    }
}
