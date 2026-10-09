package com.example.caprichoapp.feature.onboarding

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.CaprichoTheme
import com.example.caprichoapp.core.designsystem.components.LowSalaryConfirmDialog
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.designsystem.pixel.PixelAmountField
import com.example.caprichoapp.core.designsystem.pixel.PixelButton
import com.example.caprichoapp.core.designsystem.pixel.PixelCutShape
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon
import com.example.caprichoapp.core.designsystem.pixel.PixelIconButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.core.navigation.PlatformBackHandler
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onSaved: (Profile) -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Gesto/botón atrás del sistema: mismo efecto que la flecha de la pantalla
    PlatformBackHandler(enabled = !state.isSaving, onBack = onBack)

    LaunchedEffect(state.savedProfile) {
        state.savedProfile?.let(onSaved)
    }

    OnboardingContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onNicknameChange = viewModel::onNicknameChange,
        onSalaryChange = viewModel::onSalaryChange,
        onSaveClick = { viewModel.onSave() },
        onConfirmLowSalary = viewModel::onConfirmLowSalary,
        onDismissLowSalary = viewModel::onDismissLowSalary,
        onBack = onBack,
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onConfirmLowSalary: () -> Unit = {},
    onDismissLowSalary: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val cardShape = remember { PixelCutShape(3.dp) }

    state.lowSalaryToConfirm?.let { low ->
        LowSalaryConfirmDialog(salary = low, onConfirm = onConfirmLowSalary, onDismiss = onDismissLowSalary)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(Modifier.fillMaxWidth()) {
            PixelIconButton(
                icon = PixelIcon.ArrowLeft,
                contentDescription = "Volver",
                onClick = { if (!state.isSaving) onBack() },
                buttonSize = 48.dp,
            )
        }
        Spacer(Modifier.height(8.dp))
        Mascot(MascotMood.Idle, Modifier.size(110.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "¡CONTANOS UN POCO DE VOS!",
            style = CaprichoTheme.pixelText.title,
            color = colors.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Lo usamos para calcular cuánto pesa cada capricho en tu bolsillo. " +
                "Después lo podés cambiar.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))

        // Misma tarjeta que usa el modo edición del Perfil
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerHigh, cardShape)
                .border(3.dp, colors.outline, cardShape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                PixelTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = "Tu nombre",
                    placeholder = "Cómo te llamás",
                    capitalization = KeyboardCapitalization.Words,
                    isError = state.nameError != null,
                )
                FieldMessage(text = state.nameError?.message(), isError = true)
            }

            Column {
                PixelTextField(
                    value = state.nickname,
                    onValueChange = onNicknameChange,
                    label = "Apodo (opcional)",
                    placeholder = "Cómo querés que te llame Capi",
                    capitalization = KeyboardCapitalization.Words,
                    isError = state.nicknameError != null,
                )
                FieldMessage(
                    text = state.nicknameError?.message() ?: "Así te vamos a llamar en la app",
                    isError = state.nicknameError != null,
                )
            }

            Column {
                PixelAmountField(
                    value = state.salary,
                    onValueChange = onSalaryChange,
                    label = "Sueldo mensual",
                )
                FieldMessage(
                    text = state.salaryError?.message() ?: "Lo que cobrás por mes, en pesos",
                    isError = state.salaryError != null,
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        PixelButton(
            text = if (state.isSaving) "Guardando..." else "Empezar",
            onClick = onSaveClick,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )

        state.saveError?.let {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.errorContainer, PixelCutShape(2.dp))
                    .padding(10.dp),
            ) {
                Text(
                    text = it,
                    color = colors.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** Ayuda o error debajo de un campo. Va en Nunito: es texto de lectura. */
@Composable
private fun FieldMessage(text: String?, isError: Boolean) {
    if (text == null) return
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )
}

private fun TextError.message() = when (this) {
    TextError.BLANK -> "Escribí tu nombre"
    TextError.TOO_LONG -> "Es demasiado largo"
}

private fun SalaryError.message() = when (this) {
    SalaryError.INVALID -> "Ingresá tu sueldo mensual"
    SalaryError.NOT_POSITIVE -> "El sueldo tiene que ser mayor a cero"
    SalaryError.TOO_LARGE -> "Ese monto es demasiado grande"
}
