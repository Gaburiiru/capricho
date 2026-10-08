package com.example.caprichoapp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `withEdits no deja bajar el objetivo por debajo de lo ahorrado`() {
        val edited = goal(2_000_000.0, 1_000_000.0)
            .withEdits("Moto", 900_000.0, 6, Durability.HIGH)
        assertEquals(1_000_000.0, edited.targetAmount)
        assertEquals(GoalStatus.ACHIEVED, edited.status)
    }

    @Test
    fun `withEdits respeta un objetivo valido y mantiene la meta en curso`() {
        val edited = goal(2_000_000.0, 1_000_000.0)
            .withEdits("  Moto nueva ", 1_500_000.0, 12, Durability.MEDIUM)
        assertEquals("Moto nueva", edited.title)
        assertEquals(1_500_000.0, edited.targetAmount)
        assertEquals(12, edited.installments)
        assertEquals(Durability.MEDIUM, edited.durability)
        assertEquals(GoalStatus.ACTIVE, edited.status)
    }

    @Test
    fun `withEdits reabre una meta cumplida si se sube el objetivo`() {
        val achieved = goal(1_000.0, 1_000.0).copy(status = GoalStatus.ACHIEVED)
        val edited = achieved.withEdits("Moto", 3_000.0, 1, Durability.HIGH)
        assertEquals(GoalStatus.ACTIVE, edited.status)
        assertFalse(edited.isAchieved)
    }
}
