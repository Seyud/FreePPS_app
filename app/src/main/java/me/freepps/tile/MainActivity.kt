package me.freepps.tile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.freepps.tile.ui.MainScreen
import me.freepps.tile.ui.theme.FreePPSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreePPSTheme {
                MainScreen()
            }
        }
    }
}

