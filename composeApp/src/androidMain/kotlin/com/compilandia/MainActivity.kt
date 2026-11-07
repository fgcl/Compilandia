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
    /** R4: o pedido de permissão de notificar. A resposta volta para quem pediu. */
    private var aoResponder: ((Boolean) -> Unit)? = null
    private val pedeNotificacao = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { ok ->
        aoResponder?.invoke(ok)
        aoResponder = null
    }

    override fun onPause() {
        super.onPause()
        com.compilandia.ui.voice.Narrator.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Storage.dir = filesDir
        com.compilandia.platform.Compartilha.ctx = applicationContext
        com.compilandia.platform.Idioma.ctx = applicationContext
        com.compilandia.platform.Lembrete.ctx = applicationContext
        com.compilandia.platform.Lembrete.pedir = { resposta ->
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                aoResponder = resposta
                pedeNotificacao.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else resposta(true)
        }
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
