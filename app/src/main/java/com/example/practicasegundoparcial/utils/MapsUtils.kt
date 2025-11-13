package com.example.practicasegundoparcial.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.example.practicasegundoparcial.R
import com.google.android.gms.maps.CameraUpdateFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors

class MapsUtils(private val activity: Activity) {

    private val fused = LocationServices.getFusedLocationProviderClient(activity)

    private var markerActual: Marker? = null
    private var markerSeleccionado: Marker? = null
    private var circuloActual: Circle? = null
    private var polylineRuta: Polyline? = null

    private val httpClient = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obtenerUbicacionActual(googleMap: GoogleMap, onUbicacionObtenida: (LatLng) -> Unit) {
        if (!permisosGPS()) {
            pedirPermisos()
            return
        }
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                val icono = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions().position(pos).title("UBICACIÓN ACTUAL").icon(icono)
                )
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions().center(pos).radius(40.0)
                        .strokeWidth(3f).strokeColor(0xAA2196F3.toInt()).fillColor(0x332196F3)
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
                val icono = iconoPersonalizado(R.drawable.ubicacion, 110, 110)
                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions().position(pos).title("MI UBICACIÓN").icon(icono)
                )
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions().center(pos).radius(40.0)
                        .strokeWidth(3f).strokeColor(0xAA2196F3.toInt()).fillColor(0x332196F3)
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
                val icono = iconoPersonalizado(R.drawable.ubicacion, 110, 110)
                markerActual?.remove()
                markerActual = googleMap.addMarker(
                    MarkerOptions().position(pos).title("MI UBICACIÓN").icon(icono)
                )
                circuloActual?.remove()
                circuloActual = googleMap.addCircle(
                    CircleOptions().center(pos).radius(40.0)
                        .strokeWidth(3f).strokeColor(0xAA2196F3.toInt()).fillColor(0x332196F3)
                )
                onUbicacion(pos)
            }
        }
    }

    fun seleccionarPuntoEnMapa(googleMap: GoogleMap, pos: LatLng, onSeleccion: (LatLng) -> Unit) {
        markerSeleccionado?.remove()
        markerSeleccionado = googleMap.addMarker(
            MarkerOptions().position(pos)
                .title("UBICACIÓN SELECCIONADA")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        onSeleccion(pos)
    }

    fun iconoPersonalizado(resourceId: Int, ancho: Int, alto: Int): BitmapDescriptor {
        val original = BitmapFactory.decodeResource(activity.resources, resourceId)
        val reducido = Bitmap.createScaledBitmap(original, ancho, alto, false)
        return BitmapDescriptorFactory.fromBitmap(reducido)
    }

    fun agregarMarcadorPersonalizado(map: GoogleMap, pos: LatLng, titulo: String) {
        val icono = iconoPersonalizado(R.drawable.ubicacion, 110, 110)
        map.addMarker(
            MarkerOptions().position(pos).title(titulo).icon(icono)
        )
    }

    fun dibujarRutaOSRM(map: GoogleMap, a: LatLng, b: LatLng) {
        val url = "https://router.project-osrm.org/route/v1/driving/${a.longitude},${a.latitude};${b.longitude},${b.latitude}?overview=full&geometries=polyline"
        executor.execute {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val json = response.body?.string() ?: return@execute
                val jsonObj = JSONObject(json)
                val routes = jsonObj.getJSONArray("routes")
                if (routes.length() == 0) return@execute
                val geometry = routes.getJSONObject(0).getString("geometry")
                val puntos = decodificarPolyline(geometry)
                activity.runOnUiThread {
                    polylineRuta?.remove()
                    polylineRuta = map.addPolyline(
                        PolylineOptions().addAll(puntos).color(0xAA0000FF.toInt()).width(12f)
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun decodificarPolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    fun centrarEn(map: GoogleMap, pos: LatLng, zoom: Float = 16f) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom))
    }

    fun agregarMarcadorAzul(map: GoogleMap, pos: LatLng, titulo: String) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(pos)
                .title(titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        marker?.showInfoWindow()
    }

    fun agregarMarcadorMorado(map: GoogleMap, pos: LatLng, titulo: String) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(pos)
                .title(titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        marker?.showInfoWindow()
    }

    fun agregarMarcadorUbicacionActual(map: GoogleMap, pos: LatLng) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(pos)
                .title("MI UBICACIÓN")
                .icon(iconoPersonalizado(R.drawable.ubicacion, 110, 110))
        )
        marker?.showInfoWindow()
    }

    fun dibujarRutaOSRMVerde(map: GoogleMap, a: LatLng, b: LatLng) {
        val url = "https://router.project-osrm.org/route/v1/driving/${a.longitude},${a.latitude};${b.longitude},${b.latitude}?overview=full&geometries=polyline"
        executor.execute {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val json = response.body?.string() ?: return@execute
                val jsonObj = JSONObject(json)
                val routes = jsonObj.getJSONArray("routes")
                if (routes.length() == 0) return@execute
                val geometry = routes.getJSONObject(0).getString("geometry")
                val puntos = decodificarPolyline(geometry)
                activity.runOnUiThread {
                    polylineRuta?.remove()
                    polylineRuta = map.addPolyline(
                        PolylineOptions().addAll(puntos).color(0xAA00FF00.toInt()).width(12f)
                    )
                }
            } catch (_: Exception) {}
        }
    }

}
