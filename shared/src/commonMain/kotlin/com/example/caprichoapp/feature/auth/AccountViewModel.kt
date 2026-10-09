package com.example.caprichoapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import com.example.caprichoapp.domain.repository.SignUpOutcome
import com.example.caprichoapp.domain.validation.AccountValidator
import com.example.caprichoapp.domain.validation.EmailError
import com.example.caprichoapp.domain.validation.PasswordError
import com.example.caprichoapp.domain.validation.ProfileValidator
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AccountMode {
    /** Cuenta nueva: los mismos datos del onboarding + correo y contraseña. */
    SIGN_UP,

    /** Ya tiene cuenta: solo correo y contraseña. */
    SIGN_IN,
}

data class AccountUiState(
    val mode: AccountMode = AccountMode.SIGN_UP,
    val name: String = "",
    val nickname: String = "",
    val salary: String = "", // solo dígitos
    val email: String = "",
    val password: String = "",
    val nameError: TextError? = null,
    val nicknameError: TextError? = null,
    val salaryError: SalaryError? = null,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    /** Cuenta creada y perfil guardado: la pantalla avisa y la app sigue al Home. */
    val savedProfile: Profile? = null,
    /** Si no es null, se muestra "¿seguro que tu sueldo es X?" con este monto. */
    val lowSalaryToConfirm: Double? = null,
)

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: AccountMode) = _uiState.update {
        if (it.isSubmitting) it else it.copy(
            mode = mode,
            nameError = null,
            nicknameError = null,
            salaryError = null,
            emailError = null,
            passwordError = null,
            errorMessage = null,
            infoMessage = null,
        )
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }

    fun onNicknameChange(value: String) =
        _uiState.update { it.copy(nickname = value, nicknameError = null, errorMessage = null) }

    fun onSalaryChange(value: String) = _uiState.update {
        it.copy(
            salary = value.filter { c -> c in '0'..'9' }.take(MAX_SALARY_DIGITS),
            salaryError = null,
            errorMessage = null,
        )
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, errorMessage = null, infoMessage = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, errorMessage = null, infoMessage = null)
    }

    fun onSubmit(skipLowSalaryCheck: Boolean = false) {
        val current = _uiState.value
        if (current.isSubmitting) return
        when (current.mode) {
            AccountMode.SIGN_IN -> signIn(current)
            AccountMode.SIGN_UP -> signUp(current, skipLowSalaryCheck)
        }
    }

    /** El usuario confirmó que el sueldo bajo es correcto: se crea la cuenta. */
    fun onConfirmLowSalary() {
        _uiState.update { it.copy(lowSalaryToConfirm = null) }
        onSubmit(skipLowSalaryCheck = true)
    }

    /** El usuario canceló: vuelve al formulario para corregir el sueldo. */
    fun onDismissLowSalary() {
        _uiState.update { it.copy(lowSalaryToConfirm = null) }
    }

    private fun signIn(current: AccountUiState) {
        val emailError = AccountValidator.validateEmail(current.email)
        val passwordError = AccountValidator.validateLoginPassword(current.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            authRepository.signInWithEmail(AccountValidator.normalizeEmail(current.email), current.password)
                .onSuccess {
                    // authState cambia y la app navega sola (Home, o Onboarding si faltan sus datos)
                    _uiState.update { it.copy(isSubmitting = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.toAuthMessage()) }
                }
        }
    }

    private fun signUp(current: AccountUiState, skipLowSalaryCheck: Boolean) {
        val nameError = ProfileValidator.validateName(current.name)
        val nicknameError = ProfileValidator.validateNickname(current.nickname)
        val salaryError = ProfileValidator.validateSalary(current.salary)
        val emailError = AccountValidator.validateEmail(current.email)
        val passwordError = AccountValidator.validatePassword(current.password)
        if (listOf(nameError, nicknameError, salaryError, emailError, passwordError).any { it != null }) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    nicknameError = nicknameError,
                    salaryError = salaryError,
                    emailError = emailError,
                    passwordError = passwordError,
                )
            }
            return
        }

        if (!skipLowSalaryCheck) {
            ProfileValidator.lowSalaryToConfirm(current.salary)?.let { low ->
                _uiState.update { it.copy(lowSalaryToConfirm = low) }
                return
            }
        }

        val draft = ProfileDraft(
            displayName = current.name.trim(),
            nickname = current.nickname.trim().ifBlank { null },
            monthlySalary = current.salary.toDouble(),
        )
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            authRepository.signUpWithEmail(AccountValidator.normalizeEmail(current.email), current.password)
                .onSuccess { outcome ->
                    when (outcome) {
                        SignUpOutcome.SIGNED_IN -> saveProfileAfterSignUp(draft)
                        SignUpOutcome.EMAIL_CONFIRMATION_REQUIRED -> _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                mode = AccountMode.SIGN_IN,
                                password = "",
                                infoMessage = "Te mandamos un correo para confirmar tu cuenta. " +
                                    "Confirmalo y después ingresá desde acá.",
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.toAuthMessage()) }
                }
        }
    }

    private suspend fun saveProfileAfterSignUp(draft: ProfileDraft) {
        profileRepository.saveProfile(draft)
            .onSuccess { profile ->
                _uiState.update { it.copy(isSubmitting = false, savedProfile = profile) }
            }
            .onFailure {
                // La cuenta existe pero sin perfil. Se cierra la sesión para no dejar al usuario
                // trabado acá: al ingresar con su correo, la app le vuelve a pedir sus datos.
                authRepository.signOut()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        mode = AccountMode.SIGN_IN,
                        password = "",
                        errorMessage = "Creamos tu cuenta, pero no pudimos guardar tus datos. " +
                            "Ingresá con tu correo y contraseña para completarlos.",
                    )
                }
            }
    }

    private companion object {
        const val MAX_SALARY_DIGITS = 10
    }
}

/** Traduce el error de Supabase a un mensaje para el usuario (el detalle técnico no se muestra). */
internal fun Throwable.toAuthMessage(): String {
    val text = message.orEmpty().lowercase()
    return when {
        "invalid login credentials" in text || "invalid_credentials" in text ->
            "Correo o contraseña incorrectos."
        "already registered" in text || "already been registered" in text || "user_already_exists" in text ->
            "Ese correo ya tiene una cuenta. Ingresá con tu contraseña."
        "email not confirmed" in text || "email_not_confirmed" in text ->
            "Primero confirmá tu correo: revisá tu bandeja de entrada."
        "rate limit" in text || "too many" in text ->
            "Hiciste muchos intentos seguidos. Esperá un momento y probá de nuevo."
        "password" in text && ("weak" in text || "at least" in text || "characters" in text) ->
            "Esa contraseña es muy débil. Probá con una más larga."
        else -> "No pudimos conectarnos. Revisá tu conexión e intentá de nuevo."
    }
}
