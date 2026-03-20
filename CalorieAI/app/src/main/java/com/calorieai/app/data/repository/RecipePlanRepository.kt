package com.calorieai.app.data.repository

import com.calorieai.app.data.local.RecipePlanDao
import com.calorieai.app.data.model.RecipePlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipePlanRepository @Inject constructor(
    private val recipePlanDao: RecipePlanDao
) {
    fun getAll(): Flow<List<RecipePlan>> = recipePlanDao.getAll()

    suspend fun getAllOnce(): List<RecipePlan> = recipePlanDao.getAllOnce()

    suspend fun upsert(item: RecipePlan) {
        recipePlanDao.upsert(item)
    }

    suspend fun delete(item: RecipePlan) {
        recipePlanDao.delete(item)
    }

    suspend fun deleteAll() {
        recipePlanDao.deleteAll()
    }
}

