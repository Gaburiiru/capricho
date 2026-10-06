package com.example.caprichoapp.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.ProfileRepository
import com.example.caprichoapp.domain.validation.ProfileValidator
import com.example.caprichoapp.domain.validation.SalaryError
import com.example.caprichoapp.domain.validation.TextError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val name: String = "",
    val nickname: String = "",
    val salary: String = "", // solo dígitos
    val nameError: TextError? = null,
    val nicknameError: TextError? = null,
    val salaryError: SalaryError? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedProfile: Profile? = null,
)

class OnboardingViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, nameError = null, saveError = null)
    }

    fun onNicknameChange(value: String) = _uiState.update {
        it.copy(nickname = value, nicknameError = null, saveError = null)
    }

    fun onSalaryChange(value: String) = _uiState.update {
        it.copy(
            salary = value.filter { c -> c in '0'..'9' }.take(MAX_SALARY_DIGITS),
            salaryError = null,
            saveError = null
        )
    }

    fun onSave() {
        val current = _uiState.value
        if (current.isSaving) return

        val nameError = ProfileValidator.validateName(current.name)
        val nicknameError = ProfileValidator.validateNickname(current.nickname)
        val salaryError = ProfileValidator.validateSalary(current.salary)
        if (nameError != null || nicknameError != null || salaryError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    nicknameError = nicknameError,
                    salaryError = salaryError
                )
            }
            return
        }

        val draft = ProfileDraft(
            displayName = current.name.trim(),
            nickname = current.nickname.trim().ifBlank { null },
            monthlySalary = current.salary.toDouble(),
        )
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            profileRepository.saveProfile(draft).fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(isSaving = false, savedProfile = profile) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = "No pudimos guardar tus datos. Revisá tu conexión e intentá de nuevo.",
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val MAX_SALARY_DIGITS = 10
    }
}
