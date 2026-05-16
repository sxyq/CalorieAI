package com.calorieai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.calorieai.app.data.local.OnboardingDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingBypassInstrumentedTest {

    @Test
    fun markOnboardingCompleted() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        println("TARGET_PACKAGE=${targetContext.packageName}")
        println("TARGET_DATA_DIR=${targetContext.dataDir?.absolutePath}")

        val onboardingDataStore = OnboardingDataStore(targetContext)
        onboardingDataStore.setOnboardingCompleted(true)
        onboardingDataStore.clearOnboardingData()

        val completed = onboardingDataStore.isOnboardingCompleted.first()
        println("ONBOARDING_COMPLETED=$completed")
        assertTrue(completed)
    }
}
