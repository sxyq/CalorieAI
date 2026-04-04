package com.calorieai.app.service.backup

import kotlinx.serialization.json.Json

internal val backupJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
}
