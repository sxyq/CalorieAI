package com.calorieai.app.utils

/**
 * 鼓励标语数据类
 */
data class EncouragementMessage(
    val message: String,
    val emoji: String
)

/**
 * 鼓励标语列表 - 25条
 */
val encouragementMessages = listOf(
    EncouragementMessage("坚持就是胜利", "🏆"),
    EncouragementMessage("每一口都是对自己的爱", "❤️"),
    EncouragementMessage("今天的你比昨天更棒", "⭐"),
    EncouragementMessage("健康饮食，快乐生活", "🌈"),
    EncouragementMessage("你正在变得更好", "🌟"),
    EncouragementMessage("每一小步都是进步", "👣"),
    EncouragementMessage("相信自己，你可以的", "💪"),
    EncouragementMessage("今天的努力是明天的收获", "🌱"),
    EncouragementMessage("健康饮食是一种生活态度", "✨"),
    EncouragementMessage("你比想象中更强大", "🔥"),
    EncouragementMessage("保持专注，目标就在前方", "🎯"),
    EncouragementMessage("每一次选择都在塑造更好的自己", "🦋"),
    EncouragementMessage("你的坚持终将美好", "🌸"),
    EncouragementMessage("健康的身体是最好的礼物", "🎁"),
    EncouragementMessage("今天的汗水是明天的微笑", "😊"),
    EncouragementMessage("你正在创造更好的自己", "🎨"),
    EncouragementMessage("不要放弃，你做得很好", "👍"),
    EncouragementMessage("每一个健康的决定都值得庆祝", "🎉"),
    EncouragementMessage("你的努力不会白费", "💎"),
    EncouragementMessage("保持热爱，奔赴山海", "🏔️"),
    EncouragementMessage("做最好的自己", "🌞"),
    EncouragementMessage("健康生活从今天开始", "📅"),
    EncouragementMessage("你的自律让人钦佩", "👏"),
    EncouragementMessage("每一步都是成长", "🌳"),
    EncouragementMessage("享受健康生活的每一天", "☀️")
)

/**
 * 根据进度获取对应的鼓励标语
 */
fun getEncouragementByProgress(progress: Float): EncouragementMessage {
    return when {
        progress < 0.3f -> EncouragementMessage("新的一天，新的开始", "🌅")
        progress < 0.5f -> EncouragementMessage("继续加油，保持节奏", "🚀")
        progress < 0.8f -> EncouragementMessage("做得不错，继续保持", "💫")
        progress < 1.0f -> EncouragementMessage("即将达标，最后冲刺", "⚡")
        progress == 1.0f -> EncouragementMessage("完美达标，太棒了", "🎊")
        else -> EncouragementMessage("注意控制，不要过量", "⚠️")
    }
}

/**
 * 随机获取一条鼓励标语
 */
fun getRandomEncouragement(): EncouragementMessage {
    return encouragementMessages.random()
}
