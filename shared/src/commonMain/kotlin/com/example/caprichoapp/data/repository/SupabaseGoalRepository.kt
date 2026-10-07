package com.example.caprichoapp.data.repository

import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.data.mapper.toDomain
import com.example.caprichoapp.data.mapper.toDto
import com.example.caprichoapp.data.remote.dto.GoalDto
import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.repository.GoalRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

/**
 * Supabase implementation of GoalRepository.
 * Stores and retrieves goals for the currently logged in user.
 */
class SupabaseGoalRepository(
    private val supabase: SupabaseClient,
) : GoalRepository {

    override suspend fun addGoal(goal: Goal): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE).insert(goal.toDto(userId))
        Unit
    }

    override suspend fun getGoals(): Result<List<Goal>> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .select { filter { eq("user_id", userId) } }
            .decodeList<GoalDto>()
            .map { it.toDomain() }
    }

    override suspend fun updateGoal(goal: Goal): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .update(goal.toDto(userId)) {
                filter {
                    eq("id", goal.id)
                    eq("user_id", userId)
                }
            }
        Unit
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from(TABLE)
            .delete {
                filter {
                    eq("id", goalId)
                    eq("user_id", userId)
                }
            }
        Unit
    }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay una sesión activa")

    private companion object {
        const val TABLE = "goals"
    }
}
