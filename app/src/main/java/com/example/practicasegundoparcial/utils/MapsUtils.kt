package com.example.practicasegundoparcial.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.example.practicasegundoparcial.R

class MapsUtils(private val activity: Activity) {

    private val fused = LocationServices.getFusedLocationProviderClient(activity)

    private var markerActual: Marker? = null
    private var markerSeleccionado: Marker? = null
    private var circuloActual: Circle? = null

    companion object {
        const val REQUEST_LOCATION = 200
    }

    fun permisosGPS(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    fun pedirPermisos() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION
        )
    }

    // ============================================================
    // MÉTODO PRINCIPAL: UBICACIÓN ACTUAL + CÍRCULO AZUL
    // ============================================================
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obtenerUbicacionActual(googleMap: GoogleMap, onUbicacionObtenida: (LatLng) -> Unit) {

        if (!permisosGPS()) {
            pedirPermisos()
            return
        }

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)

                // ICONO AZUL
                val icono = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)

                // MARCADOR
                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("UBICACIÓN ACTUAL")
                        .icon(icono)
                )

                // CÍRCULO
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions()
                        .center(pos)
                        .radius(40.0) // radio 40 metros
                        .strokeWidth(3f)
                        .strokeColor(0xAA2196F3.toInt()) // borde azul
                        .fillColor(0x332196F3) // azul muy transparente
                )

                onUbicacionObtenida(pos)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obtenerUbicacionActualImagen(googleMap: GoogleMap, onUbicacionObtenida: (LatLng) -> Unit) {

        if (!permisosGPS()) {
            pedirPermisos()
            return
        }

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {

                val pos = LatLng(loc.latitude, loc.longitude)

                // 🔥 USAMOS EL MISMO ICONO PERSONALIZADO 🔥
                val icono = iconoPersonalizado(R.drawable.ubicacion, 110, 110)

                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("MI UBICACIÓN")
                        .icon(icono)
                )

                // Círculo azul
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions()
                        .center(pos)
                        .radius(40.0)
                        .strokeWidth(3f)
                        .strokeColor(0xAA2196F3.toInt())
                        .fillColor(0x332196F3)
                )

                onUbicacionObtenida(pos)
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun mostrarUbicacionInicialEnMapa(googleMap: GoogleMap, onUbicacion: (LatLng) -> Unit) {

        if (!permisosGPS()) {
            pedirPermisos()
            return
        }

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {

                val pos = LatLng(loc.latitude, loc.longitude)

                // MARCADOR PERSONALIZADO
                val icono = iconoPersonalizado(R.drawable.ubicacion, 110, 110)

                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("MI UBICACIÓN")
                        .icon(icono)
                )

                // CÍRCULO AZUL
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions()
                        .center(pos)
                        .radius(40.0)
                        .strokeWidth(3f)
                        .strokeColor(0xAA2196F3.toInt())
                        .fillColor(0x332196F3)
                )

                onUbicacion(pos)
            }
        }
    }


    // ============================================================
    // MARCADOR DE UBICACIÓN SELECCIONADA (color morado)
    // ============================================================
    fun seleccionarPuntoEnMapa(googleMap: GoogleMap, pos: LatLng, onSeleccion: (LatLng) -> Unit) {
        markerSeleccionado?.remove()
        markerSeleccionado = googleMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title("UBICACIÓN SELECCIONADA")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        onSeleccion(pos)
    }

    // ============================================================
    // MARCADOR CON ICONO PERSONALIZADO
    // ============================================================
    fun iconoPersonalizado(resourceId: Int, ancho: Int, alto: Int): BitmapDescriptor {
        val original = BitmapFactory.decodeResource(activity.resources, resourceId)
        val reducido = Bitmap.createScaledBitmap(original, ancho, alto, false)
        return BitmapDescriptorFactory.fromBitmap(reducido)
    }
}
