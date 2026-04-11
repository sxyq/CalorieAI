package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.PantryIngredient
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

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
        RecipeScreenContainer(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RecipePanel(
                        title = "库存总览",
                        subtitle = "用于 AI 推荐和菜单联动"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RecipeMetricBadge(
                                label = "食材总数",
                                value = uiState.pantryIngredients.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            RecipeMetricBadge(
                                label = "3天内到期",
                                value = uiState.pantryIngredients.count { isExpiringSoon(it.expiresAt) }.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("新增食材")
                        }
                    }
                }

                if (uiState.pantryIngredients.isEmpty()) {
                    item {
                        RecipePanel(
                            title = "暂无库存",
                            subtitle = "添加后可用于临期提醒与AI推荐"
                        ) {
                            Text(
                                "现在可以先补录常备食材。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.pantryIngredients.sortedBy { it.expiresAt ?: Long.MAX_VALUE },
                        key = { it.id }
                    ) { pantry ->
                        PantryIngredientItem(
                            pantry = pantry,
                            onDelete = {
                                viewModel.dispatch(RecipeAction.Pantry.DeleteIngredient(pantry))
                            }
                        )
                    }
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
private fun PantryIngredientItem(
    pantry: PantryIngredient,
    onDelete: () -> Unit
) {
    RecipePanel(
        title = pantry.name,
        subtitle = "${pantry.quantity}${pantry.unit} · ${formatExpiryText(pantry.expiresAt)}"
    ) {
        if (!pantry.notes.isNullOrBlank()) {
            Text(
                "备注：${pantry.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除")
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
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = daysToExpire,
                    onValueChange = { daysToExpire = it.filter { ch -> ch.isDigit() } },
                    label = { Text("几天后过期") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth()
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
        else -> "已过期 ${abs(days)} 天"
    }
}

private fun isExpiringSoon(expiresAt: Long?): Boolean {
    if (expiresAt == null) return false
    val now = System.currentTimeMillis()
    val threeDays = 3L * 24L * 60L * 60L * 1000L
    return expiresAt in now..(now + threeDays)
}
