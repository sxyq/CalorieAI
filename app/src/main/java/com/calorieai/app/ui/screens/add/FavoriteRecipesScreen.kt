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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFavoritesManager: () -> Unit = {},
    onNavigateToPantryManager: () -> Unit = {},
    onNavigateToMealPlanManager: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: RecipeHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text("鑿滆氨涓績") },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "杩斿洖")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        RecipeScreenContainer(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 28.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HomeOverviewSection(
                        favoriteCount = uiState.favorites.size,
                        pantryCount = uiState.pantryIngredients.size,
                        planCount = uiState.recipePlans.size
                    )
                }

                item {
                    QuickFavoriteSection(
                        selectedMealType = uiState.selectedMealType,
                        favorites = uiState.quickFavorites,
                        onMealTypeChange = { mealType ->
                            viewModel.dispatch(RecipeAction.Home.ChangeSelectedMealType(mealType))
                        },
                        onAdd = { recipe ->
                            viewModel.dispatch(
                                RecipeAction.Home.AddFavoriteToToday(
                                    recipe = recipe,
                                    mealType = uiState.selectedMealType
                                )
                            )
                        },
                        onManageFavorites = onNavigateToFavoritesManager
                    )
                }

                item {
                    AiSection(
                        isLoading = uiState.isAiLoading,
                        result = uiState.aiResult,
                        error = uiState.aiError,
                        onGenerateSuggestion = {
                            viewModel.dispatch(RecipeAction.Home.GenerateSuggestion)
                        },
                        onGeneratePlan = {
                            viewModel.dispatch(RecipeAction.Home.GeneratePlan(days = 3))
                        },
                        onClear = {
                            viewModel.dispatch(RecipeAction.Home.ClearAiResult)
                        }
                    )
                }

                item {
                    PersonalizationSection(
                        personalization = uiState.personalization,
                        onDietaryAllergensChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeDietaryAllergens(value))
                        },
                        onFlavorPreferencesChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeFlavorPreferences(value))
                        },
                        onBudgetPreferenceChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeBudgetPreference(value))
                        },
                        onMaxCookingMinutesChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeMaxCookingMinutes(value))
                        },
                        onSpecialPopulationModeChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeSpecialPopulationMode(value))
                        },
                        onWeeklyRecordGoalDaysChange = { value ->
                            viewModel.dispatch(RecipeAction.Home.ChangeWeeklyRecordGoalDays(value))
                        },
                        onSave = {
                            viewModel.dispatch(RecipeAction.Home.SavePersonalization)
                        }
                    )
                }

                item {
                    PantryAssistSection(
                        pantryItems = uiState.pantryIngredients,
                        onManagePantry = onNavigateToPantryManager
                    )
                }

                item {
                    MealPlanAssistSection(
                        plans = uiState.recipePlans,
                        onManagePlans = onNavigateToMealPlanManager
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onNavigateToPantryManager,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("椋熸潗绠＄悊")
                        }
                        Button(
                            onClick = onNavigateToMealPlanManager,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("鑿滃崟璁″垝")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeOverviewSection(
    favoriteCount: Int,
    pantryCount: Int,
    planCount: Int
) {
    RecipePanel(
        title = "鏀惰棌澶嶇敤涓绘帶鍙",
        subtitle = "鏀惰棌 -> 涓€閿姞鍏ヤ粖鏃?-> 搴撳瓨涓嶢I鍗忓悓"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecipeMetricBadge("鏀惰棌", favoriteCount.toString(), Modifier.weight(1f))
            RecipeMetricBadge("椋熸潗", pantryCount.toString(), Modifier.weight(1f))
            RecipeMetricBadge("鑿滃崟", planCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickFavoriteSection(
    selectedMealType: MealType,
    favorites: List<FavoriteRecipe>,
    onMealTypeChange: (MealType) -> Unit,
    onAdd: (FavoriteRecipe) -> Unit,
    onManageFavorites: () -> Unit
) {
    val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    RecipePanel(
        title = "蹇嵎澶嶇敤",
        subtitle = "閫夋嫨椁愭鍚庝竴閿姞鍏ヤ粖鏃ヨ褰",
        actionText = "绠＄悊鏀惰棌",
        onAction = onManageFavorites
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            mealTypes.forEach { type ->
                FilterChip(
                    selected = type == selectedMealType,
                    onClick = { onMealTypeChange(type) },
                    label = { Text(mealTypeLabel(type)) }
                )
            }
        }

        if (favorites.isEmpty()) {
            Text(
                "鏆傛棤鏀惰棌鑿滆氨锛屽厛鍦ㄧ粨鏋滈〉鎴栨敹钘忓簱娣诲姞銆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            favorites.take(4).forEach { recipe ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f)
                    ),
                    shape = CardDefaults.shape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.foodName, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${recipe.totalCalories} kcal 路 浣跨敤 ${recipe.useCount} 娆",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onAdd(recipe) }) { Text("鍔犲叆浠婃棩") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiSection(
    isLoading: Boolean,
    result: String?,
    error: String?,
    onGenerateSuggestion: () -> Unit,
    onGeneratePlan: () -> Unit,
    onClear: () -> Unit
) {
    RecipePanel(
        title = "AI 鑿滆氨鍗忓悓",
        subtitle = "鐢熸垚鎺ㄨ崘鎴栫洿鎺ョ敓鎴?澶╄彍鍗"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onGenerateSuggestion,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("鐢熸垚鎺ㄨ崘")
            }
            Button(
                onClick = onGeneratePlan,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("鐢熸垚3澶")
            }
        }

        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (!error.isNullOrBlank()) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!result.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    MarkdownText(text = result, config = MarkdownConfig.ChatReadable)
                    TextButton(onClick = onClear, modifier = Modifier.align(Alignment.End)) {
                        Text("娓呯┖缁撴灉")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalizationSection(
    personalization: RecipePersonalizationState,
    onDietaryAllergensChange: (String) -> Unit,
    onFlavorPreferencesChange: (String) -> Unit,
    onBudgetPreferenceChange: (String) -> Unit,
    onMaxCookingMinutesChange: (String) -> Unit,
    onSpecialPopulationModeChange: (String) -> Unit,
    onWeeklyRecordGoalDaysChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val modeOptions = listOf(
        "GENERAL" to "通用健康",
        "DIABETES" to "控糖",
        "GOUT" to "痛风",
        "PREGNANCY" to "孕期",
        "CHILD" to "儿童",
        "FITNESS" to "健身"
    )
    val allergenOptions = listOf("乳制品", "花生", "坚果", "海鲜", "鸡蛋", "麸质")
    val flavorOptions = listOf("清淡", "家常", "辛辣", "低脂", "高蛋白", "素食")
    val budgetOptions = listOf("经济", "均衡", "高品质")
    val cookMinutesOptions = listOf("20", "30", "45", "60")
    val weeklyGoalOptions = listOf("3", "4", "5", "6", "7")

    var modeExpanded by remember { mutableStateOf(false) }
    val selectedLabel = modeOptions.firstOrNull {
        it.first == personalization.specialPopulationMode
    }?.second ?: "通用健康"

    val selectedAllergens = parseTagSet(personalization.dietaryAllergens)
    val selectedFlavors = parseTagSet(personalization.flavorPreferences)

    RecipePanel(
        title = "个性化偏好",
        subtitle = "结构化偏好会直接影响 AI 推荐结果，减少无效自由输入"
    ) {
        Text("忌口与过敏", style = MaterialTheme.typography.labelLarge)
        ChoiceChipRows(
            options = allergenOptions,
            selectedOptions = selectedAllergens,
            onToggle = { option ->
                val next = if (option in selectedAllergens) {
                    selectedAllergens - option
                } else {
                    selectedAllergens + option
                }
                onDietaryAllergensChange(encodeTagSet(next))
            }
        )

        Text("口味偏好", style = MaterialTheme.typography.labelLarge)
        ChoiceChipRows(
            options = flavorOptions,
            selectedOptions = selectedFlavors,
            onToggle = { option ->
                val next = if (option in selectedFlavors) {
                    selectedFlavors - option
                } else {
                    selectedFlavors + option
                }
                onFlavorPreferencesChange(encodeTagSet(next))
            }
        )

        Text("预算策略", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            budgetOptions.forEach { option ->
                FilterChip(
                    selected = personalization.budgetPreference == option,
                    onClick = { onBudgetPreferenceChange(option) },
                    label = { Text(option) }
                )
            }
        }

        Text("烹饪时长上限（分钟）", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cookMinutesOptions.forEach { option ->
                FilterChip(
                    selected = personalization.maxCookingMinutes == option,
                    onClick = { onMaxCookingMinutesChange(option) },
                    label = { Text(option) }
                )
            }
        }

        OutlinedTextField(
            value = personalization.maxCookingMinutes,
            onValueChange = onMaxCookingMinutesChange,
            label = { Text("自定义时长（分钟）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("每周执行天数", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            weeklyGoalOptions.forEach { option ->
                FilterChip(
                    selected = personalization.weeklyRecordGoalDays == option,
                    onClick = { onWeeklyRecordGoalDaysChange(option) },
                    label = { Text(option) }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = modeExpanded,
            onExpandedChange = { modeExpanded = !modeExpanded }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("人群模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("当前约束摘要", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = buildString {
                        append("忌口: ")
                        append(if (selectedAllergens.isEmpty()) "未设置" else selectedAllergens.joinToString("、"))
                        append("\n口味: ")
                        append(if (selectedFlavors.isEmpty()) "未设置" else selectedFlavors.joinToString("、"))
                        append("\n预算: ")
                        append(personalization.budgetPreference.ifBlank { "未设置" })
                        append("\n时长上限: ")
                        append(personalization.maxCookingMinutes.ifBlank { "未设置" })
                        append(" 分钟")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("保存偏好")
        }
    }
}

@Composable
private fun PantryAssistSection(
    pantryItems: List<PantryIngredient>,
    onManagePantry: () -> Unit
) {
    val now = System.currentTimeMillis()
    val expiringItems = pantryItems
        .filter { it.expiresAt != null }
        .sortedBy { it.expiresAt ?: Long.MAX_VALUE }
        .take(3)

    val expiringCount = pantryItems.count { item ->
        val expiresAt = item.expiresAt ?: return@count false
        val days = ((expiresAt - now) / (24f * 60f * 60f * 1000f)).toInt()
        days <= 3
    }

    RecipePanel(
        title = "食材管理",
        subtitle = "临期预警 + 处理建议，形成可执行闭环",
        actionText = "管理食材",
        onAction = onManagePantry
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecipeMetricBadge("库存总数", pantryItems.size.toString(), Modifier.weight(1f))
            RecipeMetricBadge("3天内临期", expiringCount.toString(), Modifier.weight(1f))
        }

        if (expiringItems.isEmpty()) {
            Text(
                "当前没有临期食材，建议每周至少维护一次库存。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "优先处理建议",
                style = MaterialTheme.typography.labelLarge
            )
            expiringItems.forEach { item ->
                val days = (((item.expiresAt ?: now) - now) / (24f * 60f * 60f * 1000f)).toInt()
                val tag = when {
                    days < 0 -> "已过期"
                    days == 0 -> "今天到期"
                    else -> "${days}天内处理"
                }
                Text(
                    text = "• ${item.name} ${item.quantity}${item.unit} · $tag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseTagSet(raw: String): Set<String> {
    return raw
        .split(',', '，', ';', '；', '、')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
}

private fun encodeTagSet(values: Set<String>): String {
    return values.joinToString(separator = ",")
}

@Composable
private fun ChoiceChipRows(
    options: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    FilterChip(
                        selected = option in selectedOptions,
                        onClick = { onToggle(option) },
                        label = { Text(option) }
                    )
                }
            }
        }
    }
}
@Composable
private fun MealPlanAssistSection(
    plans: List<RecipePlan>,
    onManagePlans: () -> Unit
) {
    val latestPlan = plans.maxByOrNull { it.updatedAt }
    val formatter = remember { DateTimeFormatter.ofPattern("MM-dd") }

    RecipePanel(
        title = "鑿滃崟杈呭姪",
        subtitle = "鑿滃崟鍙洖鏄句笌澶嶇敤",
        actionText = "绠＄悊鑿滃崟",
        onAction = onManagePlans
    ) {
        if (latestPlan == null) {
            Text(
                "鏆傛棤鑿滃崟璁″垝锛屽彲鍦ㄦ椤电洿鎺ョ敓鎴愩€",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val start = LocalDate.ofEpochDay(latestPlan.startDateEpochDay).format(formatter)
            val end = LocalDate.ofEpochDay(latestPlan.endDateEpochDay).format(formatter)
            Text(
                "鏈€杩戣鍒掞細${latestPlan.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "瑕嗙洊鏃ユ湡锛?start - $end",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> "早餐"
    MealType.LUNCH -> "午餐"
    MealType.DINNER -> "晚餐"
    MealType.SNACK -> "加餐"
    MealType.BREAKFAST_SNACK -> "早加餐"
    MealType.LUNCH_SNACK -> "午加餐"
    MealType.DINNER_SNACK -> "晚加餐"
}


