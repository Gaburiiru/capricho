package com.example.caprichoapp.data.repository

import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.repository.GoalRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Simple in‑memory repository. Data is lost on app restart but good for quick prototyping */
class InMemoryGoalRepository : GoalRepository {
    private val goals = mutableListOf<Goal>()
    private val mutex = Mutex()

    override suspend fun addGoal(goal: Goal): Result<Unit> = runCatching {
        mutex.withLock {
            goals += goal
        }
    }

    override suspend fun getGoals(): Result<List<Goal>> = runCatching {
        mutex.withLock {
            goals.toList()
        }
    }

    override suspend fun updateGoal(goal: Goal): Result<Unit> = runCatching {
        mutex.withLock {
            val index = goals.indexOfFirst { it.id == goal.id }
            if (index != -1) {
                goals[index] = goal
            }
        }
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> = runCatching {
        mutex.withLock {
            goals.removeAll { it.id == goalId }
        }
    }
}
