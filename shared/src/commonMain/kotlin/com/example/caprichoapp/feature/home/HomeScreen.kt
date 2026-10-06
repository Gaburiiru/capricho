package com.example.caprichoapp.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconImage
import com.example.caprichoapp.core.designsystem.pixel.PixelTypewriterText
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiColors
import com.example.caprichoapp.core.designsystem.pixelFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MASCOT_NAME = "Capi"
private const val HOLD_BETWEEN_MESSAGES_MS = 3_500L

/** Lo que dice el bicho en su pantalla, uno tras otro. */
private val DEVICE_MESSAGES = listOf(
    "¡Hola! Soy $MASCOT_NAME. ¿Qué capricho se te antoja hoy?",
    "Contame cuánto cuesta y te digo cuánto pesa en tu bolsillo.",
    "Sin culpa: lo planeamos juntos y vos decidís.",
)

/**
 * Pantalla principal. [onStartCapricho] arranca el flujo del capricho;
 * mientras ese flujo no exista (null), el botón avisa con un mensaje en vez de quedar muerto.
 */
@Composable
fun HomeScreen(
    greetingName: String?,
    onStartCapricho: (() -> Unit)? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Entrada suave de toda la pantalla
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 24.dp.toPx()
                },
        ) {
            GreetingHeader(greetingName)
            Spacer(Modifier.height(20.dp))
            CaprichoDevice()
            Spacer(Modifier.height(20.dp))
            PixelButton(
                text = "Predecir mi capricho",
                onClick = {
                    if (onStartCapricho != null) {
                        onStartCapricho()
                    } else {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("¡Ya casi! Muy pronto vas a poder predecir tu capricho.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Tocá a $MASCOT_NAME para saludarlo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = pixelFontFamily(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

@Composable
private fun GreetingHeader(name: String?) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = if (name.isNullOrBlank()) "¡Hola!" else "¡Hola, $name!",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 34.sp,
                lineHeight = 40.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Tu bolsillo y vos, del mismo lado.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** El "bicho virtual": mascota animada que habla y rota entre sus mensajes. */
@Composable
private fun CaprichoDevice() {
    var typing by remember { mutableStateOf(true) }
    var messageIndex by rememberSaveable { mutableIntStateOf(0) }

    // Cuando termina de escribir, espera un rato y pasa al mensaje siguiente
    LaunchedEffect(messageIndex, typing) {
        if (!typing) {
            delay(HOLD_BETWEEN_MESSAGES_MS)
            messageIndex = (messageIndex + 1) % DEVICE_MESSAGES.size
        }
    }

    TamagotchiCard(Modifier.fillMaxWidth()) { device ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LcdStatusRow(device)
            Spacer(Modifier.height(6.dp))
            Mascot(
                mood = if (typing) MascotMood.Talking else MascotMood.Idle,
                modifier = Modifier.size(132.dp),
            )
            Spacer(Modifier.height(10.dp))
            PixelTypewriterText(
                text = DEVICE_MESSAGES[messageIndex],
                style = TextStyle(
                    fontFamily = pixelFontFamily(),
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                ),
                color = device.lcdInk,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                onTypingChanged = { typing = it },
            )
        }
    }
}

/** Fila superior de la pantalla: corazones (a futuro, salud financiera) y el nombre del bicho. */
@Composable
private fun LcdStatusRow(device: TamagotchiColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) {
                PixelIconImage(PixelIcon.Heart, device.lcdInk, Modifier.width(14.dp))
            }
        }
        Text(
            text = MASCOT_NAME.uppercase(),
            color = device.lcdInk,
            fontFamily = pixelFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
        )
    }
}
