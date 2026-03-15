package com.calorieai.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.calorieai.app.BuildConfig
import com.calorieai.app.ui.components.liquidGlass
import java.text.SimpleDateFormat
import java.util.*

/**
 * 关于页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // 获取版本信息
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // 获取构建时间
    val buildTime = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(BuildConfig.BUILD_TIME))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部应用信息卡片
            AppInfoCard()

            // 版本信息
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
                SettingsSectionDivider()
                AboutItem(
                    title = "检查更新",
                    subtitle = "前往GitHub查看最新版本",
                    icon = Icons.Default.SystemUpdate,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai/releases")
                    }
                )
            }

            // 法律信息
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

            // 更多
            SettingsSection(title = "更多") {
                AboutItem(
                    title = "项目主页",
                    subtitle = "访问GitHub项目页面",
                    icon = Icons.Default.Code,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "反馈问题",
                    subtitle = "在GitHub上提交Issue",
                    icon = Icons.Default.BugReport,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai/issues")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "功能建议",
                    subtitle = "提交新功能建议",
                    icon = Icons.Default.Lightbulb,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai/discussions")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "给个Star",
                    subtitle = "支持项目发展",
                    icon = Icons.Default.Star,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai")
                    }
                )
            }

            // 技术支持
            SettingsSection(title = "技术支持") {
                AboutItem(
                    title = "使用文档",
                    subtitle = "查看使用指南",
                    icon = Icons.Default.MenuBook,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai/wiki")
                    }
                )
                SettingsSectionDivider()
                AboutItem(
                    title = "常见问题",
                    subtitle = "FAQ",
                    icon = Icons.Default.Help,
                    showArrow = true,
                    onClick = {
                        openUrl(context, "https://github.com/your-repo/calorieai/wiki/FAQ")
                    }
                )
            }

            // 底部版权信息
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

    // 开源许可证对话框
    if (showLicensesDialog) {
        LicensesDialog(
            onDismiss = { showLicensesDialog = false }
        )
    }

    // 隐私政策对话框
    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

/**
 * 打开URL
 */
private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 忽略错误
    }
}

/**
 * 应用信息卡片
 */
@Composable
private fun AppInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
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
                // 应用图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary),
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
                // 应用名称
                Text(
                    text = "CalorieAI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // 应用标语
                Text(
                    text = "智能热量记录助手",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 关于页面项
 */
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

/**
 * 开源许可证对话框
 */
@Composable
private fun LicensesDialog(
    onDismiss: () -> Unit
) {
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
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium
                            )
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

/**
 * 隐私政策对话框
 */
@Composable
private fun PrivacyDialog(
    onDismiss: () -> Unit
) {
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
