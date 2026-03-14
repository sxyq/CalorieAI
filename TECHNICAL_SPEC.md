# CalorieAI 技术开发文档

## 文档说明

本文档用于记录 CalorieAI 应用开发过程中的技术细节，包括：
- 变量命名与定义
- 函数/方法签名
- 类之间的关系
- 实现描述
- 功能描述

**维护规则**：每开发一个功能后必须更新本文档

---

## 目录

1. [全局常量与配置](#全局常量与配置)
2. [数据模型](#数据模型)
3. [UI组件](#ui组件)
4. [ViewModel](#viewmodel)
5. [工具类](#工具类)
24. [Liquid Glass 设计系统](#liquid-glass-设计系统)
25. [开发日志](#开发日志)

---

## 全局常量与配置

### 颜色常量

#### 浅色主题
```kotlin
// 主色调 - 蓝色系
val PrimaryLight = Color(0xFF2196F3)          // Material Blue 500
val OnPrimaryLight = Color(0xFFFFFFFF)        // 白色文字
val PrimaryContainerLight = Color(0xFFBBDEFB) // 浅蓝容器
val OnPrimaryContainerLight = Color(0xFF1565C0)

// 次要颜色
val SecondaryLight = Color(0xFF03A9F4)        // Light Blue 500
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFB3E5FC)
val OnSecondaryContainerLight = Color(0xFF0288D1)

// 第三颜色
val TertiaryLight = Color(0xFF00BCD4)         // Cyan 500
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFB2EBF2)
val OnTertiaryContainerLight = Color(0xFF0097A7)

// 背景与表面
val BackgroundLight = Color(0xFFF5F5F5)       // 浅灰背景
val OnBackgroundLight = Color(0xFF1C1B1F)     // 深色文字
val SurfaceLight = Color(0xFFFFFFFF)          // 白色表面
val OnSurfaceLight = Color(0xFF1C1B1F)
val SurfaceVariantLight = Color(0xFFE0E0E0)
val OnSurfaceVariantLight = Color(0xFF49454F)

// 表面容器色
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8F8F8)
val SurfaceContainerLight = Color(0xFFF0F0F0)
val SurfaceContainerHighLight = Color(0xFFE8E8E8)
val SurfaceContainerHighestLight = Color(0xFFE0E0E0)

// 错误颜色
val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF9DEDC)
val OnErrorContainerLight = Color(0xFF410E0B)
```

#### 深色主题
```kotlin
// 主色调
val PrimaryDark = Color(0xFF90CAF9)           // 浅蓝
val OnPrimaryDark = Color(0xFF0D47A1)         // 深蓝文字
val PrimaryContainerDark = Color(0xFF1565C0)  // 深蓝容器
val OnPrimaryContainerDark = Color(0xFFBBDEFB)

// 次要颜色
val SecondaryDark = Color(0xFF81D4FA)
val OnSecondaryDark = Color(0xFF01579B)
val SecondaryContainerDark = Color(0xFF0288D1)
val OnSecondaryContainerDark = Color(0xFFB3E5FC)

// 第三颜色
val TertiaryDark = Color(0xFF80DEEA)
val OnTertiaryDark = Color(0xFF006064)
val TertiaryContainerDark = Color(0xFF0097A7)
val OnTertiaryContainerDark = Color(0xFFB2EBF2)

// 背景与表面
val BackgroundDark = Color(0xFF121212)        // 深色背景
val OnBackgroundDark = Color(0xFFE0E0E0)      // 浅色文字
val SurfaceDark = Color(0xFF1E1E1E)           // 深灰表面
val OnSurfaceDark = Color(0xFFE0E0E0)
val SurfaceVariantDark = Color(0xFF2C2C2C)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)

// 表面容器色
val SurfaceContainerLowestDark = Color(0xFF0D0D0D)
val SurfaceContainerLowDark = Color(0xFF1A1A1A)
val SurfaceContainerDark = Color(0xFF1E1E1E)
val SurfaceContainerHighDark = Color(0xFF252525)
val SurfaceContainerHighestDark = Color(0xFF2C2C2C)

// 错误颜色
val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)
```

#### 图表与状态颜色
```kotlin
// 图表专用颜色
val ChartRed = Color(0xFFF77E66)      // 超标/警告
val ChartGreen = Color(0xFF82ABA3)    // 达标/成功
val ChartOrange = Color(0xFFFEC37D)   // 提醒
val ChartBlue = Color(0xFF97A5C0)     // 信息
val ChartPurple = Color(0xFFB39DDB)   // 特殊标记

// 状态颜色
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFF44336)
val WarningOrange = Color(0xFFFF9800)
val InfoBlue = Color(0xFF2196F3)

// 特殊颜色
val StarYellow = Color(0xFFFFE819)    // 收藏星星
```

### 尺寸常量
```kotlin
// 圆角
val CardCornerRadius = 24.dp
val ButtonCornerRadius = 12.dp
val InputCornerRadius = 12.dp

// 间距
val PaddingSmall = 8.dp
val PaddingMedium = 12.dp
val PaddingLarge = 16.dp
val PaddingXLarge = 24.dp

// 按钮高度
val PrimaryButtonHeight = 54.dp
val IconButtonSize = 40.dp
```

---

## 数据模型

### FoodRecord（食物记录）
```kotlin
@Entity(tableName = "food_records")
data class FoodRecord(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,           // 食物名称
    val userInput: String,          // 用户原始输入
    val totalCalories: Int,         // 总热量（千卡）
    val protein: Float,             // 蛋白质（克）
    val carbs: Float,               // 碳水化合物（克）
    val fat: Float,                 // 脂肪（克）
    val mealType: MealType,         // 餐次类型
    val recordTime: Long,           // 记录时间戳
    val isStarred: Boolean = false, // 是否收藏
    val notes: String? = null       // 备注
)
```

### MealType（餐次类型）
```kotlin
enum class MealType {
    BREAKFAST,       // 早餐
    BREAKFAST_SNACK, // 早加餐
    LUNCH,           // 午餐
    LUNCH_SNACK,     // 午加餐
    DINNER,          // 晚餐
    DINNER_SNACK,    // 晚加餐
    SNACK            // 其他加餐
}
```

### ExerciseRecord（运动记录）
```kotlin
@Entity(tableName = "exercise_records")
data class ExerciseRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val exerciseType: ExerciseType,    // 运动类型
    val durationMinutes: Int,          // 运动时长（分钟）
    val caloriesBurned: Int,           // 消耗热量（千卡）
    val notes: String? = null,         // 备注
    val recordTime: Long = System.currentTimeMillis()
)
```

### ExerciseType（运动类型）
```kotlin
enum class ExerciseType(
    val displayName: String,
    val emoji: String,
    val caloriesPerMinute: Int
) {
    RUNNING("跑步", "🏃", 10),
    WALKING("快走", "🚶", 4),
    CYCLING("骑行", "🚴", 8),
    SWIMMING("游泳", "🏊", 12),
    YOGA("瑜伽", "🧘", 3),
    WEIGHT_TRAINING("力量训练", "🏋️", 6),
    HIIT("HIIT", "🔥", 15),
    // ... 共27种运动类型
    OTHER("其他运动", "🎯", 0)  // 自定义运动，热量由用户输入
}
```

### CustomExerciseInfo（自定义运动信息）
```kotlin
/**
 * 自定义运动信息存储在ExerciseRecord的notes字段中
 * 格式: "CUSTOM:{name}:{caloriesPerMinute}"
 * 例如: "CUSTOM:太极拳:5"
 */
data class CustomExerciseInfo(
    val name: String,           // 自定义运动名称
    val caloriesPerMinute: Int  // 每分钟消耗热量
)
```

### BackupData（备份数据结构）
```kotlin
/**
 * 备份数据主结构
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val backupDate: String,              // ISO-8601格式日期时间
    val foodRecords: List<FoodRecordBackup>,
    val exerciseRecords: List<ExerciseRecordBackup>,
    val userSettings: UserSettingsBackup?
)

/**
 * 食物记录备份格式
 */
@Serializable
data class FoodRecordBackup(
    val id: String,
    val foodName: String,
    val userInput: String,
    val totalCalories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val ingredients: List<IngredientBackup>,
    val mealType: String,
    val recordTime: Long,
    val iconUrl: String?,
    val iconLocalPath: String?,
    val isStarred: Boolean,
    val confidence: String,
    val notes: String?
)

/**
 * 运动记录备份格式
 */
@Serializable
data class ExerciseRecordBackup(
    val id: String,
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String?,
    val recordTime: Long
)

/**
 * 用户设置备份格式
 */
@Serializable
data class UserSettingsBackup(
    val dailyCalorieGoal: Int,
    val userName: String?,
    val userGender: String?,
    val userAge: Int?,
    val userHeight: Float?,
    val userWeight: Float?,
    val activityLevel: String,
    val dietaryPreference: String,
    val isNotificationEnabled: Boolean,
    val isDarkMode: Boolean,
    val themeMode: String,
    val useDeadlinerStyle: Boolean,
    val hideDividers: Boolean,
    val fontSize: String,
    val enableAnimations: Boolean,
    val feedbackType: String,
    val enableVibration: Boolean,
    val enableSound: Boolean,
    val startupPage: String,
    val enableQuickAdd: Boolean,
    val enableGoalReminder: Boolean,
    val enableStreakReminder: Boolean,
    val enableAutoBackup: Boolean,
    val enableCloudSync: Boolean
)
```

### AIConfig（AI配置）
```kotlin
@Entity(tableName = "ai_configs")
data class AIConfig(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,                   // 配置名称（自定义）
    val icon: String,                   // 图标（资源名或URL）
    val iconType: IconType = IconType.RESOURCE,  // 图标类型
    val protocol: AIProtocol,           // 协议类型
    val apiUrl: String,                 // API地址
    val apiKey: String,                 // API密钥（加密存储）
    val modelId: String,                // 模型ID
    val isImageUnderstanding: Boolean,  // 是否启用图像理解
    val isDefault: Boolean = false      // 是否为默认配置
)

enum class AIProtocol {
    OPENAI,     // OpenAI协议
    CLAUDE,     // Claude协议
    KIMI,       // Moonshot Kimi
    GLM,        // Zhipu GLM
    QWEN,       // Alibaba Qwen
    DEEPSEEK,   // DeepSeek
    GEMINI      // Google Gemini
}
```

### AITokenUsage（AI Token使用记录）
```kotlin
@Entity(tableName = "ai_token_usage")
data class AITokenUsage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val configId: String,               // AI配置ID
    val configName: String,             // AI配置名称
    val promptTokens: Int,              // Prompt Token数
    val completionTokens: Int,          // Completion Token数
    val totalTokens: Int,               // 总Token数
    val cost: Double                    // 估算成本（美元）
)

/**
 * Token使用统计
 */
data class TokenUsageStats(
    val totalTokens: Int,               // 总Token数
    val promptTokens: Int,              // Prompt Token总数
    val completionTokens: Int,          // Completion Token总数
    val totalCost: Double,              // 总成本
    val requestCount: Int,              // 请求次数
    val todayTokens: Int,               // 今日Token数
    val todayCost: Double,              // 今日成本
    val monthTokens: Int,               // 本月Token数
    val monthCost: Double               // 本月成本
)
```

---

## UI组件

### 1. 图表组件 (ui/components/charts/)

#### LineChartView（折线图）
```kotlin
@Composable
fun LineChartView(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Int = Color.parseColor("#2196F3"),
    fillColor: Int = Color.parseColor("#332196F3"),
    showLabels: Boolean = true
)
```
**用途**: 展示摄入趋势（周趋势/月趋势）

#### PieChartView（饼状图）
```kotlin
@Composable
fun PieChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier,
    showPercentages: Boolean = true,
    centerText: String? = null
)
```
**用途**: 展示今日摄入状态（营养素分布）

#### BarChartView（柱状图）
```kotlin
@Composable
fun BarChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier
)
```
**用途**: 展示各餐次摄入对比

#### RadarChartView（雷达图）
```kotlin
@Composable
fun RadarChartView(
    labels: List<String>,
    data: List<Float>,
    modifier: Modifier = Modifier,
    fillColor: Int = Color.parseColor("#332196F3"),
    strokeColor: Int = Color.parseColor("#2196F3")
)
```
**用途**: 展示营养素均衡分析

### 2. AIChatWidget（AI聊天小窗口）

**位置**: `ui/components/AIChatWidget.kt`

**功能描述**: 
- 悬浮按钮形式的AI助手入口
- 点击展开迷你聊天窗口
- 支持快捷功能：热量评估/菜谱规划/健康咨询
- 可展开至全屏聊天页面

**变量**:
```kotlin
// 状态
val isExpanded: Boolean = false          // 是否展开

// 快捷功能
val quickActions: List<QuickAction> = listOf(
    QuickAction("热量评估", Icons.Default.Assessment, "分析今日热量消耗是否合理"),
    QuickAction("菜谱规划", Icons.Default.RestaurantMenu, "根据目标定制健康食谱"),
    QuickAction("健康咨询", Icons.Default.HealthAndSafety, "营养、运动相关问题解答")
)
```

**函数**:
```kotlin
// 悬浮按钮组件
@Composable
fun AIChatWidget(
    onExpandToFullScreen: () -> Unit,
    modifier: Modifier = Modifier
)

// 迷你聊天窗口
@Composable
private fun AIChatMiniWindow(
    onDismiss: () -> Unit,
    onExpandToFullScreen: () -> Unit
)

// 快捷功能按钮
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
)
```

### 3. AIChatScreen（AI聊天全屏页面）

**位置**: `ui/screens/ai/AIChatScreen.kt`

**功能描述**:
- 全屏AI聊天界面
- 支持热量评估、菜谱规划、健康咨询
- 实时聊天对话
- 打字指示器动画

**变量**:
```kotlin
// ViewModel
val viewModel: AIChatViewModel = hiltViewModel()

// UI状态
val uiState: StateFlow<AIChatUiState>

// 消息数据类
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
```

**函数**:
```kotlin
// 主页面
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
)

// 快捷功能行
@Composable
private fun QuickActionsRow(
    onCalorieAssessment: () -> Unit,
    onMealPlanning: () -> Unit,
    onHealthConsult: () -> Unit
)

// 聊天消息项
@Composable
private fun ChatMessageItem(message: ChatMessage)

// 正在输入指示器
@Composable
private fun TypingIndicator()

// 聊天输入区域
@Composable
private fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
)
```

### 4. ExerciseDialog（运动记录对话框）

**位置**: `ui/components/ExerciseDialog.kt`

**功能描述**:
- 添加运动消耗记录
- 选择运动类型（27种 + 自定义）
- 输入运动时长
- 自动计算消耗热量
- 支持自定义运动（输入运动名称和消耗热量）

**变量**:
```kotlin
// 运动类型列表
val exerciseTypes: List<ExerciseType> = ExerciseType.entries

// 状态
var selectedExercise: ExerciseType? by remember { mutableStateOf(null) }
var duration: String by remember { mutableStateOf("") }
var customCalories: String by remember { mutableStateOf("") }
var isCustomExercise: Boolean by remember { mutableStateOf(false) }
var customExerciseName: String by remember { mutableStateOf("") }
var showCustomInput: Boolean by remember { mutableStateOf(false) }
```

**函数**:
```kotlin
@Composable
fun ExerciseDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ExerciseType, Int, Int?, String?) -> Unit  // 新增customName参数
)

// 自定义运动输入组件
@Composable
private fun CustomExerciseItem(
    isSelected: Boolean,
    onClick: () -> Unit
)

// 运动类型网格项
@Composable
private fun ExerciseTypeItem(
    exercise: ExerciseType,
    isSelected: Boolean,
    onClick: () -> Unit
)
```

### 5. UnifiedTrendChart（统一趋势图表）

**位置**: `ui/components/charts/UnifiedTrendChart.kt`

**功能描述**:
- 单一图表组件支持多种时间维度
- 支持按天/周/月切换
- 日期范围选择器（从X日期到X日期）
- 多数据系列：热量摄入、运动消耗、体重变化

**变量**:
```kotlin
// 时间维度枚举
enum class TimeDimension {
    DAY, WEEK, MONTH
}

// 图表数据
val chartData: TrendChartData

// 时间范围
val startDate: LocalDate
val endDate: LocalDate
```

**函数**:
```kotlin
@Composable
fun UnifiedTrendChart(
    data: TrendChartData,
    timeDimension: TimeDimension,
    startDate: LocalDate,
    endDate: LocalDate,
    onTimeDimensionChange: (TimeDimension) -> Unit,
    onDateRangeChange: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
)

// 时间维度选择器
@Composable
private fun TimeDimensionSelector(
    selected: TimeDimension,
    onSelect: (TimeDimension) -> Unit
)

// 日期范围选择器
@Composable
private fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onDateRangeChange: (LocalDate, LocalDate) -> Unit
)
```

### 6. DateRangePicker（日期范围选择器）

**位置**: `ui/components/DateRangePicker.kt`

**功能描述**:
- 选择起始日期和结束日期
- 支持快速选择（最近7天/30天/本月/上月）
- 日期有效性验证

**函数**:
```kotlin
@Composable
fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)

// 快速选择按钮
@Composable
private fun QuickSelectButtons(
    onSelect: (LocalDate, LocalDate) -> Unit
)
```

---

## ViewModel

### AIChatViewModel（AI聊天ViewModel）

**位置**: `ui/screens/ai/AIChatViewModel.kt`

**功能描述**:
管理AI聊天界面的状态和交互逻辑。

**变量**:
```kotlin
// 状态
private val _uiState = MutableStateFlow(AIChatUiState())
val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

// 依赖注入
@Inject lateinit var foodRecordRepository: FoodRecordRepository
@Inject lateinit var userSettingsRepository: UserSettingsRepository
@Inject lateinit var aiChatService: AIChatService
```

**函数**:
```kotlin
// 输入文本变化
fun onInputChange(text: String)

// 发送消息
fun sendMessage()

// 开始热量评估（自动获取今日数据并分析）
fun startCalorieAssessment()

// 开始菜谱规划
fun startMealPlanning()

// 开始健康咨询
fun startHealthConsult()

// 清空对话
fun clearChat()
```

### ExerciseRecordRepository（运动记录仓库）

**位置**: `data/repository/ExerciseRecordRepository.kt`

**函数**:
```kotlin
// 获取所有记录
fun getAllRecords(): Flow<List<ExerciseRecord>>

// 获取所有记录（一次性）
suspend fun getAllRecordsOnce(): List<ExerciseRecord>

// 获取时间段内的记录
fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<ExerciseRecord>>

// 获取时间段内的记录（同步）
suspend fun getRecordsBetweenSync(startTime: Long, endTime: Long): List<ExerciseRecord>

// 获取总消耗热量
suspend fun getTotalCaloriesBurnedBetween(startTime: Long, endTime: Long): Int

// 获取总运动时长
suspend fun getTotalDurationBetween(startTime: Long, endTime: Long): Int

// 获取运动类型分布
suspend fun getExerciseTypeDistribution(startTime: Long, endTime: Long): Map<ExerciseType, Int>

// 获取最活跃的运动类型
suspend fun getMostActiveExerciseType(startTime: Long, endTime: Long): Pair<ExerciseType, Int>?

// 添加记录
suspend fun addRecord(record: ExerciseRecord)

// 删除记录
suspend fun deleteRecord(record: ExerciseRecord)

// 清空所有记录
suspend fun clearAllRecords()
```

### BackupService（备份服务）

**位置**: `service/backup/BackupService.kt`

**功能描述**:
- 创建JSON格式备份文件
- 从备份文件恢复数据
- 备份数据验证
- 支持饮食记录、运动记录、用户设置

**函数**:
```kotlin
/**
 * 创建备份
 * @param uri 备份文件保存URI
 * @return Result<String> 成功返回备份信息，失败返回错误
 */
suspend fun createBackup(uri: Uri): Result<String>

/**
 * 恢复备份
 * @param uri 备份文件URI
 * @return Result<String> 成功返回恢复信息，失败返回错误
 */
suspend fun restoreBackup(uri: Uri): Result<String>

/**
 * 获取备份信息（不恢复）
 * @param uri 备份文件URI
 * @return Result<BackupData> 备份数据信息
 */
suspend fun getBackupInfo(uri: Uri): Result<BackupData>

/**
 * 验证备份文件
 * @param uri 备份文件URI
 * @return Boolean 是否有效
 */
suspend fun validateBackup(uri: Uri): Boolean
```

### BackupSettingsViewModel（备份设置ViewModel）

**位置**: `ui/screens/settings/BackupSettingsViewModel.kt`

**功能描述**:
- 管理备份/恢复UI状态
- 处理备份文件选择
- 显示备份信息预览
- 恢复确认对话框

**状态**:
```kotlin
data class BackupSettingsUiState(
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val backupInfo: BackupData? = null,
    val pendingRestoreUri: Uri? = null
)
```

**函数**:
```kotlin
// 创建备份
fun createBackup(uri: Uri)

// 加载备份信息
fun loadBackupInfo(uri: Uri)

// 确认恢复
fun confirmRestore()

// 关闭恢复对话框
fun dismissRestoreDialog()
```

### AITokenUsageRepository（Token使用统计仓库）

**位置**: `data/repository/AITokenUsageRepository.kt`

**功能描述**:
管理AI Token使用记录的存储和统计查询。

**函数**:
```kotlin
// 记录Token使用情况
suspend fun recordTokenUsage(
    configId: String,
    configName: String,
    promptTokens: Int,
    completionTokens: Int,
    cost: Double
)

// 获取Token使用统计（今日/本月/总计）
fun getTokenUsageStats(): Flow<TokenUsageStats?>

// 获取指定时间段的Token使用记录
fun getTokenUsageBetween(startTime: Long, endTime: Long): Flow<List<AITokenUsage>>

// 清理旧数据（保留最近3个月）
suspend fun cleanupOldData()
```

---

## 工具类

### FoodImageAnalysisService（食物图片分析服务）

**位置**: `service/ai/FoodImageAnalysisService.kt`

**功能描述**:
使用多模态AI分析食物图片，识别食物并估算营养成分。

**函数**:
```kotlin
// 分析食物图片
suspend fun analyzeFoodImage(
    imageUri: Uri,
    context: Context,
    userHint: String = ""
): Result<FoodAnalysisResult>

// 将图片转换为base64
private fun uriToBase64(uri: Uri, context: Context): String?

// 压缩图片
private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap

// 调用AI API
private suspend fun callVisionAPI(
    config: AIConfig,
    base64Image: String,
    userHint: String
): FoodAnalysisResult
```

**返回数据类**:
```kotlin
data class FoodAnalysisResult(
    val foodName: String = "",
    val estimatedWeight: Int = 0,
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val description: String = ""
)
```

### AIChatService（AI聊天服务）

**位置**: `service/ai/AIChatService.kt`

**功能描述**:
处理AI聊天消息的发送和接收，自动记录Token使用情况。

**函数**:
```kotlin
// 发送消息并获取回复
suspend fun sendMessage(message: String): String

// 调用AI API
private suspend fun callAIAPI(config: AIConfig, message: String): String

// 记录Token使用情况
private suspend fun recordTokenUsage(
    config: AIConfig, 
    responseBody: String, 
    userMessage: String, 
    systemPrompt: String
)

// 提取Token使用量
private fun extractTokenUsage(responseBody: String, protocol: String): TokenUsage?

// 计算成本（美元）
private fun calculateCost(
    promptTokens: Int, 
    completionTokens: Int, 
    protocol: String, 
    modelId: String
): Double

// 构建请求体
private fun buildRequestBody(
    config: AIConfig,
    systemPrompt: String,
    message: String
): String

// 解析响应
private fun parseResponse(responseBody: String, protocol: String): String
```

**支持的成本计算模型**:
- OpenAI: GPT-4o, GPT-4, GPT-3.5
- Claude: Claude-3-Opus, Claude-3-Sonnet, Claude-3-Haiku
- Kimi: 统一价格
- GLM: 统一价格
- Qwen: 统一价格
- DeepSeek: 统一价格

)
```

---

## Liquid Glass 设计系统

为了提升应用的视觉体验，引入了基于“液态玻璃”（Liquid Glass）风格的设计系统。该系统利用 Android S(12) 及更高版本的 `RenderEffect` 实现毛玻璃效果，并结合动态缩放交互。

### 1. 核心修饰符 (Modifiers)

#### liquidGlass
**位置**: `ui/components/LiquidGlassComponents.kt`
**实现**: 
- `graphicsLayer`: 应用高斯模糊 (RenderEffect.createBlurEffect)
- `background`: 半透明着色 (tint)
- `border`: 线性渐变边框，模拟边缘高光反射

```kotlin
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color.White.copy(alpha = 0.15f),
    blurRadius: Float = 20f,
    borderAlpha: Float = 0.3f
): Modifier
```

**参数说明**:
- `shape`: 玻璃形状，默认 24dp 圆角
- `tint`: 着色颜色，默认白色 15% 透明度（避免过度模糊）
- `blurRadius`: 模糊半径，默认 20f（已优化，避免界面过糊）
- `borderAlpha`: 边框透明度，默认 0.3f

#### interactiveScale
**位置**: `ui/components/LiquidGlassComponents.kt`
**实现**: 
- 监听 `MutableInteractionSource` 的按压状态
- 使用 `animateFloatAsState` 实现 Spring 弹簧缩放效果 (按下时缩小至 0.92f)

### 2. 容器组件 (Containers)

#### GlassGooeyContainer
**实现**: 应用“粘性”（Gooey）融合效果，通过高质量模糊配合颜色矩阵（ColorMatrix）的 Alpha 阈值过滤，使内部重叠的玻璃元素产生融合效果。

### 3. 应用场景
- **卡片替换**: 所有的 `Card` 组件逐渐重构为使用 `Box + liquidGlass`
- **背景增强**: 页面 `Scaffold` 背景使用 `Box + Brush.linearGradient` 配合低 Alpha 值的玻璃层

### 4. 页面重构记录 (2026-03-14)

#### 已完成重构的页面

| 页面 | 视觉风格 | 关键特性 |
|------|----------|----------|
| **ResultScreen** | 深度玻璃态 | 发光热量数字、GlassGooeyContainer 营养网格、对角线渐变背景 |
| **HomeScreen** | 动态层级 | 悬浮玻璃日期选择器、多色彩玻璃层卡片、微光边框记录项 |
| **StatsScreen** | 全局毛玻璃 | 玻璃 Tab 行、瀑布流统计卡片、透明背景图表容器 |
| **AddFoodScreen** | 软玻璃 | 柔和渐变背景、软玻璃输入框、软玻璃按钮组 |
| **ManualAddScreen** | 软玻璃 | 软玻璃输入表单、三列营养成分输入、软玻璃餐次选择器 |
| **CameraScreen** | 玻璃相机 | 玻璃快门按钮、玻璃提示卡片、玻璃权限按钮 |
| **SettingsScreen** | 玻璃设置 | 玻璃设置项卡片、分组玻璃容器 |
| **ProfileScreen** | 玻璃用户 | 玻璃头像区域、代谢数据玻璃卡片 |
| **AIChatScreen** | 玻璃对话 | 玻璃消息气泡、玻璃会话项 |

#### 重构技术规范

**背景层级架构**:
```kotlin
// 每一屏的根节点采用 Box + Scaffold 模式
Box(
    modifier = Modifier.fillMaxSize().background(
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            )
        )
    )
) {
    Scaffold(
        containerColor = Color.Transparent, // 关键：设为透明
        // ...
    )
}
```

**玻璃卡片标准参数** (优化后):
```kotlin
Modifier.liquidGlass(
    shape = RoundedCornerShape(20.dp),           // 统一圆角
    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f), // 低透明度避免过糊
    blurRadius = 15f,                            // 降低模糊半径
    borderAlpha = 0.3f                           // 轻微边框
)
```

**按压反馈**:
```kotlin
val interactionSource = remember { MutableInteractionSource() }
Modifier
    .interactiveScale(interactionSource)  // 缩放反馈
    .clickable(
        interactionSource = interactionSource,
        indication = null,                  // 禁用默认波纹
        onClick = { }
    )
```

#### 优化记录

**2026-03-14 玻璃效果优化**:
- 问题：界面整体过于模糊，像被厚玻璃蒙住
- 原因：blurRadius 值过高（35f-50f），tint alpha 过大
- 解决方案：
  - 默认 blurRadius: 40f → 20f
  - 默认 tint alpha: 0.25f → 0.15f
  - 默认 borderAlpha: 0.4f → 0.3f
  - 各页面 blurRadius 统一降至 12f-15f
  - 背景渐变 alpha 降至 0.15f-0.3f

---

## 开发日志

### 2026-03-13 - v3.1 运动与统计增强

#### 已完成功能
- [x] 运动类型自定义功能
  - ExerciseDialog.kt: 添加自定义运动选项
  - 支持输入运动名称和消耗热量
  - 自定义运动信息存储在notes字段
- [x] 备份与恢复功能
  - BackupService.kt: 核心备份服务
  - BackupSettingsScreen.kt: 备份设置界面
  - BackupSettingsViewModel.kt: 状态管理
  - JSON格式备份（饮食记录、运动记录、用户设置）
  - 备份信息预览和恢复确认
- [x] 统一趋势图表
  - UnifiedTrendChart.kt: 单一图表支持多种时间维度
  - 按天/周/月切换
  - 日期范围选择器（从X到X）
  - 多数据系列：热量摄入、运动消耗、体重变化
- [x] 运动统计增强
  - 概览统计添加运动相关数据
  - 上月总结添加体重变化、运动量数据
  - 运动类型分布饼图
  - 最活跃运动类型统计
- [x] 鼓励标语功能
  - 25条随机鼓励标语
  - 根据用户目标达成情况显示

#### 技术要点
1. **自定义运动存储格式**: `CUSTOM:{name}:{caloriesPerMinute}` 存储在notes字段
2. **备份数据格式**: JSON序列化，版本控制，支持增量恢复
3. **统一趋势图表**: 使用单一MPAndroidChart实例，动态切换数据集
4. **日期范围选择**: 支持快速选择（最近7天/30天/本月/上月）

#### 界面结构调整记录 (2026-03-13)
1. **AddMethodSelectorScreen.kt**: 
   - 添加 `onNavigateToWeight` 和 `onNavigateToExercise` 参数
   - 新增 `SmallMethodCard` 组件用于体重记录和运动添加
   - 在底部添加"其他记录"区域，包含体重记录和运动添加两个小卡片
2. **ProfileScreen.kt**: 
   - `BodyDataSection` 添加 `showWeight` 参数（默认 true）
   - 个人信息页面设置 `showWeight = false` 隐藏体重输入
3. **StatsScreen.kt**: 
   - 删除 `WeeklyTrendCard` 和 `MonthlyTrendCard` 调用
   - 新增 `TrendAnalysisHeader` 组件（标题栏+日期选择+时间维度）
   - 新增三个独立图表组件：`CalorieTrendChart`、`ExerciseTrendChart`、`WeightTrendChart`

#### 编译修复记录 (2026-03-13)
1. **UnifiedTrendChart.kt**: 添加 `androidx.compose.foundation.clickable` 导入
2. **StatsScreen.kt**: 使用 `exerciseType.emoji` 和 `exerciseType.displayName` 替代硬编码的 when 表达式
3. **ExerciseRecordDao.kt**: 添加 `getAllRecordsOnce(): List<ExerciseRecord>` 方法
4. **ExerciseRecordRepository.kt**: 添加 `getAllRecordsOnce()` 方法委托给 DAO
5. **StatsUtils.kt**: 更新 `computeLastMonthSummary` 函数签名，添加 exerciseRecords 和 currentWeight 参数

---

### 2026-03-13 - 阶段五高级功能完成

#### 已完成功能
- [x] AI聊天小窗口（悬浮按钮 + 迷你窗口 + 全屏页面）
  - AIChatWidget.kt: 悬浮按钮组件
  - AIChatMiniWindow: 迷你聊天窗口
  - AIChatScreen.kt: 全屏聊天页面
  - AIChatViewModel.kt: 状态管理
  - AIChatService.kt: AI服务
- [x] 运动消耗记录功能
  - ExerciseRecord.kt: 运动记录数据模型
  - ExerciseType.kt: 27种运动类型枚举
  - ExerciseRecordDao.kt: 数据库访问
  - ExerciseRecordRepository.kt: 仓库层
  - ExerciseDialog.kt: 运动记录对话框
- [x] 图表组件库
  - LineChartView.kt: 折线图（趋势分析）
  - PieChartView.kt: 饼状图（营养素分布）
  - BarChartView.kt: 柱状图（餐次对比）
  - RadarChartView.kt: 雷达图（营养均衡）
  - ChartColors.kt: 预定义图表颜色
- [x] 拍照识别功能（多模态AI）
  - FoodImageAnalysisService.kt: 图片分析服务
  - PhotoAnalysisScreen.kt: 拍照分析页面
  - PhotoAnalysisViewModel.kt: 状态管理
  - 支持OpenAI/Claude/Kimi/GLM/Qwen/Gemini多协议
- [x] 扩展用餐类型
  - BREAKFAST_SNACK: 早加餐
  - LUNCH_SNACK: 午加餐
  - DINNER_SNACK: 晚加餐
- [x] 桌面小组件（多种尺寸）
  - CalorieWidget.kt: 小组件实现
  - CalorieWidgetReceiver.kt: 广播接收器
  - 支持小(2x1)/中(3x2)/大(4x3)三种尺寸
- [x] 引导教程系统
  - TutorialStep.kt: 教程步骤数据模型
  - TutorialOverlay.kt: 教程覆盖层UI
  - TutorialManager.kt: 教程管理服务
  - 9步新手引导流程
- [x] AI提供商图标
  - 从Deadliner项目复制图标资源
  - ic_openai.xml, ic_claude.xml, ic_glm.xml等
- [x] 年份更新为2026
  - AboutScreen.kt
  - StatsScreen.kt

#### 技术要点
1. **多模态API图片传输**: 使用base64编码，格式为 `data:image/jpeg;base64,{data}`
2. **AI聊天架构**: 悬浮窗 -> 迷你窗口 -> 全屏页面，支持过渡动画
3. **图表库**: 使用MPAndroidChart，支持折线图/饼状图/柱状图/雷达图
4. **运动消耗**: 27种运动类型，每种有对应的热量消耗系数（千卡/分钟）

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-03-13 | v3.1.2 | 界面结构调整：添加方式选择页面重构、个人信息页面调整、趋势分析优化 | AI Assistant |
| 2026-03-13 | v3.1.1 | 编译修复与优化：修复clickable导入、ExerciseType枚举、添加getAllRecordsOnce方法 | AI Assistant |
| 2026-03-13 | v3.1 | 运动与统计增强：统一趋势图表、运动数据统计、体重变化估算 | AI Assistant |
| 2026-03-13 | v3.0 | AI营养助手、运动消耗记录、桌面小组件、引导教程 | AI Assistant |
| 2026-03-12 | v2.0 | UI重构与Deadliner风格适配、数据统计页面重构 | AI Assistant |
| 2026-03-12 | v1.0 | 初始文档创建 | AI Assistant |
| 2026-03-12 | v1.1-v1.7 | 阶段三开发完成 | AI Assistant |
| 2026-03-13 | v2.0 | 阶段五高级功能完成 | AI Assistant |
| 2026-03-13 | v3.1 | 运动与统计增强 | AI Assistant |

---

**文档维护**: 每开发一个功能后必须更新本文档
**最后更新**: 2026-03-13

---

## 开发进度总结

### 已完成核心功能

#### 阶段一：项目搭建 ✅
- Android项目创建
- Gradle依赖配置
- MVVM架构搭建
- Material3主题配置

#### 阶段二：核心功能 ✅
- 数据库设计与实现
- 食物录入页面（文本/语音/拍照）
- 首页与记录列表
- 基础导航架构

#### 阶段三：UI重构与Deadliner风格适配 ✅
- 首页日期切换组件
- 顶部菜单弹窗
- 设置界面重构
- AI配置界面
- 主题颜色适配
- 动画与交互优化

#### 阶段四：数据统计页面重构 ✅
- 统计页面架构设计
- 概览统计页面
- 趋势分析页面
- 上月总结页面

#### 阶段五：高级功能 ✅
- AI聊天小窗口
- 运动消耗记录
- 图表组件库
- 拍照识别（多模态AI）
- 扩展用餐类型
- 桌面小组件
- 引导教程系统
- AI提供商图标

### 待开发功能
- OPPO流体云通知（需要开发者账号）
- AI图标生成（成本考虑）

---

**当前状态**: 核心功能全部完成，应用可正常使用
