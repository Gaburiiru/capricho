package com.example.caprichoapp.feature.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelAmountField
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelButtonSize
import com.example.caprichoapp.core.designsystem.pixel.PixelButtonVariant
import com.example.caprichoapp.core.designsystem.pixel.PixelChip
import com.example.caprichoapp.core.designsystem.pixel.PixelChipGroup
import com.example.caprichoapp.core.designsystem.pixel.PixelConfirmDialog
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixel.PixelDialog
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconImage
import com.example.caprichoapp.core.designsystem.pixel.PixelIconButton
import com.example.caprichoapp.core.designsystem.pixel.PixelProgressBar
import com.example.caprichoapp.core.designsystem.pixel.PixelTag
import com.example.caprichoapp.core.designsystem.pixel.PixelTextButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.core.designsystem.pixel.PixelTypewriterText
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.core.util.plusAmount
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import org.koin.compose.viewmodel.koinViewModel

private fun Durability.shortLabel() = when (this) {
    Durability.FLEETING -> "Fugaz"
    Durability.MEDIUM -> "Media"
    Durability.HIGH -> "Alta"
}

private fun installmentsLabel(count: Int) = if (count > 1) "$count cuotas" else "Contado"

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = koinViewModel(),
    onOpenStrategy: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val selectedGoal by viewModel.selectedGoalForDetail.collectAsStateWithLifecycle()
    val goalToDelete by viewModel.goalToDelete.collectAsStateWithLifecycle()
    val goalToEdit by viewModel.goalToEdit.collectAsStateWithLifecycle()
    var achievedExpanded by rememberSaveable { mutableStateOf(false) }
    val canGenerateStrategy by viewModel.canGenerateStrategy.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadGoals()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TUS METAS",
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Ahorros y caprichos proyectados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(12.dp))
            PixelButton(
                text = "Nueva",
                onClick = viewModel::onAddGoalClick,
                size = PixelButtonSize.Compact,
                icon = PixelIcon.Plus,
            )
        }

        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            is GoalsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is GoalsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        PixelButton(
                            text = "Reintentar",
                            onClick = viewModel::loadGoals,
                            size = PixelButtonSize.Compact,
                        )
                    }
                }
            }

            is GoalsUiState.Success -> {
                if (state.goals.isEmpty()) {
                    EmptyGoalsContent(onAddClick = viewModel::onAddGoalClick)
                } else {
                    // Con metas en curso, las cumplidas quedan plegadas para dar lugar a lo pendiente;
                    // si no hay ninguna en curso se muestran siempre.
                    val showAchieved = achievedExpanded || state.inProgress.isEmpty()
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.inProgress, key = { it.id }) { goal ->
                            GoalCard(
                                goal = goal,
                                onClick = { viewModel.onGoalClick(goal) },
                                onEditClick = { viewModel.onEditGoalClick(goal) },
                                onDeleteClick = { viewModel.onRequestDeleteGoal(goal) },
                            )
                        }
                        if (state.achieved.isNotEmpty()) {
                            item(key = "achieved-header") {
                                AchievedSectionHeader(
                                    count = state.achieved.size,
                                    expanded = showAchieved,
                                    canToggle = state.inProgress.isNotEmpty(),
                                    onToggle = { achievedExpanded = !achievedExpanded },
                                )
                            }
                            if (showAchieved) {
                                items(state.achieved, key = { it.id }) { goal ->
                                    GoalCard(
                                        goal = goal,
                                        onClick = { viewModel.onGoalClick(goal) },
                                        onEditClick = null,
                                        onDeleteClick = { viewModel.onRequestDeleteGoal(goal) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = viewModel::onDismissAddDialog,
            onConfirm = { title, amount, installments, durability ->
                viewModel.addGoal(title, amount, installments, durability)
            },
        )
    }

    selectedGoal?.let { goal ->
        GoalDetailDialog(
            goal = goal,
            onDismiss = viewModel::onCloseDetailDialog,
            onAddSavings = { amount -> viewModel.addSavings(goal, amount) },
            onDelete = { viewModel.onRequestDeleteGoal(goal) },
            onOpenStrategy = onOpenStrategy,
            canGenerateStrategy = canGenerateStrategy,
        )
    }

    goalToEdit?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = viewModel::onDismissEditDialog,
            onConfirm = { title, amount, installments, durability ->
                viewModel.updateGoal(goal, title, amount, installments, durability)
            },
        )
    }

    goalToDelete?.let { goal ->
        PixelConfirmDialog(
            title = "Eliminar meta",
            message = "¿Querés eliminar \"${goal.title}\"? Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            onConfirm = viewModel::confirmDeleteGoal,
            onDismiss = viewModel::onCancelDeleteGoal,
        )
    }
}

@Composable
private fun EmptyGoalsContent(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TamagotchiCard(Modifier.fillMaxWidth(), showControls = false) { colors ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Mascot(
                    mood = MascotMood.Thinking,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.height(12.dp))
                PixelTypewriterText(
                    text = "Todavía no tenés metas guardadas.\n¡Creá una nueva o predecí un capricho para guardarlo!",
                    style = CaprichoTheme.pixelText.lcd,
                    color = colors.lcdInk,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        PixelButton(
            text = "Crear mi primera meta",
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Línea divisoria con el título de la sección de metas cumplidas; tocarla la despliega o la pliega. */
@Composable
private fun AchievedSectionHeader(
    count: Int,
    expanded: Boolean,
    canToggle: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canToggle, onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.weight(1f).height(2.dp).background(colors.outlineVariant))
        Text(
            text = "CUMPLIDAS ($count)",
            style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
            color = CaprichoTheme.impact.good,
        )
        if (canToggle) {
            PixelIconImage(
                icon = PixelIcon.ArrowRight,
                tint = CaprichoTheme.impact.good,
                modifier = Modifier.width(8.dp).rotate(if (expanded) 90f else 0f),
            )
        }
        Box(Modifier.weight(1f).height(2.dp).background(colors.outlineVariant))
    }
}

/** Tarjeta de una meta: nombre, etiquetas, progreso y cuánto falta. Tocarla abre el detalle. [onEditClick] es null en las cumplidas. */
@Composable
private fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)?,
    onDeleteClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = goal.title.uppercase(),
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 16.sp, lineHeight = 22.sp),
                    color = colors.onSurface,
                    maxLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelTag(installmentsLabel(goal.installments))
                    PixelTag("Dura: ${goal.durability.shortLabel()}", color = colors.secondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Una meta cumplida ya no se edita: solo se puede eliminar
                if (onEditClick != null) {
                    PixelIconButton(
                        icon = PixelIcon.Edit,
                        contentDescription = "Editar meta",
                        onClick = onEditClick,
                    )
                }
                PixelIconButton(
                    icon = PixelIcon.Trash,
                    contentDescription = "Eliminar meta",
                    onClick = onDeleteClick,
                    variant = PixelButtonVariant.Danger,
                )
            }
        }

        PixelProgressBar(
            progress = animatedProgress,
            color = if (goal.isAchieved) CaprichoTheme.impact.good else colors.primary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$${goal.savedAmount.toLong().formatThousands()} de $${goal.targetAmount.toLong().formatThousands()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            if (goal.isAchieved) {
                Text(
                    text = "¡ALCANZADA!",
                    style = CaprichoTheme.pixelText.tag,
                    color = CaprichoTheme.impact.good,
                )
            } else {
                Text(
                    text = "Falta $${goal.remaining.toLong().formatThousands()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                )
            }
        }
    }
}

private val QuickSavings = listOf(5_000L, 10_000L, 50_000L)

/**
 * Detalle de una meta. Tiene dos pantallas dentro del mismo diálogo:
 * el resumen con las acciones y la carga de un ahorro con el teclado pixel.
 */
@Composable
private fun GoalDetailDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onAddSavings: (Double) -> Unit,
    onDelete: () -> Unit,
    onOpenStrategy: (String) -> Unit,
    canGenerateStrategy: Boolean,
) {
    var addingSavings by remember { mutableStateOf(false) }
    var savingsInput by remember { mutableStateOf("") }

    if (addingSavings) {
        val amount = savingsInput.toLongOrNull() ?: 0L
        PixelDialog(
            onDismiss = { addingSavings = false },
            title = "Anotar ahorro",
            actions = {
                PixelButton(
                    text = if (amount > 0) "Sumar $${amount.formatThousands()}" else "Sumar ahorro",
                    onClick = {
                        onAddSavings(amount.toDouble())
                        savingsInput = ""
                        addingSavings = false
                    },
                    enabled = amount > 0,
                    icon = PixelIcon.Plus,
                    showArrow = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                PixelTextButton(text = "Volver", onClick = { addingSavings = false })
            },
        ) {
            Text(
                text = "Para \"${goal.title}\" · falta $${goal.remaining.toLong().formatThousands()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            PixelAmountField(
                value = savingsInput,
                onValueChange = { savingsInput = it },
                label = "¿Cuánto sumás?",
                initiallyExpanded = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickSavings.forEach { extra ->
                    PixelChip(
                        text = "+${extra.formatThousands()}",
                        selected = false,
                        onClick = { savingsInput = savingsInput.plusAmount(extra) },
                    )
                }
            }
        }
        return
    }

    val colors = MaterialTheme.colorScheme
    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    PixelDialog(
        onDismiss = onDismiss,
        onClose = onDismiss,
        title = goal.title,
        actions = {
            PixelTextButton(text = "Eliminar meta", onClick = onDelete, color = colors.error)
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            PixelTag(installmentsLabel(goal.installments))
            PixelTag("Dura: ${goal.durability.shortLabel()}", color = colors.secondary)
        }

        // Un solo bloque de resumen: lo que falta, el progreso y lo ya ahorrado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerHighest, PixelCutShape(3.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (goal.isAchieved) {
                Text(
                    text = "¡META CUMPLIDA!",
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 20.sp),
                    color = CaprichoTheme.impact.good,
                )
            } else {
                Text(
                    text = "FALTA JUNTAR",
                    style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "$ ${goal.remaining.toLong().formatThousands()}",
                    style = CaprichoTheme.pixelText.display.copy(fontSize = 26.sp, lineHeight = 32.sp),
                    color = colors.onSurface,
                    maxLines = 1,
                )
            }
            PixelProgressBar(
                progress = progress,
                color = if (goal.isAchieved) CaprichoTheme.impact.good else colors.primary,
                height = 10.dp,
            )
            Text(
                text = "Llevás $${goal.savedAmount.toLong().formatThousands()} de $${goal.targetAmount.toLong().formatThousands()}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }

        if (!goal.isAchieved) {
            PixelButton(
                text = "Anotar ahorro",
                onClick = { addingSavings = true },
                icon = PixelIcon.Plus,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        // Una meta cumplida ya no necesita estrategia de ahorro
        val strategyEnabled = canGenerateStrategy && !goal.isAchieved
        PixelButton(
            text = "Estrategia de ahorro",
            onClick = { if (strategyEnabled) onOpenStrategy(goal.id) },
            enabled = strategyEnabled,
            variant = PixelButtonVariant.Secondary,
            showArrow = false,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!goal.isAchieved && !canGenerateStrategy) {
            Text(
                text = "Cargá al menos un gasto en la sección Gastos para activar la estrategia.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val InstallmentOptions = listOf(1, 3, 6, 12, 18, 24)

/**
 * Edición de una meta: nombre, monto total, cuotas y durabilidad.
 * El monto no puede bajar de lo ya ahorrado: si se intenta, se avisa y al guardar se ajusta a ese mínimo.
 */
@Composable
private fun EditGoalDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetAmount: Double, installments: Int, durability: Durability) -> Unit,
) {
    var title by remember { mutableStateOf(goal.title) }
    var amountText by remember { mutableStateOf(goal.targetAmount.toLong().toString()) }
    var installments by remember { mutableStateOf(goal.installments) }
    var durability by remember { mutableStateOf(goal.durability) }
    var adjusted by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val minAmount = goal.minTargetAmount.toLong()
    val amount = amountText.toLongOrNull() ?: 0L
    val belowMin = minAmount > 0 && amount < minAmount
    val isValid = title.isNotBlank() && (amount > 0 || belowMin)
    // Si la meta ya tenía unas cuotas fuera de las opciones habituales (ej. 9), se mantienen visibles
    val installmentOptions = remember(goal.installments) { (InstallmentOptions + goal.installments).distinct().sorted() }

    PixelDialog(
        onDismiss = onDismiss,
        onClose = onDismiss,
        title = "Editar meta",
        actions = {
            PixelButton(
                text = "Guardar cambios",
                onClick = {
                    if (belowMin) {
                        // Primero se muestra el ajuste; el usuario confirma con el siguiente toque
                        amountText = minAmount.toString()
                        adjusted = true
                    } else {
                        onConfirm(title, amount.toDouble(), installments, durability)
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            PixelTextButton(text = "Cancelar", onClick = onDismiss)
        },
    ) {
        PixelTextField(
            value = title,
            onValueChange = { title = it },
            label = "Nombre de la meta",
            placeholder = "Ej: Zapatillas, Viaje...",
            maxLength = 30,
        )
        PixelAmountField(
            value = amountText,
            onValueChange = {
                amountText = it
                adjusted = false
            },
            label = "Monto total",
        )
        if (adjusted) {
            Text(
                text = "Ya ahorraste $${minAmount.formatThousands()}, así que el monto se quedó en ese mínimo.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (belowMin) {
            Text(
                text = "Ya ahorraste $${minAmount.formatThousands()}: el monto no puede ser menor. Si guardás, se queda en $${minAmount.formatThousands()}.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PixelChipGroup(
            label = "¿Cómo lo pagarías?",
            options = installmentOptions.map { it to installmentsLabel(it) },
            selected = installments,
            onSelected = { installments = it },
        )
        PixelChipGroup(
            label = "¿Cuánto te va a durar?",
            options = Durability.entries.map { it to it.shortLabel() },
            selected = durability,
            onSelected = { durability = it },
        )
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetAmount: Double, installments: Int, durability: Durability) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf(1) }
    var durability by remember { mutableStateOf(Durability.MEDIUM) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = title.isNotBlank() && amount > 0.0

    PixelDialog(
        onDismiss = onDismiss,
        title = "Nueva meta",
        actions = {
            PixelButton(
                text = "Guardar",
                onClick = { if (isValid) onConfirm(title, amount, installments, durability) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            PixelTextButton(text = "Cancelar", onClick = onDismiss)
        },
    ) {
        PixelTextField(
            value = title,
            onValueChange = { title = it },
            label = "Nombre de la meta",
            placeholder = "Ej: Zapatillas, Viaje...",
            maxLength = 30,
        )
        PixelAmountField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Monto objetivo",
        )
        PixelChipGroup(
            label = "¿Cómo lo pagarías?",
            options = listOf(1, 3, 6, 12, 18, 24).map { it to installmentsLabel(it) },
            selected = installments,
            onSelected = { installments = it },
        )
        PixelChipGroup(
            label = "¿Cuánto te va a durar?",
            options = Durability.entries.map { it to it.shortLabel() },
            selected = durability,
            onSelected = { durability = it },
        )
    }
}
