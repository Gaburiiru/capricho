package com.example.caprichoapp.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caprichoapp.core.designsystem.mascot.Mascot
import com.example.caprichoapp.core.designsystem.mascot.MascotMood
import com.example.caprichoapp.core.util.formatThousands
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onSaved: (Profile) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedProfile) {
        state.savedProfile?.let(onSaved)
    }

    OnboardingContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onNicknameChange = viewModel::onNicknameChange,
        onSalaryChange = viewModel::onSalaryChange,
        onSaveClick = viewModel::onSave,
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(8.dp))
        Mascot(MascotMood.Idle, Modifier.size(110.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "¡Contanos un poco de vos!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Lo usamos para calcular cuánto pesa cada capricho en tu bolsillo. " +
                "Después lo podés cambiar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Tu nombre") },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it.message()) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.nickname,
            onValueChange = onNicknameChange,
            label = { Text("Apodo (opcional)") },
            singleLine = true,
            isError = state.nicknameError != null,
            supportingText = {
                Text(state.nicknameError?.message() ?: "Así te vamos a llamar en la app")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.salary,
            onValueChange = onSalaryChange,
            label = { Text("Sueldo mensual") },
            prefix = { Text("$ ") },
            singleLine = true,
            isError = state.salaryError != null,
            supportingText = {
                val formatted = state.salary.toLongOrNull()?.formatThousands()
                Text(
                    state.salaryError?.message()
                        ?: formatted?.let { "Son $ $it por mes" }
                        ?: "Lo que cobrás por mes, en pesos",
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSaveClick() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSaveClick,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Empezar")
        }

        state.saveError?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
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
