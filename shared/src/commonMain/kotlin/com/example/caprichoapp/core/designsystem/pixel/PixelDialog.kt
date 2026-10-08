package com.example.caprichoapp.core.designsystem.pixel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.caprichoapp.core.designsystem.CaprichoTheme

/**
 * Diálogo con el mismo lenguaje visual que el resto de la app (esquinas escalonadas, borde y sombra dura).
 * Tiene un ancho máximo cómodo, el contenido se desplaza si no entra y las acciones quedan fijas abajo.
 * [onClose] agrega una cruz arriba a la derecha.
 */
@Composable
fun PixelDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    onClose: (() -> Unit)? = null,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = remember { PixelCutShape(4.dp) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .imePadding(),
        ) {
            Box(Modifier.padding(end = 6.dp, bottom = 6.dp)) {
                Box(
                    Modifier
                        .matchParentSize()
                        .offset(6.dp, 6.dp)
                        .background(Color.Black.copy(alpha = 0.45f), shape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 640.dp)
                        .background(colors.surfaceContainerHigh, shape)
                        .border(3.dp, colors.outline, shape)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Box(Modifier.fillMaxWidth().heightIn(min = 44.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = title.uppercase(),
                            style = CaprichoTheme.pixelText.title.copy(fontSize = 17.sp, lineHeight = 22.sp),
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = if (onClose != null) 48.dp else 0.dp),
                        )
                        if (onClose != null) {
                            PixelIconButton(
                                icon = PixelIcon.Cross,
                                contentDescription = "Cerrar",
                                onClick = onClose,
                                modifier = Modifier.align(Alignment.CenterEnd),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        content = content,
                    )
                    if (actions != null) {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            content = actions,
                        )
                    }
                }
            }
        }
    }
}

/** Confirmación de una acción. Con [destructive] el botón principal se pinta como peligro. */
@Composable
fun PixelConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    PixelDialog(
        onDismiss = onDismiss,
        title = title,
        titleColor = if (destructive) colors.error else colors.primary,
        actions = {
            PixelButton(
                text = confirmText,
                onClick = onConfirm,
                variant = if (destructive) PixelButtonVariant.Danger else PixelButtonVariant.Primary,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            PixelTextButton(text = "Cancelar", onClick = onDismiss)
        },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
