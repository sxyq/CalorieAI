package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.calorieai.app.data.model.Gender
import com.calorieai.app.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * 引导界面一：基本信息采集（合并版）
 * 性别选择、出生日期选择、身高体重选择 - Glass 毛玻璃风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen1(
    initialGender: String? = null,
    initialBirthDate: Long? = null,
    onNext: (gender: String, birthDate: Long, height: Float, weight: Float) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var selectedGender by remember { mutableStateOf<Gender?>(initialGender?.let { Gender.fromString(it) }) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var ageInput by remember { mutableStateOf("") }
    var selectedHeight by remember { mutableStateOf("170") }
    var selectedWeight by remember { mutableStateOf("65.0") }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LaunchedEffect(initialBirthDate) {
        initialBirthDate?.let { timestamp ->
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            selectedYear = calendar.get(Calendar.YEAR)
            selectedMonth = calendar.get(Calendar.MONTH) + 1
            val birthDate = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val today = LocalDate.now()
            val computedAge = today.year - birthDate.year -
                if (today.dayOfYear < birthDate.dayOfYear) 1 else 0
            if (computedAge in 1..120) {
                ageInput = computedAge.toString()
            }
        }
    }

    LaunchedEffect(selectedYear, selectedMonth) {
        val year = selectedYear ?: return@LaunchedEffect
        val month = selectedMonth ?: return@LaunchedEffect
        val today = LocalDate.now()
        val birthDate = LocalDate.of(year, month, 1)
        val computedAge = today.year - birthDate.year -
            if (today.dayOfYear < birthDate.dayOfYear) 1 else 0
        if (computedAge in 1..120) {
            ageInput = computedAge.toString()
        }
    }

    val parsedAge = ageInput.toIntOrNull()
    val inferredAgeFromBirthDate = run {
        val year = selectedYear
        val month = selectedMonth
        if (year == null || month == null) {
            null
        } else {
            val today = LocalDate.now()
            val birthDate = LocalDate.of(year, month, 1)
            today.year - birthDate.year -
                if (today.dayOfYear < birthDate.dayOfYear) 1 else 0
        }
    }
    val effectiveAge = parsedAge ?: inferredAgeFromBirthDate
    val ageValid = effectiveAge != null && effectiveAge in 10..100
    val heightValid = selectedHeight.toFloatOrNull()?.let { it in 100f..220f } == true
    val weightValid = selectedWeight.toFloatOrNull()?.let { it in 30f..200f } == true
    val isFormValid = selectedGender != null && ageValid && heightValid && weightValid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("欢迎使用") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 进度指示器
            OnboardingProgressIndicator(currentStep = 1, totalSteps = 4, isDark = isDark)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "让我们认识您",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "填写基本信息，获得个性化健康建议",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 性别选择
            Text(
                text = "您的性别",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderOption(
                    gender = Gender.MALE,
                    isSelected = selectedGender == Gender.MALE,
                    onClick = { selectedGender = Gender.MALE },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                GenderOption(
                    gender = Gender.FEMALE,
                    isSelected = selectedGender == Gender.FEMALE,
                    onClick = { selectedGender = Gender.FEMALE },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                GenderOption(
                    gender = Gender.OTHER,
                    isSelected = selectedGender == Gender.OTHER,
                    onClick = { selectedGender = Gender.OTHER },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = ageInput,
                onValueChange = { value ->
                    ageInput = value.filter { it.isDigit() }.take(3)
                    val manualAge = ageInput.toIntOrNull()
                    if (manualAge != null && manualAge in 10..100) {
                        val now = LocalDate.now()
                        selectedYear = now.year - manualAge
                        selectedMonth = selectedMonth ?: 1
                    }
                },
                label = { Text("年龄（可输入）") },
                placeholder = { Text("例如 25") },
                singleLine = true,
                isError = ageInput.isNotBlank() && !ageValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (ageInput.isNotBlank() && !ageValid) {
                Text(
                    text = "年龄范围建议 10-100 岁",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 出生日期
            Text(
                text = "出生年月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateSelectorCard(
                    label = "年份",
                    value = selectedYear?.toString() ?: "选择年份",
                    isSelected = selectedYear != null,
                    onClick = { showYearPicker = true },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                DateSelectorCard(
                    label = "月份",
                    value = selectedMonth?.let { "${it}月" } ?: "选择月份",
                    isSelected = selectedMonth != null,
                    onClick = { showMonthPicker = true },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 身高体重
            Text(
                text = "身体数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberSelectorCard(
                    label = "身高 (cm)",
                    value = selectedHeight,
                    onValueChange = { selectedHeight = it },
                    minValue = "100",
                    maxValue = "220",
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                NumberSelectorCard(
                    label = "体重 (kg)",
                    value = selectedWeight,
                    onValueChange = { selectedWeight = it },
                    minValue = "30",
                    maxValue = "200",
                    allowDecimal = true,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        val now = LocalDate.now()
                        val age = parsedAge ?: 25
                        val resolvedYear = selectedYear ?: (now.year - age)
                        val resolvedMonth = selectedMonth ?: 1
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, resolvedYear)
                            set(Calendar.MONTH, resolvedMonth - 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onNext(
                            selectedGender!!.name,
                            calendar.timeInMillis,
                            selectedHeight.toFloatOrNull() ?: 170f,
                            selectedWeight.toFloatOrNull() ?: 65f
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "下一步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedYear = selectedYear,
            onYearSelected = { selectedYear = it },
            onDismiss = { showYearPicker = false },
            isDark = isDark
        )
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedMonth,
            onMonthSelected = { selectedMonth = it },
            onDismiss = { showMonthPicker = false },
            isDark = isDark
        )
    }
}

@Composable
private fun GenderOption(
    gender: Gender,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "genderScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gender.emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gender.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DateSelectorCard(
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NumberSelectorCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minValue: String,
    maxValue: String,
    allowDecimal: Boolean = false,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val min = minValue.toFloatOrNull() ?: 0f
    val max = maxValue.toFloatOrNull() ?: 999f
    val step = if (allowDecimal) 0.1f else 1f

    fun formatValue(v: Float): String {
        return if (allowDecimal) String.format(Locale.US, "%.1f", v) else v.toInt().toString()
    }

    fun sanitizeInput(input: String): String {
        if (!allowDecimal) return input.filter { it.isDigit() }
        val filtered = input.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        return if (firstDot >= 0) {
            val before = filtered.substring(0, firstDot + 1)
            val after = filtered.substring(firstDot + 1).replace(".", "")
            (before + after).take(8)
        } else {
            filtered.take(8)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    val sanitized = sanitizeInput(input)
                    onValueChange(sanitized)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val current = value.toFloatOrNull() ?: min
                        val next = (current - step).coerceAtLeast(min)
                        onValueChange(formatValue(next))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "减少")
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = {
                        val current = value.toFloatOrNull() ?: min
                        val next = (current + step).coerceAtMost(max)
                        onValueChange(formatValue(next))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "增加")
                }
            }
            Text(
                text = if (allowDecimal) "范围: ${min.toInt()}-$max kg，可输入小数" else "范围: ${min.toInt()}-${max.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearPickerDialog(
    selectedYear: Int?,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val currentYear = LocalDate.now().year
    val years = (currentYear - 80..currentYear - 15).toList().reversed()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年份") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 350.dp)
            ) {
                items(years) { year ->
                    val isSelected = year == selectedYear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onYearSelected(year)
                                onDismiss()
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun MonthPickerDialog(
    selectedMonth: Int?,
    onMonthSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val months = listOf(
        "1月" to 1, "2月" to 2, "3月" to 3, "4月" to 4,
        "5月" to 5, "6月" to 6, "7月" to 7, "8月" to 8,
        "9月" to 9, "10月" to 10, "11月" to 11, "12月" to 12
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 350.dp)
            ) {
                items(months) { (name, month) ->
                    val isSelected = month == selectedMonth
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMonthSelected(month)
                                onDismiss()
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun OnboardingProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isCurrent) 6.dp else 4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}
