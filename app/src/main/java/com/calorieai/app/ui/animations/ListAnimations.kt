package com.calorieai.app.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.animation.AnimationEasing
import com.calorieai.app.ui.animation.AnimationSpecs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 列表动画时长配置
 */
object ListAnimationDurations {
    const val ITEM_ENTER = 300
    const val ITEM_EXIT = 250
    const val ITEM_STAGGER = 50
    const val SCROLL_DELAY = 16
    const val ITEM_EXPAND = 300
    const val ITEM_COLLAPSE = 250
}

/**
 * 列表项进入动画（fadeIn + slideIn）
 * 从下方滑入并淡入
 */
@Composable
fun rememberListItemEnterAnimation(
    index: Int,
    baseDelay: Int = 0,
    staggerDelay: Int = ListAnimationDurations.ITEM_STAGGER
): ListItemAnimationState {
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        val delay = baseDelay + index * staggerDelay

        delay(delay.toLong())

        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = ListAnimationDurations.ITEM_ENTER,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ListAnimationDurations.ITEM_ENTER,
                    easing = LinearEasing
                )
            )
        }

        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = AnimationSpecs.SpringBouncy
            )
        }
    }

    return ListItemAnimationState(offsetY, alpha, scale)
}

/**
 * 列表项动画状态
 */
class ListItemAnimationState(
    private val offsetY: Animatable<Float, *>,
    private val alpha: Animatable<Float, *>,
    private val scale: Animatable<Float, *>
) {
    val offsetYValue: Float get() = offsetY.value
    val alphaValue: Float get() = alpha.value
    val scaleValue: Float get() = scale.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val oy = offsetYValue
        val a = alphaValue
        val s = scaleValue
        return modifier.graphicsLayer {
            translationY = oy
            this.alpha = a
            scaleX = s
            scaleY = s
        }
    }
}

/**
 * 列表项删除动画（fadeOut + slideOut）
 * 向上滑出并淡出
 */
@Composable
fun rememberListItemExitAnimation(
    trigger: Boolean,
    onAnimationEnd: (() -> Unit)? = null
): ListItemExitState {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            launch {
                offsetY.animateTo(
                    targetValue = -30f,
                    animationSpec = tween(
                        durationMillis = ListAnimationDurations.ITEM_EXIT,
                        easing = AnimationEasing.EaseInCubic
                    )
                )
            }

            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = ListAnimationDurations.ITEM_EXIT,
                        easing = AnimationEasing.EaseOutCubic
                    )
                )
            }

            launch {
                scale.animateTo(
                    targetValue = 0.95f,
                    animationSpec = tween(
                        durationMillis = ListAnimationDurations.ITEM_EXIT,
                        easing = AnimationEasing.EaseOutCubic
                    )
                )
            }

            onAnimationEnd?.invoke()
        }
    }

    return ListItemExitState(offsetY, alpha, scale)
}

/**
 * 列表项退出动画状态
 */
class ListItemExitState(
    private val offsetY: Animatable<Float, *>,
    private val alpha: Animatable<Float, *>,
    private val scale: Animatable<Float, *>
) {
    val offsetYValue: Float get() = offsetY.value
    val alphaValue: Float get() = alpha.value
    val scaleValue: Float get() = scale.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val oy = offsetYValue
        val a = alphaValue
        val s = scaleValue
        return modifier.graphicsLayer {
            translationY = oy
            this.alpha = a
            scaleX = s
            scaleY = s
        }
    }
}

/**
 * 可动画显示/隐藏的列表项
 */
@Composable
fun AnimatedListItem(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enterDelay: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = AnimationSpecs.Normal
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = ListAnimationDurations.ITEM_ENTER,
                easing = AnimationEasing.EaseOutCubic
            ),
            initialOffsetY = { it }
        ),
        exit = fadeOut(
            animationSpec = AnimationSpecs.Normal
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = ListAnimationDurations.ITEM_EXIT,
                easing = AnimationEasing.EaseInCubic
            ),
            targetOffsetY = { -it / 3 }
        )
    ) {
        content()
    }
}

/**
 * 支持延迟加载动画的 LazyColumn 状态
 * 用于在滚动时触发动画
 */
class LazyListAnimationController(
    val listState: LazyListState,
    private val scope: CoroutineScope
) {
    var isScrolling by mutableStateOf(false)
        private set

    var scrollProgress by mutableFloatStateOf(0f)
        private set

    init {
        scope.launch {
            listState.interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                        isScrolling = true
                    }
                    is androidx.compose.foundation.interaction.PressInteraction.Release -> {
                        isScrolling = false
                    }
                }
            }
        }

        scope.launch {
            var lastIndex = 0
            listState.layoutInfo.visibleItemsInfo.forEach { item ->
                if (item.index != lastIndex) {
                    scrollProgress = item.index.toFloat() / 10f
                    lastIndex = item.index
                }
            }
        }
    }
}

/**
 * 创建列表滚动动画控制器
 */
@Composable
fun rememberListAnimationController(
    listState: LazyListState = rememberLazyListState()
): LazyListAnimationController {
    val scope = rememberCoroutineScope()
    return remember(listState) {
        LazyListAnimationController(listState, scope)
    }
}

/**
 * 列表项展开/收起动画
 */
@Composable
fun rememberListItemExpandAnimation(
    isExpanded: Boolean,
    durationMillis: Int = ListAnimationDurations.ITEM_EXPAND
): ListItemExpandState {
    val expandHeight = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isExpanded) {
        launch {
            expandHeight.animateTo(
                targetValue = if (isExpanded) 1f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }

        launch {
            rotation.animateTo(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }
    }

    return ListItemExpandState(expandHeight, rotation)
}

/**
 * 列表项展开动画状态
 */
class ListItemExpandState(
    private val expandHeight: Animatable<Float, *>,
    private val rotation: Animatable<Float, *>
) {
    val expandHeightValue: Float get() = expandHeight.value
    val rotationValue: Float get() = rotation.value
    val isExpanded: Boolean get() = expandHeight.value > 0.5f
}

/**
 * 列表项拖拽排序动画
 */
@Composable
fun rememberListItemDragAnimation(
    isDragging: Boolean,
    offsetY: Float = 0f
): ListItemDragState {
    val elevation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isDragging) {
        if (isDragging) {
            launch {
                elevation.animateTo(
                    targetValue = 8f,
                    animationSpec = AnimationSpecs.Fast
                )
            }

            launch {
                scale.animateTo(
                    targetValue = 1.05f,
                    animationSpec = AnimationSpecs.SpringBouncy
                )
            }
        } else {
            launch {
                elevation.animateTo(
                    targetValue = 0f,
                    animationSpec = AnimationSpecs.Normal
                )
            }

            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = AnimationSpecs.SpringBouncy
                )
            }
        }
    }

    return ListItemDragState(elevation, scale)
}

/**
 * 列表项拖拽动画状态
 */
class ListItemDragState(
    private val elevation: Animatable<Float, *>,
    private val scale: Animatable<Float, *>
) {
    val elevationValue: Float get() = elevation.value
    val scaleValue: Float get() = scale.value
}

/**
 * 交错动画列表项
 * 用于创建瀑布流或交错网格效果
 */
@Composable
fun rememberStaggeredItemAnimation(
    index: Int,
    columns: Int = 2
): StaggeredItemState {
    val row = index / columns
    val column = index % columns
    val delay = (row * columns + column) * ListAnimationDurations.ITEM_STAGGER

    val offsetX = remember { Animatable(column * 30f) }
    val offsetY = remember { Animatable(row * 30f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())

        launch {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = ListAnimationDurations.ITEM_ENTER,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }

        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = ListAnimationDurations.ITEM_ENTER,
                    easing = AnimationEasing.EaseOutCubic
                )
            )
        }

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ListAnimationDurations.ITEM_ENTER,
                    easing = LinearEasing
                )
            )
        }
    }

    return StaggeredItemState(offsetX, offsetY, alpha)
}

/**
 * 交错动画状态
 */
class StaggeredItemState(
    private val offsetX: Animatable<Float, *>,
    private val offsetY: Animatable<Float, *>,
    private val alpha: Animatable<Float, *>
) {
    val offsetXValue: Float get() = offsetX.value
    val offsetYValue: Float get() = offsetY.value
    val alphaValue: Float get() = alpha.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val ox = offsetXValue
        val oy = offsetYValue
        val a = alphaValue
        return modifier.graphicsLayer {
            translationX = ox
            translationY = oy
            this.alpha = a
        }
    }
}

/**
 * 列表项加载占位符动画
 */
@Composable
fun rememberShimmerAnimation(): ShimmerState {
    val shimmerOffset = remember { Animatable(-1f) }

    LaunchedEffect(Unit) {
        while (true) {
            shimmerOffset.animateTo(
                targetValue = 2f,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                )
            )
        }
    }

    return ShimmerState(shimmerOffset)
}

/**
 * 闪烁动画状态
 */
class ShimmerState(
    private val shimmerOffset: Animatable<Float, *>
) {
    val shimmerOffsetValue: Float get() = shimmerOffset.value
}

/**
 * 平滑滚动到指定索引
 */
suspend fun LazyListState.smoothScrollToItemWithAnimation(
    index: Int,
    scrollOffset: Int = 0
) {
    animateScrollToItem(index, scrollOffset)
}

/**
 * 检测列表是否滚动到底部
 */
@Composable
fun LazyListState.isAtBottom(): Boolean {
    val layoutInfo = layoutInfo
    val totalItemsNumber = layoutInfo.totalItemsCount
    val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

    return lastVisibleItemIndex > (totalItemsNumber - 3)
}

/**
 * 列表项滑动删除动画
 */
@Composable
fun rememberSwipeToDeleteAnimation(
    swipeProgress: Float,
    threshold: Float = 0.4f
): SwipeToDeleteState {
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(swipeProgress) {
        if (swipeProgress > 0) {
            val progress = (swipeProgress / threshold).coerceIn(0f, 1f)
            alpha.animateTo(
                targetValue = 1f - progress,
                animationSpec = AnimationSpecs.Fast
            )
            scale.animateTo(
                targetValue = 1f - progress * 0.2f,
                animationSpec = AnimationSpecs.Fast
            )
        } else {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(150, easing = AnimationEasing.EaseOutCubic)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(150, easing = AnimationEasing.EaseOutCubic)
            )
        }
    }

    return SwipeToDeleteState(alpha, scale)
}

/**
 * 滑动删除动画状态
 */
class SwipeToDeleteState(
    private val alpha: Animatable<Float, *>,
    private val scale: Animatable<Float, *>
) {
    val alphaValue: Float get() = alpha.value
    val scaleValue: Float get() = scale.value

    fun applyToModifier(modifier: Modifier): Modifier {
        val a = alphaValue
        val s = scaleValue
        return modifier.graphicsLayer {
            this.alpha = a
            scaleX = s
            scaleY = s
        }
    }
}

/**
 * 列表项刷新动画
 */
@Composable
fun rememberRefreshAnimation(
    isRefreshing: Boolean
): RefreshAnimationState {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            var count = 0
            while (count < 100) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = AnimationEasing.EaseInOutCubic
                    )
                )
                count++
            }
        }
    }

    return RefreshAnimationState(rotation)
}

/**
 * 刷新动画状态
 */
class RefreshAnimationState(
    private val rotation: Animatable<Float, *>
) {
    val rotationValue: Float get() = rotation.value
}
