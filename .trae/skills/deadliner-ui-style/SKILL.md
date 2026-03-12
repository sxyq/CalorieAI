---
name: "deadliner-ui-style"
description: "定义Deadliner App的Material3界面风格规范，包括颜色系统、组件样式、动画效果、布局结构等。在开发CalorieAI食物热量记录App时，必须遵循此风格规范以保持与Deadliner一致的视觉体验。"
---

# Deadliner UI 风格规范

基于 [Deadliner](https://github.com/AritxOnly/Deadliner) 开源项目的界面风格提取，用于指导 CalorieAI 食物热量记录App的UI开发。

## 1. 设计哲学

### 核心特征
- **Material3 Expressive**: 使用 Material3 Expressive 主题，强调动态色彩和流畅动画
- **简洁现代**: 干净利落的界面，减少视觉噪音
- **情感化设计**: 通过彩带动画、圆角、渐变等元素增添活力
- **一致性**: 全应用统一的视觉语言和交互模式

## 2. 颜色系统

### 2.1 主题基础
```kotlin
// 主题父类
parent="Theme.Material3Expressive.DayNight.NoActionBar"

// 动态颜色
- 支持 Android 12+ 动态取色 (Dynamic Colors)
- 支持自定义种子颜色 (Seed Color)
- 自动适配浅色/深色模式
```

### 2.2 核心颜色属性
| 颜色属性 | 用途 | 示例值 |
|---------|------|--------|
| `colorPrimary` | 主色调，进度条、按钮 | 动态生成 |
| `colorPrimaryContainer` | 主色容器背景 | 动态生成 |
| `colorSecondary` | 次色调，强调元素 | 动态生成 |
| `colorSecondaryContainer` | 次色容器背景 | 动态生成 |
| `colorSurface` | 页面背景 | 动态生成 |
| `colorSurfaceContainer` | 卡片容器背景 | 动态生成 |
| `colorSurfaceContainerHigh` | 高亮容器背景 | 动态生成 |
| `colorOnSurface` | 表面上的文字 | 动态生成 |
| `colorOutline` | 边框、分割线 | 动态生成 |

### 2.3 图表/状态颜色
```xml
<color name="chart_red">#FFF77E66</color>    <!-- 删除、错误、过期 -->
<color name="chart_green">#FF82ABA3</color>  <!-- 完成、成功 -->
<color name="chart_orange">#FFFEC37D</color> <!-- 警告、临近 -->
<color name="chart_blue">#FF97A5C0</color>   <!-- 信息、默认 -->
<color name="stared_yellow">#ffffe819</color> <!-- 收藏、星标 -->
```

## 3. 布局结构

### 3.1 首页布局 (MainActivity)
```
┌─────────────────────────────────────┐
│  TitleBar          [Search] [Set]   │  ← 顶部标题栏，圆形按钮
├─────────────────────────────────────┤
│  TabLayout (Deadline | Habit)       │  ← 标签切换
├─────────────────────────────────────┤
│                                     │
│  SwipeRefreshLayout + RecyclerView  │  ← 可刷新列表
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Item Card                  │   │  ← 圆角卡片
│  │  ├─ Title + Time + Star     │   │
│  │  ├─ Note (optional)         │   │
│  │  └─ Progress Bar            │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│         [BottomAppBar]              │  ← 底部应用栏
│              [+]                    │  ← 悬浮按钮 (FAB)
└─────────────────────────────────────┘
```

### 3.2 页面层级
- **主页面**: ConstraintLayout 根布局
- **列表**: SwipeRefreshLayout 包裹 RecyclerView
- **底部**: CoordinatorLayout + BottomAppBar + FAB
- **顶部**: 自定义标题栏（非系统ActionBar）

## 4. 组件规范

### 4.1 卡片 (Card)

#### 基础卡片样式
```xml
<!-- item_background.xml -->
<shape>
    <solid android:color="?attr/colorSurfaceContainerHigh" />
    <corners android:radius="24dp" />  <!-- 大圆角 -->
</shape>
```

#### Compose 卡片实现
```kotlin
val shape = RoundedCornerShape(24.dp)  // 统一圆角

Card(
    modifier = Modifier.fillMaxWidth(),
    shape = shape
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // 内容区域
    }
}
```

### 4.2 按钮

#### 圆形图标按钮
```xml
<ImageButton
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:padding="8dp"
    android:background="@drawable/circle_background_main"
    android:scaleType="centerInside" />
```

#### 悬浮操作按钮 (FAB)
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/ic_add"
    app:backgroundTint="?attr/colorSecondaryContainer"
    app:layout_anchor="@id/bottomAppBar" />
```

#### 主要按钮
```kotlin
MaterialButton(
    modifier = Modifier.height(54.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
)
```

### 4.3 输入框

#### Material3 输入框样式
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.FilledBox"
    app:boxCornerRadiusTopStart="12dp"
    app:boxCornerRadiusTopEnd="12dp"
    app:boxCornerRadiusBottomStart="12dp"
    app:boxCornerRadiusBottomEnd="12dp"
    app:boxStrokeWidth="0dp"
    app:boxStrokeWidthFocused="0dp"
    app:boxBackgroundColor="?attr/colorSurfaceContainerHighest">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>
```

### 4.4 进度条

#### 线性进度条
```kotlin
LinearProgressIndicator(
    progress = { progress },
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
)
```

#### 渐变进度条（高级）
```kotlin
Box(
    modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(progress)
        .background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    indicatorColor.copy(alpha = 0.4f), 
                    indicatorColor
                )
            )
        )
)
```

### 4.5 Tab 标签

```xml
<com.google.android.material.tabs.TabLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:tabIndicatorColor="?attr/colorPrimary"
    app:tabMode="fixed"
    app:tabTextColor="?attr/colorOnSurface"
    app:tabSelectedTextColor="?attr/colorPrimary" />
```

## 5. 动画效果

### 5.1 彩带动画 (Konfetti)

#### 使用场景
- 完成任务/目标时
- 收藏/加星时
- 达成里程碑时

#### 实现方式
```kotlin
// XML 布局
<nl.dionsegijn.konfetti.xml.KonfettiView
    android:id="@+id/konfettiView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

// Compose
KonfettiView(
    modifier = Modifier.fillMaxSize(),
    parties = PartyPresets.getPreset()  // 预定义效果
)
```

### 5.2 滑动手势动画

#### 滑动删除/完成
```kotlin
SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = true,  // 右滑：完成
    enableDismissFromEndToStart = true,  // 左滑：删除
    backgroundContent = {
        // 根据滑动方向显示不同背景色和图标
        // 绿色背景 + 勾选图标（右滑）
        // 红色背景 + 删除图标（左滑）
    }
)
```

### 5.3 页面过渡动画

```xml
<!-- res/anim/ -->
<translate android:fromYDelta="100%" android:toYDelta="0%" />  <!-- 从底部滑入 -->
<translate android:fromYDelta="0%" android:toYDelta="100%" />  <!-- 向底部滑出 -->
<alpha android:fromAlpha="0.0" android:toAlpha="1.0" />        <!-- 淡入 -->
```

### 5.4 内容变化动画

```kotlin
AnimatedContent(
    targetState = currentState,
    transitionSpec = {
        fadeIn() + slideInVertically() togetherWith 
        fadeOut() + slideOutVertically()
    }
) { state ->
    // 内容
}
```

## 6. 字体排版

### 6.1 字体家族
```xml
<!-- 使用 Lexend 字体 -->
<font-family xmlns:app="http://schemas.android.com/apk/res-auto"
    app:fontProviderAuthority="com.google.android.gms.fonts"
    app:fontProviderPackage="com.google.android.gms"
    app:fontProviderQuery="Lexend"
    app:fontProviderCerts="@array/com_google_android_gms_fonts_certs">
</font-family>
```

### 6.2 文字样式
| 样式 | 大小 | 用途 |
|-----|------|------|
| `titleLarge` | 24sp | 卡片标题 |
| `titleMedium` | 20sp | 页面标题 |
| `bodyLarge` | 16sp | 正文内容 |
| `bodySmall` | 14sp | 辅助文字、备注 |
| `labelMedium` | 12sp | 标签、时间 |

## 7. 图标规范

### 7.1 图标风格
- **风格**: 线性图标 (Outline)
- **描边**: 2dp
- **尺寸**: 24dp (标准), 36dp (大), 48dp (超大)
- **颜色**: 跟随主题 `?attr/colorOnSurface`

### 7.2 常用图标
```xml
ic_add        <!-- 添加 -->
ic_check      <!-- 完成 -->
ic_delete     <!-- 删除 -->
ic_edit       <!-- 编辑 -->
ic_star       <!-- 收藏/星标 (空心) -->
ic_star_filled <!-- 收藏/星标 (实心) -->
ic_search     <!-- 搜索 -->
ic_settings   <!-- 设置 -->
ic_back       <!-- 返回 -->
```

## 8. 间距系统

### 8.1 基础间距
| 名称 | 值 | 用途 |
|-----|---|------|
| `xs` | 4dp | 图标内边距 |
| `sm` | 8dp | 元素间小间距 |
| `md` | 12dp | 卡片内边距 |
| `lg` | 16dp | 页面边距 |
| `xl` | 24dp | 大间距、圆角半径 |
| `xxl` | 32dp | 超大间距 |

### 8.2 布局边距
```kotlin
// 页面边距
Modifier.padding(16.dp)

// 卡片内边距
Modifier.padding(horizontal = 12.dp, vertical = 12.dp)

// 列表项间距
Arrangement.spacedBy(8.dp)
```

## 9. 交互模式

### 9.1 手势操作
| 手势 | 动作 | 反馈 |
|-----|------|------|
| 右滑 | 标记完成/收藏 | 绿色背景 + 彩带动画 |
| 左滑 | 删除 | 红色背景 + 确认对话框 |
| 长按 | 进入多选模式 | 选中状态边框 |
| 点击 | 打开详情 | 页面跳转 |
| 下拉 | 刷新列表 | 刷新指示器 |

### 9.2 空状态
```kotlin
// 无数据时显示
Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Icon(
        imageVector = Icons.Default.Check,
        modifier = Modifier.size(36.dp)
    )
    Text("暂无记录", fontSize = 16.sp)
}
```

## 10. 适配规范

### 10.1 屏幕适配
- **手机**: 单列列表
- **平板**: 双列网格 (StaggeredGrid)
- **深色模式**: 自动适配

### 10.2 边缘到边缘 (Edge-to-Edge)
```kotlin
// 启用全屏显示
enableEdgeToEdgeForAllDevices()

// 处理系统栏内边距
WindowInsetsCompat.Type.systemBars()
```

## 11. 代码示例

### 11.1 完整卡片实现
```kotlin
@Composable
fun FoodRecordCard(
    foodName: String,
    calories: Int,
    mealType: String,
    recordTime: String,
    isStarred: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    
    SwipeToDismissBox(
        // ... 滑动配置
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable { onClick() },
            shape = shape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 标题行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = foodName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$calories 千卡",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isStarred) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_star_filled),
                            contentDescription = null
                        )
                    }
                }
                
                // 信息行
                Text(
                    text = "$mealType · $recordTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### 11.2 主题配置
```kotlin
@Composable
fun CalorieAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## 12. 开发检查清单

在实现UI时，请确认以下要点：

- [ ] 使用 Material3 Expressive 主题
- [ ] 卡片圆角为 24dp
- [ ] 按钮圆角为 12dp
- [ ] 支持动态颜色
- [ ] 实现右滑完成/收藏动画
- [ ] 实现左滑删除动画
- [ ] 添加彩带动画效果
- [ ] 支持深色模式
- [ ] 使用 Lexend 字体
- [ ] 遵循 8dp 网格系统
- [ ] 实现边缘到边缘显示
- [ ] 添加空状态提示

---

**参考项目**: [Deadliner](https://github.com/AritxOnly/Deadliner)  
**技术栈**: Kotlin + Jetpack Compose + Material3  
**最低API**: Android 12 (API 31)
