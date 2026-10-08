package com.example.caprichoapp.domain.model

data class StrategyResponse(
    val summary: String,
    val tips: List<String>,
    val estimatedSavings: String? = null,
)
