package com.example.caprichoapp.data.mapper

import com.example.caprichoapp.data.remote.dto.GoalDto
import com.example.caprichoapp.domain.model.Durability
import com.example.caprichoapp.domain.model.Goal
import com.example.caprichoapp.domain.model.GoalStatus

fun GoalDto.toDomain(): Goal = Goal(
    id = id.orEmpty(),
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    installments = installments,
    durability = runCatching { Durability.valueOf(durability) }.getOrDefault(Durability.FLEETING),
    status = runCatching { GoalStatus.valueOf(status) }.getOrDefault(GoalStatus.ACTIVE),
)

fun Goal.toDto(userId: String? = null): GoalDto = GoalDto(
    id = id.ifBlank { null },
    userId = userId,
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    installments = installments,
    durability = durability.name,
    status = status.name,
)
