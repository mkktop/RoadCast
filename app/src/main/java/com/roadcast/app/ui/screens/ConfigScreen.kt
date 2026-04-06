package com.roadcast.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.roadcast.app.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    modifier: Modifier = Modifier
) {
    val areas by viewModel.allAreas.observeAsState(emptyList())
    val supermarkets by viewModel.allSupermarkets.observeAsState(emptyList())

    var showAddAreaDialog by remember { mutableStateOf(false) }
    var editingArea by remember { mutableStateOf<DeliveryArea?>(null) }
    var showDeleteAreaDialog by remember { mutableStateOf<DeliveryArea?>(null) }

    var addingSupermarketForArea by remember { mutableStateOf<Long?>(null) }
    var editingSupermarket by remember { mutableStateOf<Supermarket?>(null) }
    var showDeleteSupermarketDialog by remember { mutableStateOf<Supermarket?>(null) }

    val supermarketsByArea = supermarkets.groupBy { it.areaId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("配置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAreaDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加区域")
            }
        },
        modifier = modifier
    ) { padding ->
        if (areas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "点击 + 创建送货区域",
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                areas.forEach { area ->
                    val areaMarkets = supermarketsByArea[area.id] ?: emptyList()

                    // Area header
                    item(key = "area_${area.id}") {
                        AreaSectionHeader(
                            area = area,
                            supermarketCount = areaMarkets.size,
                            onEdit = { editingArea = area },
                            onDelete = { showDeleteAreaDialog = area }
                        )
                    }

                    // Supermarkets under this area
                    items(
                        items = areaMarkets,
                        key = { it.id }
                    ) { market ->
                        SupermarketRow(
                            supermarket = market,
                            onEdit = { editingSupermarket = market },
                            onDelete = { showDeleteSupermarketDialog = market }
                        )
                    }

                    // Add supermarket button
                    item(key = "add_market_${area.id}") {
                        TextButton(
                            onClick = { addingSupermarketForArea = area.id },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("添加超市")
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Add Area Dialog
    if (showAddAreaDialog) {
        AreaNameDialog(
            title = "添加区域",
            initialName = "",
            onConfirm = { name ->
                viewModel.addArea(name)
                showAddAreaDialog = false
            },
            onDismiss = { showAddAreaDialog = false }
        )
    }

    // Edit Area Dialog
    editingArea?.let { area ->
        AreaNameDialog(
            title = "编辑区域",
            initialName = area.name,
            onConfirm = { name ->
                viewModel.updateArea(area.copy(name = name))
                editingArea = null
            },
            onDismiss = { editingArea = null }
        )
    }

    // Delete Area Confirmation
    showDeleteAreaDialog?.let { area ->
        val marketCount = (supermarketsByArea[area.id]?.size ?: 0)
        AlertDialog(
            onDismissRequest = { showDeleteAreaDialog = null },
            title = { Text("删除区域") },
            text = {
                Text(
                    if (marketCount > 0)
                        "区域「${area.name}」下有 $marketCount 个超市，删除后将一并移除。确定删除？"
                    else
                        "确定删除区域「${area.name}」？"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArea(area)
                        showDeleteAreaDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAreaDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Add Supermarket Dialog
    addingSupermarketForArea?.let { areaId ->
        SupermarketEditDialog(
            title = "添加超市",
            initialSupermarket = null,
            onConfirm = { name, contactPerson, phone, address, remark ->
                viewModel.addSupermarket(name, areaId, contactPerson, phone, address, remark)
                addingSupermarketForArea = null
            },
            onDismiss = { addingSupermarketForArea = null }
        )
    }

    // Edit Supermarket Dialog
    editingSupermarket?.let { market ->
        SupermarketEditDialog(
            title = "编辑超市",
            initialSupermarket = market,
            onConfirm = { name, contactPerson, phone, address, remark ->
                viewModel.updateSupermarket(
                    market.copy(
                        name = name,
                        contactPerson = contactPerson,
                        phone = phone,
                        address = address,
                        remark = remark
                    )
                )
                editingSupermarket = null
            },
            onDismiss = { editingSupermarket = null }
        )
    }

    // Delete Supermarket Confirmation
    showDeleteSupermarketDialog?.let { market ->
        AlertDialog(
            onDismissRequest = { showDeleteSupermarketDialog = null },
            title = { Text("删除超市") },
            text = { Text("确定删除超市「${market.name}」？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSupermarket(market)
                        showDeleteSupermarketDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSupermarketDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AreaSectionHeader(
    area: DeliveryArea,
    supermarketCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    area.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$supermarketCount 个超市",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SupermarketRow(
    supermarket: Supermarket,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                supermarket.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val details = listOfNotNull(
                supermarket.contactPerson,
                supermarket.phone
            ).filter { it.isNotBlank() }
            if (details.isNotEmpty()) {
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (!supermarket.address.isNullOrBlank()) {
                Text(
                    supermarket.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "删除",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AreaNameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("区域名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
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

@Composable
private fun SupermarketEditDialog(
    title: String,
    initialSupermarket: Supermarket?,
    onConfirm: (name: String, contactPerson: String?, phone: String?, address: String?, remark: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialSupermarket?.name ?: "") }
    var contactPerson by remember { mutableStateOf(initialSupermarket?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(initialSupermarket?.phone ?: "") }
    var address by remember { mutableStateOf(initialSupermarket?.address ?: "") }
    var remark by remember { mutableStateOf(initialSupermarket?.remark ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("超市名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("联系人") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("电话") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            contactPerson.ifBlank { null },
                            phone.ifBlank { null },
                            address.ifBlank { null },
                            remark.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
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
