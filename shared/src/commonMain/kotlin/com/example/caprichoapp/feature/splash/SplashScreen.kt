package com.example.caprichoapp.feature.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.domain.repository.AuthState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    authState: AuthState,
    onFinished: (AuthState) -> Unit,
) {
    var minTimeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_500.milliseconds)
        minTimeElapsed = true
    }
    LaunchedEffect(authState, minTimeElapsed) {
        if (minTimeElapsed && authState !is AuthState.Loading) onFinished(authState)
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(mood = MascotMood.Idle, modifier = Modifier.size(160.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Capricho",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}