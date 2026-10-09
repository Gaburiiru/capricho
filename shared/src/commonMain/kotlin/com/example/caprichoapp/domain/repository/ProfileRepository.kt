package com.example.caprichoapp.domain.repository

import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft

interface ProfileRepository {
    /** Devuelve null si el usuario todavía no hizo el onboarding. */
    suspend fun fetchProfile(): Result<Profile?>

    suspend fun saveProfile(draft: ProfileDraft): Result<Profile>
}
