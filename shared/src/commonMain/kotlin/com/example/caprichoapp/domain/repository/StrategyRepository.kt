package com.example.caprichoapp.domain.repository

import com.example.caprichoapp.domain.model.StrategyResponse

interface StrategyRepository {
    suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse>
    suspend fun hasStoredExpenses(): Result<Boolean>
}
