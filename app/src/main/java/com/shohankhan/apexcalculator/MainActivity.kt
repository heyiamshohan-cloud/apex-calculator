package com.shohankhan.apexcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohankhan.apexcalculator.core.designsystem.ApexTheme
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.navigation.ApexApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ApexApplication).container
        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            ApexTheme(themeMode = settings.themeMode, accent = settings.accent) {
                ApexApp()
            }
        }
    }
}
