package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryIngredientsManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: PantryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RecipeUiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食材库存管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PantryOverviewCard(
                    totalCount = uiState.pantryIngredients.size,
                    expiringSoonCount = uiState.pantryIngredients.count { isExpiringSoon(it.expiresAt) }
                )
            }
            item {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("新增食材")
                }
            }
            if (uiState.pantryIngredients.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Text(
                            text = "暂无食材库存，可添加后用于菜谱推荐与菜单生成。",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                items(
                    items = uiState.pantryIngredients.sortedBy { it.expiresAt ?: Long.MAX_VALUE },
                    key = { it.id }
                ) { pantry ->
                    PantryIngredientItem(
                        itemName = pantry.name,
                        quantityText = "${pantry.quantity}${pantry.unit}",
                        expiryText = formatExpiryText(pantry.expiresAt),
                        notes = pantry.notes,
                        onDelete = {
                            viewModel.dispatch(RecipeAction.Pantry.DeleteIngredient(pantry))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPantryIngredientDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, quantity, unit, daysToExpire, notes ->
                viewModel.dispatch(
                    RecipeAction.Pantry.AddIngredient(
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        daysToExpire = daysToExpire,
                        notes = notes
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PantryOverviewCard(
    totalCount: Int,
    expiringSoonCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OverviewPill("食材总数", totalCount.toString(), Modifier.weight(1f))
            OverviewPill("3天内到期", expiringSoonCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PantryIngredientItem(
    itemName: String,
    quantityText: String,
    expiryText: String,
    notes: String?,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(itemName, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "$quantityText · $expiryText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!notes.isNullOrBlank()) {
                    Text(
                        "备注：$notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@Composable
private fun AddPantryIngredientDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: Float, unit: String, daysToExpire: Int?, notes: String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var unit by rememberSaveable { mutableStateOf("份") }
    var daysToExpire by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加食材") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("数量") }
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("单位") }
                )
                OutlinedTextField(
                    value = daysToExpire,
                    onValueChange = { daysToExpire = it.filter { ch -> ch.isDigit() } },
                    label = { Text("几天后过期") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注（可选）") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        name.trim(),
                        quantity.toFloatOrNull() ?: 1f,
                        unit.trim().ifBlank { "份" },
                        daysToExpire.toIntOrNull(),
                        notes.trim().ifBlank { null }
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatExpiryText(expiresAt: Long?): String {
    if (expiresAt == null) return "未设置保质提醒"
    val days = ((expiresAt - System.currentTimeMillis()) / (24f * 60f * 60f * 1000f)).toInt()
    return when {
        days > 0 -> "$days 天后过期"
        days == 0 -> "今天到期"
        else -> "已过期 ${kotlin.math.abs(days)} 天"
    }
}

private fun isExpiringSoon(expiresAt: Long?): Boolean {
    if (expiresAt == null) return false
    val now = System.currentTimeMillis()
    val threeDays = 3L * 24L * 60L * 60L * 1000L
    return expiresAt in now..(now + threeDays)
}
