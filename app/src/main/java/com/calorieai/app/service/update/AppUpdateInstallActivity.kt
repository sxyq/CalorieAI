package com.calorieai.app.service.update

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import android.util.Log

/** Receives PackageInstaller status in an activity so OEM installers can show confirmation UI. */
class AppUpdateInstallActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val installerIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }
        if (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE) ==
            PackageInstaller.STATUS_PENDING_USER_ACTION && installerIntent != null
        ) {
            installerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(installerIntent)
        } else if (
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE) !=
            PackageInstaller.STATUS_SUCCESS
        ) {
            Log.w(
                TAG,
                "package installation failed: " +
                    intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            )
        }
        finish()
    }

    companion object {
        const val ACTION_INSTALL_RESULT = "com.calorieai.app.action.APP_UPDATE_INSTALL_RESULT"
        const val EXTRA_SESSION_ID = "session_id"
        private const val TAG = "AppUpdateInstall"
    }
}
