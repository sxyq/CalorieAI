# Changelog

本文件按 GitHub 仓库中可识别的线上版本节点整理，而不是只按本地未发布状态记录。

## 当前线上状态

- 当前线上 `main`：`2f24a8f`
- 当前线上 tag：`V1.0.0` -> `07b8f6f`
- 说明：仓库历史里存在“tag 名称仍为 `V1.0.0`，但提交说明写为 `v2.0 重大更新`”的命名差异，以下按实际代码演进解释

## 节点 C · current main · `2f24a8f`

当前线上主分支代码，工程化和稳定性明显强于此前两个线上版本节点。

### Added

- `MainActivityStartupCoordinator`，统一首屏引导判定、提醒重同步和更新检查
- `AppNotificationChannels`，集中管理通知通道
- `OngoingActivityNotifier`，承接持续活动通知与 Android 16 `Live Updates` 升级路径
- promoted ongoing 通知能力检查与 `POST_PROMOTED_NOTIFICATIONS` 权限支持
- 本地构建所需的启动图标资源与 OCR 占位目录

### Changed

- `compileSdk` / `targetSdk` 升级到 `36`
- `androidx.core:core-ktx` 升级到 `1.15.0`
- 首页启动流程从 Activity 内联逻辑收敛到独立协调器
- 通知能力判断与通知通道管理统一收口
- 备份快照采集改为在 IO 线程并发抓取
- Room 初始化统一为单一 builder，主应用与 Widget 共享同一套迁移链
- onboarding 完成状态收敛到 `UserSettings`
- AI 默认配置不再内置默认生产 API Key
- Release 签名改为从 `local.properties` / 环境变量读取
- 网络日志策略收紧，减少敏感数据泄露风险

### Fixed

- destructive migration 带来的潜在清库风险
- AI 会话/分析取消后仍写回 UI 的问题
- Widget 与主应用数据库配置分叉
- onboarding 步数与恢复状态不一致
- 新环境构建时缺少图标资源的问题

### Verified

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:assembleRelease`

## 节点 B · tag `V1.0.0` · `07b8f6f`

虽然 tag 仍叫 `V1.0.0`，但从代码和提交说明看，这是一次比初始发布大得多的功能扩展节点。

### Major additions

- 收藏菜谱
- Pantry 库存食材管理
- WebDAV 云备份
- 智能恢复模式（覆盖 / 合并）
- 饮水追踪增强
- 更多桌面小组件尺寸
- AI 调用统计与更多 AI 交互能力

### Major changes

- AI 文本/图片分析能力进一步增强
- UI 体系和 Liquid Glass 风格继续扩展
- onboarding、统计、设置、AI、菜谱等模块大幅增量开发
- 数据库表结构扩展到支持收藏菜谱、库存和更多业务数据

## 节点 A · v1.0 release · `97ce712`

这是仓库最早的公开发布节点，建立了产品基础能力。

### Initial release scope

- AI 食物识别（文本 / 拍照 / 语音）
- 13 种营养素追踪
- 个性化营养参考值
- 运动记录
- 数据统计与可视化
- 备份与恢复
- AI 营养助手与对话历史
- 桌面小组件
- 自定义主题、壁纸与基础动画体验

## 辅助节点 · `8ef6c7b`

`CalorieAI v1.0 - Clean Release`，主要作用是对初始发布内容做仓库清理：

- 删除示例测试文件
- 删除 Python 脚本
- 删除示例备份数据
- 删除开发文档，仅保留必要发布内容

## Notes

- 当前本地代码与 GitHub `origin/main` 已对齐
- `12_workspace/` 为本地工作目录，不属于仓库正式内容
- Android 16 持续活动通知能力已接入结构层，但 OEM 特殊展示效果仍需真机验证
