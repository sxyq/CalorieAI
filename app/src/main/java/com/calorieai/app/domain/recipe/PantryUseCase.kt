package com.calorieai.app.domain.recipe

import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.data.repository.PantryIngredientRepository
import com.calorieai.app.service.notification.PantryExpiryReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryUseCase @Inject constructor(
    private val pantryIngredientRepository: PantryIngredientRepository,
    private val pantryExpiryReminderScheduler: PantryExpiryReminderScheduler
) {
    fun observePantry(): Flow<List<PantryIngredient>> = pantryIngredientRepository.getAll()

    suspend fun getPantryOnce(): List<PantryIngredient> = pantryIngredientRepository.getAllOnce()

    suspend fun addIngredient(
        name: String,
        quantity: Float,
        unit: String,
        daysToExpire: Int?,
        notes: String?
    ): Result<PantryIngredient> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("食材名称不能为空"))
        }

        val now = System.currentTimeMillis()
        val expiresAt = daysToExpire
            ?.takeIf { it > 0 }
            ?.let { now + it * 24L * 60L * 60L * 1000L }

        val item = PantryIngredient(
            name = name.trim(),
            quantity = quantity,
            unit = unit.trim().ifBlank { "份" },
            expiresAt = expiresAt,
            notes = notes?.trim().takeUnless { it.isNullOrBlank() },
            createdAt = now,
            updatedAt = now
        )

        return runCatching {
            pantryIngredientRepository.upsert(item)
            pantryExpiryReminderScheduler.scheduleFor(item)
            item
        }
    }

    suspend fun removeIngredient(item: PantryIngredient): Result<Unit> {
        return runCatching {
            pantryIngredientRepository.delete(item)
            pantryExpiryReminderScheduler.cancelFor(item.id)
        }
    }
}

