package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

/**
 * Botón principal estilo arcade: esquinas escalonadas, sombra dura
 * y efecto de "hundirse" al apretarlo.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    val shadowColor = if (enabled) {
        if (isDarkTheme()) colors.primaryContainer else colors.onPrimaryContainer
    } else {
        colors.outlineVariant
    }
    val buttonBg = if (enabled) colors.primary else colors.surfaceContainerHighest
    val textColor = if (enabled) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f)

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateDpAsState(if (pressed && enabled) 4.dp else 0.dp, tween(60), label = "press")

    Box(
        modifier = modifier.padding(end = 4.dp, bottom = 4.dp),
    ) {
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
                .height(56.dp)
                .clip(shape)
                .background(buttonBg, shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = text.uppercase(),
                    color = textColor,
                    style = CaprichoTheme.pixelText.button.copy(fontSize = 14.sp),
                    maxLines = 1,
                    softWrap = false,
                )
                PixelIconImage(
                    icon = PixelIcon.ArrowRight,
                    tint = textColor,
                    modifier = Modifier.height(14.dp).width(10.dp),
                )
            }
        }
    }
}
