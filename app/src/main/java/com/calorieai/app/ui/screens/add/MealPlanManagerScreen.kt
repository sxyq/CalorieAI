package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPlanDialog by rememberSaveable { mutableStateOf(false) }

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
                title = { Text("菜单计划管理") },
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
                        title = "计划操作",
                        subtitle = "可手动维护，也可AI一键生成"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showPlanDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("手动新增")
                            }
                            Button(
                                onClick = {
                                    viewModel.dispatch(
                                        RecipeAction.MealPlan.GenerateByAi(
                                            days = 3,
                                            startDate = LocalDate.now()
                                        )
                                    )
                                },
                                enabled = !uiState.isGenerating,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null)
                                Text("AI 生成3天")
                            }
                        }
                        if (uiState.isGenerating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                if (!uiState.aiError.isNullOrBlank()) {
                    item {
                        RecipePanel(title = "AI 生成失败") {
                            Text(
                                uiState.aiError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (!uiState.aiResult.isNullOrBlank()) {
                    item {
                        RecipePanel(
                            title = "最近一次 AI 结果",
                            actionText = "清空结果",
                            onAction = { viewModel.dispatch(RecipeAction.MealPlan.ClearAiResult) }
                        ) {
                            MarkdownText(
                                text = uiState.aiResult.orEmpty(),
                                config = MarkdownConfig.Compact
                            )
                        }
                    }
                }

                if (uiState.plans.isEmpty()) {
                    item {
                        RecipePanel(
                            title = "暂无菜单计划",
                            subtitle = "先新增一个计划，后续可反复复用"
                        ) {
                            Text(
                                "你可以手动录入，也可以使用AI快速生成。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.plans, key = { it.id }) { plan ->
                        RecipePlanItem(
                            plan = plan,
                            onDelete = {
                                viewModel.dispatch(RecipeAction.MealPlan.DeletePlan(plan))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPlanDialog) {
        ManualPlanDialog(
            onDismiss = { showPlanDialog = false },
            onSave = { title, days, menu ->
                viewModel.dispatch(
                    RecipeAction.MealPlan.SaveManualPlan(
                        title = title,
                        startDate = LocalDate.now(),
                        days = days,
                        menuText = menu
                    )
                )
                showPlanDialog = false
            }
        )
    }
}

@Composable
private fun RecipePlanItem(plan: RecipePlan, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    RecipePanel(
        title = plan.title,
        subtitle = "${LocalDate.ofEpochDay(plan.startDateEpochDay).format(formatter)} - ${
            LocalDate.ofEpochDay(plan.endDateEpochDay).format(formatter)
        }"
    ) {
        MarkdownText(plan.menuText, config = MarkdownConfig.Compact)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("删除")
            }
        }
    }
}

@Composable
private fun ManualPlanDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, days: Int, menu: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("本周菜单") }
    var days by rememberSaveable { mutableStateOf("3") }
    var menu by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增菜单计划") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter { ch -> ch.isDigit() } },
                    label = { Text("天数") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = menu,
                    onValueChange = { menu = it },
                    label = { Text("菜单内容") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, days.toIntOrNull() ?: 3, menu) },
                enabled = title.isNotBlank() && menu.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
