package com.example.practicasegundoparcial.database

import android.content.ContentValues
import android.content.Context
import com.example.practicasegundoparcial.models.Alumno

class AlumnoDAO(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // INSERTAR
    fun insertar(alumno: Alumno): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues()

        valores.put(DatabaseHelper.ALU_CI, alumno.ci)
        valores.put(DatabaseHelper.ALU_NOMBRES, alumno.nombres)
        valores.put(DatabaseHelper.ALU_APELLIDOS, alumno.apellidos)
        valores.put(DatabaseHelper.ALU_FECHA_NAC, alumno.fechaNacimiento)
        valores.put(DatabaseHelper.ALU_LATITUD, alumno.latitud)
        valores.put(DatabaseHelper.ALU_LONGITUD, alumno.longitud)

        val id = db.insert(DatabaseHelper.TABLA_ALUMNO, null, valores)
        db.close()
        return id
    }

    // OBTENER TODOS
    fun obtenerTodos(): List<Alumno> {
        val lista = mutableListOf<Alumno>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLA_ALUMNO}", null)

        while (cursor.moveToNext()) {
            val alumno = Alumno(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_ID)),
                ci = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_CI)),
                nombres = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_NOMBRES)),
                apellidos = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_APELLIDOS)),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_FECHA_NAC)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LATITUD)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LONGITUD))
            )
            lista.add(alumno)
        }

        cursor.close()
        db.close()
        return lista
    }

    // OBTENER POR ID (PK autoincremental)
    fun obtenerPorId(idAlumno: Int): Alumno? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_ALUMNO} WHERE ${DatabaseHelper.ALU_ID}=?",
            arrayOf(idAlumno.toString())
        )

        var alumno: Alumno? = null

        if (cursor.moveToFirst()) {
            alumno = Alumno(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_ID)),
                ci = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_CI)),
                nombres = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_NOMBRES)),
                apellidos = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_APELLIDOS)),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_FECHA_NAC)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LATITUD)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LONGITUD))
            )
        }

        cursor.close()
        db.close()
        return alumno
    }

    // BUSCAR POR CI
    fun buscarPorCI(ciBuscar: Int): Alumno? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLA_ALUMNO} WHERE ${DatabaseHelper.ALU_CI}=?",
            arrayOf(ciBuscar.toString())
        )

        var alumno: Alumno? = null

        if (cursor.moveToFirst()) {
            alumno = Alumno(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_ID)),
                ci = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_CI)),
                nombres = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_NOMBRES)),
                apellidos = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_APELLIDOS)),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_FECHA_NAC)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LATITUD)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ALU_LONGITUD))
            )
        }

        cursor.close()
        db.close()
        return alumno
    }

    // ACTUALIZAR
    fun actualizar(alumno: Alumno): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues()

        valores.put(DatabaseHelper.ALU_CI, alumno.ci)
        valores.put(DatabaseHelper.ALU_NOMBRES, alumno.nombres)
        valores.put(DatabaseHelper.ALU_APELLIDOS, alumno.apellidos)
        valores.put(DatabaseHelper.ALU_FECHA_NAC, alumno.fechaNacimiento)
        valores.put(DatabaseHelper.ALU_LATITUD, alumno.latitud)
        valores.put(DatabaseHelper.ALU_LONGITUD, alumno.longitud)

        val filas = db.update(
            DatabaseHelper.TABLA_ALUMNO,
            valores,
            "${DatabaseHelper.ALU_ID}=?",
            arrayOf(alumno.id.toString())
        )

        db.close()
        return filas
    }

    // ELIMINAR
    fun eliminar(idAlumno: Int): Int {
        val db = dbHelper.writableDatabase

        val filas = db.delete(
            DatabaseHelper.TABLA_ALUMNO,
            "${DatabaseHelper.ALU_ID}=?",
            arrayOf(idAlumno.toString())
        )

        db.close()
        return filas
    }
}
