package com.example.caprichoapp.core.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import org.koin.compose.koinInject

/**
 * Ejecuta [onReconnected] cada vez que vuelve internet mientras la pantalla está visible.
 * Se usa para recargar los datos de Supabase que fallaron mientras no había conexión.
 */
@Composable
fun ReconnectEffect(onReconnected: () -> Unit) {
    val monitor = koinInject<ConnectivityMonitor>()
    val latest = rememberUpdatedState(onReconnected)
    LaunchedEffect(monitor) {
        monitor.reconnected.collect { latest.value() }
    }
}
