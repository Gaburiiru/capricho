package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

/** [Regular] ocupa el ancho disponible (acciones principales); [Compact] se ajusta al texto (headers, filas). */
enum class PixelButtonSize { Regular, Compact }

/** [Primary] acción principal, [Secondary] acción alternativa, [Danger] acción destructiva. */
enum class PixelButtonVariant { Primary, Secondary, Danger }

private data class ButtonPalette(val background: Color, val content: Color, val shadow: Color, val border: Color?)

@Composable
private fun buttonPalette(variant: PixelButtonVariant, enabled: Boolean): ButtonPalette {
    val colors = MaterialTheme.colorScheme
    val dark = isDarkTheme()
    if (!enabled) {
        return ButtonPalette(
            background = colors.surfaceContainerHighest,
            content = colors.onSurfaceVariant.copy(alpha = 0.5f),
            shadow = colors.outlineVariant,
            border = null,
        )
    }
    return when (variant) {
        PixelButtonVariant.Primary -> ButtonPalette(
            background = colors.primary,
            content = colors.onPrimary,
            shadow = if (dark) colors.primaryContainer else colors.onPrimaryContainer,
            border = null,
        )
        PixelButtonVariant.Secondary -> ButtonPalette(
            background = colors.surfaceContainerHighest,
            content = colors.onSurface,
            shadow = if (dark) colors.outline.copy(alpha = 0.7f) else colors.outlineVariant,
            border = colors.outline,
        )
        PixelButtonVariant.Danger -> ButtonPalette(
            background = colors.errorContainer,
            content = colors.onErrorContainer,
            shadow = colors.error.copy(alpha = 0.45f),
            border = null,
        )
    }
}

/**
 * Botón estilo arcade: esquinas escalonadas, sombra dura y efecto de "hundirse" al apretarlo.
 * La flecha final solo se muestra por defecto en los botones principales de ancho completo;
 * el texto nunca se corta: el compacto se ajusta a su contenido en lugar de tener un ancho fijo.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: PixelButtonSize = PixelButtonSize.Regular,
    variant: PixelButtonVariant = PixelButtonVariant.Primary,
    icon: PixelIcon? = null,
    showArrow: Boolean = size == PixelButtonSize.Regular && variant == PixelButtonVariant.Primary && icon == null,
) {
    val compact = size == PixelButtonSize.Compact
    val shape = remember { PixelCutShape(3.dp) }
    val palette = buttonPalette(variant, enabled)
    val depth = if (compact) 3.dp else 4.dp

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateDpAsState(if (pressed && enabled) depth else 0.dp, tween(60), label = "press")

    Box(modifier = modifier.padding(end = depth, bottom = depth)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(depth, depth)
                .background(palette.shadow, shape),
        )
        Box(
            Modifier
                .offset { IntOffset(press.roundToPx(), press.roundToPx()) }
                .then(if (compact) Modifier else Modifier.fillMaxWidth())
                .heightIn(min = if (compact) 40.dp else 56.dp)
                .clip(shape)
                .background(palette.background, shape)
                .then(if (palette.border != null) Modifier.border(2.dp, palette.border, shape) else Modifier)
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = if (compact) 14.dp else 16.dp, vertical = if (compact) 6.dp else 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (icon != null) {
                    PixelIconImage(
                        icon = icon,
                        tint = palette.content,
                        modifier = Modifier.width(if (compact) 12.dp else 14.dp),
                    )
                }
                Text(
                    text = text.uppercase(),
                    color = palette.content,
                    style = CaprichoTheme.pixelText.button.copy(fontSize = if (compact) 12.sp else 14.sp),
                    maxLines = 1,
                    softWrap = false,
                )
                if (showArrow) {
                    PixelIconImage(
                        icon = PixelIcon.ArrowRight,
                        tint = palette.content,
                        modifier = Modifier.width(10.dp),
                    )
                }
            }
        }
    }
}

/** Botón cuadrado solo con ícono (editar, borrar, cerrar); 44dp por defecto, el mínimo cómodo para tocar. */
@Composable
fun PixelIconButton(
    icon: PixelIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PixelButtonVariant = PixelButtonVariant.Secondary,
    buttonSize: Dp = 44.dp,
) {
    val shape = remember { PixelCutShape(2.dp) }
    val palette = buttonPalette(variant, enabled = true)

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(shape)
            .background(palette.background, shape)
            .then(if (palette.border != null) Modifier.border(2.dp, palette.border, shape) else Modifier)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PixelIconImage(icon = icon, tint = palette.content, modifier = Modifier.width(16.dp))
    }
}

/** Acción de texto, para "Cancelar" o "Eliminar" dentro de diálogos (área táctil de 48dp). */
@Composable
fun PixelTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(PixelCutShape(2.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = CaprichoTheme.pixelText.button.copy(fontSize = 12.sp),
            maxLines = 1,
            softWrap = false,
        )
    }
}
