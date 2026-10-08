package com.example.caprichoapp.feature.history

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import com.example.caprichoapp.core.designsystem.pixel.PixelIconButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTag
import com.example.caprichoapp.core.designsystem.pixel.PixelTextButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.core.designsystem.pixel.TamagotchiCard
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.core.util.today
import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.model.Expense
import com.example.caprichoapp.domain.model.ExpenseKind
import com.example.caprichoapp.domain.model.Durability
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

private enum class TimeFilter(val label: String) {
    DAY("Día"),
    MONTH("Mes"),
    YEAR("Año"),
}

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.MONTH) }

    // Al navegar a la pantalla se refresca siempre el sueldo y los gastos actualizados
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Los diálogos se cierran recién cuando el guardado salió bien
    LaunchedEffect(state.savedCount) {
        if (state.savedCount > 0) {
            showAddDialog = false
            editingExpense = null
        }
    }

    val openAddDialog = {
        viewModel.clearSaveError()
        showAddDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GASTOS",
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 22.sp),
                    color = colors.primary,
                )
                Text(
                    text = "Historial y control de egresos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            PixelButton(
                text = "Gasto",
                onClick = openAddDialog,
                size = PixelButtonSize.Compact,
                icon = PixelIcon.Plus,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Selector de período (Día / Mes / Año)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeFilter.entries.forEach { filter ->
                PixelChip(
                    text = filter.label,
                    selected = filter == selectedTimeFilter,
                    onClick = { selectedTimeFilter = filter },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // El error de carga va arriba: con la lista vacía quedaba fuera de la pantalla
        state.error?.let { message ->
            ErrorBanner(message)
            Spacer(Modifier.height(12.dp))
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (state.expenses.isEmpty()) {
            EmptyHistoryState(onAddClick = openAddDialog)
        } else {
            val totalSpent = state.expenses.sumOf { it.amount }
            val percentOfSalary = if (state.monthlySalary > 0.0) (totalSpent / state.monthlySalary) * 100.0 else 0.0
            val categoryTotals = remember(state.expenses) {
                state.expenses
                    .groupBy { it.categoryName }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }
                    .entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }
            }
            val palette = categoryPalette()
            val categoryColors = remember(categoryTotals, palette) {
                categoryTotals.keys.mapIndexed { index, name -> name to palette[index % palette.size] }.toMap()
            }

            // El resumen es parte de la lista: todo se desplaza junto y queda más lugar para los gastos
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "summary") {
                    SummaryCard(
                        total = totalSpent,
                        monthlySalary = state.monthlySalary,
                        percentOfSalary = percentOfSalary,
                        categoryTotals = categoryTotals,
                        categoryColors = categoryColors,
                        onClick = { showDetailDialog = true },
                    )
                }
                items(state.expenses, key = { it.id.ifBlank { it.title ?: "sin-id" } }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        categoryColor = categoryColors[expense.categoryName] ?: colors.primary,
                        onEdit = {
                            viewModel.clearSaveError()
                            editingExpense = expense
                        },
                        onDelete = { expenseToDelete = expense },
                    )
                }
            }

            if (showDetailDialog) {
                ExpenseDetailDialog(
                    totalSpent = totalSpent,
                    monthlySalary = state.monthlySalary,
                    percentOfSalary = percentOfSalary,
                    categoryTotals = categoryTotals,
                    categoryColors = categoryColors,
                    onDismiss = { showDetailDialog = false },
                )
            }
        }
    }

    if (showAddDialog) {
        ExpenseDialog(
            initialExpense = null,
            categories = state.categories,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = {
                viewModel.clearSaveError()
                showAddDialog = false
            },
            onSave = viewModel::addExpense,
        )
    }

    editingExpense?.let { expense ->
        ExpenseDialog(
            initialExpense = expense,
            categories = state.categories,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = {
                viewModel.clearSaveError()
                editingExpense = null
            },
            onSave = viewModel::updateExpense,
        )
    }

    expenseToDelete?.let { expense ->
        PixelConfirmDialog(
            title = "Eliminar gasto",
            message = "¿Querés eliminar \"${expense.title ?: expense.categoryName}\" por $${expense.amount.toLong().formatThousands()}?",
            confirmText = "Eliminar",
            onConfirm = {
                viewModel.deleteExpense(expense.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null },
        )
    }
}

/** Colores de las categorías del gráfico: distinguibles entre sí y legibles en tema claro y oscuro. */
@Composable
private fun categoryPalette(): List<Color> = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.secondary,
    Color(0xFF38BDF8),
    Color(0xFFFBBF24),
    Color(0xFFA78BFA),
    Color(0xFFFB923C),
    Color(0xFF2DD4BF),
)

@Composable
private fun EmptyHistoryState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TamagotchiCard(Modifier.fillMaxWidth(), showControls = false) { lcd ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Mascot(mood = MascotMood.Thinking, modifier = Modifier.size(110.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Todavía no cargaste gastos.\nCon ellos armamos tu estrategia de ahorro.",
                    style = CaprichoTheme.pixelText.lcd,
                    color = lcd.lcdInk,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        PixelButton(
            text = "Agregar primer gasto",
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun Double.formatPercentage(): String {
    if (this <= 0.0) return "0%"
    val rounded10 = (this * 10 + 0.5).toInt()
    if (rounded10 == 0) return "<0,1%"
    val whole = rounded10 / 10
    val decimal = rounded10 % 10
    return if (decimal == 0) "$whole%" else "$whole,$decimal%"
}

@Composable
private fun SummaryCard(
    total: Double,
    monthlySalary: Double,
    percentOfSalary: Double,
    categoryTotals: Map<String, Double>,
    categoryColors: Map<String, Color>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(4.dp) }
    val freeColor = Color(0xFF0EA5E9)

    val hasSalary = monthlySalary > 0.0
    val freeSalary = (monthlySalary - total).coerceAtLeast(0.0)
    val freePercent = if (hasSalary) (freeSalary / monthlySalary * 100.0) else 0.0
    val chartBase = if (hasSalary) maxOf(monthlySalary, total) else categoryTotals.values.sum()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Sueldo estilo billetera (sin solapamientos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SUELDO",
                        style = CaprichoTheme.pixelText.tag,
                        color = colors.primary,
                    )
                    Text(
                        text = if (hasSalary) "$ ${monthlySalary.toLong().formatThousands()}" else "Sin configurar",
                        style = CaprichoTheme.pixelText.display.copy(fontSize = 20.sp, lineHeight = 24.sp),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                if (hasSalary) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LIBRE",
                            style = CaprichoTheme.pixelText.tag,
                            color = freeColor,
                        )
                        Text(
                            text = "${freePercent.formatPercentage()} ($${freeSalary.toLong().formatThousands()})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = freeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Gasto total y porcentaje equivalente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Gasto total:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = "$ ${total.toLong().formatThousands()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                    )
                }
                if (hasSalary) {
                    Text(
                        text = "${percentOfSalary.formatPercentage()} del sueldo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                    )
                }
            }

            // Barra horizontal estilo línea de tiempo/presupuesto (ordenada por mayor gasto)
            BudgetProgressBar(
                categoryTotals = categoryTotals,
                categoryColors = categoryColors,
                chartBase = chartBase,
                hasSalary = hasSalary,
                freeSalary = freeSalary,
                freeColor = freeColor,
            )

            // Indicador al pie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Toca para ver gráfico y detalle",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "Ver más ↗",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressBar(
    categoryTotals: Map<String, Double>,
    categoryColors: Map<String, Color>,
    chartBase: Double,
    hasSalary: Boolean,
    freeSalary: Double,
    freeColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = remember { PixelCutShape(2.dp) }
    val emptyColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(shape)
            .background(emptyColor),
        onDraw = {
            if (chartBase <= 0.0) return@Canvas

            val barWidth = size.width
            val barHeight = size.height
            var currentX = 0f

            categoryTotals.entries.forEach { entry ->
                val segmentWidth = ((entry.value / chartBase) * barWidth).toFloat()
                if (segmentWidth > 0f) {
                    drawRect(
                        color = categoryColors[entry.key] ?: emptyColor,
                        topLeft = androidx.compose.ui.geometry.Offset(currentX, 0f),
                        size = androidx.compose.ui.geometry.Size(segmentWidth, barHeight),
                    )
                    currentX += segmentWidth
                }
            }

            if (hasSalary && freeSalary > 0.0) {
                val freeWidth = ((freeSalary / chartBase) * barWidth).toFloat()
                if (freeWidth > 0f) {
                    drawRect(
                        color = freeColor,
                        topLeft = androidx.compose.ui.geometry.Offset(currentX, 0f),
                        size = androidx.compose.ui.geometry.Size(freeWidth, barHeight),
                    )
                }
            }
        },
    )
}

@Composable
private fun ExpenseDetailDialog(
    totalSpent: Double,
    monthlySalary: Double,
    percentOfSalary: Double,
    categoryTotals: Map<String, Double>,
    categoryColors: Map<String, Color>,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val emptyChartColor = colors.outlineVariant
    val freeColor = Color(0xFF0EA5E9)

    val hasSalary = monthlySalary > 0.0
    val freeSalary = (monthlySalary - totalSpent).coerceAtLeast(0.0)
    val freePercent = if (hasSalary) (freeSalary / monthlySalary * 100.0) else 0.0
    val chartBase = if (hasSalary) maxOf(monthlySalary, totalSpent) else categoryTotals.values.sum()

    PixelDialog(
        onDismiss = onDismiss,
        title = "DESGLOSE DE GASTOS",
        onClose = onDismiss,
        actions = {
            PixelButton(
                text = "Cerrar",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLowest, PixelCutShape(2.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (hasSalary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Sueldo:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        Text("$${monthlySalary.toLong().formatThousands()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Gasto total:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Text("$${totalSpent.toLong().formatThousands()} (${percentOfSalary.formatPercentage()})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                }
                if (hasSalary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Sueldo libre:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        Text("$${freeSalary.toLong().formatThousands()} (${freePercent.formatPercentage()})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = freeColor)
                    }
                }
            }

            Canvas(
                modifier = Modifier.size(150.dp),
                onDraw = {
                    if (chartBase <= 0.0) {
                        drawArc(
                            color = emptyChartColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = true,
                        )
                        return@Canvas
                    }

                    var startAngle = -90f
                    categoryTotals.entries.forEach { entry ->
                        val sweep = ((entry.value / chartBase) * 360.0).toFloat()
                        drawArc(
                            color = categoryColors[entry.key] ?: emptyChartColor,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                        )
                        startAngle += sweep
                    }

                    if (hasSalary && freeSalary > 0.0) {
                        val freeSweep = ((freeSalary / chartBase) * 360.0).toFloat()
                        drawArc(
                            color = freeColor,
                            startAngle = startAngle,
                            sweepAngle = freeSweep,
                            useCenter = true,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categoryTotals.entries.forEach { entry ->
                    val share = if (hasSalary) {
                        entry.value / monthlySalary * 100.0
                    } else {
                        if (chartBase > 0.0) entry.value / chartBase * 100.0 else 0.0
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(categoryColors[entry.key] ?: colors.primary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$${entry.value.toLong().formatThousands()} · ${share.formatPercentage()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurfaceVariant,
                        )
                    }
                }

                if (hasSalary && freeSalary > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(freeColor)
                                .border(1.dp, colors.outlineVariant),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sueldo libre",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$${freeSalary.toLong().formatThousands()} · ${freePercent.formatPercentage()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = freeColor,
                        )
                    }
                }
            }
        }
    }
}

/** "2026-10-08" -> "08/10" */
private fun LocalDate.shortDate(): String {
    val iso = toString()
    return "${iso.substring(8, 10)}/${iso.substring(5, 7)}"
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    categoryColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, shape)
            .border(2.dp, colors.outlineVariant, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = expense.title ?: expense.categoryName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$${expense.amount.toLong().formatThousands()}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = colors.primary,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(categoryColor))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${expense.categoryName} · ${expense.spentAt.shortDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        PixelIconButton(
            icon = PixelIcon.Edit,
            contentDescription = "Editar gasto",
            onClick = onEdit,
            buttonSize = 40.dp,
        )
        PixelIconButton(
            icon = PixelIcon.Trash,
            contentDescription = "Eliminar gasto",
            onClick = onDelete,
            variant = PixelButtonVariant.Danger,
            buttonSize = 40.dp,
        )
    }
}

/** Alta y edición de un gasto: solo nombre, monto y categoría. */
@Composable
private fun ExpenseDialog(
    initialExpense: Expense?,
    categories: List<Category>,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit,
) {
    val expenseCategories = categories.ifEmpty {
        listOf(
            Category(name = "Vivienda"),
            Category(name = "Comida"),
            Category(name = "Transporte"),
            Category(name = "Salud"),
            Category(name = "Ropa"),
            Category(name = "Juegos"),
            Category(name = "Otros"),
        )
    }

    var title by remember(initialExpense) { mutableStateOf(initialExpense?.title ?: "") }
    var amountText by remember(initialExpense) {
        mutableStateOf(initialExpense?.amount?.toLong()?.takeIf { it > 0 }?.toString() ?: "")
    }
    var selectedCategory by remember(initialExpense, expenseCategories) {
        mutableStateOf(
            expenseCategories.firstOrNull { it.id == initialExpense?.categoryId }
                ?: expenseCategories.firstOrNull { it.name == initialExpense?.categoryName }
                ?: expenseCategories.firstOrNull { it.name == "Otros" }
                ?: expenseCategories.firstOrNull()
                ?: Category(name = initialExpense?.categoryName ?: "Otros"),
        )
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val valid = title.isNotBlank() && amount > 0.0

    PixelDialog(
        onDismiss = onDismiss,
        title = if (initialExpense != null) "Editar gasto" else "Nuevo gasto",
        actions = {
            PixelButton(
                text = when {
                    isSaving -> "Guardando..."
                    initialExpense != null -> "Guardar cambios"
                    else -> "Agregar gasto"
                },
                onClick = {
                    if (!valid || isSaving) return@PixelButton
                    val defaultDate = today()
                    // El tipo y la durabilidad ya no se piden: se conservan los de un gasto existente
                    val finalExpense = (initialExpense ?: Expense(amount = amount, spentAt = defaultDate)).copy(
                        title = title.trim(),
                        amount = amount,
                        categoryId = selectedCategory.id.ifBlank { initialExpense?.categoryId },
                        categoryName = selectedCategory.name,
                        kind = initialExpense?.kind ?: ExpenseKind.ONE_OFF,
                        durability = initialExpense?.durability ?: Durability.MEDIUM,
                        spentAt = initialExpense?.spentAt ?: defaultDate,
                    )
                    onSave(finalExpense)
                },
                enabled = valid && !isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            PixelTextButton(text = "Cancelar", onClick = onDismiss)
        },
    ) {
        PixelTextField(
            value = title,
            onValueChange = { title = it },
            label = "Nombre",
            placeholder = "Ej: Alquiler, Súper...",
            maxLength = 40,
        )
        PixelAmountField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Monto",
        )
        PixelChipGroup(
            label = "Categoría",
            options = expenseCategories.map { it to it.name },
            selected = selectedCategory,
            onSelected = { selectedCategory = it },
        )
        if (saveError != null) {
            Text(
                text = saveError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = message,
        color = colors.onErrorContainer,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorContainer, PixelCutShape(3.dp))
            .padding(12.dp),
    )
}
