package com.calorieai.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calorieai.app.BuildConfig
import com.calorieai.app.R
import com.calorieai.app.ui.components.SettingsTopAppBar
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import com.calorieai.app.ui.theme.*
import com.calorieai.app.utils.SecureLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "AboutScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scope = rememberCoroutineScope()
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showUsageDocDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var logoTapCount by remember { mutableStateOf(0) }
    var exportingLogcat by remember { mutableStateOf(false) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    val buildTime = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(BuildConfig.BUILD_TIME))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopAppBar(
                title = "关于",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AppInfoCard(
                isDark = isDark,
                onLogoClick = {
                    if (exportingLogcat) return@AppInfoCard
                    logoTapCount += 1
                    SecureLogger.event(TAG, "logo_tap", "count" to logoTapCount)
                    val remaining = 5 - logoTapCount
                    if (remaining > 0) {
                        Toast.makeText(context, "再点击 $remaining 次导出日志", Toast.LENGTH_SHORT).show()
                    } else {
                        logoTapCount = 0
                        exportingLogcat = true
                        SecureLogger.event(TAG, "export_logcat_triggered")
                        scope.launch {
                            val result = exportLogcatToDownload(context)
                            exportingLogcat = false
                            result.onSuccess { fileName ->
                                SecureLogger.event(TAG, "export_logcat_success", "fileName" to fileName)
                                Toast.makeText(
                                    context,
                                    "日志已保存到 Download/$fileName",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure { error ->
                                SecureLogger.e(TAG, "export_logcat_failed | ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "日志导出失败：${error.message ?: "未知错误"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )

            SettingsSection(title = "版本") {
                AboutItem(
                    title = "版本号",
                    subtitle = "v$versionName",
                    icon = Icons.Default.Info,
                    showArrow = false
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "构建时间",
                    subtitle = buildTime,
                    icon = Icons.Default.Update,
                    showArrow = false
                )
            }

            SettingsSection(title = "法律信息") {
                AboutItem(
                    title = "开源许可证",
                    subtitle = "查看第三方开源库许可",
                    icon = Icons.Default.Policy,
                    showArrow = true,
                    onClick = { showLicensesDialog = true }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "隐私政策",
                    subtitle = "了解我们如何保护您的隐私",
                    icon = Icons.Default.Policy,
                    showArrow = true,
                    onClick = { showPrivacyDialog = true }
                )
            }

            SettingsSection(title = "帮助") {
                AboutItem(
                    title = "使用手册",
                    subtitle = "查看完整用户操作教程",
                    icon = Icons.Default.MenuBook,
                    showArrow = true,
                    onClick = { showUsageDocDialog = true }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "常见问题",
                    subtitle = "FAQ",
                    icon = Icons.Default.Help,
                    showArrow = true,
                    onClick = { showFaqDialog = true }
                )
            }

            SettingsSection(title = "更多") {
                AboutItem(
                    title = "项目主页",
                    subtitle = "访问GitHub项目页面",
                    icon = Icons.Default.Code,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/sxyq/CalorieAI")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "反馈问题",
                    subtitle = "在GitHub上提交Issue",
                    icon = Icons.Default.BugReport,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/sxyq/CalorieAI/issues")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "功能建议",
                    subtitle = "提交新功能建议",
                    icon = Icons.Default.Lightbulb,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/sxyq/CalorieAI/discussions")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "给个Star",
                    subtitle = "支持项目发展",
                    icon = Icons.Default.Star,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/sxyq/CalorieAI")
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "© 2026 CalorieAI\nAll rights reserved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showLicensesDialog) {
        LicensesDialog(onDismiss = { showLicensesDialog = false })
    }

    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showUsageDocDialog) {
        UsageDocDialog(onDismiss = { showUsageDocDialog = false })
    }

    if (showFaqDialog) {
        FaqDialog(onDismiss = { showFaqDialog = false })
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 忽略错误
    }
}

@Composable
private fun AppInfoCard(
    isDark: Boolean,
    onLogoClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .glassCardThemed(isDark = isDark, cornerRadius = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onLogoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CalorieAI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "智能热量记录助手",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private suspend fun exportLogcatToDownload(context: Context): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "calorieai_logcat_$timeStamp.txt"
        SecureLogger.event(TAG, "export_logcat_start", "fileName" to fileName)

        val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "threadtime", "-b", "all"))
        val logText = process.inputStream.bufferedReader().use { it.readText() }
        val errText = process.errorStream.bufferedReader().use { it.readText() }
        process.waitFor()
        SecureLogger.event(
            TAG,
            "export_logcat_collected",
            "logLength" to logText.length,
            "errLength" to errText.length
        )

        if (logText.isBlank() && errText.isNotBlank()) {
            throw IllegalStateException(errText)
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("无法创建下载文件")

        resolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(logText.ifBlank { "无可用日志输出" })
        } ?: throw IllegalStateException("无法写入下载文件")
        SecureLogger.event(TAG, "export_logcat_write_done", "fileName" to fileName)

        fileName
    }
}

@Composable
private fun AboutItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showArrow: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicensesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "开源许可证",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val licenses = listOf(
                    "Kotlin" to "Apache 2.0",
                    "Jetpack Compose" to "Apache 2.0",
                    "Material Design 3" to "Apache 2.0",
                    "Hilt" to "Apache 2.0",
                    "Room" to "Apache 2.0",
                    "DataStore" to "Apache 2.0",
                    "OkHttp" to "Apache 2.0",
                    "Coil" to "Apache 2.0"
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    licenses.forEach { (name, license) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = license,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "隐私政策",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            CalorieAI 隐私政策
                            
                            最后更新：2026年3月
                            
                            1. 数据收集
                            我们仅收集您主动输入的数据，包括：
                            • 食物记录和营养信息
                            • 体重记录
                            • 运动记录
                            • 用户设置偏好
                            
                            2. 数据存储
                            • 所有数据仅存储在您的设备本地
                            • 我们不会将您的数据上传到云端服务器
                            • 除非您主动使用AI功能，否则数据不会离开您的设备
                            
                            3. AI功能
                            • 使用AI分析功能时，您的食物描述会发送到您配置的AI服务
                            • 请查看相应AI服务的隐私政策了解数据处理方式
                            
                            4. 数据安全
                            • 您的数据受到Android系统安全机制保护
                            • 我们建议您设置设备锁以增强安全性
                            
                            5. 数据导出
                            • 您可以随时导出您的所有数据
                            • 导出的数据为JSON格式，您可以自行备份或删除
                            
                            6. 儿童隐私
                            • 本应用不面向13岁以下儿童
                            
                            7. 政策更新
                            • 我们可能会不时更新本隐私政策
                            • 重大变更将在应用内通知您
                            
                            8. 联系我们
                            • 如有隐私相关问题，请通过GitHub Issues联系我们
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun UsageDocDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scrollState = rememberScrollState()
    val tutorialMarkdown = remember {
        loadUserTutorialMarkdown(context)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CalorieAI 使用手册",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "已接入最新用户手册内容，建议从“第一章：初次使用设置”开始阅读。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 4.dp)
                ) {
                    MarkdownText(
                        text = tutorialMarkdown,
                        config = MarkdownConfig.ChatReadable,
                        isDark = isDark,
                        onLinkClick = { url -> openUrl(context, url) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun loadUserTutorialMarkdown(context: Context): String {
    return runCatching {
        context.resources
            .openRawResource(R.raw.calorieai_user_tutorial)
            .bufferedReader()
            .use { it.readText() }
            .let(::sanitizeUserTutorialMarkdown)
    }.getOrElse {
        """
        # 使用手册加载失败
        
        无法读取内置用户手册文件，请稍后重试。
        """.trimIndent()
    }
}

private fun sanitizeUserTutorialMarkdown(raw: String): String {
    val withoutImageAppendix = raw.substringBefore("## 📷 图片占位符清单").trim()
    val cleanedLines = withoutImageAppendix.lines().filterNot { line ->
        val trimmed = line.trim()
        trimmed.contains("【图片插入位置") ||
            trimmed.contains("此处插入：") ||
            trimmed.contains("图片说明：")
    }
    return cleanedLines.joinToString("\n").replace(Regex("\n{3,}"), "\n\n").trim()
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "常见问题",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            ❓ 如何使用AI添加食物？
                            
                            1. 确保已在设置中配置AI API密钥
                            2. 点击首页添加按钮
                            3. 选择"AI识别"
                            4. 描述您吃的食物，例如：
                               "一碗米饭，一份红烧肉，一杯牛奶"
                            5. AI会自动分析并计算营养数据
                            
                            ❓ 如何更改每日热量目标？
                            
                            1. 进入"我的"页面
                            2. 点击"身体档案"
                            3. 编辑每日热量目标
                            
                            ❓ 数据会同步到云端吗？
                            
                            不会。所有数据仅存储在您的设备本地，
                            除非您主动使用AI功能，数据不会离开
                            您的设备。您可以随时导出数据备份。
                            
                            ❓ 如何导出数据？
                            
                            1. 进入"我的"页面
                            2. 点击"数据导出"
                            3. 选择导出位置保存JSON文件
                            
                            ❓ 深色模式如何切换？
                            
                            1. 进入"我的"页面
                            2. 点击"设置"
                            3. 点击"界面外观"
                            4. 选择主题模式：浅色/深色/跟随系统
                            
                            ❓ AI功能无法使用？
                            
                            请检查：
                            1. 是否已配置AI API密钥
                            2. 网络连接是否正常
                            3. API密钥是否有效
                            
                            ❓ 如何删除记录？
                            
                            在记录详情页面向左滑动即可删除。
                            
                            ❓ 还有其他问题？
                            
                            请在GitHub上提交Issue：
                            github.com/sxyq/CalorieAI/issues
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}
