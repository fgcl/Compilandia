package com.compilandia

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.compilandia.ui.App

/** Versão web: o mesmo App do celular, desenhado num canvas pelo Compose. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("jogo") { App() }
}
