package me.freepps.tile

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import me.freepps.tile.ui.MainScreen
import me.freepps.tile.ui.theme.FreePPSTheme

class MainActivity : ComponentActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private val prefsFileName = "permission_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            setPermissionRequested()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (isFirstTimeRequest()) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            FreePPSTheme {
                MainScreen()
            }
        }
    }

    private fun isFirstTimeRequest(): Boolean {
        val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
        return !sharedPreferences.getBoolean("permission_requested", false)
    }

    private fun setPermissionRequested() {
        val sharedPreferences = getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("permission_requested", true).apply()
    }
}

