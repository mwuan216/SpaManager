package com.spacare.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spacare.manager.core.designsystem.theme.SpaCareTheme
import com.spacare.manager.ui.SpaMainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaCareTheme {
                SpaMainApp()
            }
        }
    }
}

