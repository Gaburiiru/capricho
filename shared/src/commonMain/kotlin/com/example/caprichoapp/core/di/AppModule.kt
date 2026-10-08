package com.example.caprichoapp.core.di

import com.example.caprichoapp.BuildKonfig
import com.example.caprichoapp.data.repository.SupabaseAuthRepository
import com.example.caprichoapp.data.repository.SupabaseCategoryRepository
import com.example.caprichoapp.data.repository.SupabaseExpenseRepository
import com.example.caprichoapp.data.repository.SupabaseGoalRepository
import com.example.caprichoapp.data.repository.SupabaseProfileRepository
import com.example.caprichoapp.data.repository.SupabaseStrategyRepository
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.domain.repository.CategoryRepository
import com.example.caprichoapp.domain.repository.ExpenseRepository
import com.example.caprichoapp.domain.repository.GoalRepository
import com.example.caprichoapp.domain.repository.ProfileRepository
import com.example.caprichoapp.domain.repository.StrategyRepository
import com.example.caprichoapp.feature.auth.LoginViewModel
import com.example.caprichoapp.feature.auth.SessionViewModel
import com.example.caprichoapp.feature.goals.GoalsViewModel
import com.example.caprichoapp.feature.history.HistoryViewModel
import com.example.caprichoapp.feature.onboarding.OnboardingViewModel
import com.example.caprichoapp.feature.predict.PredictCaprichoViewModel
import com.example.caprichoapp.feature.strategy.StrategyViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SettingsCodeVerifierCache
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<SupabaseClient> {
        val baseUrl = BuildKonfig.SUPABASE_URL
            .trim()
            .removeSuffix("/")
            .removeSuffix("/rest/v1")
            .removeSuffix("/")

        createSupabaseClient(
            supabaseUrl = baseUrl,
            supabaseKey = BuildKonfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth) {
                sessionManager = try {
                    SettingsSessionManager()
                } catch (_: Exception) {
                    MemorySessionManager()
                }
                codeVerifierCache = try {
                    SettingsCodeVerifierCache()
                } catch (_: Exception) {
                    MemoryCodeVerifierCache()
                }
            }
            install(Postgrest)
        }
    }

    single {
        HttpClient {
            // Gemini puede tardar más que el timeout por defecto del engine (10 s en OkHttp)
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }
    }

    // Repositorios (el dominio solo conoce la interfaz)
    singleOf(::SupabaseAuthRepository) bind AuthRepository::class
    singleOf(::SupabaseProfileRepository) bind ProfileRepository::class
    singleOf(::SupabaseCategoryRepository) bind CategoryRepository::class
    singleOf(::SupabaseGoalRepository) bind GoalRepository::class
    singleOf(::SupabaseExpenseRepository) bind ExpenseRepository::class
    singleOf(::SupabaseStrategyRepository) bind StrategyRepository::class

    // ViewModels
    viewModelOf(::SessionViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::PredictCaprichoViewModel)
    viewModelOf(::GoalsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::StrategyViewModel)
}
