# CalorieAI v1.0 - 智能食物热量记录应用

[![Android](https://img.shields.io/badge/Android-12%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-blue)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02%2B-orange)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/Version-1.0-success)](https://github.com/)

## 📱 应用简介

CalorieAI v1.0 是一款基于 Material3 设计语言的智能食物热量记录应用，参考 [Deadliner](https://github.com/AritxOnly/Deadliner) 项目的界面风格和交互设计，帮助用户轻松管理每日饮食热量摄入。

**版本**: v1.0  
**发布日期**: 2026-03-15  
**包名**: com.calorieai.app

---

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

### 🏃 运动消耗记录
- **27种运动类型**: 跑步、游泳、瑜伽、HIIT、力量训练等
- **热量消耗计算**: 根据运动时长自动计算
- **运动记录管理**: 独立页面管理历史记录

### 📈 数据统计与可视化
- **今日概览**: 饼状图展示营养素分布
- **趋势分析**: 三个独立折线图（摄入/运动/体重）
- **上月总结**: 详细统计报告（瀑布流卡片）
- **BMR/TDEE**: 基础代谢率和每日总消耗展示

### 🎨 UI/UX特性
- **Deadliner风格**: 现代化Material3界面
- **Liquid Glass**: 毛玻璃视觉效果
- **自定义主题**: 支持壁纸、颜色、字体设置
- **流畅动画**: 页面切换、列表入场、卡片交互动画
- **桌面小组件**: 多种尺寸支持（2x1/3x2/4x3）

### 💾 数据管理
- **本地存储**: Room数据库
- **备份恢复**: JSON格式导出/导入
- **体重追踪**: 独立体重记录与趋势

### 🤖 AI助手
- **悬浮窗设计**: 迷你窗口快速对话
- **营养咨询**: 热量评估、菜谱规划、健康建议
- **对话历史**: 保存和管理历史对话
- **Token统计**: 调用次数和成本追踪

### ⚙️ 设置与配置
- **AI服务配置**: 支持OpenAI/Claude/Kimi/GLM/Qwen/DeepSeek/Gemini
- **外观设置**: 主题、字体、壁纸、动画
- **通知设置**: 餐次提醒、目标提醒
- **个人信息**: BMR/TDEE计算、热量目标

---

## 🛠️ 技术栈

- **语言**: Kotlin 1.9+
- **UI框架**: Jetpack Compose + Material3
- **架构**: MVVM + Repository模式
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **图表库**: MPAndroidChart
- **网络请求**: Retrofit + OkHttp
- **图片加载**: Coil

---

## 📋 系统要求

- **最低Android版本**: Android 12 (API 31)
- **目标Android版本**: Android 14 (API 34)
- **存储空间**: 约50MB

---

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

2. 使用Android Studio打开项目

3. 同步Gradle依赖
```bash
./gradlew sync
```

4. 构建Release版本
```bash
./gradlew assembleRelease
```

---

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
│   └── widget/         # 桌面小组件
├── ui/
│   ├── components/     # 可复用组件
│   │   └── charts/     # 图表组件
│   ├── screens/        # 页面
│   └── theme/          # 主题
└── utils/              # 工具类
```

---

## 🔐 权限说明

- **相机权限**: 拍照识别食物
- **录音权限**: 语音输入功能
- **通知权限**: 餐次提醒
- **存储权限**: 数据备份导入导出

---

## 📝 更新日志

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

#### 技术特性
- ✅ Kotlin + Jetpack Compose
- ✅ MVVM架构
- ✅ Room数据库
- ✅ Hilt依赖注入
- ✅ Material3设计系统

---

## 📚 相关文档

- [开发规划](DEVELOPMENT_PLAN.md) - 全程开发规划与进度
- [技术文档](TECHNICAL_SPEC.md) - 技术细节与API文档

---

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

## 📄 许可证

[MIT License](LICENSE)

---

## 👨‍💻 开发者

- **开发周期**: 6周 (2026-03-11 至 2026-03-15)
- **技术栈**: Kotlin + Jetpack Compose + Material3
- **参考项目**: [Deadliner](https://github.com/AritxOnly/Deadliner)

---

**感谢使用 CalorieAI v1.0！**
