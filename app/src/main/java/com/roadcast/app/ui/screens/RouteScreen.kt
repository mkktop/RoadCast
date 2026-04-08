package com.roadcast.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.Collator
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
    var editingStop by remember { mutableStateOf<RouteStop?>(null) }

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
            var localStops by remember { mutableStateOf(stops) }

            // 仅在站点增减时从数据库同步（不在排序时重置，避免拖拽中闪退）
            val stopIdSet = stops.map { it.id }.toSet()
            LaunchedEffect(stopIdSet) {
                localStops = stops
            }

            val lazyListState = rememberLazyListState()
            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                // from.index / to.index 是 LazyColumn 绝对索引，需要减去 header 偏移量
                val headerOffset = 1
                val fromIndex = from.index - headerOffset
                val toIndex = to.index - headerOffset
                if (fromIndex in localStops.indices && toIndex in localStops.indices) {
                    val reordered = localStops.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    localStops = reordered
                    viewModel.updateStopsOrder(reordered.mapIndexed { index, stop ->
                        stop.copy(orderIndex = index)
                    })
                }
            }

            LazyColumn(
                state = lazyListState,
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

                items(
                    count = localStops.size,
                    key = { index -> localStops[index].id }
                ) { index ->
                    val stop = localStops[index]
                    val supermarket = supermarketMap[stop.supermarketId]
                    val area = supermarket?.let { areaMap[it.areaId] }

                    ReorderableItem(reorderableState, key = stop.id) { isDragging ->
                        val dragHandleModifier = if (stop.status == StopStatus.PENDING) {
                            Modifier.draggableHandle()
                        } else {
                            Modifier
                        }
                        RouteStopItem(
                            index = index + 1,
                            stop = stop,
                            supermarketName = supermarket?.name ?: "未知",
                            areaName = area?.name,
                            isDragging = isDragging,
                            dragHandleModifier = dragHandleModifier,
                            onClick = { editingStop = stop },
                            onDelete = { viewModel.removeStop(stop) },
                            onComplete = { viewModel.markCompleted(stop) },
                            onSkip = { viewModel.markSkipped(stop) },
                            onRedeliver = { viewModel.markAsPending(stop) }
                        )
                    }
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
            onConfirm = { selectedIds ->
                viewModel.addStops(selectedIds)
                showSupermarketPicker = false
            },
            onDismiss = { showSupermarketPicker = false }
        )
    }

    // Delivery items editor dialog
    editingStop?.let { stop ->
        DeliveryItemsDialog(
            stop = stop,
            supermarketName = supermarketMap[stop.supermarketId]?.name ?: "未知",
            onConfirm = { items ->
                viewModel.updateDeliveryItems(stop, items)
                editingStop = null
            },
            onDismiss = { editingStop = null }
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
    isDragging: Boolean,
    dragHandleModifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onRedeliver: () -> Unit
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

    val elevation = if (isDragging) 8.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle (only for PENDING items)
            if (stop.status == StopStatus.PENDING) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "拖拽排序",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = dragHandleModifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

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
                // Show delivery items hint
                if (!stop.deliveryItems.isNullOrBlank()) {
                    val itemCount = stop.deliveryItems.lines().filter { it.isNotBlank() }.size
                    Text(
                        "送货清单 ($itemCount 项)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
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

            // Action buttons
            when (stop.status) {
                StopStatus.PENDING -> {
                    TextButton(onClick = onSkip, contentPadding = PaddingValues(0.dp)) {
                        Text("跳过", style = MaterialTheme.typography.labelMedium, color = Orange500)
                    }
                    IconButton(onClick = onComplete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "完成",
                            tint = Green500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                StopStatus.SKIPPED -> {
                    TextButton(onClick = onRedeliver, contentPadding = PaddingValues(0.dp)) {
                        Text("重新配送", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                StopStatus.COMPLETED -> { }
            }

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

// Two-step supermarket picker: Step 1 = select area, Step 2 = select supermarkets
@Composable
private fun SupermarketPickerDialog(
    supermarkets: List<Supermarket>,
    areas: List<DeliveryArea>,
    onConfirm: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val supermarketsByArea = supermarkets.groupBy { it.areaId }
    var selectedArea by remember { mutableStateOf<DeliveryArea?>(null) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    // Area selection step
    if (selectedArea == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择区域") },
            text = {
                if (areas.isEmpty()) {
                    Text(
                        "暂无区域，请先在配置页面添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(areas.size) { index ->
                            val area = areas[index]
                            val marketCount = supermarketsByArea[area.id]?.size ?: 0
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedArea = area },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            area.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (marketCount > 0) {
                                            Text(
                                                "$marketCount 个超市",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    } else {
        // Supermarket selection step
        val area = selectedArea!!
        val collator = Collator.getInstance(Locale.CHINESE)
        val areaMarkets = (supermarketsByArea[area.id] ?: emptyList())
            .sortedWith(
                compareByDescending<Supermarket> { it.isFavorite }
                    .thenBy { collator.getCollationKey(it.name) }
            )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(area.name)
                }
            },
            text = {
                if (areaMarkets.isEmpty()) {
                    Text(
                        "该区域暂无超市，请先在配置页面添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(areaMarkets.size) { idx ->
                            val market = areaMarkets[idx]
                            val isSelected = market.id in selectedIds

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedIds.remove(market.id)
                                        else selectedIds.add(market.id)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (isSelected) selectedIds.remove(market.id)
                                        else selectedIds.add(market.id)
                                    }
                                )
                                Text(
                                    market.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (market.isFavorite) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "已收藏",
                                        modifier = Modifier.size(16.dp),
                                        tint = StarYellow
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        selectedArea = null
                        selectedIds.clear()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("返回")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirm(selectedIds.toList()) },
                        enabled = selectedIds.isNotEmpty()
                    ) {
                        Text("添加 (${selectedIds.size})")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}

private val Green500 = androidx.compose.ui.graphics.Color(0xFF4CAF50)
private val Orange500 = androidx.compose.ui.graphics.Color(0xFFFF9800)
private val StarYellow = androidx.compose.ui.graphics.Color(0xFFFFC107)

@Composable
private fun DeliveryItemsDialog(
    stop: RouteStop,
    supermarketName: String,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf(stop.deliveryItems ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("送货清单")
                Text(
                    supermarketName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            OutlinedTextField(
                value = items,
                onValueChange = { items = it },
                label = { Text("每行输入一个送货品项") },
                placeholder = { Text("可乐 x5\n矿泉水 x10\n面包 x3") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(items.ifBlank { null })
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
