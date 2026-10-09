package com.example.caprichoapp.feature.auth

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.caprichoapp.core.designsystem.pixel.PixelTextButton
import com.example.caprichoapp.core.designsystem.pixel.PixelTextField
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.validation.AccountValidator
import com.example.caprichoapp.domain.validation.EmailError
import com.example.caprichoapp.domain.validation.PasswordError
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import org.koin.compose.viewmodel.koinViewModel

/**
 * Crear cuenta (nombre, apodo, sueldo, correo y contraseña) o ingresar (correo y contraseña).
 *
 * [onSignedUp] avisa que la cuenta y el perfil quedaron guardados; [onCreatingChange] avisa a
 * la navegación que hay una cuenta en creación, para que no mande al Onboarding mientras se
 * guarda el perfil. Al ingresar con una cuenta existente, la navegación sigue sola por la sesión.
 */
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onSignedUp: (Profile) -> Unit,
    onCreatingChange: (Boolean) -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedProfile) {
        state.savedProfile?.let(onSignedUp)
    }
    val creating = state.isSubmitting && state.mode == AccountMode.SIGN_UP
    LaunchedEffect(creating) { onCreatingChange(creating) }
    DisposableEffect(Unit) { onDispose { onCreatingChange(false) } }

    AccountContent(
        state = state,
        onBack = onBack,
        onModeChange = viewModel::onModeChange,
        onNameChange = viewModel::onNameChange,
        onNicknameChange = viewModel::onNicknameChange,
        onSalaryChange = viewModel::onSalaryChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = { viewModel.onSubmit() },
        onConfirmLowSalary = viewModel::onConfirmLowSalary,
        onDismissLowSalary = viewModel::onDismissLowSalary,
    )
}

@Composable
fun AccountContent(
    state: AccountUiState,
    onBack: () -> Unit,
    onModeChange: (AccountMode) -> Unit,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onConfirmLowSalary: () -> Unit = {},
    onDismissLowSalary: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val cardShape = remember { PixelCutShape(3.dp) }
    val signUp = state.mode == AccountMode.SIGN_UP

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
    ) {
        Row(Modifier.fillMaxWidth()) {
            PixelIconButton(
                icon = PixelIcon.ArrowLeft,
                contentDescription = "Volver",
                onClick = onBack,
                buttonSize = 48.dp,
            )
        }
        Spacer(Modifier.height(8.dp))
        Mascot(if (signUp) MascotMood.Idle else MascotMood.Happy, Modifier.size(96.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (signUp) "CREÁ TU CUENTA" else "INGRESAR",
            style = CaprichoTheme.pixelText.title,
            color = colors.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (signUp) {
                "Con tu correo y contraseña vas a poder volver a entrar cuando quieras."
            } else {
                "Entrá con tu correo y contraseña para recuperar tus gastos y metas."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerHigh, cardShape)
                .border(3.dp, colors.outline, cardShape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (signUp) {
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

            Column {
                PixelTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = "Correo",
                    placeholder = "tucorreo@mail.com",
                    maxLength = AccountValidator.MAX_EMAIL_LENGTH,
                    keyboardType = KeyboardType.Email,
                    isError = state.emailError != null,
                )
                FieldMessage(text = state.emailError?.message(), isError = true)
            }
            Column {
                PixelTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    placeholder = if (signUp) "Mínimo ${AccountValidator.MIN_PASSWORD_LENGTH} caracteres" else "Tu contraseña",
                    maxLength = AccountValidator.MAX_PASSWORD_LENGTH,
                    isPassword = true,
                    isError = state.passwordError != null,
                )
                FieldMessage(text = state.passwordError?.message(), isError = true)
            }
        }
        Spacer(Modifier.height(24.dp))

        PixelButton(
            text = when {
                state.isSubmitting -> if (signUp) "Creando cuenta..." else "Ingresando..."
                signUp -> "Crear cuenta"
                else -> "Ingresar"
            },
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        PixelTextButton(
            text = if (signUp) "¿Ya tenés cuenta? Ingresá" else "¿No tenés cuenta? Creala",
            onClick = { onModeChange(if (signUp) AccountMode.SIGN_IN else AccountMode.SIGN_UP) },
        )

        state.errorMessage?.let { MessageBox(it, isError = true) }
        state.infoMessage?.let { MessageBox(it, isError = false) }
    }
}

@Composable
private fun MessageBox(text: String, isError: Boolean) {
    val colors = MaterialTheme.colorScheme
    Spacer(Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isError) colors.errorContainer else colors.primaryContainer, PixelCutShape(2.dp))
            .padding(10.dp),
    ) {
        Text(
            text = text,
            color = if (isError) colors.onErrorContainer else colors.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
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

private fun EmailError.message() = when (this) {
    EmailError.BLANK -> "Escribí tu correo"
    EmailError.INVALID -> "Revisá el correo: parece que le falta algo"
}

private fun PasswordError.message() = when (this) {
    PasswordError.EMPTY -> "Escribí tu contraseña"
    PasswordError.TOO_SHORT -> "Tiene que tener al menos ${AccountValidator.MIN_PASSWORD_LENGTH} caracteres"
    PasswordError.TOO_LONG -> "Es demasiado larga"
}
