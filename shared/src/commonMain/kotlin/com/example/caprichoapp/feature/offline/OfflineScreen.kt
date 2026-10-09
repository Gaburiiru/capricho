package com.example.caprichoapp.feature.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.mascot.PixelSprite
import kotlinx.coroutines.delay

/**
 * Pantalla de "sin conexión". Se dibuja ENCIMA de la pantalla en la que estaba el usuario
 * (no la reemplaza), así el estado queda intacto y al volver internet se sigue desde ahí.
 * No tiene botones: se va sola cuando vuelve la conexión.
 */
@Composable
fun OfflineScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            // Tapa lo de abajo: ningún toque llega a la pantalla que quedó atrás
            .pointerInput(Unit) {}
            .semantics { contentDescription = "No hay conexión a internet. Buscando conexión." }
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(
            mood = MascotMood.Searching,
            modifier = Modifier.size(160.dp),
            enabled = false, // quieta: lo que se mueve son los ojos y la señal
        )
        Spacer(Modifier.height(28.dp))
        SearchingSignal(Modifier.size(width = 56.dp, height = 51.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "NO HAY CONEXIÓN A INTERNET",
            style = CaprichoTheme.pixelText.title,
            color = colors.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Buscando conexión",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
            // Ancho fijo para que los puntitos no muevan el texto
            AnimatedDots(Modifier.width(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Apenas vuelva, seguís justo donde estabas.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AnimatedDots(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(DOTS_STEP_MS)
            count = (count + 1) % 4
        }
    }
    Text(
        text = ".".repeat(count),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Ícono de señal (wifi) pixel art que se va encendiendo de a un arco, como buscando red. */
@Composable
private fun SearchingSignal(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val palette = remember(colors) {
        mapOf('X' to colors.primary, 'D' to colors.onSurfaceVariant.copy(alpha = 0.25f))
    }
    var level by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(SIGNAL_STEP_MS)
            level = (level + 1) % (WIFI_LEVELS + 1)
        }
    }
    PixelSprite(rows = remember(level) { wifiFrame(level) }, palette = palette, modifier = modifier)
}

private const val DOTS_STEP_MS = 450L
private const val SIGNAL_STEP_MS = 450L
private const val WIFI_LEVELS = 3

// 'X' = tinta. Filas 0-2: arco externo · 3-4: medio · 5-6: interno · 8-9: punto (siempre prendido).
internal val WIFI_FULL = listOf(
    "..XXXXXXX..",
    ".XX.....XX.",
    "XX.......XX",
    "...XXXXX...",
    "..XX...XX..",
    "....XXX....",
    "...XX.XX...",
    "...........",
    "....XXX....",
    "....XXX....",
)

/** [level] 0 = solo el punto; 3 = señal completa. Los arcos apagados quedan tenues ('D'). */
internal fun wifiFrame(level: Int): List<String> = WIFI_FULL.mapIndexed { index, row ->
    val lit = when {
        index >= 8 -> true
        index >= 5 -> level >= 1
        index >= 3 -> level >= 2
        else -> level >= 3
    }
    if (lit) row else row.replace('X', 'D')
}
