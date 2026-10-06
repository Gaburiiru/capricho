package com.example.caprichoapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.caprichoapp.domain.repository.AuthState
import com.example.caprichoapp.feature.auth.LoginScreen
import com.example.caprichoapp.feature.auth.SessionViewModel
import com.example.caprichoapp.feature.home.HomeScreen
import com.example.caprichoapp.feature.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()

    // Reacciona a cambios de sesión una vez pasada la Splash
    LaunchedEffect(authState) {
        val dest = navController.currentDestination
        when {
            authState is AuthState.SignedIn && dest?.hasRoute<LoginRoute>() == true ->
                navController.navigate(HomeRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                    launchSingleTop = true
                }
            authState is AuthState.SignedOut && dest?.hasRoute<HomeRoute>() == true ->
                navController.navigate(LoginRoute) {
                    popUpTo(HomeRoute) { inclusive = true }
                    launchSingleTop = true
                }
        }
    }

    NavHost(navController = navController, startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashScreen(authState = authState) { state ->
                val target: Any = if (state is AuthState.SignedIn) HomeRoute else LoginRoute
                navController.navigate(target) {
                    popUpTo(SplashRoute) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        composable<LoginRoute> { LoginScreen() }
        composable<HomeRoute> {
            HomeScreen(onSignOut = sessionViewModel::signOut)
        }
    }
}
