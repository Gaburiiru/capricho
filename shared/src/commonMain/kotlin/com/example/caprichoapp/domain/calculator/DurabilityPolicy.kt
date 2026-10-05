package com.example.caprichoapp.domain.calculator

import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.ImpactLevel

object DurabilityPolicy {

    private data class Thresholds(val good: Double, val warning: Double)

    private val thresholds = mapOf(
        Durability.FLEETING to Thresholds(good = 0.05, warning = 0.10),
        Durability.MEDIUM to Thresholds(good = 0.10, warning = 0.25),
        Durability.HIGH to Thresholds(good = 0.20, warning = 0.40),
    )

    fun levelFor(impact: Double, durability: Durability): ImpactLevel {
        val t = thresholds.getValue(durability)
        return when {
            impact < t.good -> ImpactLevel.GOOD
            impact <= t.warning -> ImpactLevel.WARNING
            else -> ImpactLevel.HEAVY
        }
    }
}