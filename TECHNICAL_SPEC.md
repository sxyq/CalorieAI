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
6. [开发日志](#开发日志)

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
    BREAKFAST,  // 早餐
    LUNCH,      // 午餐
    DINNER,     // 晚餐
    SNACK       // 加餐
}
```

### AIConfig（AI配置）
```kotlin
@Entity(tableName = "ai_configs")
data class AIConfig(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,               // 配置名称（自定义）
    val icon: String,               // 图标（emoji或资源名）
    val protocol: AIProtocol,       // 协议类型
    val apiUrl: String,             // API地址
    val apiKey: String,             // API密钥（加密存储）
    val modelId: String,            // 模型ID
    val isImageUnderstanding: Boolean, // 是否启用图像理解
    val isDefault: Boolean = false  // 是否为默认配置
)

enum class AIProtocol {
    OPENAI,     // OpenAI协议
    CLAUDE      // Claude协议
}
```

---

## UI组件

### 1. DateSelector（日期选择器）

**位置**: `ui/components/DateSelector.kt`

**功能描述**: 
- 显示前天/昨天/今天/明天的日期切换栏
- 支持左右滑动切换日期
- 丝滑动画过渡效果

**变量**:
```kotlin
// 状态
val selectedDate: StateFlow<LocalDate>    // 当前选中的日期
val dates: List<DateItem>                 // 显示的日期列表（前天、昨天、今天、明天）

// 动画
val animatedOffset: Animatable<Float, AnimationVector1D>  // 滑动偏移量
val animationSpec: TweenSpec<Float> = tween(300)          // 动画规格
```

**函数**:
```kotlin
// 组件
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)

// 日期项数据类
data class DateItem(
    val date: LocalDate,
    val label: String,      // "前天", "昨天", "今天", "明天"
    val isSelected: Boolean
)

// 处理滑动
fun handleSwipe(direction: SwipeDirection)

// 动画到指定日期
suspend fun animateToDate(date: LocalDate)
```

**实现描述**:
- 使用 `LazyRow` 水平排列日期项
- 使用 `Animatable` 实现滑动动画
- 当前选中的日期高亮显示
- 左右滑动时带动画效果

---

### 2. TopMenuPopup（顶部菜单弹窗）

**位置**: `ui/components/TopMenuPopup.kt`

**功能描述**:
- 右上角三个点按钮触发的菜单
- 包含：设置、概览、编辑资料
- 从右上角展开动画

**变量**:
```kotlin
// 状态
val isVisible: Boolean = false          // 是否显示
val expanded: MutableState<Boolean>     // 展开状态

// 菜单项
val menuItems: List<MenuItem> = listOf(
    MenuItem("设置", Icons.Default.Settings, Screen.Settings),
    MenuItem("概览", Icons.Default.BarChart, Screen.Overview),
    MenuItem("编辑资料", Icons.Default.Edit, Screen.EditProfile)
)
```

**函数**:
```kotlin
// 组件
@Composable
fun TopMenuButton(
    onMenuItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun TopMenuPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onMenuItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
)

// 菜单项数据类
data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val destination: Screen
)
```

**实现描述**:
- 使用 `DropdownMenu` 实现弹窗
- 添加进入/退出动画
- 菜单项使用卡片式布局
- 点击后导航到对应页面

---

### 3. SettingsScreen（设置界面）

**位置**: `ui/screens/settings/SettingsScreen.kt`

**功能描述**:
- 分组卡片式布局（参考Deadliner）
- 包含：界面外观、交互与行为、通知、备份、AI配置、关于

**变量**:
```kotlin
// ViewModel
val viewModel: SettingsViewModel = hiltViewModel()

// 设置分组
val settingGroups: List<SettingGroup> = listOf(
    SettingGroup("界面外观", Icons.Default.Palette, "主题、颜色、字体", Screen.Appearance),
    SettingGroup("交互与行为", Icons.Default.TouchApp, "反馈、后台行为", Screen.Interaction),
    SettingGroup("通知", Icons.Default.Notifications, "提醒时间配置", Screen.Notification),
    SettingGroup("备份", Icons.Default.Backup, "导入导出数据", Screen.Backup),
    SettingGroup("AI配置", Icons.Default.Psychology, "OpenAI/Claude设置", Screen.AISettings),
    SettingGroup("关于", Icons.Default.Info, "版本、隐私政策", Screen.About)
)
```

**函数**:
```kotlin
// 主界面
@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
)

// 设置分组项
@Composable
fun SettingGroupItem(
    group: SettingGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// 设置项数据类
data class SettingGroup(
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val destination: Screen
)
```

**实现描述**:
- 使用 `LazyColumn` 显示设置分组
- 每个分组使用 `Card` 组件，圆角24dp
- 左侧图标 + 标题 + 副标题 + 右侧箭头
- 点击后导航到子页面

---

### 4. AISettingsScreen（AI配置列表界面）

**位置**: `ui/screens/settings/AISettingsScreen.kt`

**功能描述**:
- AI配置列表页面
- 支持添加、编辑、删除多个AI配置
- 每个配置可自定义名称和图标

**变量**:
```kotlin
// ViewModel
val viewModel: AISettingsViewModel = hiltViewModel()

// 状态
val uiState: StateFlow<AISettingsUiState>

// UI状态数据类
data class AISettingsUiState(
    val configs: List<AIConfig> = emptyList(),      // AI配置列表
    val defaultConfigId: String? = null,            // 默认配置ID
    val isLoading: Boolean = true                   // 加载状态
)
```

**函数**:
```kotlin
// 配置列表页面
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String?) -> Unit,  // null表示添加新配置
    viewModel: AISettingsViewModel = hiltViewModel()
)

// 添加新配置按钮
@Composable
fun AddConfigButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// AI配置项
@Composable
fun AIConfigItem(
    config: AIConfig,
    isDefault: Boolean,
    onClick: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
)

// 空配置状态
@Composable
fun EmptyConfigState()
```

**实现描述**:
- 列表页面显示所有配置，每个配置显示图标、名称、协议类型、模型ID
- 可设置默认配置
- 支持删除配置
- 空状态显示提示信息

---

### 5. AIConfigDetailScreen（AI配置详情界面）

**位置**: `ui/screens/settings/AIConfigDetailScreen.kt`

**功能描述**:
- 添加/编辑AI配置详情页面
- 支持自定义名称、图标、协议、API地址、密钥、模型ID
- 测试连接功能
- 图像理解开关
- 预设配置快速选择

**变量**:
```kotlin
// 本地状态
var showIconSelector: Boolean = false       // 显示图标选择器
var showPresetSelector: Boolean = false     // 显示预设选择器
var apiKeyVisible: Boolean = false          // API密钥是否可见
```

**函数**:
```kotlin
// 配置详情页面
@Composable
fun AIConfigDetailScreen(
    configId: String?,                      // null表示添加新配置
    onNavigateBack: () -> Unit,
    viewModel: AIConfigDetailViewModel = hiltViewModel()
)

// 图标选择区域
@Composable
fun IconSelectorSection(
    selectedIcon: String,
    onClick: () -> Unit
)

// 协议选择器
@Composable
fun ProtocolSelector(
    selectedProtocol: AIProtocol,
    onProtocolSelected: (AIProtocol) -> Unit
)

// 协议选项
@Composable
fun ProtocolOption(
    protocol: AIProtocol,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// 测试连接按钮
@Composable
fun TestConnectionButton(
    isTesting: Boolean,
    testResult: TestResult?,
    onTest: () -> Unit,
    onClearResult: () -> Unit
)

// 图像理解开关卡片
@Composable
fun ImageUnderstandingCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
)

// 图标选择器弹窗
@Composable
fun IconSelectorDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
)

// 预设选择器弹窗
@Composable
fun PresetSelectorDialog(
    onPresetSelected: (AIConfig) -> Unit,
    onDismiss: () -> Unit
)

// 预设项
@Composable
fun PresetItem(
    icon: String,
    name: String,
    description: String,
    onClick: () -> Unit
)
```

**实现描述**:
- 图标选择区域：显示当前选中的emoji图标，点击弹出图标选择器
- 协议选择器：OpenAI/Claude两个选项，卡片式单选
- API密钥输入：支持显示/隐藏切换
- 测试连接：模拟测试，检查必填字段
- 图像理解开关：卡片式开关，带图标和描述
- 预设选择器：提供OpenAI GPT-4o和Claude 3.5 Sonnet预设
- 所有输入框初始留白，不预填任何内容

---

## Repository

### UserSettingsRepository（用户设置仓库）

**位置**: `data/repository/UserSettingsRepository.kt`

**功能描述**:
- 用户设置的增删改查操作

**函数**:
```kotlin
// 获取设置流
fun getSettings(): Flow<UserSettings?>

// 保存设置
suspend fun saveSettings(settings: UserSettings)

// 一次性获取设置
suspend fun getSettingsOnce(): UserSettings?
```

---

### AIConfigRepository（AI配置仓库）

**位置**: `data/repository/AIConfigRepository.kt`

**功能描述**:
- AI配置的增删改查操作
- 支持多配置管理

**函数**:
```kotlin
// 获取所有配置
fun getAllConfigs(): Flow<List<AIConfig>>

// 获取默认配置
fun getDefaultConfig(): Flow<AIConfig?>

// 根据ID获取配置
suspend fun getConfigById(id: String): AIConfig?

// 添加配置
suspend fun addConfig(config: AIConfig)

// 更新配置
suspend fun updateConfig(config: AIConfig)

// 删除配置
suspend fun deleteConfig(config: AIConfig)

// 根据ID删除配置
suspend fun deleteConfigById(id: String)

// 设置默认配置
suspend fun setDefaultConfig(id: String)
```

---

## DAO

### UserSettingsDao（用户设置DAO）

**位置**: `data/local/UserSettingsDao.kt`

**函数**:
```kotlin
@Query("SELECT * FROM user_settings WHERE id = 1")
fun getSettings(): Flow<UserSettings?>

@Query("SELECT * FROM user_settings WHERE id = 1")
suspend fun getSettingsSync(): UserSettings?

@Query("SELECT * FROM user_settings WHERE id = 1")
suspend fun getSettingsOnce(): UserSettings?

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertSettings(settings: UserSettings)

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertOrUpdate(settings: UserSettings)

@Update
suspend fun updateSettings(settings: UserSettings)

@Query("UPDATE user_settings SET dailyCalorieGoal = :goal WHERE id = 1")
suspend fun updateDailyGoal(goal: Int)

@Query("UPDATE user_settings SET isNotificationEnabled = :enabled WHERE id = 1")
suspend fun updateNotificationEnabled(enabled: Boolean)
```

---

## ViewModel

### HomeViewModel（首页ViewModel）

**位置**: `ui/screens/home/HomeViewModel.kt`

**变量**:
```kotlin
// 状态
private val _selectedDate = MutableStateFlow(LocalDate.now())
val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

private val _foodRecords = MutableStateFlow<List<FoodRecord>>(emptyList())
val foodRecords: StateFlow<List<FoodRecord>> = _foodRecords.asStateFlow()

private val _todayStats = MutableStateFlow<TodayStats>(TodayStats())
val todayStats: StateFlow<TodayStats> = _todayStats.asStateFlow()

// 依赖注入
@Inject
lateinit var foodRecordRepository: FoodRecordRepository

@Inject
lateinit var userSettingsRepository: UserSettingsRepository
```

**函数**:
```kotlin
// 选择日期
fun selectDate(date: LocalDate)

// 获取指定日期的记录
fun getFoodRecordsByDate(date: LocalDate)

// 计算今日统计
fun calculateTodayStats()

// 删除记录
fun deleteRecord(recordId: String)

// 切换收藏状态
fun toggleStar(recordId: String)
```

---

### AISettingsViewModel（AI设置ViewModel）

**位置**: `ui/screens/settings/AISettingsViewModel.kt`

**变量**:
```kotlin
// 状态
private val _aiConfigs = MutableStateFlow<List<AIConfig>>(emptyList())
val aiConfigs: StateFlow<List<AIConfig>> = _aiConfigs.asStateFlow()

private val _defaultConfig = MutableStateFlow<AIConfig?>(null)
val defaultConfig: StateFlow<AIConfig?> = _defaultConfig.asStateFlow()

// 依赖注入
@Inject
lateinit var aiConfigRepository: AIConfigRepository
```

**函数**:
```kotlin
// 加载所有配置
fun loadAIConfigs()

// 设置默认配置
fun setDefaultConfig(configId: String)

// 删除配置
fun deleteConfig(configId: String)
```

---

### AIConfigDetailViewModel（AI配置详情ViewModel）

**位置**: `ui/screens/settings/AIConfigDetailViewModel.kt`

**变量**:
```kotlin
// 状态
private val _uiState = MutableStateFlow(AIConfigDetailUiState())
val uiState: StateFlow<AIConfigDetailUiState> = _uiState.asStateFlow()

private var configId: String? = null  // 当前编辑的配置ID（null表示新增）

// UI状态数据类
data class AIConfigDetailUiState(
    val name: String = "",                          // 配置名称
    val selectedIcon: String = "🤖",                // 选中的图标
    val protocol: AIProtocol = AIProtocol.OPENAI,   // 协议类型
    val apiUrl: String = "",                        // API地址
    val apiKey: String = "",                        // API密钥
    val modelId: String = "",                       // 模型ID
    val isImageUnderstanding: Boolean = false,      // 是否启用图像理解
    val isEditing: Boolean = false,                 // 是否为编辑模式
    val isTesting: Boolean = false,                 // 是否正在测试连接
    val testResult: TestResult? = null,             // 测试结果
    val errorMessage: String? = null                // 错误信息
)

// 测试结果密封类
sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}
```

**函数**:
```kotlin
// 加载配置（编辑模式）
fun loadConfig(id: String?)

// 更新名称
fun updateName(name: String)

// 更新图标
fun updateIcon(icon: String)

// 更新协议
fun updateProtocol(protocol: AIProtocol)

// 更新API地址
fun updateApiUrl(url: String)

// 更新API密钥
fun updateApiKey(key: String)

// 更新模型ID
fun updateModelId(modelId: String)

// 更新图像理解开关
fun updateImageUnderstanding(enabled: Boolean)

// 测试连接
fun testConnection()

// 清除测试结果
fun clearTestResult()

// 保存配置
fun saveConfig(): Boolean

// 清除错误信息
fun clearError()

// 应用预设配置
fun applyPreset(preset: AIConfig)
```

---

## 工具类

### Animations（动画组件库）

**位置**: `ui/components/Animations.kt`

**功能描述**:
参考Deadliner风格实现的动画组件库，提供统一的动画效果和交互反馈。

**组件列表**:

#### 1. AnimatedListItem（列表项入场动画）
```kotlin
@Composable
fun AnimatedListItem(
    index: Int,                    // 列表项索引，用于计算延迟
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```
**动画效果**: 从下方滑入 + 淡入效果
**动画规格**:
- delay = index * 50ms (阶梯式延迟)
- offsetY: 50f -> 0f, duration = 400ms, easing = EaseOutCubic
- alpha: 0f -> 1f, duration = 300ms, easing = LinearEasing

#### 2. AnimatedContentSwitch（页面内容切换动画）
```kotlin
@Composable
fun <T> AnimatedContentSwitch(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
)
```
**动画效果**: 淡入 + 垂直滑动切换
**动画规格**:
- fadeIn: 300ms
- slideInVertically: 400ms, easing = EaseOutCubic, initialOffsetY = height / 10
- fadeOut: 200ms
- slideOutVertically: 300ms, easing = EaseInCubic, targetOffsetY = -height / 10

#### 3. AnimatedCard（卡片点击缩放动画）
```kotlin
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
)
```
**动画效果**: 点击时缩小再恢复，带弹簧效果
**动画规格**:
- scale: 1f -> 0.95f (100ms) -> 1f (spring)
- spring: dampingRatio = MediumBouncy, stiffness = Low

#### 4. AnimatedNumber（数字变化动画）
```kotlin
@Composable
fun AnimatedNumber(
    targetNumber: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
)
```
**动画效果**: 数字从0递增到目标值
**动画规格**:
- duration = 800ms
- easing = EaseOutCubic

#### 5. AnimatedProgressBar（进度条动画）
```kotlin
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
)
```
**动画效果**: 进度条平滑过渡
**动画规格**:
- duration = 1000ms
- easing = EaseOutCubic

#### 6. fadingTopEdge（顶部渐隐效果）
```kotlin
fun Modifier.fadingTopEdge(height: Dp = 32.dp): Modifier
```
**动画效果**: 列表顶部渐隐遮罩
**用途**: 用于LazyColumn，使顶部内容有渐隐效果

---

### StatsUtils（统计工具类）

**位置**: `utils/StatsUtils.kt`

**功能描述**:
参考Deadliner的OverviewUtils实现，提供各种数据统计计算功能。

**函数列表**:

#### 1. computeTodayStats（计算今日统计数据）
```kotlin
fun computeTodayStats(
    records: List<FoodRecord>,
    targetCalories: Int
): TodayStats
```
**返回数据**:
- date: LocalDate - 日期
- totalCalories: Int - 今日总摄入
- targetCalories: Int - 目标热量
- remainingCalories: Int - 剩余可摄入
- isTargetMet: Boolean - 是否达标
- recordCount: Int - 记录数量

#### 2. computeMealTypeStats（计算各餐次摄入统计）
```kotlin
fun computeMealTypeStats(records: List<FoodRecord>): Map<MealType, Int>
```
**返回数据**: 各餐次（早餐/午餐/晚餐/加餐）的热量统计

#### 3. computeHistoryStats（计算历史摄入统计）
```kotlin
fun computeHistoryStats(records: List<FoodRecord>): HistoryStats
```
**返回数据**:
- totalDays: Int - 总记录天数
- targetMetDays: Int - 达标天数
- overTargetDays: Int - 超标天数

#### 4. computeWeeklyTrend（计算周趋势数据）
```kotlin
fun computeWeeklyTrend(
    records: List<FoodRecord>,
    weeks: Int = 4
): List<WeeklyStat>
```
**返回数据**:
- weekStart: LocalDate - 周开始日期
- weekEnd: LocalDate - 周结束日期
- avgCalories: Int - 平均每日摄入
- totalCalories: Int - 周总摄入
- daysRecorded: Int - 记录天数

#### 5. computeMonthlyTrend（计算月度趋势数据）
```kotlin
fun computeMonthlyTrend(
    records: List<FoodRecord>,
    months: Int = 6
): List<MonthlyStat>
```
**返回数据**:
- month: String - 月份（yyyy-MM格式）
- totalCalories: Int - 月总摄入
- avgDailyCalories: Int - 平均每日摄入
- daysRecorded: Int - 记录天数

#### 6. computeLastMonthSummary（计算上月总结）
```kotlin
fun computeLastMonthSummary(records: List<FoodRecord>): MonthSummary
```
**返回数据**:
- year/month: Int - 年月
- totalCalories: Int - 总摄入
- avgDailyCalories: Int - 平均每日摄入
- maxDailyCalories: Int - 最高单日摄入
- targetMetDays: Int - 达标天数
- overTargetDays: Int - 超标天数
- breakfastTotal/lunchTotal/dinnerTotal/snackTotal: Int - 各餐次总计
- totalRecords: Int - 记录总数

#### 7. computeStreakDays（计算连续记录天数）
```kotlin
fun computeStreakDays(records: List<FoodRecord>): Int
```
**返回数据**: 连续记录天数（从今天往前数）

---

### DateUtils（日期工具）

**位置**: `utils/DateUtils.kt`

**函数**:
```kotlin
// 获取相对日期标签
fun getRelativeDateLabel(date: LocalDate): String
// 返回：前天、昨天、今天、明天、后天等

// 获取日期范围fun getDateRange(start: LocalDate, end: LocalDate): List<LocalDate>

// 格式化日期
fun formatDate(date: LocalDate, pattern: String = "yyyy-MM-dd"): String
```

---

## 开发日志

### 2026-03-12 - 阶段三开发开始

#### 已完成功能
- [x] Git初始提交
- [x] 技术文档创建
- [x] 日期工具类 (DateUtils.kt)
  - `getRelativeDateLabel()` - 获取相对日期标签（前天/昨天/今天/明天）
  - `getWeekDayLabel()` - 获取星期标签
  - `getDateRange()` - 获取日期范围
  - `formatDate()` - 格式化日期
- [x] 日期选择器组件 (DateSelector.kt)
  - 支持前天/昨天/今天/明天切换
  - 左右滑动切换日期
  - 丝滑动画效果
  - 选中状态高亮显示
  - **修复**: 添加 `@OptIn(ExperimentalAnimationApi::class)` 注解
- [x] 顶部菜单按钮 (TopMenuButton.kt)
  - 三个点按钮触发
  - 弹出菜单（设置、概览、编辑资料）
  - 卡片式布局，圆角24dp
  - 欢迎语 + 关闭按钮
- [x] 修复编译错误
  - DateSelector: 添加实验性动画API注解
  - HomeScreen: 修复 MenuScreen 和 Screen 枚举类型不匹配
- [x] 设置界面重构 (SettingsScreen.kt)
  - 参考Deadliner风格：卡片式布局，圆角24dp
  - 图标+标题+副标题+箭头
  - 分组：界面外观、交互与行为、通知、备份、AI配置、关于
  - SettingGroupItem组件复用
- [x] SettingsViewModel更新
  - 添加MealReminderType枚举
  - 更新每日热量目标
  - 更新通知开关
  - 更新提醒时间
- [x] AI配置界面 - 列表页 (AISettingsScreen.kt)
  - AIConfig数据模型（支持多配置）
  - AIConfigDao数据库访问
  - AIConfigRepository仓库层
  - AISettingsViewModel（加载配置列表、设置默认、删除）
  - AISettingsScreen界面（列表展示）
  - AddConfigButton组件
  - AIConfigItem组件（显示图标、名称、协议、模型ID）
  - EmptyConfigState空状态
  - 支持设为默认、删除操作

#### 已完成功能（续）
- [x] AI配置详情页（添加/编辑）
  - AIConfigDetailViewModel（加载配置、更新字段、测试连接、保存配置）
  - AIConfigDetailScreen（表单界面）
  - IconSelectorSection（图标选择区域）
  - ProtocolSelector（协议选择器）
  - TestConnectionButton（测试连接按钮）
  - ImageUnderstandingCard（图像理解开关卡片）
  - IconSelectorDialog（图标选择弹窗）
  - PresetSelectorDialog（预设选择弹窗）
  - 支持OpenAI/Claude两种协议格式
  - 预设配置：OpenAI GPT-4o、Claude 3.5 Sonnet
- [x] 导航图更新
  - 添加AISettings路由
  - 添加AIConfigDetail路由（支持可选configId参数）
  - SettingsScreen添加onNavigateToAISettings回调
- [x] Bug修复
  - 创建UserSettingsRepository（之前缺失导致KSP错误）
  - 更新UserSettingsDao（添加insertOrUpdate和getSettingsOnce方法）
  - 修复AIConfigDetailViewModel方法调用错误（insertConfig -> addConfig）
  - 添加getMealTypeName函数到FoodRecord.kt，修复AddFoodScreen编译错误
  - 修复MealTypeSelector函数名冲突（重命名为ManualMealTypeSelector）
  - 修复ManualAddViewModel方法调用错误（insertRecord -> addRecord）
  - 修复AISettingsScreen when表达式缺少分支（添加KIMI/GLM/QWEN/DEEPSEEK/GEMINI）
  - 修复DateSelector警告（移除未使用变量，更新动画API）
  - 修复ManualAddScreen警告（移除未使用变量）
  - 修复HomeScreen生命周期观察者内存泄漏（使用DisposableEffect）

#### 设置界面详情页开发
- [x] 界面外观设置页面（AppearanceSettingsScreen）
  - 主题选择（浅色/深色/跟随系统）
  - 主界面风格开关
  - 分割线留白设计开关
  - 字体大小选择（小/中/大）
  - 界面动画开关
  - AppearanceSettingsViewModel
  - UserSettings数据模型更新（添加themeMode等字段）
  - 导航路由添加
- [x] 交互与行为设置页面（InteractionSettingsScreen）
  - 操作反馈类型选择（无/仅振动/仅声音/振动和声音）
  - 振动反馈开关
  - 声音反馈开关
  - 后台行为选择（标准/保持运行/省电模式）
  - 启动页面选择（首页/统计/添加）
  - 快速添加开关
  - InteractionSettingsViewModel
  - UserSettings数据模型更新（添加feedbackType等字段）
  - 导航路由添加
- [x] 通知设置页面（NotificationSettingsScreen）
  - 总开关（启用通知）- 主卡片样式
  - 提醒时间设置（早餐/午餐/晚餐）- 时间选择器
  - 摄入目标提醒开关
  - 连续记录提醒开关
  - NotificationSettingsViewModel
  - UserSettings数据模型更新（添加enableGoalReminder等字段）
  - 导航路由添加
- [x] 共享设置组件（SettingsComponents.kt）
  - SettingsSection（分组卡片，支持蓝色标题）
  - SettingsSwitchItem（开关项，支持主开关样式）
  - SettingsSectionDivider（分割线）
- [x] 备份设置页面（BackupSettingsScreen）
  - 数据备份说明卡片
  - 导出数据按钮
  - 导入数据按钮
  - 自动备份开关（带上次备份时间显示）
  - 云同步开关（带立即同步/从云端恢复按钮）
  - BackupSettingsViewModel
  - UserSettings数据模型更新（添加enableAutoBackup等字段）
  - 导航路由添加
- [x] 关于页面（AboutScreen）
  - 顶部应用信息卡片（图标+名称+标语）
  - 版本信息（版本号、构建时间）
  - 法律信息（开源许可证、隐私政策）
  - 更多链接（项目主页、反馈问题）
  - 底部版权信息
  - 导航路由添加

#### 已完成
- [x] 界面外观设置页面
- [x] 交互与行为设置页面
- [x] 通知设置页面
- [x] 备份设置页面
- [x] 关于页面
- [x] AI配置管理
- [x] 动画与交互优化（Animations.kt）
  - AnimatedListItem（列表项入场动画）
  - AnimatedContentSwitch（页面内容切换动画）
  - AnimatedCard（卡片点击缩放动画）
  - AnimatedNumber（数字变化动画）
  - AnimatedProgressBar（进度条动画）
  - fadingTopEdge（顶部渐隐效果）
- [x] 数据统计页面重构（StatsScreen.kt）
  - 三标签设计（概览统计/趋势分析/上月总结）
  - 今日摄入状态统计卡片
  - 各餐次摄入统计卡片（条形图）
  - 历史摄入统计卡片
  - 连续记录天数卡片
  - 周趋势卡片
  - 月度趋势卡片
  - 上月总结头部卡片
  - 统计指标网格（双列布局）
  - StatsUtils（数据统计工具类）
  - StatsViewModel

#### 已完成（2026-03-12更新）
- [x] 主题颜色适配（蓝色主色调）
  - Color.kt: 完整的Material3蓝色系配色方案
  - Theme.kt: 浅色/深色主题配色方案
  - 状态栏适配（跟随主题白色/黑色）
  - 表面容器色（surfaceContainer系列）
- [x] 语音输入功能优化
  - VoiceInputDialog: 带录音动画的对话框
  - VoiceRecordingAnimation: 脉冲波纹+波形动画
  - VoiceWaveform: 语音波形动画
  - VoiceInputButton: 语音输入按钮组件
  - AddFoodScreen集成语音输入对话框

#### 待开发
- [ ] 拍照识别优化（多模态大模型）
- [ ] 滑动手势与动画（Konfetti彩带）

---

### StatsViewModel（统计页面ViewModel）

**位置**: `ui/screens/stats/StatsViewModel.kt`

**功能描述**:
统计页面的ViewModel，管理统计数据的状态和计算。

**变量**:
```kotlin
// 状态
private val _uiState = MutableStateFlow(StatsUiState())
val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

// UI状态数据类
data class StatsUiState(
    val todayStats: TodayStats? = null,
    val mealTypeStats: Map<MealType, Int> = emptyMap(),
    val historyStats: HistoryStats? = null,
    val weeklyStats: List<WeeklyStat> = emptyList(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val lastMonthSummary: MonthSummary? = null,
    val streakDays: Int = 0,
    val isLoading: Boolean = true
)

// 依赖注入
@Inject
lateinit var foodRecordRepository: FoodRecordRepository

@Inject
lateinit var userSettingsRepository: UserSettingsRepository
```

**函数**:
```kotlin
// 加载统计数据
private fun loadStats()

// 重新加载（供外部调用刷新）
fun refreshStats()
```

---

### StatsScreen（统计页面）

**位置**: `ui/screens/stats/StatsScreen.kt`

**功能描述**:
参考Deadliner风格的三标签统计页面，包含概览统计、趋势分析、上月总结三个标签页。

**页面结构**:
```
StatsScreen (Scaffold)
├── CenterAlignedTopAppBar (顶部标题栏)
│   ├── 返回按钮
│   ├── 标题 "概览"
│   └── 设置按钮 (右侧)
├── PrimaryTabRow (标签导航栏)
│   ├── Tab 0: 概览统计 (ic_analytics)
│   ├── Tab 1: 趋势分析 (ic_monitor)
│   └── Tab 2: 上月总结 (ic_dashboard)
└── AnimatedContent (内容区域)
    ├── 0 -> OverviewStatsContent (概览统计)
    ├── 1 -> TrendAnalysisContent (趋势分析)
    └── 2 -> MonthlySummaryContent (上月总结)
```

**组件列表**:

#### OverviewStatsContent（概览统计内容）
使用LazyColumn展示4个统计卡片：
- TodayStatsCard: 今日摄入状态统计（三列布局：已摄入/剩余/目标）
- MealTypeStatsCard: 各餐次摄入统计（条形图）
- HistoryStatsCard: 历史摄入统计（三列布局：达标/超标/记录天数）
- StreakCard: 连续记录天数（主色调背景卡片）

#### TrendAnalysisContent（趋势分析内容）
使用Column+verticalScroll展示2个趋势卡片：
- WeeklyTrendCard: 周摄入趋势（列表展示最近4周数据）
- MonthlyTrendCard: 月度趋势（列表展示最近6个月数据）

#### MonthlySummaryContent（上月总结内容）
使用Column+verticalScroll展示：
- SummaryHeaderCard: 顶部大图卡片（显示年月+"上月总结"）
- SummaryMetricsGrid: 统计指标网格（双列布局，10个指标）

**样式规范**:
- 卡片圆角: 24.dp
- 卡片内边距: 20.dp
- 卡片间距: 8.dp
- 统计项颜色: 达标-绿色(#FF82ABA3), 超标-红色(#FFF77E66)
- 使用AnimatedListItem实现入场动画

---

### VoiceInputDialog（语音输入对话框）

**位置**: `ui/components/VoiceInputDialog.kt`

**功能描述**:
语音输入对话框组件，包含录音动画、波形效果和实时状态显示。

**组件**:

#### 1. VoiceInputDialog（主对话框）
```kotlin
@Composable
fun VoiceInputDialog(
    isVisible: Boolean,
    voiceState: VoiceState,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit
)
```
**功能**:
- 显示录音状态对话框
- 根据VoiceState显示不同状态（准备/聆听/处理/成功/错误）
- 停止录音按钮
- 权限错误提示

#### 2. VoiceRecordingAnimation（录音动画）
```kotlin
@Composable
private fun VoiceRecordingAnimation(voiceState: VoiceState)
```
**动画效果**:
- 外圈脉冲波纹（录音时）
- 中间圈颜色变化（根据状态）
- 内圈图标（麦克风/进度指示器）
- 底部波形动画（录音时）

**状态颜色**:
- 聆听中: primary颜色
- 处理中: secondary颜色
- 错误: error颜色

#### 3. VoiceWaveform（语音波形）
```kotlin
@Composable
private fun VoiceWaveform(modifier: Modifier = Modifier)
```
**动画效果**:
- 5个条形波浪动画
- 不同延迟创建波浪效果
- duration: 400-800ms
- easing: EaseInOutCubic

#### 4. VoiceInputButton（语音输入按钮）
```kotlin
@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```
**功能**:
- 48dp圆形按钮
- 录音状态颜色变化
- 图标切换（麦克风/停止）

---

### VoiceInputHelper（语音输入帮助类）

**位置**: `service/voice/VoiceInputHelper.kt`

**功能描述**:
封装Android SpeechRecognizer，提供语音转文字功能。

**变量**:
```kotlin
private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()
```

**函数**:
```kotlin
// 开始录音
fun startListening(
    context: Context,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
)

// 停止录音
fun stopListening()

// 释放资源
fun destroy()
```

**VoiceState状态**:
```kotlin
sealed class VoiceState {
    object Idle : VoiceState()                    // 空闲
    object Listening : VoiceState()               // 聆听中
    object Processing : VoiceState()              // 处理中
    data class Partial(val text: String) : VoiceState()  // 部分结果
    data class Success(val text: String) : VoiceState()  // 识别成功
    data class Error(val message: String) : VoiceState() // 错误
}
```

**错误类型**:
- ERROR_AUDIO: 音频错误
- ERROR_CLIENT: 客户端错误
- ERROR_INSUFFICIENT_PERMISSIONS: 权限不足
- ERROR_NETWORK: 网络错误
- ERROR_NO_MATCH: 未能识别
- ERROR_RECOGNIZER_BUSY: 识别器繁忙
- ERROR_SERVER: 服务器错误
- ERROR_SPEECH_TIMEOUT: 说话超时

---

## 导航结构

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddFood : Screen("add_food")
    object Camera : Screen("camera")
    object Result : Screen("result/{recordId}")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object AISettings : Screen("ai_settings")
    object AIConfigDetail : Screen("ai_config_detail?configId={configId}")
}
```

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-03-12 | v1.0 | 初始文档创建 | AI Assistant |
| 2026-03-12 | v1.1 | 完成日期选择器和顶部菜单 | AI Assistant |
| 2026-03-12 | v1.2 | 修复编译错误 | AI Assistant |
| 2026-03-12 | v1.3 | 完成AI配置详情页 | AI Assistant |
| 2026-03-12 | v1.4 | 完成动画与交互优化 | AI Assistant |
| 2026-03-12 | v1.5 | 完成数据统计页面重构 | AI Assistant |
| 2026-03-12 | v1.6 | 完成主题颜色适配和语音输入优化 | AI Assistant |

---

**文档维护**: 每开发一个功能后必须更新本文档
**最后更新**: 2026-03-12

---

## 开发进度总结

### 已完成核心功能

#### 阶段一：项目搭建 ✅
- Android项目创建
- Gradle依赖配置
- MVVM架构搭建
- Material3主题配置
- Room数据库实现
- Hilt依赖注入

#### 阶段二：核心功能 ✅
- 数据库设计与实现
- 食物录入页面（文本/语音/拍照）
- 首页与记录列表
- 热量记录详情展示
- 基础导航架构
- 手动输入热量数据
- ML Kit文字识别

#### 阶段三：UI重构与Deadliner风格适配 (100%) ✅
- 首页日期切换组件（前天/昨天/今天/明天）
- 顶部菜单弹窗（设置/概览/编辑资料）
- 设置界面重构（参考Deadliner卡片式布局）
  - 界面外观设置
  - 交互与行为设置
  - 通知设置
  - 备份设置
  - 关于页面
- AI配置界面（支持OpenAI/Claude多配置）
- **主题颜色适配** ✅
  - 蓝色主色调（Material Blue 500）
  - 完整的浅色/深色主题配色方案
  - 状态栏跟随主题（白色/黑色）
  - 表面容器色（surfaceContainer系列）
- **动画与交互优化** ✅
  - Animations.kt（6个动画组件）
  - 列表项入场动画
  - 页面内容切换动画
  - 卡片点击缩放动画
  - 数字变化动画
  - 进度条动画
  - 顶部渐隐效果

#### 阶段四：数据统计页面重构 ✅
- **StatsScreen.kt** - 三标签统计页面
- **StatsViewModel.kt** - 统计数据管理
- **StatsUtils.kt** - 统计计算工具类
- 概览统计（今日/历史/连续记录）
- 趋势分析（周趋势/月度趋势）
- 上月总结（瀑布流卡片布局）

#### 阶段五：高级功能（进行中）
- **语音输入优化** ✅
  - VoiceInputDialog: 带录音动画的对话框
  - VoiceRecordingAnimation: 脉冲波纹+波形动画
  - VoiceWaveform: 语音波形动画效果
  - 实时状态显示（聆听中/处理中/成功/错误）
  - 权限处理流程

### 待开发功能

#### 高级功能（阶段五剩余）
- 拍照识别优化（多模态大模型）
- 滑动手势与动画（Konfetti彩带）
- OPPO流体云通知
- 桌面小组件
- 数据备份与恢复
