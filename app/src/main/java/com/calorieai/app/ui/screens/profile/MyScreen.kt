package com.calorieai.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.calorieai.app.BuildConfig
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.ui.theme.*
import com.calorieai.app.viewmodel.MyViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(
    onNavigateToBodyProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val userSettings by viewModel.userSettings.collectAsState()

    // 椤甸潰鑾峰緱鐒︾偣鏃跺埛鏂版暟鎹?
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSettings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "鎴戠殑",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 鐢ㄦ埛璧勬枡鍗＄墖 - 涓嶅彲鐐瑰嚮锛屾樉绀哄ご鍍?
            UserProfileHeader(userSettings, isDark)

            // 鍔熻兘鑿滃崟 - 鍙繚鐣欒韩浣撴。妗堝拰璁剧疆
            FunctionMenu(
                onNavigateToBodyProfile = onNavigateToBodyProfile,
                onNavigateToSettings = onNavigateToSettings,
                isDark = isDark
            )

            // 搴旂敤淇℃伅
            AppInfoCard(isDark)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserProfileHeader(
    userSettings: UserSettings?,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 澶村儚
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!userSettings?.userAvatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = userSettings?.userAvatarUri,
                        contentDescription = "澶村儚",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 鐢ㄦ埛鍚?
            Text(
                text = userSettings?.userName ?: "鍋ュ悍鐢ㄦ埛",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 韬綋鏁版嵁鎽樿
            Text(
                text = userSettings?.let {
                    val height = it.userHeight?.roundToInt()
                    val weight = it.userWeight?.roundToInt()
                    val bmi = it.bmi
                    buildString {
                        if (height != null) append("${height}cm")
                        if (weight != null) append(" 路 ${weight}kg")
                        if (bmi != null) append(" 路 BMI ${String.format("%.1f", bmi)}")
                    }.takeIf { it.isNotEmpty() } ?: "瀹屽杽韬綋鏁版嵁锛岃幏鍙栦釜鎬у寲寤鸿"
                } ?: "瀹屽杽韬綋鏁版嵁锛岃幏鍙栦釜鎬у寲寤鸿",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FunctionMenu(
    onNavigateToBodyProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuItem(
                icon = Icons.Outlined.AssignmentInd,
                title = "韬綋妗ｆ",
                subtitle = "查看和编辑身体数据",
                onClick = onNavigateToBodyProfile
            )

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            MenuItem(
                icon = Icons.Outlined.Settings,
                title = "璁剧疆",
                subtitle = "个性化配置和偏好",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
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

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AppInfoCard(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CalorieAI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "鐗堟湰 ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "鏅鸿兘鍋ュ悍绠＄悊鍔╂墜",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

