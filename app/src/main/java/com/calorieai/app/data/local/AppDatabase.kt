package com.calorieai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calorieai.app.data.model.AIConfig
import com.calorieai.app.data.model.AITokenUsage
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.model.Converters
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.FavoriteRecipe
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.model.RecipePlan
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.model.WeightRecord
import com.calorieai.app.data.local.dao.WaterRecordDao
import com.calorieai.app.data.local.dao.WeightRecordDao
import com.calorieai.app.data.model.AIChatHistory

@Database(
    entities = [
        FoodRecord::class,
        UserSettings::class,
        AIConfig::class,
        ExerciseRecord::class,
        AITokenUsage::class,
        WeightRecord::class,
        AIChatHistory::class,
        WaterRecord::class,
        APICallRecord::class,
        FavoriteRecipe::class,
        PantryIngredient::class,
        RecipePlan::class
    ],
    version = 22,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodRecordDao(): FoodRecordDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun aiConfigDao(): AIConfigDao
    abstract fun exerciseRecordDao(): ExerciseRecordDao
    abstract fun aiTokenUsageDao(): AITokenUsageDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun aiChatHistoryDao(): AIChatHistoryDao
    abstract fun waterRecordDao(): WaterRecordDao
    abstract fun apiCallRecordDao(): APICallRecordDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
    abstract fun pantryIngredientDao(): PantryIngredientDao
    abstract fun recipePlanDao(): RecipePlanDao
    
    companion object {
        /**
         * 从版本12迁移到版本13
         * 添加引导流程和用户目标相关字段
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加引导流程相关字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingCompleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingCurrentStep INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN onboardingDataJson TEXT")
                
                // 添加用户目标相关字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN goalType TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN targetWeight REAL")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN weightLossStrategy TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN estimatedWeeksToGoal INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN weeklyWeightChangeGoal REAL")
                
                // 添加用户身体档案字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN birthDate INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN exerciseHabitsJson TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN bmr INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN tdee INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN bmi REAL")
            }
        }
        
        /**
         * 从版本13迁移到版本14
         * 添加饮水记录表
         */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建饮水记录表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS water_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        recordTime INTEGER NOT NULL,
                        recordDate INTEGER NOT NULL,
                        note TEXT
                    )
                """)
                // 添加每日饮水目标字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN dailyWaterGoal INTEGER NOT NULL DEFAULT 2000")
                // 添加用户头像URI字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN userAvatarUri TEXT")
            }
        }

        /**
         * 从版本14迁移到版本15
         * 添加API调用记录表
         */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS api_call_records (
                        id TEXT PRIMARY KEY NOT NULL,
                        timestamp INTEGER NOT NULL,
                        configId TEXT NOT NULL,
                        configName TEXT NOT NULL,
                        modelId TEXT NOT NULL,
                        inputText TEXT NOT NULL,
                        outputText TEXT NOT NULL,
                        promptTokens INTEGER NOT NULL DEFAULT 0,
                        completionTokens INTEGER NOT NULL DEFAULT 0,
                        totalTokens INTEGER NOT NULL DEFAULT 0,
                        cost REAL NOT NULL DEFAULT 0.0,
                        duration INTEGER NOT NULL DEFAULT 0,
                        isSuccess INTEGER NOT NULL DEFAULT 1,
                        errorMessage TEXT
                    )
                """)
            }
        }

        /**
         * 从版本15迁移到版本16
         * 添加收藏菜谱表
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorite_recipes (
                        id TEXT PRIMARY KEY NOT NULL,
                        sourceRecordId TEXT NOT NULL,
                        foodName TEXT NOT NULL,
                        userInput TEXT NOT NULL,
                        totalCalories INTEGER NOT NULL,
                        protein REAL NOT NULL,
                        carbs REAL NOT NULL,
                        fat REAL NOT NULL,
                        fiber REAL NOT NULL DEFAULT 0,
                        sugar REAL NOT NULL DEFAULT 0,
                        sodium REAL NOT NULL DEFAULT 0,
                        cholesterol REAL NOT NULL DEFAULT 0,
                        saturatedFat REAL NOT NULL DEFAULT 0,
                        calcium REAL NOT NULL DEFAULT 0,
                        iron REAL NOT NULL DEFAULT 0,
                        vitaminC REAL NOT NULL DEFAULT 0,
                        vitaminA REAL NOT NULL DEFAULT 0,
                        potassium REAL NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        lastUsedAt INTEGER,
                        useCount INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_recipes_sourceRecordId
                    ON favorite_recipes(sourceRecordId)
                """)
            }
        }

        /**
         * 从版本16迁移到版本17
         * 兼容旧版收藏菜谱结构到新版字段
         */
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorite_recipes_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        sourceRecordId TEXT NOT NULL,
                        foodName TEXT NOT NULL,
                        userInput TEXT NOT NULL,
                        totalCalories INTEGER NOT NULL,
                        protein REAL NOT NULL,
                        carbs REAL NOT NULL,
                        fat REAL NOT NULL,
                        fiber REAL NOT NULL DEFAULT 0,
                        sugar REAL NOT NULL DEFAULT 0,
                        sodium REAL NOT NULL DEFAULT 0,
                        cholesterol REAL NOT NULL DEFAULT 0,
                        saturatedFat REAL NOT NULL DEFAULT 0,
                        calcium REAL NOT NULL DEFAULT 0,
                        iron REAL NOT NULL DEFAULT 0,
                        vitaminC REAL NOT NULL DEFAULT 0,
                        vitaminA REAL NOT NULL DEFAULT 0,
                        potassium REAL NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        lastUsedAt INTEGER,
                        useCount INTEGER NOT NULL DEFAULT 0
                    )
                """)

                db.execSQL("""
                    INSERT INTO favorite_recipes_new (
                        id, sourceRecordId, foodName, userInput, totalCalories,
                        protein, carbs, fat, fiber, sugar, sodium, cholesterol,
                        saturatedFat, calcium, iron, vitaminC, vitaminA, potassium,
                        createdAt, lastUsedAt, useCount
                    )
                    SELECT
                        'legacy_' || id,
                        'legacy_' || id,
                        foodName,
                        foodName,
                        calories,
                        protein, carbs, fat, fiber, sugar, sodium, cholesterol,
                        saturatedFat, calcium, iron, vitaminC, vitaminA, potassium,
                        createdAt,
                        NULL,
                        0
                    FROM favorite_recipes
                """)

                db.execSQL("DROP TABLE favorite_recipes")
                db.execSQL("ALTER TABLE favorite_recipes_new RENAME TO favorite_recipes")
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_recipes_sourceRecordId
                    ON favorite_recipes(sourceRecordId)
                """)
            }
        }

        /**
         * 从版本17迁移到版本18
         * 为AI个性化推荐新增忌口与偏好字段、周目标字段
         */
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN dietaryAllergens TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN flavorPreferences TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN budgetPreference TEXT")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN maxCookingMinutes INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN specialPopulationMode TEXT NOT NULL DEFAULT 'GENERAL'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN weeklyRecordGoalDays INTEGER NOT NULL DEFAULT 5")
            }
        }

        /**
         * 从版本18迁移到版本19
         * 新增食材库存、标准化菜谱、菜单计划
         */
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pantry_ingredients (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        unit TEXT NOT NULL,
                        expiresAt INTEGER,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_pantry_ingredients_name
                    ON pantry_ingredients(name)
                    """
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipe_guides (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        ingredientsText TEXT NOT NULL,
                        stepsText TEXT NOT NULL,
                        toolsText TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        servings INTEGER NOT NULL,
                        calories INTEGER NOT NULL,
                        protein REAL NOT NULL,
                        carbs REAL NOT NULL,
                        fat REAL NOT NULL,
                        sourceType TEXT NOT NULL,
                        linkedFavoriteId TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipe_plans (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        startDateEpochDay INTEGER NOT NULL,
                        endDateEpochDay INTEGER NOT NULL,
                        menuText TEXT NOT NULL,
                        generatedByAI INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """
                )
            }
        }

        /**
         * 从版本19迁移到版本20
         * 新增长按底栏快捷跳转开关
         */
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_settings ADD COLUMN enableLongPressHomeToAdd INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE user_settings ADD COLUMN enableLongPressOverviewToStats INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE user_settings ADD COLUMN enableLongPressMyToProfileEdit INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        /**
         * 从版本20迁移到版本21
         * 将 recipe_guides 合并进 favorite_recipes，并移除 recipe_guides 表
         */
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipe_guides (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        ingredientsText TEXT NOT NULL,
                        stepsText TEXT NOT NULL,
                        toolsText TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        servings INTEGER NOT NULL,
                        calories INTEGER NOT NULL,
                        protein REAL NOT NULL,
                        carbs REAL NOT NULL,
                        fat REAL NOT NULL,
                        sourceType TEXT NOT NULL,
                        linkedFavoriteId TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """
                )

                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeIngredientsText TEXT")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeStepsText TEXT")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeToolsText TEXT")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeDifficulty TEXT")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeDurationMinutes INTEGER")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeServings INTEGER")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeSourceType TEXT")
                db.execSQL("ALTER TABLE favorite_recipes ADD COLUMN recipeUpdatedAt INTEGER")

                db.execSQL(
                    """
                    UPDATE favorite_recipes
                    SET
                        recipeIngredientsText = (
                            SELECT rg.ingredientsText
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeStepsText = (
                            SELECT rg.stepsText
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeToolsText = (
                            SELECT rg.toolsText
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeDifficulty = (
                            SELECT rg.difficulty
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeDurationMinutes = (
                            SELECT rg.durationMinutes
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeServings = (
                            SELECT rg.servings
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeSourceType = (
                            SELECT rg.sourceType
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        ),
                        recipeUpdatedAt = (
                            SELECT rg.updatedAt
                            FROM recipe_guides rg
                            WHERE rg.linkedFavoriteId = favorite_recipes.id
                            LIMIT 1
                        )
                    WHERE EXISTS (
                        SELECT 1
                        FROM recipe_guides rg
                        WHERE rg.linkedFavoriteId = favorite_recipes.id
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO favorite_recipes (
                        id,
                        sourceRecordId,
                        foodName,
                        userInput,
                        totalCalories,
                        protein,
                        carbs,
                        fat,
                        fiber,
                        sugar,
                        sodium,
                        cholesterol,
                        saturatedFat,
                        calcium,
                        iron,
                        vitaminC,
                        vitaminA,
                        potassium,
                        recipeIngredientsText,
                        recipeStepsText,
                        recipeToolsText,
                        recipeDifficulty,
                        recipeDurationMinutes,
                        recipeServings,
                        recipeSourceType,
                        recipeUpdatedAt,
                        createdAt,
                        lastUsedAt,
                        useCount
                    )
                    SELECT
                        'guide_' || rg.id,
                        'guide:' || rg.id,
                        rg.name,
                        rg.name,
                        rg.calories,
                        rg.protein,
                        rg.carbs,
                        rg.fat,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        rg.ingredientsText,
                        rg.stepsText,
                        rg.toolsText,
                        rg.difficulty,
                        rg.durationMinutes,
                        rg.servings,
                        rg.sourceType,
                        rg.updatedAt,
                        rg.createdAt,
                        NULL,
                        0
                    FROM recipe_guides rg
                    WHERE rg.linkedFavoriteId IS NULL
                       OR NOT EXISTS (
                            SELECT 1
                            FROM favorite_recipes fr
                            WHERE fr.id = rg.linkedFavoriteId
                       )
                    """
                )

                db.execSQL("DROP TABLE IF EXISTS recipe_guides")
            }
        }

        /**
         * 从版本21迁移到版本22
         * 为高频查询字段补充索引，优化统计页/首页/日志页查询性能
         */
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_food_records_recordTime ON food_records(recordTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_food_records_mealType_recordTime ON food_records(mealType, recordTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_exercise_records_recordTime ON exercise_records(recordTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_weight_records_recordDate ON weight_records(recordDate)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_water_records_recordTime ON water_records(recordTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_water_records_recordDate ON water_records(recordDate)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_api_call_records_timestamp ON api_call_records(timestamp)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_api_call_records_configId ON api_call_records(configId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_api_call_records_configId_timestamp ON api_call_records(configId, timestamp)"
                )
            }
        }
    }
}
