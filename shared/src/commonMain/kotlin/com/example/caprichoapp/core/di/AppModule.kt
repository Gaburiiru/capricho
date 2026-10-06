package com.example.caprichoapp.core.di

import com.example.caprichoapp.BuildKonfig
import com.example.caprichoapp.data.repository.SupabaseAuthRepository
import com.example.caprichoapp.domain.repository.AuthRepository
import com.example.caprichoapp.feature.auth.LoginViewModel
import com.example.caprichoapp.feature.auth.SessionViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SettingsCodeVerifierCache
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
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

    // Repositorios (el dominio solo conoce la interfaz)
    singleOf(::SupabaseAuthRepository) bind AuthRepository::class

    // ViewModels
    viewModelOf(::SessionViewModel)
    viewModelOf(::LoginViewModel)
}
