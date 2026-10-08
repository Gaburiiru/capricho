package com.example.caprichoapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object LoginRoute

@Serializable
data object OnboardingRoute

// ---- Pestañas de la barra inferior ----
@Serializable
data object HistoryRoute
@Serializable
data object GoalsRoute
@Serializable
data object HomeRoute
@Serializable
data object ProfileRoute

// ---- Flujo de Predicción ----
@Serializable
data object PredictCaprichoRoute

// ---- Estrategia de ahorro ----
@Serializable
data class StrategyRoute(val goalId: String)
