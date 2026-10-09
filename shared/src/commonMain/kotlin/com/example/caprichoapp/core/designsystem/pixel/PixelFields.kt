package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.util.MAX_AMOUNT_DIGITS
import com.example.caprichoapp.core.util.appendAmountDigit
import com.example.caprichoapp.core.util.formatAmount

/** Rótulo corto en mayúsculas que va arriba de cada campo. */
@Composable
private fun FieldLabel(text: String, highlighted: Boolean) {
    Text(
        text = text.uppercase(),
        style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp, lineHeight = 14.sp),
        color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Campo de texto con el estilo de la app: rótulo arriba, borde escalonado y resaltado al enfocar. */
@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLength: Int = 40,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isError: Boolean = false,
    /** Oculta lo que se escribe y usa el teclado de contraseñas. */
    isPassword: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    var focused by remember { mutableStateOf(false) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, focused)
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization,
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .background(colors.surfaceContainerHighest, shape)
                .border(2.dp, if (isError) colors.error else if (focused) colors.primary else colors.outline, shape)
                .heightIn(min = 52.dp)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                    inner()
                }
            },
        )
    }
}

/**
 * Campo de monto: muestra el valor con puntos de miles ("$ 1.500.000") y, al tocarlo,
 * despliega el teclado numérico pixel de la app (sin teclado del sistema).
 * [value] es el monto en dígitos planos, sin ceros a la izquierda.
 */
@Composable
fun PixelAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    keyHeight: Dp = 48.dp,
    maxDigits: Int = MAX_AMOUNT_DIGITS,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(3.dp) }
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val text = "$ ${value.formatAmount()}"
    // Se achica el texto, no la caja: el campo mantiene siempre el mismo alto
    val fontSize = (230 / text.length.coerceAtLeast(1)).coerceIn(16, 26)

    LaunchedEffect(expanded) {
        if (expanded) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FieldLabel(label, expanded)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(shape)
                .background(colors.surfaceContainerHighest, shape)
                .border(2.dp, if (expanded) colors.primary else colors.outline, shape)
                .clickable(role = Role.Button, onClickLabel = if (expanded) "Cerrar teclado" else "Editar monto") {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    expanded = !expanded
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = CaprichoTheme.pixelText.display.copy(fontSize = fontSize.sp, lineHeight = (fontSize * 1.2f).sp),
                color = if (value.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.6f) else colors.onSurface,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (expanded) "LISTO" else "EDITAR",
                style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp),
                color = colors.primary,
            )
        }
        if (expanded) {
            PixelNumericKeypad(
                onDigitClick = { onValueChange(value.appendAmountDigit(it, maxDigits)) },
                onClearClick = { onValueChange("") },
                onDeleteClick = { onValueChange(value.dropLast(1)) },
                keyHeight = keyHeight,
            )
        }
    }
}

/** Selector de una opción entre pocas (categorías, cuotas, durabilidad) con chips que se acomodan en filas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> PixelChipGroup(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, highlighted = false)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (value, text) ->
                PixelChip(text = text, selected = value == selected, onClick = { onSelected(value) })
            }
        }
    }
}

@Composable
fun PixelChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(2.dp) }

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .background(if (selected) colors.primary else colors.surfaceContainerHighest, shape)
            .border(2.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onClick()
                },
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (selected) colors.onPrimary else colors.onSurface,
            maxLines = 1,
        )
    }
}

/** Etiqueta informativa (no tocable): "6 cuotas", "Alta durabilidad". */
@Composable
fun PixelTag(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val shape = remember { PixelCutShape(2.dp) }
    Text(
        text = text.uppercase(),
        style = CaprichoTheme.pixelText.tag.copy(fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.sp),
        color = color,
        maxLines = 1,
        modifier = modifier
            .background(color.copy(alpha = 0.14f), shape)
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Barra de progreso escalonada, con el mismo largo de relleno animable desde afuera. */
@Composable
fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    height: Dp = 12.dp,
) {
    val shape = remember { PixelCutShape(2.dp) }
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(color, shape),
        )
    }
}
