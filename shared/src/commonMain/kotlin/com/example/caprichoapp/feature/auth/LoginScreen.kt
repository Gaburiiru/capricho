package com.example.caprichoapp.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelButtonVariant
import com.example.caprichoapp.core.designsystem.pixel.PixelWordmark
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onOpenAccount: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onSignInClick = onOpenAccount,
        onSkipClick = viewModel::onSkip,
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(MascotMood.Idle, Modifier.size(140.dp))
        Spacer(Modifier.height(16.dp))
        PixelWordmark()
        Spacer(Modifier.height(8.dp))
        Text(
            "Iniciá sesión para guardar tus gastos y metas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        PixelButton(
            text = if (state.isLoading) "Conectando..." else "Iniciar sesión",
            onClick = onSignInClick,
            enabled = !state.isLoading,
            showArrow = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        PixelButton(
            text = "Saltar sesión (temporal)",
            onClick = onSkipClick,
            enabled = !state.isLoading,
            variant = PixelButtonVariant.Secondary,
            showArrow = false,
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}