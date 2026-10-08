package com.example.caprichoapp.domain.repository

import com.example.caprichoapp.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}
