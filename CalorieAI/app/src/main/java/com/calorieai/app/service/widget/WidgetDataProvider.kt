package com.calorieai.app.service.widget

import android.content.Context
import androidx.room.Room
import com.calorieai.app.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object WidgetDataProvider {

    data class TodaySnapshot(
        val dateLabel: String,
        val calorieIntake: Int,
        val calorieGoal: Int,
        val waterIntakeMl: Int,
        val waterGoalMl: Int,
        val exerciseBurned: Int,
        val exerciseMinutes: Int,
        val protein: Float,
        val carbs: Float,
        val fat: Float,
        val mealCount: Int
    ) {
        val remainingCalories: Int
            get() = calorieGoal - calorieIntake
    }

    @Volatile
    private var database: AppDatabase? = null
    private val lock = Any()

    private fun db(context: Context): AppDatabase {
        database?.let { return it }
        return synchronized(lock) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "calorieai_database"
            )
                .addMigrations(
                    AppDatabase.MIGRATION_12_13,
                    AppDatabase.MIGRATION_13_14,
                    AppDatabase.MIGRATION_14_15,
                    AppDatabase.MIGRATION_15_16,
                    AppDatabase.MIGRATION_16_17
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { database = it }
        }
    }

    suspend fun loadTodaySnapshot(context: Context): TodaySnapshot {
        return try {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDayExclusive = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDayInclusive = endOfDayExclusive - 1
            val database = db(context)

            val foodRecords = database.foodRecordDao()
                .getRecordsBetweenOnce(startOfDay, endOfDayInclusive)
            val exerciseRecords = database.exerciseRecordDao()
                .getRecordsBetweenSync(startOfDay, endOfDayExclusive)
            val waterIntakeMl = database.waterRecordDao()
                .getTotalAmountByDate(startOfDay) ?: 0
            val settings = database.userSettingsDao().getSettingsSync()

            TodaySnapshot(
                dateLabel = SimpleDateFormat("M月d日", Locale.getDefault()).format(Date()),
                calorieIntake = foodRecords.sumOf { it.totalCalories },
                calorieGoal = settings?.dailyCalorieGoal ?: 2000,
                waterIntakeMl = waterIntakeMl,
                waterGoalMl = settings?.dailyWaterGoal ?: 2000,
                exerciseBurned = exerciseRecords.sumOf { it.caloriesBurned },
                exerciseMinutes = exerciseRecords.sumOf { it.durationMinutes },
                protein = foodRecords.sumOf { it.protein.toDouble() }.toFloat(),
                carbs = foodRecords.sumOf { it.carbs.toDouble() }.toFloat(),
                fat = foodRecords.sumOf { it.fat.toDouble() }.toFloat(),
                mealCount = foodRecords.size
            )
        } catch (_: Exception) {
            TodaySnapshot(
                dateLabel = SimpleDateFormat("M月d日", Locale.getDefault()).format(Date()),
                calorieIntake = 0,
                calorieGoal = 2000,
                waterIntakeMl = 0,
                waterGoalMl = 2000,
                exerciseBurned = 0,
                exerciseMinutes = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f,
                mealCount = 0
            )
        }
    }
}

