# CalorieAI - 全程开发规划

## 项目概述

**项目名称**: CalorieAI  
**类型**: Android原生应用 (Kotlin + Jetpack Compose)  
**参考风格**: Deadliner (Material3 Expressive)  
**数据存储**: 本地Room数据库  
**核心功能**: AI食物热量记录

---

## 开发阶段总览

```
阶段一：项目搭建与基础架构 (第1周)
├── 1.1 创建Android项目
├── 1.2 配置Gradle依赖
├── 1.3 搭建MVVM架构
├── 1.4 配置Material3主题
└── 1.5 创建基础组件库

阶段二：核心功能开发 (第2-3周)
├── 2.1 数据库设计与实现
├── 2.2 食物录入页面
├── 2.3 AI服务集成
├── 2.4 热量估算结果展示
└── 2.5 首页与记录列表

阶段三：功能完善 (第4周)
├── 3.1 拍照识别营养成分表
├── 3.2 语音输入功能
├── 3.3 AI图标生成
├── 3.4 数据统计页面
└── 3.5 滑动手势与动画

阶段四：高级功能 (第5周)
├── 4.1 OPPO流体云通知
├── 4.2 桌面小组件
├── 4.3 数据备份与恢复
└── 4.4 引导教程

阶段五：优化与测试 (第6周)
├── 5.1 性能优化
├── 5.2 UI细节调整
├── 5.3 测试与Bug修复
└── 5.4 打包发布
```

---

## 详细开发计划

### 阶段一：项目搭建与基础架构

#### 任务 1.1: 创建Android项目
**预计时间**: 2小时  
**执行步骤**:
1. 使用Android Studio创建新项目
2. 包名: `com.calorieai.app`
3. 最低API: 31 (Android 12)
4. 目标API: 34 (Android 14)
5. 语言: Kotlin
6. 架构: Empty Activity (Compose)

**输出物**:
- 可运行的基础项目

#### 任务 1.2: 配置Gradle依赖
**预计时间**: 2小时  
**依赖清单**:
```kotlin
// build.gradle.kts (Module: app)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Material3 Expressive
