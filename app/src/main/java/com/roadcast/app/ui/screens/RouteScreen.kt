package com.roadcast.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.roadcast.app.data.*
import com.roadcast.app.viewmodel.RouteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    viewModel: RouteViewModel,
    modifier: Modifier = Modifier
) {
    val stops by viewModel.todayStops.observeAsState(emptyList())
    val supermarkets by viewModel.allSupermarkets.observeAsState(emptyList())
    val areas by viewModel.allAreas.observeAsState(emptyList())

    val supermarketMap = supermarkets.associateBy { it.id }
    val areaMap = areas.associateBy { it.id }

    var showSupermarketPicker by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日行程") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (stops.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "清空行程")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSupermarketPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加站点")
            }
        },
        modifier = modifier
    ) { padding ->
        if (stops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "点击 + 添加配送站点",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        todayFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(12.dp))
                }

                itemsIndexed(
                    items = stops,
                    key = { _, stop -> stop.id }
                ) { visualIndex, stop ->
                    val supermarket = supermarketMap[stop.supermarketId]
                    val area = supermarket?.let { areaMap[it.areaId] }

                    RouteStopItem(
                        index = visualIndex + 1,
                        stop = stop,
                        supermarketName = supermarket?.name ?: "未知",
                        areaName = area?.name,
                        isFirst = visualIndex == 0,
                        isLast = visualIndex == stops.size - 1,
                        onMoveUp = { viewModel.moveUp(stop) },
                        onMoveDown = { viewModel.moveDown(stop) },
                        onDelete = { viewModel.removeStop(stop) },
                        onComplete = { viewModel.markCompleted(stop) },
                        onSkip = { viewModel.markSkipped(stop) }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Supermarket picker dialog
    if (showSupermarketPicker) {
        SupermarketPickerDialog(
            supermarkets = supermarkets,
            areas = areas,
            existingStopSupermarketIds = stops.map { it.supermarketId }.toSet(),
            onConfirm = { selectedIds ->
                viewModel.addStops(selectedIds)
                showSupermarketPicker = false
            },
            onDismiss = { showSupermarketPicker = false }
        )
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空行程") },
            text = { Text("确定要清空今日所有行程站点吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTodayRoute()
                        showClearDialog = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun RouteStopItem(
    index: Int,
    stop: RouteStop,
    supermarketName: String,
    areaName: String?,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val statusColor = when (stop.status) {
        StopStatus.PENDING -> MaterialTheme.colorScheme.primary
        StopStatus.COMPLETED -> Green500
        StopStatus.SKIPPED -> Orange500
    }

    val statusText = when (stop.status) {
        StopStatus.PENDING -> null
        StopStatus.COMPLETED -> "已完成"
        StopStatus.SKIPPED -> "已跳过"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order number
            Surface(
                color = statusColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "$index",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    supermarketName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (areaName != null) {
                    Text(
                        areaName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (statusText != null) {
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            statusText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }

            // Action buttons for PENDING items
            if (stop.status == StopStatus.PENDING) {
                IconButton(onClick = onComplete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "完成",
                        tint = Green500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Reorder buttons for PENDING items
            if (stop.status == StopStatus.PENDING) {
                Column {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "上移",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "下移",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SupermarketPickerDialog(
    supermarkets: List<Supermarket>,
    areas: List<DeliveryArea>,
    existingStopSupermarketIds: Set<Long>,
    onConfirm: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Long>() }
    val supermarketsByArea = supermarkets.groupBy { it.areaId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加配送站点") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (supermarkets.isEmpty()) {
                    item {
                        Text(
                            "暂无超市，请先在配置页面添加",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                areas.forEach { area ->
                    val areaMarkets = supermarketsByArea[area.id] ?: return@forEach
                    item {
                        Text(
                            area.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(areaMarkets.size) { index ->
                        val market = areaMarkets[index]
                        val isExisting = market.id in existingStopSupermarketIds
                        val isSelected = market.id in selectedIds

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isSelected || isExisting,
                                onCheckedChange = {
                                    if (!isExisting) {
                                        if (isSelected) selectedIds.remove(market.id)
                                        else selectedIds.add(market.id)
                                    }
                                },
                                enabled = !isExisting
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    market.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isExisting) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                                if (isExisting) {
                                    Text(
                                        "已在行程中",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("添加 (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private val Green500 = androidx.compose.ui.graphics.Color(0xFF4CAF50)
private val Orange500 = androidx.compose.ui.graphics.Color(0xFFFF9800)
