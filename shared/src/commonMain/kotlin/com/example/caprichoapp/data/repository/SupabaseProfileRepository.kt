package com.example.caprichoapp.data.repository

import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.data.mapper.toDomain
import com.example.caprichoapp.data.mapper.toDto
import com.example.caprichoapp.data.remote.dto.ProfileDto
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft
import com.example.caprichoapp.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class SupabaseProfileRepository(
    private val supabase: SupabaseClient,
) : ProfileRepository {

    override suspend fun fetchProfile(): Result<Profile?> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()
    }

    // upsert: si el guardado se reintenta, no duplica ni falla por clave repetida
    override suspend fun saveProfile(draft: ProfileDraft): Result<Profile> =
        runCatchingCancellable {
            val userId = requireUserId()
            supabase.from(TABLE)
                .upsert(draft.toDto(userId)) { select() }
                .decodeSingle<ProfileDto>()
                .toDomain()
        }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay una sesión activa")

    private companion object {
        const val TABLE = "profiles"
    }
}
