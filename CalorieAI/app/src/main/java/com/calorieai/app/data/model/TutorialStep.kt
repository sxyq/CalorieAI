package com.calorieai.app.data.model

import androidx.annotation.DrawableRes

/**
 * 引导教程步骤
 */
data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String = "",
    val targetRoute: String? = null, // 目标页面路由，用于高亮特定页面
    val highlightArea: HighlightArea? = null // 高亮区域
)

/**
 * 高亮区域类型
 */
enum class HighlightArea {
    TOP_MENU,           // 顶部菜单按钮
    ADD_BUTTON,         // 添加按钮
    DATE_SELECTOR,      // 日期选择器
    CALENDAR,           // 日历
    STATS_CARD,         // 统计卡片
    RECORD_LIST,        // 记录列表
    BOTTOM_NAV          // 底部导航
}

/**
 * 引导教程配置
 */
object TutorialConfig {
    
    val steps = listOf(
        TutorialStep(
            id = "welcome",
            title = "欢迎使用 CalorieAI",
            description = "智能热量记录助手，让健康管理变得简单有趣。我们将带你快速了解主要功能。",
            emoji = "👋"
        ),
        TutorialStep(
            id = "date_selector",
            title = "日期选择",
            description = "点击这里可以展开日历，选择任意日期查看或添加记录。只能选择今天及之前的日期哦！",
            emoji = "📅",
            highlightArea = HighlightArea.DATE_SELECTOR
        ),
        TutorialStep(
            id = "add_food",
            title = "记录食物",
            description = "点击右下角的+号，可以通过文字、语音或拍照快速记录你的饮食。试试说：\"番茄炒蛋，米饭200g\"",
            emoji = "🍽️",
            highlightArea = HighlightArea.ADD_BUTTON
        ),
        TutorialStep(
            id = "today_stats",
            title = "今日概览",
            description = "这里显示你今天的摄入情况，包括已摄入、目标和剩余热量。还会显示你的基础代谢和运动消耗！",
            emoji = "📊",
            highlightArea = HighlightArea.STATS_CARD
        ),
        TutorialStep(
            id = "record_list",
            title = "饮食记录",
            description = "你添加的所有食物都会显示在这里。左滑可以删除，点击可以查看详情。",
            emoji = "📝",
            highlightArea = HighlightArea.RECORD_LIST
        ),
        TutorialStep(
            id = "top_menu",
            title = "更多功能",
            description = "点击右上角菜单，可以访问个人信息编辑、概览统计、设置等功能。",
            emoji = "⚙️",
            highlightArea = HighlightArea.TOP_MENU
        ),
        TutorialStep(
            id = "stats_page",
            title = "数据统计",
            description = "在概览页面，你可以查看详细的统计数据、趋势分析和月度总结。",
            emoji = "📈",
            targetRoute = "stats"
        ),
        TutorialStep(
            id = "profile",
            title = "个人信息",
            description = "记得完善你的个人信息，系统会根据你的身高、体重、年龄自动计算基础代谢率(BMR)和每日推荐摄入量！",
            emoji = "👤",
            targetRoute = "profile"
        ),
        TutorialStep(
            id = "complete",
            title = "准备开始！",
            description = "教程完成！现在你可以开始记录你的饮食了。坚持记录，健康生活！💪",
            emoji = "🎉"
        )
    )
    
    const val PREFS_TUTORIAL_COMPLETED = "tutorial_completed"
    const val PREFS_TUTORIAL_VERSION = "tutorial_version"
    const val CURRENT_TUTORIAL_VERSION = 1
}
