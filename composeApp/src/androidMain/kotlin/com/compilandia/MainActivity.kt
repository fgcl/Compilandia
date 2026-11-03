package com.compilandia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.compilandia.platform.Storage
import com.compilandia.ui.App
import com.compilandia.ui.AppState
import com.compilandia.ui.ThemeState

class MainActivity : ComponentActivity() {
    override fun onPause() {
        super.onPause()
        com.compilandia.ui.voice.Narrator.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Storage.dir = filesDir
        com.compilandia.platform.Compartilha.ctx = applicationContext
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!AppState.current.back()) finish()
            }
        })
        setContent {
            App()
            // Ícones da barra de status acompanham o tema do app, não o do sistema.
            val dark = ThemeState.colors.dark
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
}
