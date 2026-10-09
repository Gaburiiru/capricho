package com.example.caprichoapp.core.designsystem.components

import androidx.compose.runtime.Composable
import com.example.caprichoapp.core.designsystem.pixel.PixelConfirmDialog
import com.example.caprichoapp.core.util.formatThousands

/**
 * "¿Seguro que tu sueldo es $9.000?" — se usa en el onboarding y en editar perfil cuando el
 * sueldo ingresado es sospechosamente bajo. Muestra el monto que escribió el usuario.
 */
@Composable
fun LowSalaryConfirmDialog(
    salary: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PixelConfirmDialog(
        title = "¿Seguro?",
        message = "¿Seguro que tu sueldo mensual es de $${salary.toLong().formatThousands()}? " +
            "Es un monto bajo y con él calculamos cuánto pesa cada capricho.",
        confirmText = "Sí, es correcto",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = false,
    )
}
