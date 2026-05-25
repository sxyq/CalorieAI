# CalorieAI

CalorieAI 是一个基于 Kotlin、Jetpack Compose 与 Hilt 的 Android 热量记录应用，聚焦于食物记录、营养分析、AI 助手与本地健康数据管理。

项目目标是把日常饮食、运动、饮水、体重和 AI 辅助分析整合到一个本地优先的 Android 应用中，在保证界面体验的同时，提供可扩展的模型配置、通知提醒和数据备份能力。

## 项目概览

- 平台：Android
- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 架构：MVVM + Repository + Hilt
- 存储：Room + DataStore
- 网络：Retrofit + OkHttp SSE
- 后台任务：WorkManager
- 最低系统版本：Android 8.0 (`minSdk 26`)

## 核心功能

### 食物与营养记录

- 通过文本描述食物并生成热量与营养分析
- 支持拍照识别与图片理解输入
- 支持手动录入食物记录
- 支持 13 种营养指标追踪
- 支持收藏菜谱与历史复用

### AI 助手与模型配置

- 内置 AI 对话界面与历史会话管理
- 支持流式响应
- 支持多种模型协议与服务商类型：`OpenAI`、`Claude`、`Kimi`、`GLM`、`Qwen`、`DeepSeek`、`Gemini`、`LongCat`
- AI 配置、调用记录与 Token 用量保存在本地数据库

### 健康追踪

- 饮食记录
- 运动记录
- 体重记录
- 饮水记录与目标管理
- BMR / TDEE 相关个人设置

### 提醒与通知

- 餐次提醒
- 饮水提醒
- 精确闹钟与 WorkManager 协同调度
- 持续活动通知能力预留，便于后续接入更高版本 Android 的实时通知展示能力

### 数据管理

- Room 本地持久化
- 本地 JSON 备份与恢复
- WebDAV 云备份与恢复
- 恢复预览、覆盖/合并两种恢复模式

### 桌面与交互

- Compose + Material 3 UI
- 主题、字体、壁纸等个性化设置
- 多种桌面小组件
- 新手引导与设置迁移逻辑

## 技术栈

- Kotlin `1.9.22`
- Android Gradle Plugin `8.2.2`
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- Retrofit + OkHttp SSE
- Coil
- MPAndroidChart
- Kotlin Serialization

## 项目结构

```text
app/src/main/java/com/calorieai/app/
├── data/                   # Room、DataStore、Repository、数据模型
├── di/                     # Hilt 注入配置
├── domain/                 # 部分领域逻辑
├── service/
│   ├── ai/                 # AI 请求、图像/文本分析、上下文与流式调用
│   ├── backup/             # 本地/WebDAV 备份恢复
│   ├── notification/       # 提醒、通知、持续活动通知能力
│   ├── startup/            # 首屏启动编排
│   ├── update/             # 应用更新检查
│   ├── voice/              # 语音输入
│   └── widget/             # 桌面小组件
├── ui/                     # Compose 页面、组件、导航、主题
├── utils/                  # 时间、统计等工具类
└── viewmodel/              # 部分旧目录下的 ViewModel
```

## 构建环境

建议环境：

- JDK `17`
- Android SDK Platform `36`
- Build Tools `36.0.0`
- 可用的 `adb` / `sdkmanager`

仓库当前没有 Unix 版 `./gradlew`，可以直接通过 wrapper jar 运行 Gradle：

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar \
  org.gradle.wrapper.GradleWrapperMain :app:assembleDebug --console=plain
```

单元测试：

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar \
  org.gradle.wrapper.GradleWrapperMain :app:testDebugUnitTest --console=plain
```

Release 构建：

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar \
  org.gradle.wrapper.GradleWrapperMain :app:assembleRelease --console=plain
```

## 本地配置说明

### OCR 资源目录

构建脚本会在 `preBuild` 阶段同步打包 OCR 资源。默认从下面目录查找：

```text
~/.paddlex/official_models
```

也可以通过 `local.properties` 或环境变量覆盖：

```properties
bundled.paddle.ocr.root=/absolute/path/to/official_models
local.ocr.service.url=http://127.0.0.1:8000
```

对应环境变量：

- `BUNDLED_PADDLE_OCR_ROOT`
- `LOCAL_OCR_SERVICE_URL`

需要包含以下模型目录：

- `PP-OCRv5_mobile_det`
- `PP-OCRv5_server_rec`
- `PP-LCNet_x1_0_textline_ori`

### Release 签名

正式签名信息不再硬编码在仓库中，需要通过 `local.properties` 或环境变量提供：

```properties
release.keystore.path=/absolute/path/to/your.keystore
release.store.password=***
release.key.alias=***
release.key.password=***
```

对应环境变量：

- `RELEASE_KEYSTORE_PATH`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

### AI 服务配置

AI API Key 不随仓库分发。首次运行后请在应用设置页中自行配置模型提供商、接口地址与密钥。

## 数据与通知说明

- 主应用与 Widget 复用同一套 Room 数据库初始化逻辑
- 支持本地导出 JSON 与 WebDAV 云备份
- 通知系统包含餐次提醒、饮水提醒与持续活动通知能力抽象
- 项目已具备向更高版本 Android 实时通知能力继续演进的结构基础

## 更新日志

版本变化与历史演进请查看 [CHANGELOG.md](./CHANGELOG.md)。
