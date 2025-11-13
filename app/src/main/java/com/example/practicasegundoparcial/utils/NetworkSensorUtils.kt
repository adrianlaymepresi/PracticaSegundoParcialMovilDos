package com.example.practicasegundoparcial.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkSensorUtils(
    private val context: Context,
    private val onNetworkChange: (String) -> Unit
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun iniciarMonitoreo() {
        // Obtener estado inicial
        actualizarEstadoRed()

        // Crear callback para monitorear cambios
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                actualizarEstadoRed()
            }

            override fun onLost(network: Network) {
                actualizarEstadoRed()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                actualizarEstadoRed()
            }
        }

        // Registrar el callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback?.let {
            connectivityManager.registerNetworkCallback(networkRequest, it)
        }
    }

    fun detenerMonitoreo() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        networkCallback = null
    }

    private fun actualizarEstadoRed() {
        val estado = obtenerEstadoConexion()
        onNetworkChange(estado)
    }

    private fun obtenerEstadoConexion(): String {
        val network = connectivityManager.activeNetwork
        if (network == null) {
            return "❌ Sin conexión a Internet"
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            return "❌ Sin conexión a Internet"
        }

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                "📶 Conectado a WiFi"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                "📱 Conectado a Datos Móviles"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                "🔌 Conectado a Ethernet"
            }
            else -> {
                "❌ Sin conexión a Internet"
            }
        }
    }
}
