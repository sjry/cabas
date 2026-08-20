package com.sjarry.cabas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sjarry.cabas.ui.CabasApp
import com.sjarry.cabas.ui.theme.CabasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CabasTheme {
                CabasApp()
            }
        }
    }
}
