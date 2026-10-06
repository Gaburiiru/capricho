package com.example.caprichoapp.data.mapper

import com.example.caprichoapp.data.remote.dto.ProfileDto
import com.example.caprichoapp.domain.model.Profile
import com.example.caprichoapp.domain.model.ProfileDraft

fun ProfileDto.toDomain() = Profile(
    id = id,
    displayName = displayName,
    monthlySalary = monthlySalary,
    nickname = nickname,
)

fun ProfileDraft.toDto(userId: String) = ProfileDto(
    id = userId,
    displayName = displayName,
    nickname = nickname,
    monthlySalary = monthlySalary,
)
