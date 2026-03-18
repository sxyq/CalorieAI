# CalorieAI 代码安全审查报告

生成时间：2026-03-17

## 一、严重安全问题

### 🔴 1. API密钥硬编码（严重）

**文件**: `service/ai/AIDefaultConfigInitializer.kt`
**位置**: 第24行
**问题**:
```kotlin
const val DEFAULT_API_KEY = "ak_1qe7Ym0Yp8Hs3qa74O5wt2gy6Rt6I"
```

**风险等级**: 🔴 严重
**影响**:
- API密钥暴露在源代码中
- 如果代码被公开（如开源），密钥会被泄露
- 攻击者可以使用该密钥进行未授权的API调用
- 可能导致财务损失和账户被封禁

**修复建议**:
1. **立即撤销并重新生成该API密钥**
2. 使用Android Keystore存储敏感信息
3. 使用BuildConfig或gradle.properties存储密钥
4. 将敏感配置移至服务端

**修复代码示例**:
```kotlin
// 方案1: 使用BuildConfig
const val DEFAULT_API_KEY = BuildConfig.DEFAULT_API_KEY

// 方案2: 使用EncryptedSharedPreferences
private fun getApiKey(context: Context): String {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    return sharedPreferences.getString("api_key", "") ?: ""
}
```

---

### 🔴 2. API密钥在备份中暴露（严重）

**文件**: `service/backup/BackupService.kt`
**位置**: AIConfigBackup数据类
**问题**:
```kotlin
@Serializable
data class AIConfigBackup(
    ...
    val apiKey: String,  // API密钥被包含在备份中
    ...
)
```

**风险等级**: 🔴 严重
**影响**:
- 备份文件中包含明文API密钥
- 备份文件可能被分享或存储在不安全的位置
- 攻击者获取备份文件后可提取密钥

**修复建议**:
1. 备份时加密API密钥
2. 或在备份中排除敏感字段
3. 添加备份文件加密功能

**修复代码示例**:
```kotlin
@Serializable
data class AIConfigBackup(
    val id: String,
    val name: String,
    ...
    val apiKey: String? = null,  // 可选，默认不备份
    val hasApiKey: Boolean = false  // 仅标记是否有密钥
)

// 备份时
val backup = AIConfigBackup(
    ...
    apiKey = if (includeSensitiveData) config.apiKey else null,
    hasApiKey = config.apiKey.isNotBlank()
)
```

---

## 二、中等安全问题

### 🟡 3. 日志信息泄露（中等）

**文件**: `service/ai/common/AIApiClient.kt`
**问题**: 错误消息中可能包含敏感信息
```kotlin
throw AIApiException("API调用失败(${response.code}): $errorBody", response.code, errorBody)
```

**风险等级**: 🟡 中等
**影响**:
- 生产环境中可能泄露API响应细节
- 调试信息可能帮助攻击者了解系统内部

**修复建议**:
1. 生产环境使用通用错误消息
2. 敏感信息仅在调试模式下记录

---

### 🟡 4. URL验证不足（中等）

**文件**: `service/ai/AIDefaultConfigInitializer.kt`
**问题**: API URL修复逻辑可能被利用
```kotlin
val fixedUrl = when {
    config.apiUrl.endsWith("/") -> "${config.apiUrl}v1/chat/completions"
    config.apiUrl.contains("/v1") -> "${config.apiUrl}/chat/completions"
    else -> "${config.apiUrl}/v1/chat/completions"
}
```

**风险等级**: 🟡 中等
**影响**:
- 用户输入的URL可能指向恶意服务器
- 可能导致数据泄露到第三方服务器

**修复建议**:
1. 添加URL白名单验证
2. 使用HTTPS强制验证
3. 添加证书固定（Certificate Pinning）

---

### 🟡 5. 流式响应处理安全（中等）

**文件**: `service/ai/common/AIApiClient.kt`
**问题**: 流式响应解析时忽略错误
```kotlin
try {
    val response = gson.fromJson(data, ChatResponse::class.java)
    ...
} catch (e: Exception) {
    // 忽略解析错误，继续处理
}
```

**风险等级**: 🟡 中等
**影响**:
- 恶意服务器可能发送畸形数据
- 解析错误可能隐藏攻击尝试

**修复建议**:
1. 记录解析错误
2. 添加错误计数和阈值
3. 超过阈值时终止连接

---

## 三、低风险安全问题

### 🟢 6. SharedPreferences未加密（低）

**文件**: `data/repository/UserSettingsRepository.kt`
**问题**:
```kotlin
private val prefs by lazy {
    context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
}
```

**风险等级**: 🟢 低
**影响**:
- 已root设备可读取SharedPreferences
- 用户设置可能被篡改

**修复建议**:
使用EncryptedSharedPreferences替代普通SharedPreferences

---

### 🟢 7. 数据库未加密（低）

**文件**: `data/local/AppDatabase.kt`
**问题**: Room数据库未加密

**风险等级**: 🟢 低
**影响**:
- 已root设备可读取数据库文件
- 敏感数据可能被提取

**修复建议**:
使用SQLCipher加密数据库

---

## 四、代码质量问题

### 1. 重复代码

**问题**: `AIApiClient.kt`中`chat`、`chatRaw`、`chatStream`方法有大量重复代码

**优化建议**:
```kotlin
private fun buildRequestBody(
    config: AIConfig,
    systemPrompt: String,
    userMessage: String,
    stream: Boolean
): Map<String, Any> {
    val isOmniModel = config.modelId.contains("Omni", ignoreCase = true)
    return if (isOmniModel) {
        mapOf(
            "model" to config.modelId,
            "messages" to listOf(
                mapOf("role" to "system", "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))),
                mapOf("role" to "user", "content" to listOf(mapOf("type" to "text", "text" to userMessage)))
            ),
            "stream" to stream
        )
    } else {
        mapOf(
            "model" to config.modelId,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userMessage)
            ),
            "stream" to stream
        )
    }
}
```

### 2. 异常处理不完整

**问题**: 部分Repository方法缺少异常处理

**优化建议**:
```kotlin
suspend fun saveSettings(settings: UserSettings): Result<Unit> {
    return try {
        userSettingsDao.insertOrUpdate(settings)
        syncToPreferences(settings)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 3. 硬编码字符串

**问题**: 多处硬编码字符串，缺少国际化支持

**优化建议**:
将所有用户可见字符串移至`strings.xml`资源文件

---

## 五、性能优化建议

### 1. 网络请求优化

**当前问题**:
- 每次请求创建新的Gson实例
- 缺少请求缓存

**优化建议**:
```kotlin
@Singleton
class AIApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()  // 单例复用
    
    // 添加请求缓存
    private val cache = LruCache<String, String>(50)
}
```

### 2. 数据库查询优化

**当前问题**:
- 部分查询可能缺少索引
- 大量数据查询未分页

**优化建议**:
```kotlin
// 添加索引
@Entity(
    tableName = "food_records",
    indices = [
        Index(value = ["recordTime"]),
        Index(value = ["mealType"])
    ]
)
data class FoodRecord(...)

// 分页查询
@Query("SELECT * FROM food_records ORDER BY recordTime DESC LIMIT :limit OFFSET :offset")
suspend fun getRecordsPaged(limit: Int, offset: Int): List<FoodRecord>
```

### 3. Compose重组优化

**当前问题**:
- 部分Composable可能触发不必要的重组

**优化建议**:
```kotlin
// 使用remember和derivedStateOf
@Composable
fun MessageList(messages: List<ChatMessage>) {
    val sortedMessages = remember(messages) {
        messages.sortedBy { it.timestamp }
    }
    // ...
}

// 使用key优化LazyColumn
LazyColumn {
    items(
        items = messages,
        key = { message -> message.id }
    ) { message ->
        MessageItem(message)
    }
}
```

---

## 六、修复优先级

| 优先级 | 问题 | 状态 |
|--------|------|------|
| 🔴 P0 | API密钥硬编码 | ✅ 已修复 |
| 🔴 P0 | 备份中密钥暴露 | ✅ 已修复 |
| 🟡 P1 | URL验证不足 | 尽快修复 |
| 🟡 P1 | 日志信息泄露 | ✅ 已修复 |
| 🟡 P2 | 流式响应处理 | 计划修复 |
| 🟢 P3 | SharedPreferences加密 | ✅ 已修复 |
| 🟢 P3 | 数据库加密 | 建议修复 |

---

## 七、总结

本次安全审查发现了**2个严重安全问题**、**3个中等安全问题**和**2个低风险问题**。

**最紧急的问题**是API密钥硬编码在源代码中，这需要立即修复。建议：

1. **立即行动**:
   - 撤销并重新生成暴露的API密钥
   - 从代码中移除硬编码密钥
   - 使用安全的密钥存储方案

2. **短期改进**:
   - 添加URL验证
   - 优化日志输出
   - 加密备份中的敏感数据

3. **长期规划**:
   - 实施数据库加密
   - 添加证书固定
   - 完善安全测试流程
