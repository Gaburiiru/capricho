package com.example.caprichoapp.feature.strategy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconButton
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.domain.model.StrategyResponse
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StrategyScreen(
    goalId: String,
    onNavigateBack: () -> Unit,
    viewModel: StrategyViewModel = koinViewModel(parameters = { parametersOf(goalId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // Volver como ícono (igual que en el flujo del capricho) y el título al lado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PixelIconButton(
                icon = PixelIcon.ArrowLeft,
                contentDescription = "Volver",
                onClick = onNavigateBack,
                buttonSize = 48.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ESTRATEGIA",
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 22.sp),
                    color = colors.primary,
                )
                Text(
                    text = "Ahorro inteligente para tu meta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        when (val state = uiState) {
            is StrategyUiState.Loading -> {
                StatusMessage(
                    mood = MascotMood.Thinking,
                    message = "Estoy armando tu estrategia...",
                )
            }

            is StrategyUiState.Error -> {
                StatusMessage(
                    mood = MascotMood.Worried,
                    message = state.message,
                    action = {
                        PixelButton(
                            text = "Reintentar",
                            onClick = viewModel::loadStrategy,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                )
            }

            is StrategyUiState.Success -> {
                StrategyContent(state.data)
            }
        }
    }
}

/** Mascota con un mensaje, centrada en la pantalla: sirve para "cargando" y para los errores. */
@Composable
private fun StatusMessage(
    mood: MascotMood,
    message: String,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TamagotchiCard(modifier = Modifier.fillMaxWidth(), brand = null, showControls = false) { lcd ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Mascot(mood = mood, modifier = Modifier.size(104.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = lcd.lcdInk,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
private fun StrategyContent(data: StrategyResponse) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        // El resumen lo "dice" la mascota, como en el resto de la app
        item {
            TamagotchiCard(Modifier.fillMaxWidth(), brand = null, showControls = false) { lcd ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Mascot(mood = MascotMood.Celebrating, modifier = Modifier.size(72.dp))
                    Text(
                        text = data.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = lcd.lcdInk,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (!data.estimatedSavings.isNullOrBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primaryContainer, shape)
                        .border(2.dp, colors.primary, shape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "AHORRO ESTIMADO",
                        style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                    Text(
                        text = data.estimatedSavings,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onPrimaryContainer,
                    )
                }
            }
        }

        item {
            Text(
                text = "PASOS PARA LOGRARLO",
                style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
                color = colors.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        itemsIndexed(data.tips) { index, tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHigh, shape)
                    .border(2.dp, colors.outlineVariant, shape)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Número del consejo, para poder seguirlos en orden
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(colors.primary, PixelCutShape(2.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = CaprichoTheme.pixelText.tag.copy(fontSize = 13.sp),
                        color = colors.onPrimary,
                    )
                }
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
