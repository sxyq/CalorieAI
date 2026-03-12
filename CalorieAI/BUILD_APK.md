# CalorieAI APK 构建指南

## 📋 构建前准备

### 1. 图标设置
请将提供的苹果图标保存到以下位置：

```
app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png  (432x432)
app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png   (324x324)
app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png    (216x216)
app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png     (162x162)
app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png     (108x108)
```

### 2. 使用 Android Studio 构建

#### 步骤 1: 打开项目
1. 启动 Android Studio
2. 选择 `Open an existing project`
3. 选择 `CalorieAI` 文件夹

#### 步骤 2: 同步项目
1. 等待 Gradle 同步完成
2. 如果出现错误，点击 `Sync Now`

#### 步骤 3: 构建 APK
1. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. 或者使用快捷键 `Ctrl+F9`

#### 步骤 4: 获取 APK
构建完成后，Android Studio 会显示通知：
- 点击通知中的 `locate` 链接
- 或者在 `app/build/outputs/apk/debug/` 目录下找到 `app-debug.apk`

### 3. 使用命令行构建

#### 步骤 1: 打开终端
在 Android Studio 中：
- 点击底部工具栏的 `Terminal` 标签
- 或者使用快捷键 `Alt+F12`

#### 步骤 2: 执行构建命令
```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

#### 步骤 3: 找到 APK
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

### 4. 构建 AAB (Google Play 发布)

```bash
./gradlew bundleRelease
```

AAB 文件位置：`app/build/outputs/bundle/release/app-release.aab`

---

## 🔧 签名配置（正式发布）

### 步骤 1: 生成签名密钥
```bash
keytool -genkey -v -keystore calorieai.keystore -alias calorieai -keyalg RSA -keysize 2048 -validity 10000
```

### 步骤 2: 配置签名信息
在 `app/build.gradle.kts` 中添加：

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("calorieai.keystore")
            storePassword = "your_store_password"
            keyAlias = "calorieai"
            keyPassword = "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... 其他配置
        }
    }
}
```

### 步骤 3: 重新构建
```bash
./gradlew assembleRelease
```

---

## 📱 安装 APK

### 方法 1: 通过 USB 安装
1. 启用手机的开发者选项和 USB 调试
2. 连接手机到电脑
3. 在 Android Studio 中点击 `Run` 按钮

### 方法 2: 手动安装
1. 将 APK 文件传输到手机
2. 在手机上打开文件管理器
3. 点击 APK 文件安装
4. 允许安装未知来源应用

---

## ⚠️ 注意事项

1. **图标格式**: 请确保图标是 PNG 格式，背景透明
2. **图标尺寸**: 建议提供 512x512 像素的原始图标
3. **签名密钥**: 请妥善保管签名密钥文件，丢失后无法更新应用
4. **版本号**: 每次发布时更新 `versionCode` 和 `versionName`

---

## 📦 输出文件

构建完成后，你会得到以下文件：

| 文件 | 用途 | 位置 |
|------|------|------|
| app-debug.apk | 调试版本 | `app/build/outputs/apk/debug/` |
| app-release.apk | 发布版本 | `app/build/outputs/apk/release/` |
| app-release.aab | Play Store 版本 | `app/build/outputs/bundle/release/` |

---

## 🚀 下一步

构建完成后，你可以：
1. 在手机上测试 APK
2. 上传到 Google Play Console
3. 分享给其他人测试

**祝发布顺利！** 🎉
