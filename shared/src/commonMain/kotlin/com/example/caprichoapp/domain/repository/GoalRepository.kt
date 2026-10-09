package com.example.caprichoapp.domain.repository

import com.example.caprichoapp.domain.model.Goal

interface GoalRepository {
    /** Persist a new goal */
    suspend fun addGoal(goal: Goal): Result<Unit>
    /** Retrieve all goals */
    suspend fun getGoals(): Result<List<Goal>>
    /** Update an existing goal */
    suspend fun updateGoal(goal: Goal): Result<Unit>
    /** Delete a goal by id */
    suspend fun deleteGoal(goalId: String): Result<Unit>
}
