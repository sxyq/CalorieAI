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
class OnboardingStateInstrumentedTest {

    @Test
    fun onboardingCompletedFlag_isTrue() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val onboardingDataStore = OnboardingDataStore(context)
        val completed = onboardingDataStore.isOnboardingCompleted.first()
        assertTrue("Expected onboarding_completed=true but was false", completed)
    }
}
