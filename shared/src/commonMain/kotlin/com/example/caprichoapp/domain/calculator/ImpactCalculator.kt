package com.example.caprichoapp.domain.calculator

import com.example.caprichoapp.domain.model.Goal

object ImpactCalculator {

    /** Monto que se paga por mes. Inmediato = 1 cuota. */
    fun monthlyAmount(amount: Double, installments: Int): Double {
        require(installments >= 1) { "Las cuotas deben ser al menos 1" }
        return amount / installments
    }

    /** Fracción del sueldo (0.10 = 10%). */
    fun salaryImpact(amount: Double, installments: Int, salary: Double): Double {
        require(salary > 0) { "El sueldo debe ser mayor a 0" }
        return monthlyAmount(amount, installments) / salary
    }

    /** Fracción de lo que falta de la meta (0.20 = 20%). Si la meta ya está cumplida, 0. */
    fun goalImpact(amount: Double, goal: Goal): Double {
        val remaining = goal.remaining
        if (remaining <= 0.0) return 0.0
        return amount / remaining
    }

    /**
     * Fracción de la meta que TE FALTA hoy (0.77 = te falta el 77% del valor de la meta).
     * Baja a medida que ahorrás.
     */
    fun goalGapNow(goal: Goal): Double {
        if (goal.targetAmount <= 0.0) return 0.0
        return goal.remaining / goal.targetAmount
    }

    /**
     * Fracción de la meta que te FALTARÍA si hacés el gasto: lo que falta hoy más el monto
     * (el gasto es plata que no va a la meta). Puede superar 1.0. Si la meta ya está cumplida, 0.
     * Baja a medida que ahorrás, porque parte de una distancia menor.
     */
    fun goalGapAfter(amount: Double, goal: Goal): Double {
        if (goal.targetAmount <= 0.0 || goal.remaining <= 0.0) return 0.0
        return (goal.remaining + amount) / goal.targetAmount
    }

    /** Meses extra que te aleja de la meta. Null si no hay capacidad de ahorro. */
    fun delayInMonths(amount: Double, monthlySavingCapacity: Double): Double? {
        if (monthlySavingCapacity <= 0.0) return null
        return amount / monthlySavingCapacity
    }
}