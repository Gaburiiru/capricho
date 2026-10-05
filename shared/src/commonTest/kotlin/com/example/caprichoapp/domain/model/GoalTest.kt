package com.example.caprichoapp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalTest {
    private fun goal(target: Double, saved: Double) = Goal(
        id = "1", title = "Moto", targetAmount = target, savedAmount = saved,
        durability = Durability.HIGH,
    )

    @Test
    fun `remaining es objetivo menos ahorrado`() {
        assertEquals(700.0, goal(1000.0, 300.0).remaining)
    }

    @Test
    fun `remaining nunca es negativo si ahorraste de más`() {
        assertEquals(0.0, goal(1000.0, 1200.0).remaining)
    }

    @Test
    fun `isAchieved es verdadero al llegar al objetivo`() {
        assertTrue(goal(1000.0, 1000.0).isAchieved)
    }
}