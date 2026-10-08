package com.example.caprichoapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caprichoapp.core.designsystem.pixel.PixelBottomBar
import com.example.caprichoapp.feature.auth.LoginScreen
import com.example.caprichoapp.feature.auth.SessionState
import com.example.caprichoapp.feature.auth.SessionViewModel
import com.example.caprichoapp.feature.goals.GoalsScreen
import com.example.caprichoapp.feature.history.HistoryScreen
import com.example.caprichoapp.feature.home.HomeScreen
import com.example.caprichoapp.feature.onboarding.OnboardingScreen
import com.example.caprichoapp.feature.predict.PredictCaprichoScreen
import com.example.caprichoapp.feature.profile.ProfileScreen
import com.example.caprichoapp.feature.splash.SplashScreen
import com.example.caprichoapp.feature.strategy.StrategyScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = MainTab.fromDestination(backStackEntry?.destination)
    val barItems = remember { MainTab.entries.map { it.toBarItem() } }

    // Reacciona a cambios de sesión una vez pasada la Splash.
    // Regla: sin sesión -> Login; con sesión y sin perfil -> Onboarding; con perfil -> Home.
    LaunchedEffect(sessionState) {
        val dest = navController.currentDestination
        val onLogin = dest?.hasRoute<LoginRoute>() == true
        val onOnboarding = dest?.hasRoute<OnboardingRoute>() == true
        val onTab = MainTab.fromDestination(dest) != null

        when (sessionState) {
            is SessionState.Ready -> when {
                onLogin -> navController.replace(from = LoginRoute, to = HomeRoute)
                onOnboarding -> navController.replace(from = OnboardingRoute, to = HomeRoute)
                else -> Unit
            }

            is SessionState.NeedsOnboarding -> when {
                onLogin -> navController.replace(from = LoginRoute, to = OnboardingRoute)
                else -> Unit
            }

            is SessionState.SignedOut -> when {
                // Home es la raíz de las pestañas: sacarla limpia todo el stack
                onTab -> navController.replace(from = HomeRoute, to = LoginRoute)
                onOnboarding -> navController.replace(from = OnboardingRoute, to = LoginRoute)
                else -> Unit
            }

            else -> Unit // Loading / ProfileError: no se navega
        }
    }

    val profile = (sessionState as? SessionState.Ready)?.profile

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = SplashRoute) {
                composable<SplashRoute> {
                    SplashScreen(
                        sessionState = sessionState,
                        onRetry = sessionViewModel::retry,
                    ) { state ->
                        val target: Any = when (state) {
                            is SessionState.Ready -> HomeRoute
                            is SessionState.NeedsOnboarding -> OnboardingRoute
                            else -> LoginRoute
                        }
                        navController.replace(from = SplashRoute, to = target)
                    }
                }
                composable<LoginRoute> { LoginScreen() }
                composable<OnboardingRoute> {
                    OnboardingScreen(onSaved = sessionViewModel::onProfileSaved)
                }
                composable<HomeRoute> {
                    HomeScreen(
                        greetingName = profile?.shownName,
                        onStartCapricho = { navController.navigate(PredictCaprichoRoute) },
                    )
                }
                composable<PredictCaprichoRoute> {
                    PredictCaprichoScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<HistoryRoute> { HistoryScreen() }
                composable<GoalsRoute> {
                    GoalsScreen(
                        onOpenStrategy = { goalId -> navController.navigate(StrategyRoute(goalId)) },
                    )
                }
                composable<StrategyRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<StrategyRoute>()
                    StrategyScreen(
                        goalId = route.goalId,
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<ProfileRoute> {
                    ProfileScreen(
                        profile = profile,
                        onProfileSaved = sessionViewModel::onProfileSaved,
                        onSignOut = sessionViewModel::signOut,
                    )
                }
            }
        }

        // La barra solo aparece en las pantallas principales
        if (currentTab != null) {
            PixelBottomBar(
                items = barItems,
                selectedIndex = currentTab.ordinal,
                onSelect = { index -> navController.switchTab(MainTab.entries[index]) },
            )
        }
    }
}

/** Navega a [to] sacando [from] del back stack, sin duplicar la pantalla destino. */
private fun NavHostController.replace(from: Any, to: Any) {
    navigate(to) {
        popUpTo(from) { inclusive = true }
        launchSingleTop = true
    }
}

/** Cambio de pestaña: conserva el estado de cada una y no apila copias. */
private fun NavHostController.switchTab(tab: MainTab) {
    navigate(tab.route) {
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
