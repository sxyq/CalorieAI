package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.SettingsTopAppBar
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.utils.MetabolicConstants
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }

    val bmr = calculateBMR(
        gender = uiState.gender,
        weight = uiState.weight,
        height = uiState.height,
        age = uiState.age
    )
    val tdee = calculateTDEE(bmr, uiState.activityLevel)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopAppBar(
                title = "个人信息编辑",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveProfile()
                            onNavigateBack()
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileHeroCard(
                avatarUrl = uiState.avatarUrl,
                userName = uiState.userName,
                userId = uiState.userId,
                onAvatarClick = { showAvatarPicker = true },
                onUserNameChange = viewModel::updateUserName,
                onUserIdChange = viewModel::updateUserId
            )

            ProfileSectionCard(
                title = "身体数据",
                subtitle = "用于计算基础代谢和每日建议热量"
            ) {
                BodyDataSection(
                    gender = uiState.gender,
                    age = uiState.age,
                    height = uiState.height,
                    weight = uiState.weight,
                    onGenderChange = viewModel::updateGender,
                    onAgeChange = viewModel::updateAge,
                    onHeightChange = viewModel::updateHeight,
                    onWeightChange = viewModel::updateWeight,
                    showWeight = true
                )
            }

            ProfileSectionCard(
                title = "代谢估算",
                subtitle = "根据身体数据和活动水平动态计算"
            ) {
                MetabolismSection(
                    bmr = bmr,
                    tdee = tdee,
                    activityLevel = uiState.activityLevel,
                    onActivityLevelChange = viewModel::updateActivityLevel
                )
            }

            ProfileSectionCard(
                title = "每日目标",
                subtitle = "设置你每天希望摄入的热量"
            ) {
                CalorieGoalSection(
                    calorieGoal = uiState.calorieGoal,
                    tdee = tdee,
                    onCalorieGoalChange = viewModel::updateCalorieGoal
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("选择头像") },
            text = { Text("头像选择功能将在后续版本中添加") },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeroCard(
    avatarUrl: String?,
    userName: String,
    userId: String,
    onAvatarClick: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onUserIdChange: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                val displayText = userName.trim().take(1).uppercase()
                if (avatarUrl != null || displayText.isNotEmpty()) {
                    Text(
                        text = if (displayText.isNotEmpty()) displayText else "你",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑头像",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (userName.isBlank()) "设置你的昵称" else userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (userId.isBlank()) "ID 未设置" else "ID: $userId",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isDark) {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text("昵称") },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("专属 ID") },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                placeholder = { Text("例如：longcat_2026") },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(22.dp),
                tint = if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun BodyDataSection(
    gender: String,
    age: Int?,
    height: Float?,
    weight: Float?,
    onGenderChange: (String) -> Unit,
    onAgeChange: (Int?) -> Unit,
    onHeightChange: (Float?) -> Unit,
    onWeightChange: (Float?) -> Unit,
    showWeight: Boolean = true
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "性别",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("👨 男" to "MALE", "👩 女" to "FEMALE").forEach { (label, value) ->
                FilterChip(
                    selected = gender == value,
                    onClick = { onGenderChange(value) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = age?.toString() ?: "",
            onValueChange = { onAgeChange(it.toIntOrNull()) },
            label = { Text("年龄") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            suffix = { Text("岁") },
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = height?.toString() ?: "",
            onValueChange = { onHeightChange(it.toFloatOrNull()) },
            label = { Text("身高") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (showWeight) ImeAction.Next else ImeAction.Done
            ),
            suffix = { Text("cm") },
            shape = RoundedCornerShape(14.dp)
        )

        if (showWeight) {
            OutlinedTextField(
                value = weight?.toString() ?: "",
                onValueChange = { onWeightChange(it.toFloatOrNull()) },
                label = { Text("体重") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                suffix = { Text("kg") },
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

@Composable
private fun MetabolismSection(
    bmr: Int,
    tdee: Int,
    activityLevel: String,
    onActivityLevelChange: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val activityLevels = listOf(
        "SEDENTARY" to "久坐",
        "LIGHT" to "轻度",
        "MODERATE" to "中度",
        "ACTIVE" to "高度",
        "VERY_ACTIVE" to "极高"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "活动水平",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        activityLevels.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (value, label) ->
                    FilterChip(
                        selected = activityLevel == value,
                        onClick = { onActivityLevelChange(value) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetabolismCard(
                title = "基础代谢(BMR)",
                value = bmr,
                description = "静息消耗",
                modifier = Modifier.weight(1f)
            )
            MetabolismCard(
                title = "每日总消耗(TDEE)",
                value = tdee,
                description = "随活动水平变化",
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "说明：基础代谢(BMR)由性别/年龄/身高/体重决定，不会因活动水平变化；活动水平影响的是 TDEE。",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun MetabolismCard(
    title: String,
    value: Int,
    description: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDark) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$value kcal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun CalorieGoalSection(
    calorieGoal: Int,
    tdee: Int,
    onCalorieGoalChange: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val clampedGoal = calorieGoal.coerceIn(1200, 4000)
    val diff = clampedGoal - tdee
    val diffText = when {
        diff > 0 -> "较 TDEE 高 ${abs(diff)} kcal，偏向增重/增肌"
        diff < 0 -> "较 TDEE 低 ${abs(diff)} kcal，偏向减脂"
        else -> "与 TDEE 持平，偏向维持体重"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = clampedGoal.toString(),
            onValueChange = { onCalorieGoalChange(it.toIntOrNull() ?: 2000) },
            label = { Text("目标热量") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            suffix = { Text("kcal") },
            shape = RoundedCornerShape(14.dp)
        )

        Slider(
            value = clampedGoal.toFloat(),
            onValueChange = { onCalorieGoalChange(it.roundToInt()) },
            valueRange = 1200f..4000f
        )

        Text(
            text = "区间 1200 - 4000 kcal",
            style = MaterialTheme.typography.labelMedium,
            color = if (isDark) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presetValues = listOf(
                "BMR" to calculateBMRFromTDEE(tdee, "SEDENTARY"),
                "TDEE" to tdee,
                "减脂" to (tdee * 0.8f).roundToInt(),
                "增肌" to (tdee * 1.1f).roundToInt()
            )
            presetValues.forEach { (label, value) ->
                TextButton(
                    onClick = { onCalorieGoalChange(value) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) {
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
                            }
                        )
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        Text(
            text = diffText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

fun calculateBMR(
    gender: String,
    weight: Float?,
    height: Float?,
    age: Int?
): Int = MetabolicConstants.calculateBMR(gender, weight, height, age)

fun calculateTDEE(bmr: Int, activityLevel: String): Int =
    MetabolicConstants.calculateTDEE(bmr, activityLevel)

private fun calculateBMRFromTDEE(tdee: Int, activityLevel: String): Int =
    MetabolicConstants.calculateBMRFromTDEE(tdee, activityLevel)
