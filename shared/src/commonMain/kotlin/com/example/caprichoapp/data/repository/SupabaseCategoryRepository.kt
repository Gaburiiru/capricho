package com.example.caprichoapp.data.repository

import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.data.remote.dto.CategoryDto
import com.example.caprichoapp.domain.model.Category
import com.example.caprichoapp.domain.repository.CategoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseCategoryRepository(
    private val supabase: SupabaseClient,
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> = runCatchingCancellable {
        supabase.from(TABLE)
            .select()
            .decodeList<CategoryDto>()
            .sortedBy { it.name.lowercase() }
            .map { dto ->
                Category(
                    id = dto.id,
                    name = dto.name,
                    icon = dto.icon,
                )
            }
    }

    private companion object {
        const val TABLE = "categories"
    }
}
