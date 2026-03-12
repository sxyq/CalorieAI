# CalorieAI - 智能食物热量记录应用

[![Android](https://img.shields.io/badge/Android-12%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02%2B-orange)](https://developer.android.com/jetpack/compose)

## 📱 应用简介

CalorieAI 是一款基于 Material3 设计语言的智能食物热量记录应用，帮助用户轻松管理每日饮食热量摄入。

## ✨ 核心功能

### 🍽️ 多种记录方式
- **文本输入**：简单描述食物即可记录
- **语音输入**：支持语音转文字，解放双手
- **拍照识别**：拍摄营养成分表，自动识别

### 📊 热量管理
- 手动输入热量和营养成分（蛋白质/碳水/脂肪）
- 今日摄入概览，实时掌握热量情况
- 收藏常用食物，快速记录

### 📈 数据统计
- 今日摄入统计
- 本周/本月平均摄入
- 最高摄入记录
- 总记录数统计

### 🔔 智能提醒
- 餐次提醒（早餐/午餐/晚餐）
- OPPO流体云通知适配
- 自定义提醒时间

### 🎨 桌面小组件
- 今日摄入概览小组件
- 一键打开应用

### 💾 数据备份
- JSON格式导出/导入
- 数据迁移无忧

## 🛠️ 技术栈

- **语言**: Kotlin 1.9+
- **UI框架**: Jetpack Compose + Material3
- **架构**: MVVM + Repository模式
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **后台任务**: WorkManager
- **图片加载**: Coil

## 📋 系统要求

- **最低Android版本**: Android 12 (API 31)
- **目标Android版本**: Android 14 (API 34)
- **存储空间**: 约50MB

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 克隆项目
```bash
git clone https://github.com/yourusername/CalorieAI.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖
```bash
./gradlew sync
```

4. 构建Release版本
```bash
./gradlew assembleRelease
```

## 📁 项目结构

```
app/src/main/java/com/calorieai/app/
├── data/
│   ├── local/          # Room数据库
│   ├── model/          # 数据模型
│   └── repository/     # 仓库层
├── di/                 # 依赖注入
├── service/
│   ├── ai/             # AI服务
│   ├── backup/         # 备份服务
│   ├── notification/   # 通知服务
│   ├── voice/          # 语音服务
│   └── widget/         # 桌面小组件
├── ui/
│   ├── components/     # 可复用组件
│   ├── navigation/     # 导航
│   ├── screens/        # 页面
│   └── theme/          # 主题
└── utils/              # 工具类
```

## 🔐 权限说明

- **相机权限**: 用于拍照识别营养成分表
- **录音权限**: 用于语音输入功能
- **通知权限**: 用于餐次提醒功能
- **存储权限**: 用于数据备份导入导出

## 📝 开发计划

- [x] 阶段一：项目搭建与基础架构
- [x] 阶段二：核心功能开发
- [x] 阶段三：功能完善
- [x] 阶段四：高级功能
- [x] 阶段五：优化与发布

## 🐛 已知问题

1. AI热量估算功能当前需要手动输入，后续版本将接入真实AI服务
2. 相机识别功能基础已实现，完整集成将在后续版本优化

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

[MIT License](LICENSE)

## 👨‍💻 开发者

- **开发周期**: 6周
- **技术栈**: Kotlin + Jetpack Compose + Material3

---

**感谢使用 CalorieAI！**
