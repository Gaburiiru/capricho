package com.example.caprichoapp.core.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.caprichoapp.core.designsystem.pixel.PixelBarItem
import com.example.caprichoapp.core.designsystem.pixel.PixelIcon

enum class MainTab(val route: Any, val icon: PixelIcon, val label: String) {
    Home(HomeRoute, PixelIcon.Home, "Inicio"),
    History(HistoryRoute, PixelIcon.History, "Gastos"),
    Goals(GoalsRoute, PixelIcon.Goals, "Metas"),
    Profile(ProfileRoute, PixelIcon.Profile, "Perfil"), ;

    fun toBarItem() = PixelBarItem(icon = icon, label = label)

    companion object {
        fun fromDestination(destination: NavDestination?): MainTab? = when {
            destination == null -> null
            destination.hasRoute<HomeRoute>() -> Home
            destination.hasRoute<HistoryRoute>() -> History
            destination.hasRoute<GoalsRoute>() -> Goals
            destination.hasRoute<ProfileRoute>() -> Profile
            else -> null
        }
    }
}
