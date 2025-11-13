package com.example.practicasegundoparcial

import android.Manifest
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.practicasegundoparcial.database.AlumnoDAO
import com.example.practicasegundoparcial.models.Alumno
import com.example.practicasegundoparcial.utils.MapsUtils
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class RutaDosAlumnosActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var spA: Spinner
    private lateinit var spB: Spinner
    private lateinit var btnRuta: Button
    private lateinit var googleMap: GoogleMap
    private lateinit var mapsUtils: MapsUtils
    private lateinit var dao: AlumnoDAO
    private lateinit var alumnos: List<Alumno>
    private var ubicacionActual: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_dos_alumnos)

        dao = AlumnoDAO(this)
        mapsUtils = MapsUtils(this)

        spA = findViewById(R.id.spAlumnoA)
        spB = findViewById(R.id.spAlumnoB)
        btnRuta = findViewById(R.id.btnMostrarRuta)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapaRuta) as SupportMapFragment
        mapFragment.getMapAsync(this)

        cargarSpinners()
        btnRuta.setOnClickListener { mostrarRutaEntreAlumnos() }
    }

    private fun cargarSpinners() {
        alumnos = dao.obtenerTodos()

        val nombres = mutableListOf("MI UBICACIÓN ACTUAL")
        nombres.addAll(alumnos.map { "${it.ci} - ${it.nombres} ${it.apellidos}" })

        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        spA.adapter = adaptador
        spB.adapter = adaptador
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        mapsUtils.obtenerUbicacionActualImagen(googleMap) {
            ubicacionActual = it
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
            mapsUtils.agregarMarcadorUbicacionActual(googleMap, it)
        }
    }

    private fun obtenerPunto(pos: Int): LatLng? {
        return if (pos == 0) ubicacionActual
        else {
            val alumno = alumnos[pos - 1]
            LatLng(alumno.latitud, alumno.longitud)
        }
    }

    private fun mostrarRutaEntreAlumnos() {
        val posA = spA.selectedItemPosition
        val posB = spB.selectedItemPosition

        val puntoA = obtenerPunto(posA)
        val puntoB = obtenerPunto(posB)

        if (puntoA == null || puntoB == null) {
            Toast.makeText(this, "Esperando ubicación actual", Toast.LENGTH_SHORT).show()
            return
        }

        if (posA == posB) {
            Toast.makeText(this, "Seleccione dos puntos distintos", Toast.LENGTH_SHORT).show()
            return
        }

        googleMap.clear()

        if (posA == 0)
            mapsUtils.agregarMarcadorAzul(googleMap, puntoA, "MI UBICACIÓN")
        else
            mapsUtils.agregarMarcadorAzul(googleMap, puntoA, alumnos[posA - 1].nombres)

        if (posB == 0)
            mapsUtils.agregarMarcadorMorado(googleMap, puntoB, "MI UBICACIÓN")
        else
            mapsUtils.agregarMarcadorMorado(googleMap, puntoB, alumnos[posB - 1].nombres)

        mapsUtils.dibujarRutaOSRMVerde(googleMap, puntoA, puntoB)

        val bounds = LatLngBounds.builder().include(puntoA).include(puntoB).build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
    }
}
