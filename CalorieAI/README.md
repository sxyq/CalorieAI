# CalorieAI - 智能食物热量记录应用

[![Android](https://img.shields.io/badge/Android-12%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02%2B-orange)](https://developer.android.com/jetpack/compose)

## 📱 应用简介

CalorieAI 是一款基于 Material3 设计语言的智能食物热量记录应用，参考 [Deadliner](https://github.com/AritxOnly/Deadliner) 项目的界面风格和交互设计，帮助用户轻松管理每日饮食热量摄入。

## ✨ 核心功能

### 🍽️ 多种记录方式
- **文本输入**：简单描述食物即可记录，AI自动估算热量
- **语音输入**：支持语音转文字，带录音动画效果
- **拍照识别**：拍摄食物照片，多模态AI识别营养成分

### 🤖 AI营养助手
- **热量评估**：根据今日摄入数据，AI分析热量是否合理
- **菜谱规划**：根据目标热量定制健康食谱
- **健康咨询**：营养、运动相关问题解答
- **悬浮窗设计**：点击展开迷你窗口，可全屏聊天

### 📊 数据统计与图表
- **今日摄入概览**：饼状图展示营养素分布（蛋白质/碳水/脂肪）
- **各餐次统计**：柱状图展示早餐/早加餐/午餐/午加餐/晚餐/晚加餐摄入
- **周/月趋势**：折线图展示热量变化趋势
- **上月总结**：瀑布流卡片展示详细统计指标
- **连续记录天数**：激励用户坚持记录

### 🏃 运动消耗记录
- **27种运动类型**：跑步、游泳、瑜伽、HIIT等
- **热量消耗计算**：根据运动时长自动计算
- **今日净摄入**：摄入热量 - 基础代谢 - 运动消耗

### 👤 个人信息与BMR计算
- **基础代谢率(BMR)**：基于Mifflin-St Jeor公式计算
- **每日总消耗(TDEE)**：根据活动水平计算
- **热量目标设置**：支持减脂/增肌/维持模式

### 🎨 界面与交互
- **日期切换栏**：前天/昨天/今天/明天滑动切换
- **顶部菜单**：设置/概览/编辑资料快捷入口
- **可展开日历**：查看近期摄入记录
- **流畅动画**：列表入场、页面切换、卡片点击动画

### 🔔 智能提醒
- **餐次提醒**：早餐/午餐/晚餐/加餐提醒
- **自定义时间**：可设置提醒时间
- **OPPO流体云通知适配**：系统级通知展示

### 🎨 桌面小组件
- **多种尺寸**：小(2x1)、中(3x2)、大(4x3)
- **今日概览**：显示热量进度
- **快捷操作**：一键添加记录

### 💾 数据备份
- **JSON格式导出/导入**：数据迁移无忧
- **自动备份设置**：定期自动备份

### 🤖 AI配置管理
- **多提供商支持**：OpenAI、Claude、Kimi、GLM、Qwen、DeepSeek、Gemini
- **多配置管理**：可保存多个AI配置并切换
- **Token使用统计**：今日/本月调用次数和成本
- **图像理解开关**：控制是否启用拍照识别

### 📚 引导教程
- **首次启动引导**：9步新手教程
- **功能高亮提示**：帮助用户快速上手

## 🛠️ 技术栈

- **语言**: Kotlin 1.9+
- **UI框架**: Jetpack Compose + Material3
- **架构**: MVVM + Repository模式
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **后台任务**: WorkManager
- **图片加载**: Coil
- **图表库**: MPAndroidChart
- **网络请求**: OkHttp

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
│   ├── ai/             # AI服务（热量估算/图片识别/聊天）
│   ├── backup/         # 备份服务
│   ├── notification/   # 通知服务
│   ├── voice/          # 语音服务
│   └── widget/         # 桌面小组件
├── ui/
│   ├── components/     # 可复用组件
│   │   └── charts/     # 图表组件
│   ├── navigation/     # 导航
│   ├── screens/        # 页面
│   └── theme/          # 主题
└── utils/              # 工具类
```

## 🔐 权限说明

- **相机权限**: 用于拍照识别食物
- **录音权限**: 用于语音输入功能
- **通知权限**: 用于餐次提醒功能
- **存储权限**: 用于数据备份导入导出

## 📝 更新日志

### v2.0 (2026-03-13)
- ✅ 新增AI营养助手（悬浮窗设计）
- ✅ 新增运动消耗记录功能
- ✅ 新增多种图表展示（饼状图/折线图/柱状图/雷达图）
- ✅ 扩展用餐类型（早加餐/午加餐/晚加餐）
- ✅ 新增拍照识别功能（多模态AI）
- ✅ 新增桌面小组件（多种尺寸）
- ✅ 新增引导教程系统
- ✅ 新增AI提供商图标（Deadliner风格）
- ✅ 优化统计页面图表展示

### v1.0 (2026-03-12)
- ✅ 基础功能完成
- ✅ 文本/语音/拍照输入
- ✅ AI热量估算
- ✅ 数据统计页面
- ✅ 设置界面重构

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

[MIT License](LICENSE)

## 👨‍💻 开发者

- **开发周期**: 6周
- **技术栈**: Kotlin + Jetpack Compose + Material3
- **参考项目**: [Deadliner](https://github.com/AritxOnly/Deadliner)

---

**感谢使用 CalorieAI！**
