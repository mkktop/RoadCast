package com.roadcast.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Route : Screen("route")
    data object Config : Screen("config")
}
