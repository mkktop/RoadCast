package com.siji.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siji.app.data.Delivery
import com.siji.app.data.DeliveryStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryListScreen(
    deliveries: List<Delivery>,
    onBack: () -> Unit,
    onMarkDelivering: (Long) -> Unit,
    onMarkCompleted: (Long) -> Unit,
    onDelete: (Delivery) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("全部", "待配送", "配送中", "已完成")

    val filteredDeliveries = when (selectedTab) {
        1 -> deliveries.filter { it.status == DeliveryStatus.PENDING }
        2 -> deliveries.filter { it.status == DeliveryStatus.DELIVERING }
        3 -> deliveries.filter { it.status == DeliveryStatus.COMPLETED }
        else -> deliveries
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("配送列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(filteredDeliveries) { delivery ->
                    DeliveryCard(
                        delivery = delivery,
                        onMarkDelivering = { onMarkDelivering(delivery.id) },
                        onMarkCompleted = { onMarkCompleted(delivery.id) }
                    )
                }
            }
        }
    }
}
