package com.example.caprichoapp.feature.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.caprichoapp.core.designsystem.pixel.PixelIconImage
import com.example.caprichoapp.core.designsystem.pixel.PixelTypewriterText
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val selectedGoal by viewModel.selectedGoalForDetail.collectAsStateWithLifecycle()
    val goalToDelete by viewModel.goalToDelete.collectAsStateWithLifecycle()

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
            PixelButton(
                text = "+ Nueva",
                onClick = viewModel::onAddGoalClick,
                modifier = Modifier.width(115.dp),
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
                        )
                        Spacer(Modifier.height(12.dp))
                        PixelButton(
                            text = "Reintentar",
                            onClick = viewModel::loadGoals,
                        )
                    }
                }
            }

            is GoalsUiState.Success -> {
                if (state.goals.isEmpty()) {
                    EmptyGoalsContent(onAddClick = viewModel::onAddGoalClick)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.goals, key = { it.id }) { goal ->
                            GoalCard(
                                goal = goal,
                                onClick = { viewModel.onGoalClick(goal) },
                                onDeleteClick = { viewModel.onRequestDeleteGoal(goal) },
                            )
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
        )
    }

    goalToDelete?.let { goal ->
        DeleteGoalConfirmationDialog(
            goalTitle = goal.title,
            onDismiss = viewModel::onCancelDeleteGoal,
            onConfirm = viewModel::confirmDeleteGoal,
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

@Composable
private fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val buttonShape = remember { PixelCutShape(2.dp) }
    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val formattedTarget = goal.targetAmount.toLong().formatThousands()
    val formattedSaved = goal.savedAmount.toLong().formatThousands()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, colors.outlineVariant, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = goal.title.uppercase(),
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 15.sp),
                    color = colors.onSurface,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val durabilityText = when (goal.durability) {
                    Durability.FLEETING -> "Fugaz"
                    Durability.MEDIUM -> "Uso medio"
                    Durability.HIGH -> "Alta"
                }
                Text(
                    text = durabilityText,
                    style = CaprichoTheme.pixelText.tag,
                    color = colors.primary,
                    modifier = Modifier
                        .background(colors.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(buttonShape)
                        .background(colors.errorContainer, buttonShape)
                        .clickable(role = Role.Button, onClick = onDeleteClick),
                    contentAlignment = Alignment.Center,
                ) {
                    PixelIconImage(
                        icon = PixelIcon.Trash,
                        tint = colors.onErrorContainer,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Guardado: $$formattedSaved",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = "Objetivo: $$formattedTarget",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Barra de progreso Pixel
        val barShape = remember { PixelCutShape(2.dp) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(colors.surfaceContainerHighest, barShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(12.dp)
                    .background(
                        if (goal.isAchieved) CaprichoTheme.impact.good else colors.primary,
                        barShape,
                    ),
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (goal.installments > 1) "${goal.installments} cuotas" else "Contado",
                style = MaterialTheme.typography.labelMedium,
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
                    text = "Falta: $${(goal.remaining.toLong()).formatThousands()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onAddSavings: (Double) -> Unit,
    onDelete: () -> Unit,
) {
    var savingsInput by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(4.dp) }

    val formattedTarget = goal.targetAmount.toLong().formatThousands()
    val formattedSaved = goal.savedAmount.toLong().formatThousands()
    val formattedRemaining = goal.remaining.toLong().formatThousands()

    val durabilityText = when (goal.durability) {
        Durability.FLEETING -> "Fugaz / Efímero"
        Durability.MEDIUM -> "Uso medio"
        Durability.HIGH -> "Alta durabilidad"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PixelButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        title = {
            Text(
                text = goal.title.uppercase(),
                style = CaprichoTheme.pixelText.title.copy(fontSize = 18.sp),
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (goal.installments > 1) "${goal.installments} cuotas" else "Contado",
                        style = CaprichoTheme.pixelText.tag,
                        color = colors.primary,
                    )
                    Text(
                        text = durabilityText,
                        style = CaprichoTheme.pixelText.tag,
                        color = colors.secondary,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHighest, PixelCutShape(2.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Objetivo total:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = "$$formattedTarget",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Ahorrado actual:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = "$$formattedSaved",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CaprichoTheme.impact.good,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (goal.isAchieved) "Estado:" else "Falta juntar:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = if (goal.isAchieved) "¡Cumplida!" else "$$formattedRemaining",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (goal.isAchieved) CaprichoTheme.impact.good else colors.error,
                        )
                    }
                }

                if (!goal.isAchieved) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Anotar nuevo ahorro ($):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = savingsInput,
                                onValueChange = { savingsInput = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("Ej: 5000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            PixelButton(
                                text = "+ Sumar",
                                onClick = {
                                    val amount = savingsInput.toDoubleOrNull() ?: 0.0
                                    if (amount > 0.0) {
                                        onAddSavings(amount)
                                        savingsInput = ""
                                    }
                                },
                                enabled = (savingsInput.toDoubleOrNull() ?: 0.0) > 0.0,
                                modifier = Modifier.width(115.dp),
                            )
                        }
                    }
                } else {
                    Text(
                        text = "🎉 ¡Felicidades! Lograste alcanzar esta meta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaprichoTheme.impact.good,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelCutShape(2.dp))
                        .background(colors.errorContainer)
                        .clickable(onClick = onDelete)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "ELIMINAR META",
                        color = colors.onErrorContainer,
                        style = CaprichoTheme.pixelText.button.copy(fontSize = 14.sp),
                    )
                }
            }
        },
        containerColor = colors.surfaceContainerHigh,
        shape = shape,
    )
}

@Composable
private fun DeleteGoalConfirmationDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
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
                    text = "Eliminar",
                    onClick = onConfirm,
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
                text = "ELIMINAR META",
                style = CaprichoTheme.pixelText.title.copy(fontSize = 18.sp),
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que querés eliminar '$goalTitle'? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        containerColor = colors.surfaceContainerHigh,
        shape = shape,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetAmount: Double, installments: Int, durability: Durability) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf(1) }
    var durability by remember { mutableStateOf(Durability.MEDIUM) }

    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(4.dp) }

    val isValid = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PixelButton(
                    text = "Guardar",
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (isValid) {
                            onConfirm(title, amount, installments, durability)
                        }
                    },
                    enabled = isValid,
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
                text = "NUEVA META",
                style = CaprichoTheme.pixelText.title.copy(fontSize = 18.sp),
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la meta") },
                    placeholder = { Text("Ej: Zapatillas Pro, Auto...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Monto objetivo ($)") },
                    placeholder = { Text("Ej: 150000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var durExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = durExpanded,
                        onExpandedChange = { durExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = when (durability) {
                                Durability.FLEETING -> "Fugaz"
                                Durability.MEDIUM -> "Media"
                                Durability.HIGH -> "Alta"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Durabilidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = durExpanded,
                            onDismissRequest = { durExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fugaz") },
                                onClick = { durability = Durability.FLEETING; durExpanded = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Media") },
                                onClick = { durability = Durability.MEDIUM; durExpanded = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Alta") },
                                onClick = { durability = Durability.HIGH; durExpanded = false },
                            )
                        }
                    }

                    var instExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = instExpanded,
                        onExpandedChange = { instExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = if (installments == 1) "1 pago" else "$installments cuotas",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cuotas") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = instExpanded,
                            onDismissRequest = { instExpanded = false },
                        ) {
                            listOf(1, 3, 6, 12, 18, 24).forEach { count ->
                                DropdownMenuItem(
                                    text = { Text(if (count == 1) "1 pago" else "$count cuotas") },
                                    onClick = { installments = count; instExpanded = false },
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = colors.surfaceContainerHigh,
        shape = shape,
    )
}
