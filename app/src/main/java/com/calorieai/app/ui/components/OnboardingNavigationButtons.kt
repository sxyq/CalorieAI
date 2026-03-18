package com.calorieai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingNavigationButtons(
    onBack: (() -> Unit)?,
    onNext: () -> Unit,
    isNextEnabled: Boolean = true,
    nextButtonText: String = "下一步",
    backButtonText: String = "上一步",
    showCheckIcon: Boolean = false,
    buttonHeight: Int = 56,
    cornerRadius: Int = 16,
    nextButtonColor: Color? = null,
    backButtonContentColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight.dp),
                shape = RoundedCornerShape(cornerRadius.dp),
                colors = backButtonContentColor?.let {
                    ButtonDefaults.outlinedButtonColors(contentColor = it)
                } ?: ButtonDefaults.outlinedButtonColors()
            ) {
                Text(
                    text = backButtonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "buttonScale"
        )

        Button(
            onClick = onNext,
            enabled = isNextEnabled,
            interactionSource = interactionSource,
            modifier = Modifier
                .let { if (onBack != null) it.weight(1f) else it.fillMaxWidth() }
                .height(buttonHeight.dp)
                .scale(scale),
            shape = RoundedCornerShape(cornerRadius.dp),
            colors = nextButtonColor?.let {
                ButtonDefaults.buttonColors(containerColor = it)
            } ?: ButtonDefaults.buttonColors()
        ) {
            if (showCheckIcon) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = nextButtonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
