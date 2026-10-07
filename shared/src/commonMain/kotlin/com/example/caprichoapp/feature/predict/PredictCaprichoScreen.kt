package com.example.caprichoapp.feature.predict

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconImage
import com.example.caprichoapp.core.designsystem.pixel.PixelNumericKeypad
import com.example.caprichoapp.core.designsystem.pixel.PixelPillGroup
import com.example.caprichoapp.core.designsystem.pixel.PixelPillOption
import com.example.caprichoapp.core.designsystem.pixel.PixelTypewriterText
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.util.amountFontSizeSp
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Durability
import org.koin.compose.viewmodel.koinViewModel

// Estimaciones para repartir el alto del paso 1 entre la tarjeta del monto y el teclado
private val AmountCardEstimate = 196.dp
private val CardKeypadGap = 16.dp
private val KeypadRowGaps = 30.dp // 3 separaciones de 10.dp

@Composable
fun PredictCaprichoScreen(
    onNavigateBack: () -> Unit,
    viewModel: PredictCaprichoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        HeaderProgress(
            step = state.step,
            onBackClick = {
                if (state.step > 1) viewModel.onPreviousStep() else onNavigateBack()
            },
        )

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = state.step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
        ) { currentStep ->
            when (currentStep) {
                1 -> StepAmount(
                    rawAmount = state.rawAmount,
                    onDigitClick = viewModel::onDigitInput,
                    onClearClick = viewModel::onClearInput,
                    onDeleteClick = viewModel::onDeleteInput,
                    onNext = viewModel::onNextStep,
                    isValid = state.amount > 0.0,
                )
                2 -> StepInstallments(
                    selected = state.installments,
                    onSelected = viewModel::onInstallmentsSelected,
                    onNext = viewModel::onNextStep,
                    isValid = state.installments >= 1,
                )
                3 -> StepTiming(
                    selected = state.timing,
                    onSelected = viewModel::onTimingSelected,
                    onNext = viewModel::onNextStep,
                    isValid = state.timing != null,
                )
                4 -> StepDurability(
                    selected = state.durability,
                    onSelected = viewModel::onDurabilitySelected,
                    onNext = viewModel::onNextStep,
                    isValid = state.durability != null,
                )
                5 -> StepResult(
                    diagnosis = state.diagnosis,
                    rawAmount = state.rawAmount,
                    isGoalSaved = state.isGoalSaved,
                    onSaveGoalClick = { viewModel.showSaveGoalDialog(true) },
                    onFinish = onNavigateBack,
                )
            }
        }
    }

    if (state.showSaveGoalDialog) {
        SaveGoalDialog(
            isSaving = state.isSavingGoal,
            errorMessage = state.goalSaveError,
            onDismiss = { viewModel.showSaveGoalDialog(false) },
            onConfirm = { title -> viewModel.saveAsGoal(title) },
        )
    }
}

@Composable
private fun HeaderProgress(
    step: Int,
    onBackClick: () -> Unit,
) {
    val shape = remember { PixelCutShape(2.dp) }
    val barShape = remember { PixelCutShape(2.dp) }
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Botón volver: 48dp de área táctil y flecha dibujada (la fuente pixel no trae "◄")
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
                .background(colors.surfaceContainerHigh, shape)
                .border(2.dp, colors.outline, shape)
                .clickable(role = Role.Button, onClickLabel = "Volver", onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            PixelIconImage(
                icon = PixelIcon.ArrowLeft,
                tint = colors.onSurface,
                modifier = Modifier.width(14.dp),
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PASO $step DE 5",
                color = colors.primary,
                style = CaprichoTheme.pixelText.tag,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(5) { index ->
                    val active = index < step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(if (active) colors.primary else colors.surfaceContainerHighest, barShape),
                    )
                }
            }
        }
    }
}

/**
 * Estructura común de los pasos: pregunta arriba, ayuda opcional,
 * contenido que ocupa el espacio sobrante y botón fijo abajo.
 */
@Composable
private fun StepLayout(
    title: String,
    button: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Text(
            text = title.uppercase(),
            color = titleColor,
            style = CaprichoTheme.pixelText.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
        Spacer(Modifier.height(16.dp))
        button()
    }
}

// Paso 1: Monto
@Composable
private fun StepAmount(
    rawAmount: String,
    onDigitClick: (Char) -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNext: () -> Unit,
    isValid: Boolean,
) {
    val formatted = rawAmount.toLongOrNull()?.formatThousands() ?: "0"

    StepLayout(
        title = "¿Cuánto cuesta tu capricho?",
        button = {
            PixelButton(
                text = "Continuar",
                onClick = onNext,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            // Las teclas crecen en pantallas altas; en las bajas se mantiene un mínimo cómodo y se desplaza
            val keyHeight: Dp = ((viewportHeight - AmountCardEstimate - CardKeypadGap - KeypadRowGaps) / 4)
                .coerceIn(52.dp, 72.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                AmountCard(amountText = "$ $formatted")
                Spacer(Modifier.height(CardKeypadGap))
                PixelNumericKeypad(
                    onDigitClick = onDigitClick,
                    onClearClick = onClearClick,
                    onDeleteClick = onDeleteClick,
                    keyHeight = keyHeight,
                )
            }
        }
    }
}

@Composable
private fun AmountCard(amountText: String) {
    val pixel = CaprichoTheme.pixelText
    val size = amountFontSizeSp(amountText.length)

    TamagotchiCard(Modifier.fillMaxWidth(), showControls = false) { colors ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "MONTO ESTIMADO",
                color = colors.lcdInk.copy(alpha = 0.8f),
                style = pixel.tag,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = amountText,
                color = colors.lcdInk,
                // El tamaño se ajusta al largo para que el monto siempre entre en una línea
                style = pixel.display.copy(fontSize = size.sp, lineHeight = (size * 1.15f).sp),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// Paso 2: Modalidad / Cuotas
@Composable
private fun StepInstallments(
    selected: Int,
    onSelected: (Int) -> Unit,
    onNext: () -> Unit,
    isValid: Boolean,
) {
    val options = listOf(
        PixelPillOption(1, "1", "Contado"),
        PixelPillOption(3, "3", "Cuotas"),
        PixelPillOption(6, "6", "Cuotas"),
        PixelPillOption(12, "12", "Cuotas"),
        PixelPillOption(18, "18", "Cuotas"),
        PixelPillOption(24, "24", "Cuotas"),
    )

    StepLayout(
        title = "¿Cómo lo vas a pagar?",
        hint = "Elegí en cuántos pagos lo hacés.",
        button = {
            PixelButton(
                text = "Siguiente",
                onClick = onNext,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        PixelPillGroup(
            options = options,
            selectedOption = selected,
            onOptionSelected = onSelected,
            modifier = Modifier.fillMaxSize(),
            columns = 2,
        )
    }
}

// Paso 3: Afectación Temporal
@Composable
private fun StepTiming(
    selected: CaprichoTiming?,
    onSelected: (CaprichoTiming) -> Unit,
    onNext: () -> Unit,
    isValid: Boolean,
) {
    val options = listOf(
        PixelPillOption(
            CaprichoTiming.THIS_MONTH,
            "Este mes",
            "Impacta en tu presupuesto de este mes",
        ),
        PixelPillOption(
            CaprichoTiming.NEXT_MONTH,
            "El mes que viene",
            "Entra en el resumen o la tarjeta del próximo mes",
        ),
        PixelPillOption(
            CaprichoTiming.UNKNOWN,
            "No sé todavía",
            "Todavía no tenés certeza de la fecha de cobro",
        ),
    )

    StepLayout(
        title = "¿Afecta este mes o el que viene?",
        hint = "Así sabemos en qué presupuesto pega.",
        button = {
            PixelButton(
                text = "Siguiente",
                onClick = onNext,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        PixelPillGroup(
            options = options,
            selectedOption = selected,
            onOptionSelected = onSelected,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// Paso 4: Durabilidad
@Composable
private fun StepDurability(
    selected: Durability?,
    onSelected: (Durability) -> Unit,
    onNext: () -> Unit,
    isValid: Boolean,
) {
    val options = listOf(
        PixelPillOption(
            Durability.FLEETING,
            "Fugaz / efímero",
            "Ej: salidas, alfajores, comida, eventos",
        ),
        PixelPillOption(
            Durability.MEDIUM,
            "Duración media",
            "Ej: ropa, calzado, accesorios de uso común",
        ),
        PixelPillOption(
            Durability.HIGH,
            "Alta durabilidad",
            "Ej: heladera, zapatillas pro, electrodomésticos",
        ),
    )

    StepLayout(
        title = "¿Cuánto tiempo te va a durar?",
        hint = "Pensá cuánto lo vas a disfrutar.",
        button = {
            PixelButton(
                text = "Predecir capricho",
                onClick = onNext,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        PixelPillGroup(
            options = options,
            selectedOption = selected,
            onOptionSelected = onSelected,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// Paso 5: Diagnóstico / Resultado
@Composable
private fun StepResult(
    diagnosis: CaprichoDiagnosis?,
    rawAmount: String,
    isGoalSaved: Boolean,
    onSaveGoalClick: () -> Unit,
    onFinish: () -> Unit,
) {
    if (diagnosis == null) return

    val formattedTotal = rawAmount.toLongOrNull()?.formatThousands() ?: "0"
    val formattedMonthly = diagnosis.monthlyPayment.toLong().formatThousands()
    val impactInt = (diagnosis.salaryImpactPercent * 10).toInt()
    val impactText = "${impactInt / 10},${impactInt % 10} %"

    val mood = when (diagnosis.verdict) {
        RecommendationVerdict.GREAT_CAPRICHO -> MascotMood.Happy
        RecommendationVerdict.MODERATE_RISK -> MascotMood.Thinking
        RecommendationVerdict.HEAVY_CAPRICHO -> MascotMood.Sad
    }
    // El impacto se pinta con el semáforo del tema, igual que en el resto de la app
    val impactColor = when (diagnosis.verdict) {
        RecommendationVerdict.GREAT_CAPRICHO -> CaprichoTheme.impact.good
        RecommendationVerdict.MODERATE_RISK -> CaprichoTheme.impact.warning
        RecommendationVerdict.HEAVY_CAPRICHO -> CaprichoTheme.impact.heavy
    }

    val stats = buildList {
        add(ResultStat("Costo total", "$ $formattedTotal"))
        if (diagnosis.isInstallment) add(ResultStat("Pago mensual", "$ $formattedMonthly"))
        add(ResultStat("Impacto en tu sueldo", impactText, impactColor))
        add(ResultStat("Durabilidad", diagnosis.durationLabel))
    }

    StepLayout(
        title = "Diagnóstico del capricho",
        titleColor = MaterialTheme.colorScheme.primary,
        button = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PixelButton(
                    text = if (isGoalSaved) "✓ Meta guardada" else "Guardar como meta",
                    onClick = onSaveGoalClick,
                    enabled = !isGoalSaved,
                    modifier = Modifier.fillMaxWidth(),
                )
                PixelButton(
                    text = "Volver al inicio",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            ) {
                TamagotchiCard(Modifier.fillMaxWidth(), showControls = false) { colors ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Mascot(
                            mood = mood,
                            modifier = Modifier.size(130.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        PixelTypewriterText(
                            text = diagnosis.mascotMessage,
                            style = CaprichoTheme.pixelText.lcd,
                            color = colors.lcdInk,
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                stats.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pair.forEach { stat -> StatTile(stat, Modifier.weight(1f)) }
                    }
                }

                if (diagnosis.goalImpacts.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "IMPACTO EN TUS METAS",
                        style = CaprichoTheme.pixelText.title.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    diagnosis.goalImpacts.forEach { impact ->
                        GoalImpactCard(impact = impact)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalImpactCard(impact: GoalImpactInfo) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val formattedImpact = impact.impactPercent.toString().replace('.', ',')
    val formattedRemaining = impact.goalRemainingAmount.toLong().formatThousands()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, CaprichoTheme.impact.warning, shape)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixelIconImage(
                icon = PixelIcon.Goals,
                tint = CaprichoTheme.impact.warning,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "META: ${impact.goalTitle.uppercase()}",
                style = CaprichoTheme.pixelText.title.copy(fontSize = 14.sp),
                color = colors.onSurface,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Te estás alejando un $formattedImpact% de esta meta",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = CaprichoTheme.impact.warning,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Falta juntar $$formattedRemaining para alcanzar el objetivo.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SaveGoalDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(4.dp) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PixelButton(
                    text = if (isSaving) "Guardando..." else "Guardar meta",
                    onClick = { if (title.isNotBlank() && !isSaving) onConfirm(title) },
                    enabled = title.isNotBlank() && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(PixelCutShape(2.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Cancelar",
                        color = colors.onSurfaceVariant,
                        style = CaprichoTheme.pixelText.button.copy(fontSize = 14.sp),
                    )
                }
            }
        },
        title = {
            Text(
                text = "GUARDAR COMO META",
                style = CaprichoTheme.pixelText.title.copy(fontSize = 18.sp),
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ingresá un nombre para identificar esta meta:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del capricho / meta") },
                    placeholder = { Text("Ej: Zapatillas Pro, Viaje...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        containerColor = colors.surfaceContainerHigh,
        shape = shape,
    )
}

private data class ResultStat(
    val label: String,
    val value: String,
    val valueColor: Color = Color.Unspecified,
)

@Composable
private fun StatTile(stat: ResultStat, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }

    Column(
        modifier = modifier
            .background(colors.surfaceContainerHigh, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleLarge,
            color = if (stat.valueColor == Color.Unspecified) colors.onSurface else stat.valueColor,
            maxLines = 2,
        )
    }
}
