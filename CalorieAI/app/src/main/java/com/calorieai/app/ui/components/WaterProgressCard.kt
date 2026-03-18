package com.calorieai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*

/**
 * 统一的饮水进度卡片组件
 * @param currentAmount 当前饮水量（毫升）
 * @param targetAmount 目标饮水量（毫升）
 * @param isDark 是否深色模式
 * @param onTargetClick 点击目标区域的回调（可选）
 * @param modifier 修饰符
 */
@Composable
fun WaterProgressCard(
    currentAmount: Int,
    targetAmount: Int,
    isDark: Boolean,
    onTargetClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                GlassDarkColors.CardBackground.copy(alpha = 0.6f)
            } else {
                GlassLightColors.CardBackground.copy(alpha = 0.6f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF26C6DA),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "今日饮水",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(160.dp),
                    color = Color(0xFF26C6DA),
                    strokeWidth = 12.dp,
                    trackColor = if (isDark) {
                        Color.White.copy(alpha = 0.1f)
                    } else {
                        Color.Black.copy(alpha = 0.1f)
                    }
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentAmount}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26C6DA)
                    )
                    Text(
                        text = "ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "目标: ${targetAmount}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${percentage}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFF26C6DA)
                    }
                )
            }
            
            if (onTargetClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onTargetClick) {
                    Text("修改目标", color = Color(0xFF26C6DA))
                }
            }
        }
    }
}
