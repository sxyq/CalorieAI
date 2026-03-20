package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.components.OnboardingNavigationButtons
import com.calorieai.app.ui.theme.*

/**
 * 引导界面二：身体数据采集
 * 体重和身高输入，带单位选择和范围验证 - Glass 毛玻璃风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen2(
    initialWeight: Float? = null,
    initialHeight: Float? = null,
    initialWeightUnit: String = "kg",
    initialHeightUnit: String = "cm",
    onNext: (weight: Float, height: Float, weightUnit: String, heightUnit: String) -> Unit,
    onBack: () -> Unit
) {
    var weightInput by remember { mutableStateOf(initialWeight?.toString() ?: "") }
    var heightInput by remember { mutableStateOf(initialHeight?.toString() ?: "") }
    var weightUnit by remember { mutableStateOf(initialWeightUnit) }
    var heightUnit by remember { mutableStateOf(initialHeightUnit) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 验证状态
    var weightError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }

    // 实时验证
    fun validateWeight(value: String): String? {
        if (value.isBlank()) return null
        val num = value.toFloatOrNull()
        return when {
            num == null -> "请输入有效数字"
            weightUnit == "kg" && (num < 30 || num > 200) -> "体重范围：30-200kg"
            weightUnit == "lb" && (num < 66 || num > 440) -> "体重范围：66-440lb"
            else -> null
        }
    }

    fun validateHeight(value: String): String? {
        if (value.isBlank()) return null
        val num = value.toFloatOrNull()
        return when {
            num == null -> "请输入有效数字"
            heightUnit == "cm" && (num < 100 || num > 250) -> "身高范围：100-250cm"
            heightUnit == "ft" && (num < 3.3 || num > 8.2) -> "身高范围：3.3-8.2ft"
            else -> null
        }
    }

    // 单位转换
    fun convertWeight(value: Float, from: String, to: String): Float {
        return if (from == to) value
        else if (from == "kg" && to == "lb") value * 2.20462f
        else value / 2.20462f
    }

    fun convertHeight(value: Float, from: String, to: String): Float {
        return if (from == to) value
        else if (from == "cm" && to == "ft") value / 30.48f
        else value * 30.48f
    }

    // 表单验证
    val isFormValid = weightInput.isNotBlank() &&
                      heightInput.isNotBlank() &&
                      weightError == null &&
                      heightError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("身体数据") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgressIndicator(currentStep = 2, totalSteps = 6, isDark = isDark)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "您的身体数据",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
            )

            Text(
                text = "这些数据将帮助我们为您计算每日热量需求和BMI指数",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 体重输入 - Glass 卡片
            BodyDataInputCard(
                label = "体重",
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    weightError = validateWeight(it)
                },
                unit = weightUnit,
                onUnitChange = { newUnit ->
                    weightInput.toFloatOrNull()?.let { currentValue ->
                        val converted = convertWeight(currentValue, weightUnit, newUnit)
                        weightInput = String.format("%.1f", converted)
                    }
                    weightUnit = newUnit
                    weightError = validateWeight(weightInput)
                },
                units = listOf("kg", "lb"),
                error = weightError,
                icon = Icons.Default.FitnessCenter,
                placeholder = "请输入体重",
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 身高输入 - Glass 卡片
            BodyDataInputCard(
                label = "身高",
                value = heightInput,
                onValueChange = {
                    heightInput = it
                    heightError = validateHeight(it)
                },
                unit = heightUnit,
                onUnitChange = { newUnit ->
                    heightInput.toFloatOrNull()?.let { currentValue ->
                        val converted = convertHeight(currentValue, heightUnit, newUnit)
                        heightInput = if (newUnit == "ft") {
                            String.format("%.2f", converted)
                        } else {
                            String.format("%.1f", converted)
                        }
                    }
                    heightUnit = newUnit
                    heightError = validateHeight(heightInput)
                },
                units = listOf("cm", "ft"),
                error = heightError,
                icon = Icons.Default.Height,
                placeholder = "请输入身高",
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // BMI 提示 - Glass 卡片
            if (isFormValid) {
                val weight = weightInput.toFloatOrNull() ?: 0f
                val height = heightInput.toFloatOrNull() ?: 0f
                val bmi = if (weightUnit == "kg" && heightUnit == "cm") {
                    calculateBMI(weight, height)
                } else if (weightUnit == "lb" && heightUnit == "ft") {
                    calculateBMIImperial(weight, height)
                } else null

                bmi?.let {
                    val bmiCategory = getBMICategory(it)
                    val primaryContainerColor = if (isDark) GlassDarkColors.PrimaryContainer else GlassLightColors.PrimaryContainer
                    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
                    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(GlassUtils.CornerRadius.MEDIUM))
                            .background(primaryContainerColor.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "您的BMI: ${String.format("%.1f", it)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "BMI分类: $bmiCategory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }
                }
            }

            // 按钮行
            OnboardingNavigationButtons(
                onBack = onBack,
                onNext = {
                    if (isFormValid) {
                        val weight = weightInput.toFloatOrNull() ?: return@OnboardingNavigationButtons
                        val height = heightInput.toFloatOrNull() ?: return@OnboardingNavigationButtons
                        onNext(weight, height, weightUnit, heightUnit)
                    }
                },
                isNextEnabled = isFormValid,
                nextButtonColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary,
                backButtonContentColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 身体数据输入卡片 - Glass 毛玻璃风格
 */
@Composable
private fun BodyDataInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    units: List<String>,
    error: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDark) GlassDarkColors.CardBackground else GlassLightColors.CardBackground
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val surfaceVariantColor = if (isDark) GlassDarkColors.SurfaceVariant else GlassLightColors.SurfaceVariant
    val errorColor = if (isDark) GlassDarkColors.Error else GlassLightColors.Error

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GlassUtils.CornerRadius.LARGE))
            .background(surfaceColor.copy(alpha = GlassAlpha.CARD_BACKGROUND))
            .padding(20.dp)
    ) {
        Column {
            // 标签和单位选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$label *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                }

                // 单位切换 - Glass 风格
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    units.forEach { u ->
                        val isSelected = u == unit
                        val unitBackgroundColor = if (isSelected) {
                            primaryColor
                        } else {
                            surfaceVariantColor.copy(alpha = 0.5f)
                        }
                        val unitTextColor = if (isSelected) {
                            if (isDark) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
                        } else {
                            onSurfaceVariantColor
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(unitBackgroundColor)
                                .clickable { onUnitChange(u) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = u,
                                style = MaterialTheme.typography.labelMedium,
                                color = unitTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 输入框 - Glass 风格
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = error != null,
                supportingText = {
                    if (error != null) {
                        Text(
                            text = error,
                            color = errorColor
                        )
                    } else {
                        val rangeText = when {
                            label == "体重" && unit == "kg" -> "范围: 30-200kg"
                            label == "体重" && unit == "lb" -> "范围: 66-440lb"
                            label == "身高" && unit == "cm" -> "范围: 100-250cm"
                            label == "身高" && unit == "ft" -> "范围: 3.3-8.2ft"
                            else -> ""
                        }
                        Text(
                            text = rangeText,
                            color = onSurfaceVariantColor
                        )
                    }
                },
                trailingIcon = {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    errorBorderColor = errorColor,
                    unfocusedBorderColor = surfaceVariantColor
                )
            )
        }
    }
}

/**
 * 计算BMI（公制）
 */
private fun calculateBMI(weightKg: Float, heightCm: Float): Float {
    val heightM = heightCm / 100f
    return weightKg / (heightM * heightM)
}

/**
 * 计算BMI（英制）
 */
private fun calculateBMIImperial(weightLb: Float, heightFt: Float): Float {
    val heightInches = heightFt * 12
    return (weightLb / (heightInches * heightInches)) * 703
}

/**
 * 获取BMI分类
 */
private fun getBMICategory(bmi: Float): String {
    return when {
        bmi < 18.5f -> "偏瘦"
        bmi < 24f -> "正常"
        bmi < 28f -> "偏胖"
        else -> "肥胖"
    }
}
