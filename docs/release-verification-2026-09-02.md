# CalorieAI 1.3.2 更新链路验证报告（2026-09-02）

## 一、结论

更新链路的服务端与接口全部验证通过。真机实测**未完成**，阻塞原因是手机未连接到本机 ADB，不是代码或服务器问题。

## 二、真机实测：阻塞

**状态：阻塞（等待设备连接）**

目标真机序列号为 `d715a3a4`。以下路径全部检查过，均未发现该设备：

| 检查项 | 结果 |
|---|---|
| `adb devices` | 只有 `emulator-5554`（模拟器），非真机 |
| `ioreg` USB 设备树 | 仅两个主板 USB 控制器 `AppleT8142USBXHCI`，无手机设备 |
| `system_profiler SPUSBDataType` | 无手机 |
| 局域网 5555/5556/5557/5558 | 全部关闭 |
| mDNS 无线调试配对服务 | 空 |
| ADB server | 正常，v1.0.41，端口 5037 监听中 |

ADB 工具本身工作正常，问题在物理连接或手机端设置。

### 待用户操作（三选一）

1. **USB 线**：换用可传数据的线；手机解锁后把 USB 用途改为「传输文件」而非「仅充电」；开发者选项开启 USB 调试；若曾拒绝授权，先「撤销 USB 调试授权」再重插。
2. **无线调试配对**：开发者选项 → 无线调试 → 「使用配对码配对设备」，把屏幕上的 IP:端口 和配对码提供给执行方。
3. **无线调试直连**：开发者选项已开启无线调试时，直接提供手机 IP，执行 `adb connect <手机IP>:5555`。

设备出现在 `adb devices` 后即可执行实测。

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

1. **真机端到端实测未完成**：弹窗、下载、安装、数据保留四项均无真机证据。这是当前唯一阻塞项，等待设备连接。
2. **单元测试未执行**：本机 JDK 24 与 Gradle 8.13 存在 `AndroidUnitTest -> Type T not present` 环境错误。需安装 JDK 21 后重跑。
3. **磁盘余量偏紧**：可用 15G，每个 APK 约 363M。按「只保留 2 个版本」策略可维持，但后续需注意清理旧版本。
4. **签名凭据历史**：`signing/calorieai-release-backup.txt` 已从 Git 跟踪移除并加入 `.gitignore`，本地文件保留。历史提交中仍可能存在旧凭据，建议轮换签名密码并单独安排历史清理。
5. **官网相关**：按用户要求本轮暂停，未进行。

## 十、Git 状态

- 分支：`main`
- HEAD：`e182cf1 feat: 发布 CalorieAI 1.3.2 更新网关`
- `origin/main` 与本地 HEAD 一致
- 本轮新增未跟踪文件：`docs/`、`审查/`（用户的审查资料目录，未改动）
- 本轮未提交或推送任何内容
