package com.calorieai.app.service.notification

import android.content.Context
import com.calorieai.app.data.model.PantryIngredient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryExpiryReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleFor(item: PantryIngredient) {
        val expiresAt = item.expiresAt ?: return
        PantryExpiryReminderWorker.schedule(
            context = context,
            ingredientId = item.id,
            ingredientName = item.name,
            expiresAt = expiresAt
        )
    }

    fun cancelFor(itemId: String) {
        PantryExpiryReminderWorker.cancel(context, itemId)
    }
}

