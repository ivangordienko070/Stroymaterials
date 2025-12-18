// MainActivity.kt
package com.example.stroymaterials

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.stroymaterials.data.preferences.AppPreferences
import com.example.stroymaterials.presentation.navigation.AppNavigation
import com.example.stroymaterials.ui.theme.StroymaterialsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferences = AppPreferences(this)
            var themeMode by remember { mutableStateOf(preferences.themeMode) }
            
            // Обновляем тему при изменении настроек
            LaunchedEffect(Unit) {
                // Можно добавить слушатель изменений настроек
            }
            
            StroymaterialsTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}