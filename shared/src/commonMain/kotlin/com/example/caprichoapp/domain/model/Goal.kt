package com.example.caprichoapp.domain.model

data class Goal(
    val id: String = "",
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val installments: Int = 1,
    val durability: Durability,
    val status: GoalStatus = GoalStatus.ACTIVE,
) {
    val remaining: Double get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
    val isAchieved: Boolean get() = savedAmount >= targetAmount

    /** Monto objetivo mínimo al editar: no se puede bajar de lo que ya se ahorró. */
    val minTargetAmount: Double get() = savedAmount

    /**
     * Aplica la edición de la meta. El objetivo nunca queda por debajo de lo ahorrado y el estado
     * se recalcula: si lo ahorrado alcanza el objetivo pasa a ACHIEVED, si no vuelve a ACTIVE
     * (por ejemplo, al subir el monto de una meta ya cumplida). Una meta archivada sigue archivada.
     */
    fun withEdits(
        title: String,
        targetAmount: Double,
        installments: Int,
        durability: Durability,
    ): Goal {
        val safeTarget = targetAmount.coerceAtLeast(minTargetAmount)
        return copy(
            title = title.trim(),
            targetAmount = safeTarget,
            installments = installments,
            durability = durability,
            status = when {
                status == GoalStatus.ARCHIVED -> GoalStatus.ARCHIVED
                savedAmount >= safeTarget -> GoalStatus.ACHIEVED
                else -> GoalStatus.ACTIVE
            },
        )
    }
}
