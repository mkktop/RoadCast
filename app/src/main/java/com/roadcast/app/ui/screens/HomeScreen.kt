package com.roadcast.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.roadcast.app.data.Delivery
import com.roadcast.app.data.DeliveryStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pendingCount: Int,
    deliveringCount: Int,
    todayDeliveries: List<Delivery>,
    onAddClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onMarkDelivering: (Long) -> Unit,
    onMarkCompleted: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("路信") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "添加配送")
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Stats cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "待配送",
                        count = pendingCount,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "配送中",
                        count = deliveringCount,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Today's deliveries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "今日配送",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onViewAllClick) {
                        Text("查看全部")
                    }
                }
            }

            if (todayDeliveries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "暂无配送任务",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(todayDeliveries) { delivery ->
                    DeliveryCard(
                        delivery = delivery,
                        onMarkDelivering = { onMarkDelivering(delivery.id) },
                        onMarkCompleted = { onMarkCompleted(delivery.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun DeliveryCard(
    delivery: Delivery,
    onMarkDelivering: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val statusColor = when (delivery.status) {
        DeliveryStatus.PENDING -> MaterialTheme.colorScheme.primary
        DeliveryStatus.DELIVERING -> MaterialTheme.colorScheme.secondary
        DeliveryStatus.COMPLETED -> Green500
    }

    val statusText = when (delivery.status) {
        DeliveryStatus.PENDING -> "待配送"
        DeliveryStatus.DELIVERING -> "配送中"
        DeliveryStatus.COMPLETED -> "已完成"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    timeFormat.format(Date(delivery.deliveryTime)),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    delivery.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    delivery.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            // Action button
            when (delivery.status) {
                DeliveryStatus.PENDING -> {
                    IconButton(onClick = onMarkDelivering) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "开始配送",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                DeliveryStatus.DELIVERING -> {
                    IconButton(onClick = onMarkCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "完成配送",
                            tint = Green500
                        )
                    }
                }
                DeliveryStatus.COMPLETED -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green500
                    )
                }
            }
        }
    }
}

private val Green500 = androidx.compose.ui.graphics.Color(0xFF4CAF50)
