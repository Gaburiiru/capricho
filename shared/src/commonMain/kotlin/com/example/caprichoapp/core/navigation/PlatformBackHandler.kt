package com.example.caprichoapp.core.navigation

import androidx.compose.runtime.Composable

/**
 * Intercepta el gesto/botón "atrás" del sistema mientras [enabled]. En Android usa el
 * BackHandler de la actividad; en iOS no hace nada (allá no existe ese gesto).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
