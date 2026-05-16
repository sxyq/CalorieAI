package com.calorieai.app.ui.screens.ai

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun sharedAIChatViewModel(): AIChatViewModel {
    val activity = LocalContext.current.findComponentActivity()
        ?: error("AIChatViewModel requires a ComponentActivity context")
    return hiltViewModel(activity)
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
