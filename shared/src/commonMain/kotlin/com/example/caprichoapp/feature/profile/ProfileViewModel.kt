package com.example.caprichoapp.feature.profile

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

data class ProfileUiState(
    val name: String = "",
    val nickname: String = "",
    val salary: String = "", // dígitos planos del sueldo
    val isEditing: Boolean = false,
    val nameError: TextError? = null,
    val nicknameError: TextError? = null,
    val salaryError: SalaryError? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isInitialized: Boolean = false,
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun initFromProfile(profile: Profile?) {
        if (profile == null || _uiState.value.isInitialized) return
        _uiState.update {
            it.copy(
                name = profile.displayName,
                nickname = profile.nickname ?: "",
                salary = profile.monthlySalary.toLong().toString(),
                isInitialized = true,
            )
        }
    }

    fun startEditing(profile: Profile?) {
        _uiState.update {
            it.copy(
                isEditing = true,
                name = profile?.displayName ?: it.name,
                nickname = profile?.nickname ?: it.nickname,
                salary = profile?.monthlySalary?.toLong()?.toString() ?: it.salary,
                saveError = null,
                nameError = null,
                nicknameError = null,
                salaryError = null,
            )
        }
    }

    fun cancelEditing(profile: Profile?) {
        _uiState.update {
            it.copy(
                isEditing = false,
                name = profile?.displayName ?: it.name,
                nickname = profile?.nickname ?: it.nickname,
                salary = profile?.monthlySalary?.toLong()?.toString() ?: it.salary,
                nameError = null,
                nicknameError = null,
                salaryError = null,
                saveError = null,
            )
        }
    }

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
            saveError = null,
        )
    }

    fun onSave(onProfileUpdated: (Profile) -> Unit) {
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
                    salaryError = salaryError,
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
                onSuccess = { updatedProfile ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            name = updatedProfile.displayName,
                            nickname = updatedProfile.nickname ?: "",
                            salary = updatedProfile.monthlySalary.toLong().toString(),
                        )
                    }
                    onProfileUpdated(updatedProfile)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = error.message ?: "No pudimos guardar los cambios en tu perfil.",
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
