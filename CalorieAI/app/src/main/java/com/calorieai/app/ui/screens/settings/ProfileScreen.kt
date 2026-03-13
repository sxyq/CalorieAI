package com.calorieai.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 个人信息页面
 * 参考Deadliner风格，支持编辑头像、ID、身体数据
 * 计算基础代谢率(BMR)和每日总消耗(TDEE)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
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
        ) {
            // 头像区域
            ProfileAvatarSection(
                avatarUrl = uiState.avatarUrl,
                userName = uiState.userName,
                userId = uiState.userId,
                onAvatarClick = { showAvatarPicker = true },
                onUserNameChange = viewModel::updateUserName,
                onUserIdChange = viewModel::updateUserId
            )

            // 身体数据（体重已移至记录页面）
            SettingsSection(title = "身体数据") {
                BodyDataSection(
                    gender = uiState.gender,
                    age = uiState.age,
                    height = uiState.height,
                    weight = uiState.weight,
                    onGenderChange = viewModel::updateGender,
                    onAgeChange = viewModel::updateAge,
                    onHeightChange = viewModel::updateHeight,
                    onWeightChange = viewModel::updateWeight,
                    showWeight = false // 体重设置已移至记录页面
                )
            }

            // 基础代谢计算结果
            val bmr = calculateBMR(
                gender = uiState.gender,
                weight = uiState.weight,
                height = uiState.height,
                age = uiState.age
            )
            val tdee = calculateTDEE(bmr, uiState.activityLevel)

            SettingsSection(title = "代谢计算") {
                MetabolismSection(
                    bmr = bmr,
                    tdee = tdee,
                    activityLevel = uiState.activityLevel,
                    onActivityLevelChange = viewModel::updateActivityLevel
                )
            }

            // 每日目标设置
            SettingsSection(title = "每日目标") {
                CalorieGoalSection(
                    calorieGoal = uiState.calorieGoal,
                    tdee = tdee,
                    onCalorieGoalChange = viewModel::updateCalorieGoal
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 头像选择器（简化版）
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

/**
 * 头像区域
 */
@Composable
private fun ProfileAvatarSection(
    avatarUrl: String?,
    userName: String,
    userId: String,
    onAvatarClick: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onUserIdChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    // 显示头像图片
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // 编辑图标
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑头像",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 用户名输入
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ID输入
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                placeholder = { Text("设置您的专属ID") }
            )
        }
    }
}

/**
 * 身体数据区域
 */
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
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 性别选择
        Text(
            text = "性别",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("男" to "MALE", "女" to "FEMALE").forEach { (label, value) ->
                FilterChip(
                    selected = gender == value,
                    onClick = { onGenderChange(value) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 年龄
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
            suffix = { Text("岁") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 身高
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
            suffix = { Text("cm") }
        )

        // 体重（可选显示）
        if (showWeight) {
            Spacer(modifier = Modifier.height(8.dp))
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
                suffix = { Text("kg") }
            )
        }
    }
}

/**
 * 代谢计算区域
 */
@Composable
private fun MetabolismSection(
    bmr: Int,
    tdee: Int,
    activityLevel: String,
    onActivityLevelChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 活动水平选择
        Text(
            text = "活动水平",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val activityLevels = listOf(
            "SEDENTARY" to "久坐不动",
            "LIGHT" to "轻度活动",
            "MODERATE" to "中度活动",
            "ACTIVE" to "高度活动",
            "VERY_ACTIVE" to "极度活动"
        )

        activityLevels.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActivityLevelChange(value) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = activityLevel == value,
                    onClick = { onActivityLevelChange(value) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BMR和TDEE显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetabolismCard(
                title = "基础代谢",
                value = bmr,
                unit = "kcal",
                description = "静息消耗"
            )
            MetabolismCard(
                title = "每日总消耗",
                value = tdee,
                unit = "kcal",
                description = "含活动消耗"
            )
        }
    }
}

/**
 * 代谢数据卡片
 */
@Composable
private fun MetabolismCard(
    title: String,
    value: Int,
    unit: String,
    description: String
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 每日目标设置
 */
@Composable
private fun CalorieGoalSection(
    calorieGoal: Int,
    tdee: Int,
    onCalorieGoalChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "每日热量目标",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = calorieGoal.toString(),
            onValueChange = { onCalorieGoalChange(it.toIntOrNull() ?: 2000) },
            label = { Text("目标热量") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            suffix = { Text("kcal") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 快捷设置按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "BMR" to calculateBMRFromTDEE(tdee, "SEDENTARY"),
                "TDEE" to tdee,
                "减脂" to (tdee * 0.8).toInt(),
                "增肌" to (tdee * 1.1).toInt()
            ).forEach { (label, value) ->
                OutlinedButton(
                    onClick = { onCalorieGoalChange(value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "建议：减脂时摄入TDEE的80%，增肌时摄入TDEE的110%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 计算基础代谢率 (BMR)
 * 使用Mifflin-St Jeor公式
 */
fun calculateBMR(
    gender: String,
    weight: Float?,
    height: Float?,
    age: Int?
): Int {
    if (weight == null || height == null || age == null) return 0

    val bmr = if (gender == "MALE") {
        (10 * weight) + (6.25 * height) - (5 * age) + 5
    } else {
        (10 * weight) + (6.25 * height) - (5 * age) - 161
    }
    return bmr.toInt().coerceAtLeast(1000)
}

/**
 * 计算每日总消耗 (TDEE)
 */
fun calculateTDEE(bmr: Int, activityLevel: String): Int {
    val multiplier = when (activityLevel) {
        "SEDENTARY" -> 1.2
        "LIGHT" -> 1.375
        "MODERATE" -> 1.55
        "ACTIVE" -> 1.725
        "VERY_ACTIVE" -> 1.9
        else -> 1.2
    }
    return (bmr * multiplier).toInt().coerceAtLeast(1200)
}

/**
 * 从TDEE反推BMR（用于快捷设置）
 */
private fun calculateBMRFromTDEE(tdee: Int, activityLevel: String): Int {
    val multiplier = when (activityLevel) {
        "SEDENTARY" -> 1.2
        "LIGHT" -> 1.375
        "MODERATE" -> 1.55
        "ACTIVE" -> 1.725
        "VERY_ACTIVE" -> 1.9
        else -> 1.2
    }
    return (tdee / multiplier).toInt()
}
