package com.roadcast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roadcast.app.ui.navigation.Screen
import com.roadcast.app.ui.screens.*
import com.roadcast.app.ui.theme.RoadCastTheme
import com.roadcast.app.viewmodel.DeliveryViewModel
import com.roadcast.app.viewmodel.DeliveryViewModelFactory
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadCastTheme {
                RoadCastApp()
            }
        }
    }
}

@Composable
fun RoadCastApp() {
    val navController = rememberNavController()
    val viewModel: DeliveryViewModel = viewModel(
        factory = DeliveryViewModelFactory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext
                as android.app.Application
        )
    )

    val allDeliveries by viewModel.allDeliveries.observeAsState(emptyList())
    val pendingDeliveries by viewModel.pendingDeliveries.observeAsState(emptyList())
    val deliveringList by viewModel.deliveringList.observeAsState(emptyList())

    // Filter today's deliveries
    val todayDeliveries = remember(allDeliveries) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrow = today + 24 * 60 * 60 * 1000
        allDeliveries.filter { it.deliveryTime in today until tomorrow }
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                pendingCount = pendingDeliveries.size,
                deliveringCount = deliveringList.size,
                todayDeliveries = todayDeliveries,
                onAddClick = { navController.navigate(Screen.AddDelivery.route) },
                onViewAllClick = { navController.navigate(Screen.DeliveryList.route) },
                onMarkDelivering = { viewModel.markAsDelivering(it) },
                onMarkCompleted = { viewModel.markAsCompleted(it) }
            )
        }

        composable(Screen.AddDelivery.route) {
            AddDeliveryScreen(
                onSave = { delivery ->
                    viewModel.insert(delivery)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeliveryList.route) {
            DeliveryListScreen(
                deliveries = allDeliveries,
                onBack = { navController.popBackStack() },
                onMarkDelivering = { viewModel.markAsDelivering(it) },
                onMarkCompleted = { viewModel.markAsCompleted(it) },
                onDelete = { viewModel.delete(it) }
            )
        }
    }
}
