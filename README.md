# CalorieAI

CalorieAI 是一个基于 Kotlin、Jetpack Compose 与 Hilt 的 Android 热量记录应用，聚焦于「食物记录 + 营养分析 + AI 助手 + 本地数据管理」。

当前仓库文档以 `main` 分支现状为准；我已核对本地代码与 `origin/main`，两者当前一致。

## 当前状态

- 版本：`1.1.0`
- `applicationId`：`com.calorieai.app`
- `compileSdk` / `targetSdk`：`36`
- `minSdk`：`26`
- 默认输出 APK 名称：`CalorieAI-v1.1.apk`
- 当前分支已包含最近一轮启动流程、通知系统、备份链路和 AI 相关稳定性修复

## 线上版本演进

下面三个节点是目前 GitHub 仓库里最清晰、最可比的线上版本里程碑：

| 节点 | 线上标识 | 说明 |
| --- | --- | --- |
| 版本节点 A | `97ce712` | `CalorieAI v1.0 Release`，初始公开发布版本 |
| 版本节点 B | `07b8f6f` / tag `V1.0.0` | 仓库历史中一次大规模功能扩展节点，提交文案写为 `v2.0 重大更新` |
| 版本节点 C | `2f24a8f` | 当前 `main` 分支线上代码，对应现在这份 README 所描述的能力 |

从线上代码演进看：

- 节点 A 建立了基础产品形态：AI 食物识别、营养追踪、运动/统计、备份恢复、AI 助手与主题系统
- 节点 B 明显扩展了产品范围：收藏菜谱、Pantry 库存、WebDAV、饮水增强、小组件扩展、更多 AI/UI 能力
- 节点 C 主要收敛稳定性与工程化：启动流程、数据库迁移、通知能力、AI 配置安全、持续活动通知结构、Android 16 路径准备

## 核心能力

### 食物与营养记录

- 文本描述食物并调用 AI 生成热量与营养分析
- 拍照识别与图片理解输入
- 手动录入食物记录
- 支持 13 种营养指标追踪
- 收藏菜谱与历史记录复用

### AI 助手与模型配置

- 内置 AI 对话界面与历史会话管理
- 支持流式响应
- 支持的协议/提供商类型：`OpenAI`、`Claude`、`Kimi`、`GLM`、`Qwen`、`DeepSeek`、`Gemini`、`LongCat`
- AI 配置、模型调用记录、Token 用量都保存在本地数据库
- 当前仓库不再内置默认生产 API Key，需由用户自行配置

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
- 已抽象出持续活动通知能力，预留 Android 16 `Live Updates / promoted ongoing notifications` 升级路径

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

仓库当前没有 Unix 版 `./gradlew`，可直接通过 wrapper jar 运行 Gradle：

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

### 1. OCR 资源目录

构建脚本会在 `preBuild` 阶段同步打包 OCR 资源。默认从下面目录查找：

```text
~/.paddlex/official_models
```

也可以通过 `local.properties` 或环境变量覆盖：

```properties
bundled.paddle.ocr.root=/absolute/path/to/official_models
local.ocr.service.url=http://127.0.0.1:8000
```

对应的环境变量：

- `BUNDLED_PADDLE_OCR_ROOT`
- `LOCAL_OCR_SERVICE_URL`

需要包含以下模型目录：

- `PP-OCRv5_mobile_det`
- `PP-OCRv5_server_rec`
- `PP-LCNet_x1_0_textline_ori`

### 2. Release 签名

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

如果未提供这些值，`release` 构建仍可执行，但不会自动携带你自己的正式签名配置。

### 3. AI 服务配置

AI API Key 不随仓库分发。首次运行后请在应用设置页中自行配置模型提供商、接口地址与密钥。

## 通知与 Android 16 路线

当前通知系统已经完成以下重构：

- 通知能力检查集中在 `NotificationCapabilityManager`
- 通知通道集中在 `AppNotificationChannels`
- 首页启动后的提醒重同步集中在 `MainActivityStartupCoordinator`
- 已引入 `OngoingActivityNotifier`，用于承接持续活动通知
- 当设备能力满足时，会尝试请求 `POST_PROMOTED_NOTIFICATIONS` 对应的 promoted ongoing 能力

这意味着项目已经具备向 Android 16 `Live Updates` 继续演进的代码结构，但是否进入 OEM 系统的特殊展示层，仍需要真实设备验证。

## 数据库与迁移

- 主应用与 Widget 已统一复用同一套 Room builder
- 不再使用 destructive migration 兜底清库
- onboarding 完成态已收敛，旧 DataStore 状态会迁移到 `UserSettings`

## 备份与恢复

- 支持本地导出 JSON
- 支持 WebDAV 上传、下载、恢复
- 支持恢复预览
- 支持 `MERGE` 和 `OVERWRITE` 两种模式

## 当前文档边界

本 README 只描述当前仓库中已经存在且能从代码确认的能力，不包含以下假设性内容：

- 任何默认可用的商业 API Key
- 已经验证通过的 OPPO/ColorOS 流体云最终展示效果
- 自动可用的正式 release keystore

## 更新日志

请查看 [CHANGELOG.md](./CHANGELOG.md)。
