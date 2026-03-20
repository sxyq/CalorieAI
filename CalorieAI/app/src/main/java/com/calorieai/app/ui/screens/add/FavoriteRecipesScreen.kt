package com.calorieai.app.ui.screens.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFavoritesManager: () -> Unit = {},
    onNavigateToPantryManager: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: FavoriteRecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showPlanDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.saveMessage) {
        val message = uiState.saveMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        viewModel.clearSaveMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱功能") },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RecipeOverviewCard(
                    favoriteCount = uiState.favorites.size,
                    pantryCount = uiState.pantryIngredients.size,
                    planCount = uiState.recipePlans.size
                )
            }
            item {
                AICard(
                    isLoading = uiState.isAiLoading,
                    result = uiState.aiResult,
                    error = uiState.aiError,
                    onGenerate = viewModel::generateRecipeSuggestionByPantry,
                    onGeneratePlan = { viewModel.generatePlanByPantry(3) },
                    onClear = viewModel::clearAiResult
                )
            }
            item {
                AIPersonalizationRecipeCard(
                    uiState = uiState,
                    onDietaryAllergensChange = viewModel::onDietaryAllergensChange,
                    onFlavorPreferencesChange = viewModel::onFlavorPreferencesChange,
                    onBudgetPreferenceChange = viewModel::onBudgetPreferenceChange,
                    onMaxCookingMinutesChange = viewModel::onMaxCookingMinutesChange,
                    onSpecialPopulationModeChange = viewModel::onSpecialPopulationModeChange,
                    onWeeklyRecordGoalDaysChange = viewModel::onWeeklyRecordGoalDaysChange,
                    onSave = viewModel::savePersonalizationSettings
                )
            }
            item {
                SectionCard(
                    title = "设置食谱（一天或多天菜单）",
                    subtitle = "支持手动维护，也可让AI自动生成",
                    icon = Icons.Default.RestaurantMenu,
                    onAdd = { showPlanDialog = true }
                ) {
                    if (uiState.recipePlans.isEmpty()) {
                        Text("暂无菜单计划。", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.recipePlans.forEach { plan ->
                                RecipePlanItem(plan, onDelete = { viewModel.removeRecipePlan(plan) })
                            }
                        }
                    }
                }
            }
            item {
                SectionCard(
                    title = "已有食材（可选）",
                    subtitle = "食材管理已拆分为独立界面",
                    icon = Icons.Default.Inventory2
                ) {
                    Text(
                        text = "当前已有食材 ${uiState.pantryIngredients.size} 项，点击底部按钮进入详细管理。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                Button(
                    onClick = onNavigateToPantryManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("已有食材")
                }
            }
            item {
                SectionCard(
                    title = "收藏菜谱",
                    subtitle = "收藏菜谱已拆分为独立管理界面",
                    icon = Icons.Default.RestaurantMenu
                ) {
                    Text(
                        text = "当前收藏 ${uiState.favorites.size} 条，点击底部按钮进入管理。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            item {
                Button(
                    onClick = onNavigateToFavoritesManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("收藏菜谱")
                }
            }
        }
    }
    if (showPlanDialog) PlanDialog(
        onDismiss = { showPlanDialog = false },
        onSave = { title, days, menu ->
            viewModel.saveRecipePlan(title, LocalDate.now(), days, menu, generatedByAI = false)
            showPlanDialog = false
        }
    )
}

@Composable
private fun RecipeOverviewCard(
    favoriteCount: Int,
    pantryCount: Int,
    planCount: Int
) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("菜谱中心", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewStatPill("收藏", favoriteCount, Modifier.weight(1f))
                OverviewStatPill("食材", pantryCount, Modifier.weight(1f))
                OverviewStatPill("菜单", planCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OverviewStatPill(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onAdd: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                onAdd?.let {
                    IconButton(onClick = it) { Icon(Icons.Default.Add, contentDescription = "新增") }
                }
            }
            content()
        }
    }
}

@Composable
private fun AICard(isLoading: Boolean, result: String?, error: String?, onGenerate: () -> Unit, onGeneratePlan: () -> Unit, onClear: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
            }
        )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("菜谱AI", fontWeight = FontWeight.Bold)
            }
            Text(
                "可直接用已保存偏好+近期健康数据生成推荐；本地食材是可选项。",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.weight(1f)) { Text("推荐菜谱") }
                Button(onClick = onGeneratePlan, enabled = !isLoading, modifier = Modifier.weight(1f)) { Text("生成3天菜单") }
            }
            if (isLoading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        }
                    )
                ) {
                    MarkdownText(it, config = MarkdownConfig.ChatReadable, modifier = Modifier.padding(10.dp))
                }
                TextButton(onClick = onClear, modifier = Modifier.align(Alignment.End)) { Text("清空") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIPersonalizationRecipeCard(
    uiState: FavoriteRecipesUiState,
    onDietaryAllergensChange: (String) -> Unit,
    onFlavorPreferencesChange: (String) -> Unit,
    onBudgetPreferenceChange: (String) -> Unit,
    onMaxCookingMinutesChange: (String) -> Unit,
    onSpecialPopulationModeChange: (String) -> Unit,
    onWeeklyRecordGoalDaysChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var modeExpanded by remember { mutableStateOf(false) }
    val modeOptions = listOf(
        "GENERAL" to "通用健康",
        "DIABETES" to "控糖",
        "GOUT" to "痛风",
        "PREGNANCY" to "孕期",
        "CHILD" to "儿童",
        "FITNESS" to "健身"
    )
    val selectedModeLabel = modeOptions.firstOrNull { it.first == uiState.specialPopulationMode }?.second ?: "通用健康"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f)
            }
        )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("AI个性化忌口与偏好", fontWeight = FontWeight.Bold)
            }
            Text("这些设置会用于菜谱推荐、菜单生成与替代建议。", style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = uiState.dietaryAllergens,
                onValueChange = onDietaryAllergensChange,
                label = { Text("过敏原/忌口（逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.flavorPreferences,
                onValueChange = onFlavorPreferencesChange,
                label = { Text("口味偏好（逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.budgetPreference,
                onValueChange = onBudgetPreferenceChange,
                label = { Text("预算偏好（经济/均衡/高品质）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.maxCookingMinutes,
                    onValueChange = onMaxCookingMinutesChange,
                    label = { Text("烹饪时长上限(分钟)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.weeklyRecordGoalDays,
                    onValueChange = onWeeklyRecordGoalDaysChange,
                    label = { Text("每周记录目标(天)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = !modeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedModeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("特定人群模式") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    modeOptions.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onSpecialPopulationModeChange(mode)
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("保存个性化约束")
            }
        }
    }
}

@Composable
private fun RecipePlanItem(plan: RecipePlan, onDelete: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val formatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plan.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
            }
            Text("${LocalDate.ofEpochDay(plan.startDateEpochDay).format(formatter)}-${LocalDate.ofEpochDay(plan.endDateEpochDay).format(formatter)}", style = MaterialTheme.typography.bodySmall)
            MarkdownText(plan.menuText, config = MarkdownConfig.Compact)
        }
    }
}

@Composable
private fun PlanDialog(onDismiss: () -> Unit, onSave: (String, Int, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("本周菜单") }; var days by rememberSaveable { mutableStateOf("3") }; var menu by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("设置食谱计划") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("标题") }); OutlinedTextField(days, { days = it }, label = { Text("天数") })
            OutlinedTextField(menu, { menu = it }, label = { Text("菜单内容") })
        }
    }, confirmButton = { TextButton(onClick = { onSave(title, days.toIntOrNull() ?: 3, menu) }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
