package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.ActivityLevel
import com.calorieai.app.data.model.Gender
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.WeightLossStrategy

/**
 * 引导流程主容器
 * 管理4步引导流程的页面切换和动画 - Glass 毛玻璃风格
 */
@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // 完成后的过渡动画
    if (isCompleted) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            onComplete()
        }
        
        // 完成动画界面
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    Text(
                        text = "🎉",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 64.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 300))
                ) {
                    Text(
                        text = "设置完成！",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 500))
                ) {
                    Text(
                        text = "正在为您准备个性化方案...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
        return
    }

    // 保存中的加载界面
    if (isSaving) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在保存设置...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            val direction = if (targetState > initialState) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }

            val animationSpec = tween<IntOffset>(
                durationMillis = 300,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            )

            val fadeSpec = tween<Float>(
                durationMillis = 300,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            )

            slideIntoContainer(
                towards = direction,
                animationSpec = animationSpec
            ) togetherWith slideOutOfContainer(
                towards = direction,
                animationSpec = animationSpec
            ) + fadeOut(animationSpec = fadeSpec)
        },
        label = "onboardingStepTransition"
    ) { step ->
        when (step) {
            1 -> OnboardingScreen1(
                initialGender = onboardingData.gender?.name,
                initialBirthDate = onboardingData.birthDate,
                onNext = { gender, birthDate, height, weight ->
                    val genderEnum = Gender.fromString(gender)
                    if (genderEnum != null) {
                        viewModel.saveStep1Data(genderEnum, birthDate)
                        viewModel.saveStep2Data(height, weight)
                        viewModel.nextStep()
                    }
                },
                onBack = { }
            )

            2 -> OnboardingScreen3(
                initialActivityLevel = onboardingData.activityLevel?.name ?: "SEDENTARY",
                initialCalorieGoal = onboardingData.dailyCalorieGoal.takeIf { it != 2000 },
                onNext = { exerciseHabits, calorieGoal, activityLevel ->
                    val activityLevelEnum = ActivityLevel.fromString(activityLevel)
                    if (activityLevelEnum != null) {
                        viewModel.saveStep3Data(activityLevelEnum, calorieGoal)
                        viewModel.nextStep()
                    }
                },
                onBack = { viewModel.previousStep() }
            )

            3 -> OnboardingScreen4(
                initialGoalType = onboardingData.goalType?.name,
                onNext = { goalType ->
                    val goal = GoalType.fromString(goalType)
                    if (goal != null) {
                        viewModel.saveStep4Data(goal)
                        viewModel.nextStep()
                    }
                },
                onBack = { viewModel.previousStep() }
            )

            4 -> OnboardingScreen5(
                goalType = onboardingData.goalType?.name ?: GoalType.MAINTAIN.name,
                currentWeight = onboardingData.currentWeight,
                initialTargetWeight = onboardingData.targetWeight,
                initialStrategy = onboardingData.weightLossStrategy?.name,
                onNext = { targetWeight, strategy, estimatedWeeks ->
                    val weightLossStrategy = WeightLossStrategy.fromString(strategy)
                    if (weightLossStrategy != null) {
                        viewModel.saveStep5Data(targetWeight, weightLossStrategy, estimatedWeeks)
                        viewModel.completeOnboarding()
                    }
                },
                onBack = { viewModel.previousStep() }
            )
        }
    }
}
