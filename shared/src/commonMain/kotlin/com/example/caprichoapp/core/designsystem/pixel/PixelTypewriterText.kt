package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

/**
 * Texto que se "tipea" letra por letra. Reserva el espacio del texto completo
 * (con [minLines]) para que el layout no salte mientras escribe.
 */
@Composable
fun PixelTypewriterText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    charDelayMs: Long = 38,
    onTypingChanged: (Boolean) -> Unit = {},
) {
    var visibleChars by remember(text) { mutableIntStateOf(0) }
    val currentOnTypingChanged by rememberUpdatedState(onTypingChanged)

    LaunchedEffect(text) {
        visibleChars = 0
        currentOnTypingChanged(true)
        while (visibleChars < text.length) {
            delay(charDelayMs)
            visibleChars++
        }
        currentOnTypingChanged(false)
    }

    // Para TalkBack: lee siempre el texto completo, no la versión a medio escribir
    Box(modifier.clearAndSetSemantics { contentDescription = text }) {
        Text(
            text = text,
            style = style,
            color = Color.Transparent,
            textAlign = TextAlign.Center,
            minLines = minLines,
        )
        Text(
            text = text.take(visibleChars),
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            minLines = minLines,
        )
    }
}
