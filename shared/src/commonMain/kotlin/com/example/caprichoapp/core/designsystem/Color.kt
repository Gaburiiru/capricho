package com.example.caprichoapp.core.designsystem

import androidx.compose.ui.graphics.Color

// Oscuro — "Tamagotchi de noche": negro-verde de fósforo (CRT), lima brillante y plástico rosa.
// Mismos principios que el claro: todos los roles que usan los componentes están definidos con
// tinte verde (sin esto caen a los grises violáceos por defecto de Material 3).
val DarkPrimary = Color(0xFFC8F560)
val DarkOnPrimary = Color(0xFF1A2A00)
val DarkPrimaryContainer = Color(0xFF2C4A00)
val DarkOnPrimaryContainer = Color(0xFFDDFF9A)
val DarkSecondary = Color(0xFFFF9DB5)
val DarkOnSecondary = Color(0xFF4A0D20)
val DarkSecondaryContainer = Color(0xFF5A2638)      // teclas C / borrar del teclado
val DarkOnSecondaryContainer = Color(0xFFFFD9E2)
val DarkBackground = Color(0xFF0E1610)
val DarkSurface = Color(0xFF151F18)                 // barra inferior
val DarkSurfaceVariant = Color(0xFF263A2C)
val DarkOnSurface = Color(0xFFEAF2EC)
val DarkOnSurfaceVariant = Color(0xFF9FB5A2)
val DarkOutline = Color(0xFF4E6B57)                 // bordes de campos, botones y diálogos (línea de fósforo)
val DarkOutlineVariant = Color(0xFF3A5443)          // bordes de tarjetas y sombras duras
val DarkSurfaceContainerLowest = Color(0xFF090F0B)  // recuadros "hundidos" dentro de tarjetas
val DarkSurfaceContainerLow = Color(0xFF121A14)
val DarkSurfaceContainer = Color(0xFF18241B)        // opciones sin elegir
val DarkSurfaceContainerHigh = Color(0xFF1C2A20)    // tarjetas y diálogos
val DarkSurfaceContainerHighest = Color(0xFF263A2C) // campos, pistas de progreso, botón secundario
val DarkError = Color(0xFFFF8A80)
val DarkErrorContainer = Color(0xFF5E2420)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkInverseSurface = Color(0xFFC8F560)          // snackbar: cartelito lima con tinta oscura
val DarkInverseOnSurface = Color(0xFF1A2A00)
val DarkInversePrimary = Color(0xFF3A7000)

// Claro — "Tamagotchi de día": papel crema-lima, tinta verde y plástico rosa.
// En el oscuro la profundidad viene del brillo (lima sobre negro); en el claro viene de la TINTA:
// bordes y sombras duras verdes bien marcados. Todos los roles que usan los componentes están
// definidos a propósito: los que no se definen caen a los violetas por defecto de Material 3.
val LightPrimary = Color(0xFF3A7000)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFCBEB90)       // fondo de la pantalla LCD
val LightOnPrimaryContainer = Color(0xFF1C3300)     // tinta del LCD
val LightSecondary = Color(0xFFB3365A)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFD9E2)     // teclas C / borrar del teclado
val LightOnSecondaryContainer = Color(0xFF5A1229)
val LightBackground = Color(0xFFF1F5DF)             // papel crema-lima
val LightSurface = Color(0xFFFDFEF6)                // barra inferior, tarjetas
val LightSurfaceVariant = Color(0xFFE3EBCA)
val LightOnSurface = Color(0xFF1A2412)              // tinta
val LightOnSurfaceVariant = Color(0xFF4A5A3D)
val LightOutline = Color(0xFF6C7F55)                // bordes de campos y botones (3,9:1 sobre el fondo)
val LightOutlineVariant = Color(0xFF9DB07E)         // bordes de tarjetas y SOMBRAS DURAS (2,1:1)
val LightSurfaceContainerLowest = Color(0xFFEDF2DA) // recuadros "hundidos" dentro de tarjetas
val LightSurfaceContainerLow = Color(0xFFF7FAEA)
val LightSurfaceContainer = Color(0xFFFDFEF6)       // opciones sin elegir
val LightSurfaceContainerHigh = Color(0xFFFDFEF6)   // tarjetas y diálogos
val LightSurfaceContainerHighest = Color(0xFFE3EBCA) // campos, pistas de progreso, botón secundario
val LightError = Color(0xFFB3261E)
val LightErrorContainer = Color(0xFFFFDAD4)
val LightOnErrorContainer = Color(0xFF5C0F0A)
val LightInverseSurface = Color(0xFF1F2A16)         // snackbar
val LightInverseOnSurface = Color(0xFFF1F5DF)
val LightInversePrimary = Color(0xFFC8F560)         // el lima del tema oscuro, como acento
