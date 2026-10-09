package com.example.caprichoapp.feature.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelButtonSize
import com.example.caprichoapp.core.designsystem.pixel.PixelWordmark
import com.example.caprichoapp.feature.auth.SessionState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    sessionState: SessionState,
    onRetry: () -> Unit,
    onFinished: (SessionState) -> Unit,
) {
    var minTimeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_500.milliseconds)
        minTimeElapsed = true
    }
    LaunchedEffect(sessionState, minTimeElapsed) {
        val resolved =
            sessionState !is SessionState.Loading && sessionState !is SessionState.ProfileError
        if (minTimeElapsed && resolved) onFinished(sessionState)
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(mood = MascotMood.Idle, modifier = Modifier.size(160.dp))
        Spacer(Modifier.height(24.dp))
        PixelWordmark()

        if (sessionState is SessionState.ProfileError) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "No pudimos cargar tus datos. Revisá tu conexión e intentá de nuevo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            PixelButton(text = "Reintentar", onClick = onRetry, size = PixelButtonSize.Compact)
        }
    }
}
