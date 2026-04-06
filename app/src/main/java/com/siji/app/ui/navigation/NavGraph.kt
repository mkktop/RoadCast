package com.siji.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object DeliveryList : Screen("delivery_list")
    data object AddDelivery : Screen("add_delivery")
    data object EditDelivery : Screen("edit_delivery/{id}") {
        fun createRoute(id: Long) = "edit_delivery/$id"
    }
}
