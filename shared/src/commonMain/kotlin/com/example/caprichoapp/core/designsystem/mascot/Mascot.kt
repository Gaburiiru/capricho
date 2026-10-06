package com.example.caprichoapp.core.designsystem.mascot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val REACTION_MS = 1_400L

/**
 * Mascota pixel art. El [mood] viene de afuera (lo decide el ViewModel);
 * además reacciona sola al toque: salta y se pone feliz un momento.
 */
@Composable
fun Mascot(
    mood: MascotMood,
    modifier: Modifier = Modifier,
) {
    // Reacción al toque: cada toque reinicia el temporizador
    var reactions by remember { mutableIntStateOf(0) }
    var reacting by remember { mutableStateOf(false) }
    LaunchedEffect(reactions) {
        if (reactions > 0) {
            reacting = true
            delay(REACTION_MS.milliseconds)
            reacting = false
        }
    }

    val shownMood = if (reacting) MascotMood.Happy else mood
    val frames = remember(shownMood) { MascotSprites.framesFor(shownMood) }
    var frameIndex by remember(shownMood) { mutableIntStateOf(0) }

    LaunchedEffect(shownMood) {
        while (true) {
            delay(frames[frameIndex].durationMs.milliseconds)
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    val colors = MaterialTheme.colorScheme
    val palette = remember(colors) {
        mapOf(
            'B' to colors.primary,
            'K' to colors.background,
            'P' to colors.secondary,
            'N' to colors.onPrimaryContainer,
            'H' to colors.secondary,
        )
    }

    // Rebote suave constante
    val bounce by rememberInfiniteTransition(label = "bounce").animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label = "bounceY",
    )

    // Salto al tocar
    val jump = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    PixelSprite(
        rows = frames[frameIndex].rows,
        palette = palette,
        modifier = modifier
            .offset { IntOffset(0, (bounce + jump.value).dp.roundToPx()) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                reactions++
                scope.launch {
                    jump.animateTo(-24f, tween(120))
                    jump.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            },
    )
}
