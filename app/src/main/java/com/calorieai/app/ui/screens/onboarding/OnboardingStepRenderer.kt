package com.calorieai.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class OnboardingStepLayoutConfig(
    val appBarTitle: String,
    val headline: String,
    val subtitle: String,
    val currentStep: Int,
    val totalSteps: Int,
    val horizontalPadding: Dp = 24.dp,
    val scrollable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingStepRenderer(
    config: OnboardingStepLayoutConfig,
    onBack: () -> Unit,
    content: @Composable ColumnScope.(isDark: Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(config.appBarTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        val baseModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = config.horizontalPadding)
        val containerModifier = if (config.scrollable) {
            baseModifier.verticalScroll(rememberScrollState())
        } else {
            baseModifier
        }

        Column(
            modifier = containerModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgressIndicator(
                currentStep = config.currentStep,
                totalSteps = config.totalSteps,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = config.headline,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = config.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            content(isDark)
        }
    }
}
