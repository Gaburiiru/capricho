package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

data class PixelPillOption<T>(
    val value: T,
    val title: String,
    val subtitle: String? = null,
    val icon: PixelIcon? = null,
)

private val PillGap = 12.dp

/**
 * Grupo de opciones estilo arcade que aprovecha el alto disponible:
 * las opciones se agrandan para llenar la pantalla (con un máximo) y, si no entran,
 * el grupo se desplaza. Con [columns] > 1 se muestran como fichas en grilla.
 */
@Composable
fun <T> PixelPillGroup(
    options: List<PixelPillOption<T>>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 1,
) {
    val perRow = columns.coerceAtLeast(1)
    val tiles = perRow > 1
    val minItemHeight = if (tiles) 88.dp else 76.dp
    val maxItemHeight = if (tiles) 112.dp else 96.dp
    val rows = options.chunked(perRow)

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val bounded = constraints.hasBoundedHeight
        val viewportHeight = maxHeight
        val itemHeight = if (bounded) {
            ((viewportHeight - PillGap * (rows.size - 1)) / rows.size).coerceIn(minItemHeight, maxItemHeight)
        } else {
            minItemHeight
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .then(if (bounded) Modifier.heightIn(min = viewportHeight) else Modifier),
            verticalArrangement = Arrangement.spacedBy(PillGap, Alignment.Top),
        ) {
            rows.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PillGap),
                ) {
                    rowOptions.forEach { option ->
                        Box(Modifier.weight(1f)) {
                            PixelPill(
                                option = option,
                                tile = tiles,
                                itemHeight = itemHeight,
                                isSelected = option.value == selectedOption,
                                onClick = { onOptionSelected(option.value) },
                            )
                        }
                    }
                    // Completa la última fila para que las fichas mantengan su ancho
                    repeat(perRow - rowOptions.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun <T> PixelPill(
    option: PixelPillOption<T>,
    tile: Boolean,
    itemHeight: Dp,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val pixel = CaprichoTheme.pixelText
    val shape = remember { PixelCutShape(3.dp) }

    val bgColor = if (isSelected) colors.primary else colors.surfaceContainer
    val textColor = if (isSelected) colors.onPrimary else colors.onSurface
    val subtitleColor = if (isSelected) colors.onPrimary.copy(alpha = 0.9f) else colors.onSurfaceVariant
    val shadowColor = if (isDarkTheme()) colors.outline.copy(alpha = 0.6f) else colors.outlineVariant

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateDpAsState(if (pressed) 3.dp else 0.dp, tween(50), label = "pillPress")

    Box(Modifier.padding(end = 4.dp, bottom = 4.dp)) {
        // Sombra dura
        Box(
            Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(shadowColor, shape),
        )

        Box(
            Modifier
                .offset { IntOffset(press.roundToPx(), press.roundToPx()) }
                .fillMaxWidth()
                .height(itemHeight)
                .clip(shape)
                .background(bgColor, shape)
                .then(if (isSelected) Modifier.border(2.dp, colors.onPrimary, shape) else Modifier)
                .selectable(
                    selected = isSelected,
                    interactionSource = interaction,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = if (tile) Alignment.Center else Alignment.CenterStart,
        ) {
            if (tile) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = option.title.uppercase(),
                        style = pixel.tileNumber,
                        color = textColor,
                        maxLines = 1,
                    )
                    if (!option.subtitle.isNullOrBlank()) {
                        Text(
                            text = option.subtitle.uppercase(),
                            style = pixel.tileLabel,
                            color = subtitleColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (option.icon != null) {
                        PixelIconImage(
                            icon = option.icon,
                            tint = textColor,
                            modifier = Modifier.width(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = option.title.uppercase(),
                            style = pixel.pillTitle,
                            color = textColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!option.subtitle.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = subtitleColor,
                            )
                        }
                    }
                    if (isSelected) {
                        Spacer(Modifier.width(8.dp))
                        PixelIconImage(
                            icon = PixelIcon.Check,
                            tint = textColor,
                            modifier = Modifier.width(20.dp),
                        )
                    }
                }
            }

            if (tile && isSelected) {
                PixelIconImage(
                    icon = PixelIcon.Check,
                    tint = textColor,
                    modifier = Modifier.align(Alignment.TopEnd).width(16.dp),
                )
            }
        }
    }
}
