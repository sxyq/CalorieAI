# CalorieAI v1.0 - 智能食物热量记录应用

<br />

## 📱 应用简介

CalorieAI 是一款基于 Material3 + Liquid Glass 设计语言的智能食物热量记录应用，融合现代化AI技术与流畅的用户体验，帮助用户轻松管理每日饮食热量摄入和整体健康。

**版本**: v1.0

**包名**: com.calorieai.app

***

## ✨ 核心功能

### 🤖 AI智能食物识别

- **文本输入识别**: 简单描述食物，AI自动分析13种营养素
- **拍照识别**: 多模态AI识别食物照片的营养成分
- **语音输入**: 支持语音转文字记录
- **AI重试机制**: 解析失败自动重试，带进度提示
- **13种营养素分析**: 蛋白质、碳水、脂肪、纤维、糖、钠、胆固醇、饱和脂肪、钙、铁、维生素A/C、钾

### 📊 营养素追踪

- **个性化参考值**: 基于体重、性别、年龄、活动水平动态计算
- **摄入进度可视化**: 13种营养素进度条展示
- **营养素状态**: 低/中/高摄入状态提示

### 🍽️ 菜谱与食谱

- **收藏菜谱**: 保存喜爱的菜谱，支持营养成分存储
- **智能菜谱推荐**: 根据库存食材和饮食偏好推荐菜谱
- **烹饪指南**: 标准化菜谱步骤指导
- **餐食计划**: 多日餐食规划与营养平衡

### 🏃 运动消耗记录

- **27种运动类型**: 跑步、游泳、瑜伽、HIIT、力量训练等
- **热量消耗计算**: 根据运动时长自动计算
- **运动记录管理**: 独立页面管理历史记录

### 💧 饮水追踪

- **饮水记录**: 快捷添加不同容量饮水记录
- **饮水目标**: 个性化每日饮水目标
- **饮水提醒**: 定时提醒保持水分摄入
- **饮水历史**: 日历视图查看饮水记录

### 📈 数据统计与可视化

- **今日概览**: 饼状图展示营养素分布
- **趋势分析**: 三个独立折线图（摄入/运动/体重）
- **上月总结**: 详细统计报告（瀑布流卡片）
- **BMR/TDEE**: 基础代谢率和每日总消耗展示

### 🎨 UI/UX特性

- **Liquid Glass**: iOS 26 风格液态玻璃设计系统
- **动态模糊**: 设备自适应模糊效果
- **自定义主题**: 支持壁纸、颜色、字体设置
- **流畅动画**: 页面切换、列表入场、卡片交互动画
- **桌面小组件**: 6种尺寸支持（2x1/3x2/4x3/2x2/3x1/4x1）
- **快捷手势**: 长按底栏快速操作

### 💾 数据管理

- **本地存储**: Room数据库持久化
- **WebDAV云备份**: 支持坚果云、Nextcloud等WebDAV服务
- **智能恢复**: 全量覆盖/合并导入双模式
- **恢复预览**: 恢复前查看数据影响范围
- **体重追踪**: 独立体重记录与趋势

### 🤖 AI助手

- **悬浮窗设计**: 迷你窗口快速对话
- **营养咨询**: 热量评估、菜谱规划、健康建议
- **对话历史**: 保存和管理历史对话
- **Token统计**: 调用次数和成本追踪
- **模型支持**: OpenAI/Claude/Kimi/GLM/Qwen/DeepSeek/Gemini

### ⚙️ 设置与配置

- **AI服务配置**: 支持多种AI服务商配置
- **外观设置**: 主题、字体、壁纸、动画、液态玻璃效果开关
- **通知设置**: 餐次提醒、饮水提醒、目标提醒
- **交互设置**: 手势开关、快速添加、动画效果
- **个人信息**: BMR/TDEE计算、热量目标

***

## 🏗️ 架构设计

### 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Screens   │  │  Components  │  │    Theme     │      │
│  │  (Compose)   │  │  (Compose)   │  │ (Material3)  │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
├─────────┼─────────────────┼─────────────────┼──────────────┤
│         │                 │                 │              │
│         ▼                 ▼                 ▼              │
│  ┌──────────────────────────────────────────────────┐     │
│  │                ViewModel Layer                    │     │
│  │  (StateFlow + Flow + Coroutines)                  │     │
│  └──────────────────────┬───────────────────────────┘     │
├─────────────────────────┼──────────────────────────────────┤
│                         │                                  │
│                         ▼                                  │
│  ┌──────────────────────────────────────────────────┐     │
│  │              Repository Layer                     │     │
│  │  (Repository Pattern + DataSource abstraction)    │     │
│  └──────────────────────┬───────────────────────────┘     │
├─────────────────────────┼──────────────────────────────────┤
│                         │                                  │
│                         ▼                                  │
│  ┌──────────────────────────────────────────────────┐     │
│  │                Data Layer                         │     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐         │     │
│  │  │   Room   │ │  Retrofit│ │  DataStore│         │     │
│  │  │ (Local)  │ │ (Remote) │ │ (Prefs)   │         │     │
│  │  └──────────┘ └──────────┘ └──────────┘         │     │
│  └──────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### 模块职责

| 模块          | 职责                  | 关键文件                                                                         |
| ----------- | ------------------- | ---------------------------------------------------------------------------- |
| **Data**    | 数据模型、DAO、Repository | `data/model/`, `data/local/`, `data/repository/`                             |
| **DI**      | 依赖注入配置              | `di/AppModule.kt`, `di/DatabaseModule.kt`                                    |
| **Service** | 业务服务                | `service/ai/`, `service/backup/`, `service/notification/`, `service/widget/` |
| **UI**      | 界面实现                | `ui/screens/`, `ui/components/`, `ui/theme/`, `ui/animation/`                |
| **Utils**   | 工具类                 | `utils/DateUtils.kt`, `utils/StatsUtils.kt`                                  |

***

## 🛠️ 技术栈

- **语言**: Kotlin 1.9+
- **UI框架**: Jetpack Compose + Material3
- **架构**: MVVM + Repository模式
- **依赖注入**: Hilt
- **数据库**: Room (版本 20)
- **异步处理**: Kotlin Coroutines + Flow
- **图表库**: MPAndroidChart
- **网络请求**: Retrofit + OkHttp
- **图片加载**: Coil
- **后台任务**: WorkManager

***

## 📁 项目结构

```
app/src/main/java/com/calorieai/app/
├── data/
│   ├── local/              # Room数据库 DAOs
│   │   ├── AppDatabase.kt
│   │   ├── UserSettingsDao.kt
│   │   ├── FoodRecordDao.kt
│   │   ├── ExerciseRecordDao.kt
│   │   ├── WaterRecordDao.kt
│   │   ├── WeightRecordDao.kt (dao/)
│   │   ├── AIChatHistoryDao.kt
│   │   ├── AIConfigDao.kt
│   │   ├── FavoriteRecipeDao.kt
│   │   ├── PantryIngredientDao.kt
│   │   ├── RecipeGuideDao.kt
│   │   ├── RecipePlanDao.kt
│   │   ├── APICallRecordDao.kt
│   │   └── OnboardingDataStore.kt
│   ├── model/              # 数据实体模型
│   │   ├── UserSettings.kt
│   │   ├── FoodRecord.kt
│   │   ├── ExerciseRecord.kt
│   │   ├── WaterRecord.kt
│   │   ├── WeightRecord.kt
│   │   ├── AIChatHistory.kt
│   │   ├── AIConfig.kt
│   │   ├── AIFunctionConfig.kt
│   │   ├── AITokenUsage.kt
│   │   ├── APICallRecord.kt
│   │   ├── FavoriteRecipe.kt
│   │   ├── PantryIngredient.kt
│   │   ├── RecipeGuide.kt
│   │   ├── RecipePlan.kt
│   │   ├── MealPlanCache.kt
│   │   └── FoodAnalysisResult.kt
│   └── repository/         # 仓库层
│       ├── UserSettingsRepository.kt
│       ├── FoodRecordRepository.kt
│       ├── ExerciseRecordRepository.kt
│       ├── WaterRecordRepository.kt
│       ├── WeightRecordRepository.kt
│       ├── AIChatHistoryRepository.kt
│       ├── AIConfigRepository.kt
│       ├── FavoriteRecipeRepository.kt
│       ├── PantryIngredientRepository.kt
│       ├── RecipeGuideRepository.kt
│       ├── RecipePlanRepository.kt
│       └── APICallRecordRepository.kt
├── di/                     # 依赖注入
│   ├── AppModule.kt
│   └── DatabaseModule.kt
├── service/
│   ├── ai/                 # AI服务
│   │   ├── AIChatService.kt
│   │   ├── AIContextService.kt
│   │   ├── AIRateLimiter.kt
│   │   ├── AIDefaultConfigInitializer.kt
│   │   ├── FoodImageAnalysisService.kt
│   │   ├── FoodTextAnalysisService.kt
│   │   ├── MealPlanService.kt
│   │   ├── NutritionRecognitionService.kt
│   │   └── common/AIApiClient.kt
│   ├── backup/             # 备份服务
│   │   ├── BackupService.kt
│   │   └── WebDavBackupService.kt
│   ├── notification/       # 通知服务
│   │   ├── NotificationHelper.kt
│   │   ├── NotificationScheduler.kt
│   │   ├── MealReminderWorker.kt
│   │   ├── PantryExpiryReminderScheduler.kt
│   │   └── PantryExpiryReminderWorker.kt
│   ├── widget/             # 桌面小组件
│   │   ├── BaseCalorieWidget.kt
│   │   ├── CalorieWidget.kt
│   │   ├── CalorieWidgetSizes.kt
│   │   └── WidgetDataProvider.kt
│   ├── tutorial/           # 新手引导
│   │   └── TutorialManager.kt
│   └── voice/              # 语音输入
│       └── VoiceInputHelper.kt
├── ui/
│   ├── animation/          # 动画定义
│   │   └── AnimationEasing.kt
│   ├── animations/         # 交互动画
│   │   ├── CardAnimations.kt
│   │   ├── ListAnimations.kt
│   │   └── NavigationAnimations.kt
│   ├── components/         # 可复用组件
│   │   ├── AIChatWidget.kt
│   │   ├── BottomNavBar.kt
│   │   ├── CalendarView.kt
│   │   ├── DateSelector.kt
│   │   ├── ExerciseDialog.kt
│   │   ├── HeatmapCalendar.kt
│   │   ├── LiquidGlassComponents.kt
│   │   ├── LoadingComponents.kt
│   │   ├── MonthlySummaryCard.kt
│   │   ├── TokenUsageCard.kt
│   │   ├── TutorialOverlay.kt
│   │   ├── VoiceInputDialog.kt
│   │   ├── WaterProgressCard.kt
│   │   ├── charts/         # 图表组件
│   │   ├── ai/             # AI相关组件
│   │   ├── chat/           # 聊天组件
│   │   └── markdown/       # Markdown渲染
│   ├── navigation/         # 导航
│   │   ├── NavGraph.kt
│   │   └── BottomNavBehaviorViewModel.kt
│   ├── screens/            # 页面
│   │   ├── add/            # 添加记录
│   │   ├── ai/             # AI助手
│   │   ├── camera/         # 相机
│   │   ├── exercise/       # 运动记录
│   │   ├── functions/      # 功能页
│   │   ├── home/           # 首页
│   │   ├── onboarding/     # 引导页
│   │   ├── overview/       # 概览
│   │   ├── profile/        # 个人资料
│   │   ├── result/         # 结果页
│   │   ├── settings/       # 设置
│   │   ├── stats/          # 统计
│   │   ├── water/          # 饮水
│   │   └── weight/         # 体重
│   └── theme/              # 主题
│       ├── Theme.kt
│       ├── Color.kt
│       ├── AppColors.kt
│       ├── GlassColor.kt
│       ├── GlassModifiers.kt
│       ├── GlassUtils.kt
│       └── Type.kt
├── utils/                  # 工具类
│   ├── DateUtils.kt
│   ├── StatsUtils.kt
│   ├── MetabolicConstants.kt
│   ├── EncouragementMessages.kt
│   └── SecureLogger.kt
├── CalorieAIApplication.kt
└── MainActivity.kt
```

***

## 📋 系统要求

- **最低Android版本**: Android 12 (API 31)
- **目标Android版本**: Android 14 (API 34)
- **存储空间**: 约80MB

***

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 克隆项目

```bash
git clone https://github.com/yourusername/CalorieAI.git
cd CalorieAI/CalorieAI
```

1. 使用Android Studio打开项目
2. 同步Gradle依赖

```bash
./gradlew sync
```

1. 构建Release版本

```bash
./gradlew assembleRelease
```

***

## 🔐 权限说明

- **相机权限**: 拍照识别食物
- **录音权限**: 语音输入功能
- **通知权限**: 餐次提醒、饮水提醒
- **存储权限**: 数据备份导入导出
- **网络权限**: AI服务、云备份

***

## 📝 更新

#### 新增功能

- ✅ 液态玻璃UI设计系统 (Liquid Glass)
- ✅ 设备自适应模糊效果
- ✅ AI助手三形态（悬浮/全屏/独立页）
- ✅ WebDAV云备份支持
- ✅ 智能数据恢复（全量覆盖/合并导入）
- ✅ 恢复预览功能
- ✅ 饮水追踪与提醒
- ✅ 收藏菜谱功能
- ✅  pantry库存食材管理
- ✅ 6种桌面小组件尺寸
- ✅ 底栏长按快捷操作
- ✅ AI回复分段阅读与卡片展示
- ✅ Markdown复制支持

#### 优化改进

- ✅ 动画性能优化
- ✅ 低配置设备适配
- ✅ 小组件数据同步
- ✅ 通知系统重构
- ✅ 数据库性能优化

### v1.0 (2026-03-15) - 正式版本发布

**CalorieAI v1.0 正式发布**

#### 新增功能

- ✅ AI智能食物识别（文本/拍照/语音）
- ✅ 13种完整营养素追踪
- ✅ 个性化营养素参考值计算
- ✅ 运动消耗记录（27种运动）
- ✅ 数据统计与可视化
- ✅ 数据备份与恢复
- ✅ AI营养助手
- ✅ 桌面小组件
- ✅ 自定义主题与壁纸
- ✅ 流畅动画与交互

***

欢迎提交Issue和Pull Request！

***

## 📄 许可证

[MIT License](LICENSE)

***

