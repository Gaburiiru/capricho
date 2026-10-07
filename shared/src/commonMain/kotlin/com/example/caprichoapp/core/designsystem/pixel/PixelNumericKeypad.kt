package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.caprichoapp.core.designsystem.CaprichoTheme

private sealed interface Key {
    data class Digit(val char: Char) : Key
    data object Clear : Key
    data object Delete : Key
}

/**
 * Teclado numérico estilo arcade 3x4:
 * [ 1 ] [ 2 ] [ 3 ]
 * [ 4 ] [ 5 ] [ 6 ]
 * [ 7 ] [ 8 ] [ 9 ]
 * [ C ] [ 0 ] [ ⌫ ]
 *
 * [keyHeight] permite que las teclas se agranden cuando la pantalla es alta.
 */
@Composable
fun PixelNumericKeypad(
    onDigitClick: (Char) -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 60.dp,
) {
    val keypadRows = listOf(
        listOf(Key.Digit('1'), Key.Digit('2'), Key.Digit('3')),
        listOf(Key.Digit('4'), Key.Digit('5'), Key.Digit('6')),
        listOf(Key.Digit('7'), Key.Digit('8'), Key.Digit('9')),
        listOf(Key.Clear, Key.Digit('0'), Key.Delete),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    Box(modifier = Modifier.weight(1f)) {
                        KeypadButton(
                            key = key,
                            keyHeight = keyHeight,
                            onClick = {
                                when (key) {
                                    is Key.Digit -> onDigitClick(key.char)
                                    Key.Clear -> onClearClick()
                                    Key.Delete -> onDeleteClick()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: Key,
    keyHeight: Dp,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(2.dp) }
    val isActionKey = key !is Key.Digit

    val buttonBg = if (isActionKey) colors.secondaryContainer else colors.surfaceContainerHigh
    val contentColor = if (isActionKey) colors.onSecondaryContainer else colors.onSurface
    val shadowColor = if (isDarkTheme()) colors.outline.copy(alpha = 0.5f) else colors.outlineVariant

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateDpAsState(if (pressed) 3.dp else 0.dp, tween(50), label = "keyPress")

    val description = when (key) {
        is Key.Digit -> key.char.toString()
        Key.Clear -> "Borrar todo"
        Key.Delete -> "Borrar el último dígito"
    }

    Box(modifier = Modifier.padding(end = 3.dp, bottom = 3.dp)) {
        // Sombra
        Box(
            Modifier
                .matchParentSize()
                .offset(3.dp, 3.dp)
                .background(shadowColor, shape),
        )
        // Tecla
        Box(
            Modifier
                .offset { IntOffset(press.roundToPx(), press.roundToPx()) }
                .fillMaxWidth()
                .height(keyHeight)
                .clip(shape)
                .background(buttonBg, shape)
                .semantics { contentDescription = description }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (key) {
                is Key.Digit -> Text(
                    text = key.char.toString(),
                    color = contentColor,
                    style = CaprichoTheme.pixelText.keypad,
                )
                Key.Clear -> Text(
                    text = "C",
                    color = contentColor,
                    style = CaprichoTheme.pixelText.keypad,
                )
                // La fuente pixel no trae el símbolo ⌫: se dibuja como ícono
                Key.Delete -> PixelIconImage(
                    icon = PixelIcon.Backspace,
                    tint = contentColor,
                    modifier = Modifier.width(34.dp),
                )
            }
        }
    }
}
