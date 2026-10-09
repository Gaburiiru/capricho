package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

data class PixelBarItem(val icon: PixelIcon, val label: String)

/**
 * Barra de navegación inferior con íconos pixel art.
 * Solo muestra íconos; la etiqueta se usa para accesibilidad (TalkBack).
 */
@Composable
fun PixelBottomBar(
    items: List<PixelBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().height(3.dp).background(colors.outline))
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp),
            ) {
                items.forEachIndexed { index, item ->
                    BarItem(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BarItem(
    item: PixelBarItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val lift by animateDpAsState(
        targetValue = if (selected) (-2).dp else 0.dp,
        animationSpec = spring(),
        label = "lift",
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else Color.Transparent, shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = item.label },
        contentAlignment = Alignment.Center,
    ) {
        PixelIconImage(
            icon = item.icon,
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier
                .size(27.dp)
                .offset { IntOffset(0, lift.roundToPx()) },
        )
    }
}
