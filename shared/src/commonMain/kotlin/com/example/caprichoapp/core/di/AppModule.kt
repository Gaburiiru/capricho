package com.example.caprichoapp.core.di

import com.example.caprichoapp.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SettingsCodeVerifierCache
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
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
}
