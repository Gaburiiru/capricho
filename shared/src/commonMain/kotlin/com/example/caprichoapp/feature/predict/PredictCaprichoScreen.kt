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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
import com.example.caprichoapp.core.designsystem.pixel.PixelDialog
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconImage
import com.example.caprichoapp.core.designsystem.pixel.PixelNumericKeypad
import com.example.caprichoapp.core.designsystem.pixel.PixelPillGroup
import com.example.caprichoapp.core.designsystem.pixel.PixelPillOption
import com.example.caprichoapp.core.designsystem.pixel.PixelProgressBar
import com.example.caprichoapp.core.designsystem.pixel.PixelTextButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.core.designsystem.pixel.PixelTypewriterText
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.util.amountFontSizeSp
import com.example.caprichoapp.core.util.formatAmount
import com.example.caprichoapp.core.util.formatPercent
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Durability
import org.koin.compose.viewmodel.koinViewModel

// Estimaciones para repartir el alto del paso 1 entre la tarjeta del monto y el teclado
private val AmountCardEstimate = 184.dp
private val CardKeypadGap = 16.dp
private val KeypadRowGaps = 30.dp // 3 separaciones de 10.dp

// Alto fijo de la zona del monto: la carcasa nunca cambia de tamaño, solo el texto de adentro
private val AmountDisplayHeight = 60.dp

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
            summary = "$ ${state.rawAmount.formatAmount()} · " +
                if (state.installments > 1) "${state.installments} cuotas" else "Contado",
            isSaving = state.isSavingGoal,
            errorMessage = state.goalSaveError,
            onDismiss = { viewModel.showSaveGoalDialog(false) },
            onConfirm = { title -> viewModel.saveAsGoal(title) },
        )
    }
}

/** Botón volver a la izquierda y progreso centrado: el indicador no se corre por el botón. */
@Composable
private fun HeaderProgress(
    step: Int,
    onBackClick: () -> Unit,
) {
    val shape = remember { PixelCutShape(2.dp) }
    val barShape = remember { PixelCutShape(1.dp) }
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Botón volver: 48dp de área táctil y flecha dibujada (la fuente pixel no trae "◄")
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
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

        Column(
            modifier = Modifier.width(168.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "PASO $step DE 5",
                color = colors.primary,
                style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(
                                if (index < step) colors.primary else colors.surfaceContainerHighest,
                                barShape,
                            ),
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
    val formatted = rawAmount.formatAmount()

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
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "MONTO ESTIMADO",
                color = colors.lcdInk.copy(alpha = 0.8f),
                style = pixel.tag,
            )
            Spacer(Modifier.height(8.dp))
            // Zona de alto fijo: al cargar dígitos solo se achica el texto, nunca la carcasa
            Box(
                modifier = Modifier.fillMaxWidth().height(AmountDisplayHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = amountText,
                    color = colors.lcdInk,
                    style = pixel.display.copy(fontSize = size.sp, lineHeight = (size * 1.15f).sp),
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                )
            }
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

    val colors = MaterialTheme.colorScheme
    val formattedTotal = rawAmount.formatAmount()
    val formattedMonthly = diagnosis.monthlyPayment.toLong().formatThousands()

    val mood = when {
        diagnosis.salaryImpactPercent >= 150.0 -> MascotMood.Crazy
        diagnosis.salaryImpactPercent >= 100.0 -> MascotMood.Panicked
        diagnosis.salaryImpactPercent >= 50.0 -> MascotMood.Worried
        diagnosis.verdict == RecommendationVerdict.GREAT_CAPRICHO -> MascotMood.Happy
        diagnosis.verdict == RecommendationVerdict.MODERATE_RISK -> MascotMood.Thinking
        else -> MascotMood.Sad
    }
    // El impacto se pinta con el semáforo del tema, igual que en el resto de la app
    val impactColor = when (diagnosis.verdict) {
        RecommendationVerdict.GREAT_CAPRICHO -> CaprichoTheme.impact.good
        RecommendationVerdict.MODERATE_RISK -> CaprichoTheme.impact.warning
        RecommendationVerdict.HEAVY_CAPRICHO -> CaprichoTheme.impact.heavy
    }
    val impactCaption = when (diagnosis.verdict) {
        RecommendationVerdict.GREAT_CAPRICHO -> "Entra cómodo en tu mes"
        RecommendationVerdict.MODERATE_RISK -> "Es manejable, pero ojo"
        RecommendationVerdict.HEAVY_CAPRICHO -> "Pesa bastante en tu sueldo"
    }

    StepLayout(
        title = "Diagnóstico",
        titleColor = colors.primary,
        button = {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PixelButton(
                    text = if (isGoalSaved) "Meta guardada" else "Guardar como meta",
                    onClick = onSaveGoalClick,
                    enabled = !isGoalSaved,
                    icon = if (isGoalSaved) PixelIcon.Check else null,
                    showArrow = !isGoalSaved,
                    modifier = Modifier.fillMaxWidth(),
                )
                PixelTextButton(text = "Volver al inicio", onClick = onFinish)
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Top),
        ) {
            // La mascota y su mensaje en una sola fila para ahorrar alto
            TamagotchiCard(Modifier.fillMaxWidth(), brand = null, showControls = false) { lcd ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Mascot(mood = mood, modifier = Modifier.size(84.dp))
                    PixelTypewriterText(
                        text = diagnosis.mascotMessage,
                        style = CaprichoTheme.pixelText.lcd.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = lcd.lcdInk,
                        minLines = 5,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // El dato principal, bien grande y con su semáforo
            ResultHero(
                percentText = "${diagnosis.salaryImpactPercent.formatPercent()}%",
                caption = impactCaption,
                progress = (diagnosis.salaryImpactPercent / 100.0).toFloat(),
                color = impactColor,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile("Costo total", "$ $formattedTotal", Modifier.weight(1f))
                if (diagnosis.isInstallment) {
                    StatTile("Pago mensual", "$ $formattedMonthly", Modifier.weight(1f))
                } else {
                    StatTile("Forma de pago", "Contado", Modifier.weight(1f))
                }
            }

            if (diagnosis.goalImpacts.isNotEmpty()) {
                GoalImpactSection(diagnosis.goalImpacts)
            }
        }
    }
}

@Composable
private fun ResultHero(
    percentText: String,
    caption: String,
    progress: Float,
    color: Color,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, color, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "IMPACTO EN TU SUELDO",
            style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
            color = colors.onSurfaceVariant,
        )
        Text(
            text = percentText,
            style = CaprichoTheme.pixelText.display.copy(fontSize = 34.sp, lineHeight = 40.sp),
            color = color,
            maxLines = 1,
        )
        PixelProgressBar(progress = progress, color = color, height = 10.dp)
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoalImpactSection(impacts: List<GoalImpactInfo>) {
    val colors = MaterialTheme.colorScheme
    val visible = impacts.take(MAX_VISIBLE_GOALS)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "ASÍ AFECTA A TUS METAS",
            style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
            color = colors.primary,
        )
        visible.forEach { GoalImpactRow(it) }
        if (impacts.size > visible.size) {
            Text(
                text = "y ${impacts.size - visible.size} más en la sección Metas",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

private const val MAX_VISIBLE_GOALS = 2

/** Una meta, en dos líneas: su nombre y cuánto representa el capricho de lo que falta. */
@Composable
private fun GoalImpactRow(impact: GoalImpactInfo) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val heavy = impact.impactPercent >= 100.0

    val sentence = if (impact.impactPercent >= 200.0) {
        "Equivale a ${(impact.impactPercent / 100.0).formatPercent()} veces lo que te falta"
    } else {
        "Equivale al ${impact.impactPercent.formatPercent()}% de lo que te falta"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, if (heavy) CaprichoTheme.impact.warning else colors.outlineVariant, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PixelIconImage(
            icon = PixelIcon.Goals,
            tint = if (heavy) CaprichoTheme.impact.warning else colors.onSurfaceVariant,
            modifier = Modifier.width(18.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = impact.goalTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
                maxLines = 1,
            )
            Text(
                text = sentence,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SaveGoalDialog(
    summary: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val canSave = title.isNotBlank() && !isSaving

    PixelDialog(
        onDismiss = onDismiss,
        title = "Guardar como meta",
        actions = {
            PixelButton(
                text = if (isSaving) "Guardando..." else "Guardar meta",
                onClick = { if (canSave) onConfirm(title) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            PixelTextButton(text = "Cancelar", onClick = onDismiss)
        },
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        PixelTextField(
            value = title,
            onValueChange = { title = it },
            label = "¿Cómo la llamamos?",
            placeholder = "Ej: Zapatillas, Viaje...",
            maxLength = 30,
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }

    Column(
        modifier = modifier
            .background(colors.surfaceContainerHigh, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = colors.onSurface,
            maxLines = 1,
        )
    }
}
