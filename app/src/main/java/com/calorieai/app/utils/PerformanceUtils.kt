package com.calorieai.app.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object PerformanceUtils {

    /**
     * 创建优化的ImageLoader配置
     */
    fun createOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用25%的可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB磁盘缓存
                    .build()
            }
            .crossfade(true)
            .crossfade(300)
            .build()
    }

    /**
     * 列表优化建议：
     * 1. 使用LazyColumn的key参数
     * 2. 使用rememberSaveable保存滚动位置
     * 3. 避免在列表项中使用重量级计算
     */
}
