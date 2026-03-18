package com.calorieai.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.utils.DateUtils.getRelativeDateLabel
import com.calorieai.app.utils.DateUtils.getWeekDayLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日期选择器组件
 * 显示当前日期，支持左右滑动查看历史记录
 * 格式：周一 03-12（今天）/ 周日 03-11（昨天）
 */
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    // 处理滑动
    fun handleSwipe(direction: SwipeDirection) {
        val newDate = when (direction) {
            SwipeDirection.LEFT -> selectedDate.minusDays(1)  // 向左滑看更早的
            SwipeDirection.RIGHT -> {
                // 向右滑看更新的，但不能超过今天
                val nextDate = selectedDate.plusDays(1)
                if (nextDate.isAfter(today)) today else nextDate
            }
        }
        onDateSelected(newDate)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    when {
                        dragAmount < -50 -> handleSwipe(SwipeDirection.LEFT)
                        dragAmount > 50 -> handleSwipe(SwipeDirection.RIGHT)
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左箭头
            IconButton(
                onClick = { handleSwipe(SwipeDirection.LEFT) }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "前一天",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 日期显示
            DateDisplay(
                date = selectedDate
            )

            // 右箭头（如果还没到今天就显示）
            IconButton(
                onClick = { handleSwipe(SwipeDirection.RIGHT) },
                enabled = selectedDate < today
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "后一天",
                    tint = if (selectedDate < today) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
}

/**
 * 日期显示组件
 * 格式：周一 03-12（今天）
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DateDisplay(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val weekDay: String = getWeekDayLabel(date)
    val dateStr: String = date.format(DateTimeFormatter.ofPattern("MM-dd"))
    val relativeLabel: String = getRelativeDateLabel(date)

    AnimatedContent(
        targetState = date,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith
            fadeOut(animationSpec = tween(200))
        },
        label = "DateDisplay"
    ) { _ ->
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 星期
            Text(
                text = weekDay,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 日期
            Text(
                text = dateStr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 相对标签（今天/昨天/前天）
            if (relativeLabel.isNotEmpty() && relativeLabel != "今天") {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($relativeLabel)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 滑动方向
 */
private enum class SwipeDirection {
    LEFT, RIGHT
}
