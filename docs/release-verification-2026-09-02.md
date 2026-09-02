# CalorieAI 1.3.2 更新链路验证报告（2026-09-02）

## 一、结论

更新链路的服务端与接口全部验证通过。真实华为设备已完成更新检查、覆盖式弹窗、下载进度和 APK 完整性校验；当前停在华为系统锁屏密码确认，安装结果与升级后数据保留仍待用户完成确认。

## 二、真机实测：进行中

**状态：等待华为系统锁屏密码确认**

本次固定使用真实设备 `QFF0219918013405`（华为 LIO-AL00，Android 12 / API 31）；`emulator-5554` 全程未参与验收。

| 检查项 | 结果 |
|---|---|
| 设备识别 | PASS，真实设备在线 |
| 安装低版本 | PASS，`versionCode=131`，应用数据未清除 |
| 启动检查 | PASS，显示“发现新版本 1.3.2” |
| 下载进度 | PASS，真实界面显示 0%→93%→完成 |
| 大小/SHA-256 校验 | PASS，进入“下载完成，可以安装” |
| 未知来源权限 | PASS，已在系统设置中开启 |
| PackageInstaller 确认 | 进行中，华为系统等待锁屏密码 |
| 最终版本与数据保留 | 待确认，当前仍为 `versionCode=131` |

设备端当前显示“请输入锁屏密码继续安装”。完成确认后，重新读取 `dumpsys package com.calorieai.app`，预期版本升至 `versionCode=132`，并检查引导状态和本地记录仍在。

### 本次真机发现并修复的状态恢复问题

华为系统从“允许安装应用”设置页返回时会重建 `MainActivity`，旧实现只在内存中保存下载完成状态，弹窗会退回“立即下载”。现已增加已校验 APK 文件恢复逻辑：

- `AppUpdateManager.findDownloadedApk(versionCode)`
- `MainActivityStartupCoordinator.findDownloadedUpdate(versionCode)`
- `MainActivity` 启动更新检查后恢复 `Ready` 状态

修复提交：`fd1384b fix: 恢复更新安装前的已下载状态`。

## 三、实测脚本（已就绪）

脚本：[scripts/verify_update_on_device.sh](/Users/sunyiyang/Desktop/Project/CalorieAI/scripts/verify_update_on_device.sh)

语法检查通过，可执行权限已设置。执行流程：

```text
选择真机（主动排除模拟器）
  → 安装 1.3.1（低于线上版本，用于触发更新）
  → 清空 logcat
  → 启动 APP
  → 抓取更新弹窗证据
  → 点击更新
  → 监控下载进度
  → 校验 SHA-256
  → PackageInstaller 安装
  → 确认版本升到 1.3.2
  → 检查本地数据与引导状态保留
```

脚本含防误判保护：在仅存在模拟器时输出 `FAIL no real device found` 并退出，不会把模拟器结果当作真机验收。已在模拟器上验证该保护生效。

## 四、接口级验证结果（已通过）

这部分不依赖手机，是真实证据。

| 验证项 | 结果 |
|---|---|
| `latest.json` GET | HTTP 200，内容正确 |
| `latest.json` HEAD | HTTP 200，`cache-control: no-cache` |
| APK HEAD | HTTP 200，380326272 字节，`accept-ranges: bytes` |
| Range 断点 `bytes=0-1023` | HTTP 206 |
| `/healthz` | HTTP 200 `ok` |
| HTTP → HTTPS | 301 跳转 |
| 目录列表 | 404（已禁止） |
| TLS 证书 | Let's Encrypt，`CN=calorieai.sxyq27.online`，有效期至 Nov 29 2026 |
| DNS 解析 | `calorieai.sxyq27.online` → `124.222.153.108` |
| 并发下载限制 | 第二个完整下载立即返回 HTTP 429 `download_busy` |

### APK 一致性

| 项目 | 值 |
|---|---|
| 完整包 SHA-256 | `21a6bce29ed92df1190a5378a93ab0d2acaae0192740e5550dd87f6eee9ca58a` |
| 大小 | 380326272 字节（与 `latest.json` 一致） |
| 本地 vs 线上首 10MB SHA-256 | 一致（`a6d4e270d4a078a3c452db06f1d32a4e650c25f492989c78464052a8ed107781`） |

### 线上 latest.json 实际内容

```json
{
  "versionCode": 132,
  "versionName": "1.3.2",
  "minSupportedVersionCode": 100,
  "forceUpdate": false,
  "apkUrl": "https://calorieai.sxyq27.online/releases/1.3.2/CalorieAI-v1.3.2.apk",
  "apkSize": 380326272,
  "apkSha256": "21a6bce29ed92df1190a5378a93ab0d2acaae0192740e5550dd87f6eee9ca58a",
  "releaseNotes": "完善应用更新链路并修复下载繁忙与断点续传处理。"
}
```

禁止字段检查：无 `voiceModel`、`modelUrl`、`tokensUrl`、API key 等字段。

## 五、APP 更新判定逻辑验证（用线上真实 JSON 模拟）

按 [AppUpdateService.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateService.kt) 与 [AppUpdateManager.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateManager.kt) 的校验规则逐条执行：

| 校验项 | 结果 |
|---|---|
| `versionCode > 0` | PASS |
| `minSupportedVersionCode >= 0` | PASS |
| `minSupported <= latest` | PASS |
| `versionName` 格式匹配 | PASS |
| `apkSize > 0` | PASS |
| `releaseNotes` 长度 ≤ 2000 | PASS |
| `apkSha256` 符合 64 位十六进制 | PASS |
| `apkUrl` 路径模板匹配 | PASS |
| `apkUrl` 为 HTTPS | PASS |
| 检查地址路径为 `/android/stable/latest.json` | PASS |

升级判定矩阵：

| 当前 versionCode | 是否弹窗 | 是否强制 |
|---|---|---|
| 100（1.0.0） | 是 | 否（`minSupportedVersionCode=100`） |
| 131（1.3.1） | 是 | 否 |
| 132（1.3.2） | 否 | 否 |
| 133 | 否 | 否 |

## 六、代码路径确认

| 环节 | 位置 |
|---|---|
| 启动后异步检查更新 | [MainActivityStartupCoordinator.kt:51](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/startup/MainActivityStartupCoordinator.kt:51) |
| 更新弹窗与下载进度 | [MainActivity.kt:204](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/MainActivity.kt:204) |
| 断点下载 / 大小与 SHA-256 校验 / 429 繁忙提示 | [AppUpdateDownloadWorker.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateDownloadWorker.kt) |
| PackageInstaller 用户确认式安装 | [AppUpdateManager.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateManager.kt) |
| URL 白名单与字段校验 | [AppUpdateEndpoint.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateEndpoint.kt) |
| 版本与更新地址 | [app/build.gradle.kts:54](/Users/sunyiyang/Desktop/Project/CalorieAI/app/build.gradle.kts:54) |

超时设置（[AppUpdateService.kt](/Users/sunyiyang/Desktop/Project/CalorieAI/app/src/main/java/com/calorieai/app/service/update/AppUpdateService.kt)）：连接 8s、读取 15s、整体调用 20s，与 AI 长请求隔离，不会阻塞启动页。

## 七、服务器现状（124.222.153.108）

| 项目 | 状态 |
|---|---|
| 容器 `calorieai-gateway` | Up 30 hours (healthy)，内存 4.76MiB / 128MiB |
| 监听 | `127.0.0.1:18090` → 容器 8080 |
| Nginx | 正常，`calorieai-updates.conf` 已加载 |
| 证书 | Let's Encrypt，有效期至 Nov 29 2026 |
| 磁盘 | 40G 总量，已用 23G，可用 15G |
| `/srv/calorieai-updates` | 726M（1.3.1 与 1.3.2 两个版本，各 363M） |
| 内存 | 3.6G 总量，已用 48% |
| CPU | 4 核 |

其他在跑容器：`silverlink-redis`、`silverlink-mysql`，本次未触碰。

## 八、语音模型约束

本次未改动。SenseVoice 模型仍打包在 APK assets 内：

- `assets/sensevoice/model.int8.onnx`
- `assets/sensevoice/tokens.txt`

未新增模型下载任务、远程 manifest 或下载进度状态。

## 九、未完成事项与风险

1. **真机端到端实测待最后确认**：弹窗、下载进度、大小/SHA-256 校验已有真机证据；安装升级和数据保留等待锁屏密码确认。
2. **单元测试未执行**：本机 JDK 24 与 Gradle 8.13 存在 `AndroidUnitTest -> Type T not present` 环境错误。需安装 JDK 21 后重跑。
3. **磁盘余量偏紧**：可用 15G，每个 APK 约 363M。按「只保留 2 个版本」策略可维持，但后续需注意清理旧版本。
4. **签名凭据历史**：`signing/calorieai-release-backup.txt` 已从 Git 跟踪移除并加入 `.gitignore`，本地文件保留。历史提交中仍可能存在旧凭据，建议轮换签名密码并单独安排历史清理。
5. **官网相关**：按用户要求本轮暂停，未进行。

## 十、Git 状态

- 分支：`main`
- HEAD：`fd1384b fix: 恢复更新安装前的已下载状态`
- `origin/main` 与本地 HEAD 一致
- 本轮新增未跟踪文件：`docs/`、`审查/`（用户的审查资料目录，未改动）
- 修复已提交并推送到 `origin/main`；`审查/` 未跟踪目录保持原样。
