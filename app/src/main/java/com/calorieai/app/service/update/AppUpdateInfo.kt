package com.calorieai.app.service.update

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val downloadUrl: String,
    val changelog: String,
    val forceUpdate: Boolean
)
