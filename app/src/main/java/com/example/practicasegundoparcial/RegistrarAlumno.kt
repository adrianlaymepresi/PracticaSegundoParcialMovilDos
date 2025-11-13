package com.example.practicasegundoparcial

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.practicasegundoparcial.database.AlumnoDAO
import com.example.practicasegundoparcial.firebase.AlumnoFirebase
import com.example.practicasegundoparcial.firebase.BaseDatosFirebase
import com.example.practicasegundoparcial.models.Alumno
import com.example.practicasegundoparcial.utils.MapsUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.*

class RegistrarAlumno : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etCi: EditText
    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etFecha: EditText
    private lateinit var etLat: EditText
    private lateinit var etLng: EditText
    private lateinit var btnActual: Button
    private lateinit var btnRegistrar: Button

    private lateinit var googleMap: GoogleMap
    private lateinit var mapsUtils: MapsUtils

    private lateinit var alumnoDAO: AlumnoDAO
    private lateinit var firebaseDB: BaseDatosFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_alumno)

        alumnoDAO = AlumnoDAO(this)
        firebaseDB = BaseDatosFirebase(this)
        mapsUtils = MapsUtils(this)

        etCi = findViewById(R.id.etCi)
        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        etFecha = findViewById(R.id.etFecha)
        etLat = findViewById(R.id.etLatitud)
        etLng = findViewById(R.id.etLongitud)
        btnActual = findViewById(R.id.btnUbicacionActual)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)

        etFecha.setOnClickListener { abrirDatePicker() }

        btnActual.setOnClickListener { usarUbicacionActual() }

        btnRegistrar.setOnClickListener { registrarAlumno() }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // UBICACIÓN ACTUAL AUTOMÁTICA AL ABRIR EL MAPA
        mapsUtils.mostrarUbicacionInicialEnMapa(googleMap) { pos ->
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
        }

        // CLIC PARA UBICACIÓN SELECCIONADA
        googleMap.setOnMapClickListener { punto ->
            mapsUtils.seleccionarPuntoEnMapa(googleMap, punto) {
                etLat.setText(it.latitude.toString())
                etLng.setText(it.longitude.toString())
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))
        }
    }

    private fun abrirDatePicker() {
        val hoy = Calendar.getInstance()
        val year = hoy.get(Calendar.YEAR)
        val month = hoy.get(Calendar.MONTH)
        val day = hoy.get(Calendar.DAY_OF_MONTH)

        val dp = DatePickerDialog(
            this,
            { _, y, m, d ->
                etFecha.setText("$d/${m + 1}/$y")
            },
            year, month, day
        )

        dp.datePicker.maxDate = hoy.timeInMillis

        val min = Calendar.getInstance()
        min.set(1900, 0, 1)
        dp.datePicker.minDate = min.timeInMillis

        dp.show()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun usarUbicacionActual() {
        mapsUtils.obtenerUbicacionActualImagen(googleMap) {
            etLat.setText(it.latitude.toString())
            etLng.setText(it.longitude.toString())
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    private fun registrarAlumno() {

        val ciText = etCi.text.toString()
        val nombres = etNombres.text.toString()
        val apellidos = etApellidos.text.toString()
        val fecha = etFecha.text.toString()
        val latText = etLat.text.toString()
        val lngText = etLng.text.toString()

        if (ciText.isBlank() || nombres.isBlank() || apellidos.isBlank() ||
            fecha.isBlank() || latText.isBlank() || lngText.isBlank()
        ) {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (ciText.length > 9) {
            Toast.makeText(this, "CI máximo 9 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombres.length > 50 || apellidos.length > 50) {
            Toast.makeText(this, "Nombre/apellido demasiado largo", Toast.LENGTH_SHORT).show()
            return
        }

        val ci = ciText.toInt()
        val lat = latText.toDouble()
        val lng = lngText.toDouble()

        // verificar CI único
        if (alumnoDAO.buscarPorCI(ci) != null) {
            Toast.makeText(this, "CI ya registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // INSERTAR EN SQLITE
        val alumno = Alumno(0, ci, nombres, apellidos, fecha, lat, lng)
        alumnoDAO.insertar(alumno)

        // INSERTAR EN FIREBASE
        val alumnoFB = AlumnoFirebase(
            null, ci, nombres, apellidos, fecha, lat, lng
        )

        firebaseDB.agregarAlumno(alumnoFB, {
            Toast.makeText(this, "Alumno registrado correctamente", Toast.LENGTH_LONG).show()
            limpiarCampos()
        }, {
            Toast.makeText(this, "Error en Firebase", Toast.LENGTH_SHORT).show()
        })
    }

    private fun limpiarCampos() {
        etCi.text.clear()
        etNombres.text.clear()
        etApellidos.text.clear()
        etFecha.text.clear()
        etLat.text.clear()
        etLng.text.clear()
    }
}
