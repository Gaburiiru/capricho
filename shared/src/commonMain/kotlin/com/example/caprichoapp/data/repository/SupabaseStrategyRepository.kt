package com.example.caprichoapp.data.repository

import com.example.caprichoapp.BuildKonfig
import com.example.caprichoapp.core.util.runCatchingCancellable
import com.example.caprichoapp.data.remote.dto.CategoryDto
import com.example.caprichoapp.data.remote.dto.ExpenseDto
import com.example.caprichoapp.data.remote.dto.ExpensePayloadDto
import com.example.caprichoapp.data.remote.dto.GoalDto
import com.example.caprichoapp.data.remote.dto.GoalPayloadDto
import com.example.caprichoapp.data.remote.dto.ProfileDto
import com.example.caprichoapp.data.remote.dto.StrategyRequest
import com.example.caprichoapp.data.remote.dto.StrategyResponseDto
import com.example.caprichoapp.data.remote.dto.toDomain
import com.example.caprichoapp.domain.model.StrategyResponse
import com.example.caprichoapp.domain.repository.StrategyRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class SupabaseStrategyRepository(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
) : StrategyRepository {

    override suspend fun getSavingsStrategy(goalId: String): Result<StrategyResponse> = runCatchingCancellable {
        val userId = requireUserId()
        val accessToken = supabase.auth.currentAccessTokenOrNull()
            ?: throw IllegalStateException("No hay una sesión activa")

        val hasExpenses = hasStoredExpenses().getOrElse { false }
        if (!hasExpenses) {
            throw IllegalStateException("Necesitás al menos un gasto guardado para pedir una estrategia.")
        }

        val profile = supabase.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<ProfileDto>()

        val goal = supabase.from("goals")
            .select { filter { eq("id", goalId); eq("user_id", userId) } }
            .decodeSingle<GoalDto>()

        val categories = supabase.from("categories")
            .select()
            .decodeList<CategoryDto>()
            .associateBy { it.id }

        val expenses = supabase.from("expenses")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ExpenseDto>()

        val request = StrategyRequest(
            monthlySalary = profile.monthlySalary,
            targetGoal = GoalPayloadDto(
                title = goal.title,
                targetAmount = goal.targetAmount,
                savedAmount = goal.savedAmount,
                installments = goal.installments,
                durability = goal.durability,
            ),
            expenses = expenses.map { expense ->
                ExpensePayloadDto(
                    title = expense.title,
                    amount = expense.amount,
                    kind = expense.kind,
                    categoryName = expense.categoryId?.let { categories[it]?.name },
                )
            },
        )

        val baseUrl = BuildKonfig.SUPABASE_URL
            .trim()
            .removeSuffix("/")
            .removeSuffix("/rest/v1")

        val response = httpClient.post("${baseUrl}/functions/v1/$FUNCTION_NAME") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            header("apikey", BuildKonfig.SUPABASE_PUBLISHABLE_KEY)
            setBody(request)
        }

        // La Edge Function responde { "error": "..." } con status 4xx/5xx cuando algo falla
        // (secret faltante, cuota de Gemini agotada, etc.). Ktor no lanza en esos casos, y ese
        // cuerpo se intentaba parsear como estrategia: de ahí el error de serialización.
        // El detalle queda en el mensaje de la excepción (para el log); la UI muestra uno amable.
        if (!response.status.isSuccess()) {
            throw IllegalStateException(
                "$FUNCTION_NAME respondió ${response.status.value}: ${response.bodyAsText().take(300)}",
            )
        }

        val strategy = response.body<StrategyResponseDto>().toDomain()
        if (strategy.summary.isBlank()) {
            throw IllegalStateException("$FUNCTION_NAME devolvió una estrategia sin resumen")
        }
        strategy
    }

    override suspend fun hasStoredExpenses(): Result<Boolean> = runCatchingCancellable {
        val userId = requireUserId()
        supabase.from("expenses")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ExpenseDto>()
            .isNotEmpty()
    }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No hay una sesión activa")

    private companion object {
        // Nombre de la Edge Function tal como figura en Supabase → Edge Functions.
        // Las funciones creadas desde el editor del dashboard reciben un nombre aleatorio
        // y no se pueden renombrar.
        const val FUNCTION_NAME = "clever-processor"
    }
}
