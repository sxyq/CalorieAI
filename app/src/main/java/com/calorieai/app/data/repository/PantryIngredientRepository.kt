package com.calorieai.app.data.repository

import com.calorieai.app.data.local.PantryIngredientDao
import com.calorieai.app.data.model.PantryIngredient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryIngredientRepository @Inject constructor(
    private val pantryIngredientDao: PantryIngredientDao
) {
    fun getAll(): Flow<List<PantryIngredient>> = pantryIngredientDao.getAll()

    suspend fun getAllOnce(): List<PantryIngredient> = pantryIngredientDao.getAllOnce()

    suspend fun upsert(item: PantryIngredient) {
        pantryIngredientDao.upsert(item)
    }

    suspend fun delete(item: PantryIngredient) {
        pantryIngredientDao.delete(item)
    }

    suspend fun deleteAll() {
        pantryIngredientDao.deleteAll()
    }
}

