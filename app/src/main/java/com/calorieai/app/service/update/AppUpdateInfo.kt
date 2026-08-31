package com.calorieai.app.service.update

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val minSupportedVersionCode: Int,
    val downloadUrl: String,
    val apkSize: Long,
    val apkSha256: String,
    val releaseNotes: String,
    val forceUpdate: Boolean,
    val isMandatory: Boolean
)
