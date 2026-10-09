package com.example.caprichoapp.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.components.LowSalaryConfirmDialog
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelAmountField
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelTextButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    profile: Profile?,
    onProfileSaved: (Profile) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val cardShape = remember { PixelCutShape(3.dp) }

    LaunchedEffect(profile) {
        viewModel.initFromProfile(profile)
    }

    state.lowSalaryToConfirm?.let { low ->
        LowSalaryConfirmDialog(
            salary = low,
            onConfirm = { viewModel.onConfirmLowSalary(onProfileSaved) },
            onDismiss = viewModel::onDismissLowSalary,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Mascot(MascotMood.Idle, Modifier.size(110.dp))

        if (!state.isEditing) {
            if (profile != null) {
                Text(
                    text = profile.shownName.uppercase(),
                    style = CaprichoTheme.pixelText.title.copy(fontSize = 24.sp, lineHeight = 30.sp),
                    color = colors.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHigh, cardShape)
                        .border(3.dp, colors.outline, cardShape)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = "SUELDO MENSUAL",
                        style = CaprichoTheme.pixelText.tag,
                        color = colors.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$ ${profile.monthlySalary.toLong().formatThousands()}",
                        style = CaprichoTheme.pixelText.display.copy(fontSize = 26.sp, lineHeight = 30.sp),
                        color = colors.onSurface,
                    )
                }

                Spacer(Modifier.height(8.dp))

                PixelButton(
                    text = "Editar mi perfil",
                    onClick = { viewModel.startEditing(profile) },
                    icon = PixelIcon.Edit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))
            PixelTextButton(text = "Cerrar sesión", onClick = onSignOut)
        } else {
            // Modo edición
            Text(
                text = "EDITAR MI PERFIL",
                style = CaprichoTheme.pixelText.title.copy(fontSize = 20.sp),
                color = colors.primary,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHigh, cardShape)
                    .border(3.dp, colors.outline, cardShape)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    PixelTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nombre completo",
                        placeholder = "Tu nombre real",
                    )
                    state.nameError?.let { err ->
                        val msg = when (err) {
                            TextError.BLANK -> "El nombre no puede estar vacío."
                            TextError.TOO_LONG -> "El nombre es demasiado largo."
                        }
                        Text(msg, color = colors.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Column {
                    PixelTextField(
                        value = state.nickname,
                        onValueChange = viewModel::onNicknameChange,
                        label = "Apodo (opcional)",
                        placeholder = "Cómo querés que te llame Capi",
                    )
                    state.nicknameError?.let { err ->
                        val msg = when (err) {
                            TextError.BLANK -> ""
                            TextError.TOO_LONG -> "El apodo es demasiado largo."
                        }
                        if (msg.isNotEmpty()) {
                            Text(msg, color = colors.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Column {
                    PixelAmountField(
                        value = state.salary,
                        onValueChange = viewModel::onSalaryChange,
                        label = "Sueldo mensual ($)",
                        initiallyExpanded = true,
                    )
                    state.salaryError?.let { err ->
                        val msg = when (err) {
                            SalaryError.INVALID, SalaryError.NOT_POSITIVE -> "Ingresá un sueldo válido mayor a 0."
                            SalaryError.TOO_LARGE -> "El sueldo supera el máximo permitido."
                        }
                        Text(msg, color = colors.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                state.saveError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.errorContainer, PixelCutShape(2.dp))
                            .padding(10.dp),
                    ) {
                        Text(err, color = colors.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PixelButton(
                    text = if (state.isSaving) "Guardando..." else "Guardar cambios",
                    onClick = { viewModel.onSave(onProfileSaved) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )

                PixelTextButton(
                    text = "Cancelar",
                    onClick = { viewModel.cancelEditing(profile) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
