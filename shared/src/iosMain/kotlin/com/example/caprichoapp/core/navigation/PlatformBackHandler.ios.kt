package com.example.caprichoapp.core.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS no tiene botón/gesto "atrás" del sistema que haya que interceptar
}
