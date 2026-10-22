package com.qalb.azkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qalb.azkar.navigation.QalbApp
import com.qalb.azkar.ui.theme.QalbTheme

/**
 * Single Activity host for the whole Compose UI.
 * All screen behavior lives in feature packages; no scheduling/completion logic here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QalbTheme {
                QalbApp()
            }
        }
    }
}
