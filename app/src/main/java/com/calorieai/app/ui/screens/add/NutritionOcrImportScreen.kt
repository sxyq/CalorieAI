package com.calorieai.app.ui.screens.add

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.model.getMealTypeName
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionOcrImportScreen(
    selectedDate: String? = null,
    selectedMealType: MealType? = null,
    onNavigateBack: () -> Unit,
    onApplyPayload: (String) -> Unit,
    onRecordSaved: (String) -> Unit,
    viewModel: NutritionOcrImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(selectedDate, selectedMealType) {
        viewModel.setSaveContext(selectedDate, selectedMealType)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NutritionOcrImportEvent.RecordSaved -> onRecordSaved(event.recordId)
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.recognizeNutrition(uri, context)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val imageUri = saveBitmapToCache(bitmap, context)
            if (imageUri != null) {
                viewModel.recognizeNutrition(imageUri, context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u004f\u0043\u0052\u8bc6\u522b\u8425\u517b\u8868") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8fd4\u56de")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { takePictureLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("\u62cd\u7167")
                }

                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("\u76f8\u518c")
                }
            }

            if (uiState.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uiState.imageUri),
                    contentDescription = "\u8425\u517b\u8868\u56fe\u7247",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (uiState.isRecognizing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearError) {
                            Text("\u5173\u95ed")
                        }
                    }
                }
            }

            if (!uiState.isRecognizing && uiState.nutritionInfo != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "\u8bc6\u522b\u7ed3\u679c\u5ba1\u6838\uff08\u6bcf100g\uff0c\u53ef\u4fee\u6539\uff09",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "\u9910\u6b21",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                MealType.BREAKFAST,
                                MealType.LUNCH,
                                MealType.DINNER,
                                MealType.SNACK
                            ).forEach { mealType ->
                                FilterChip(
                                    selected = uiState.mealType == mealType,
                                    onClick = { viewModel.updateMealType(mealType) },
                                    label = { Text(getMealTypeName(mealType)) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.caloriesPer100g,
                                onValueChange = viewModel::updateCaloriesPer100g,
                                modifier = Modifier.weight(1f),
                                label = { Text("\u70ed\u91cf") },
                                suffix = { Text("kcal") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = uiState.proteinPer100g,
                                onValueChange = viewModel::updateProteinPer100g,
                                modifier = Modifier.weight(1f),
                                label = { Text("\u86cb\u767d\u8d28") },
                                suffix = { Text("g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.carbsPer100g,
                                onValueChange = viewModel::updateCarbsPer100g,
                                modifier = Modifier.weight(1f),
                                label = { Text("\u78b3\u6c34") },
                                suffix = { Text("g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = uiState.fatPer100g,
                                onValueChange = viewModel::updateFatPer100g,
                                modifier = Modifier.weight(1f),
                                label = { Text("\u8102\u80aa") },
                                suffix = { Text("g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.foodName,
                    onValueChange = viewModel::updateFoodName,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("\u98df\u7269\u540d\u79f0") },
                    placeholder = { Text("\u4f8b\u5982\uff1a\u9178\u5976\u3001\u71d5\u9ea6\u5976\u3001\u997c\u5e72") }
                )

                OutlinedTextField(
                    value = uiState.weightGrams,
                    onValueChange = viewModel::updateWeightGrams,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("\u5b9e\u9645\u6444\u5165\u514b\u6570") },
                    placeholder = { Text("\u8bf7\u8f93\u5165\u5b9e\u9645\u98df\u7528\u91cd\u91cf") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("\u4fdd\u5b58\u540e\u540c\u65f6\u6536\u85cf")
                    }
                    Switch(
                        checked = uiState.saveAsFavorite,
                        onCheckedChange = viewModel::updateSaveAsFavorite
                    )
                }

                val payload = viewModel.buildPayload()

                if (payload != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "\u672c\u6b21\u6444\u5165\u6362\u7b97",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("\u70ed\u91cf: ${payload.totalCalories()} kcal")
                            Text("\u86cb\u767d\u8d28: ${payload.totalProtein().displayNutrition()} g")
                            Text("\u78b3\u6c34: ${payload.totalCarbs().displayNutrition()} g")
                            Text("\u8102\u80aa: ${payload.totalFat().displayNutrition()} g")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { payload?.let { onApplyPayload(it.toJson()) } },
                        modifier = Modifier.weight(1f),
                        enabled = payload != null && !uiState.isSaving
                    ) {
                        Text("\u586b\u5165\u6dfb\u52a0\u9875")
                    }

                    Button(
                        onClick = viewModel::saveRecord,
                        modifier = Modifier.weight(1f),
                        enabled = payload != null && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("\u4fdd\u5b58\u8bb0\u5f55")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun saveBitmapToCache(bitmap: Bitmap, context: Context): Uri? {
    return runCatching {
        val targetFile = File(context.cacheDir, "nutrition_ocr_${System.currentTimeMillis()}.jpg")
        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            output.flush()
        }
        Uri.fromFile(targetFile)
    }.getOrNull()
}

private fun Float.displayNutrition(): String {
    val normalized = if (this < 0f) 0f else this
    val asLong = normalized.toLong()
    return if (normalized == asLong.toFloat()) {
        asLong.toString()
    } else {
        normalized.toString()
    }
}
