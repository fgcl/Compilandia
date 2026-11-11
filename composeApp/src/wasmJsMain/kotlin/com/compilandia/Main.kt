package com.compilandia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.compilandia.ui.App
import com.compilandia.ui.AppState

/** Versão web: o mesmo App do celular, desenhado num canvas pelo Compose. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("jogo") {
        App()
        VoltarDoNavegador()
    }
}

private fun empilhaEntrada() { js("try { history.pushState({ compilandia: true }, ''); } catch (e) {}") }
private fun voltaNoNavegador() { js("try { history.back(); } catch (e) {}") }
private fun aoVoltarNoNavegador(f: () -> Unit) { js("window.addEventListener('popstate', function () { f(); })") }

/**
 * O botão voltar do navegador (e o gesto de voltar do celular, no navegador) volta uma tela do jogo,
 * como no Android. O histórico guarda uma entrada só enquanto há para onde voltar: na sala,
 * voltar sai da página, como em qualquer site.
 */
@Composable
private fun VoltarDoNavegador() {
    LaunchedEffect(Unit) {
        val app = AppState.current
        var armado = false        // há uma entrada nossa no histórico
        var ignorar = false       // o próximo popstate fomos nós que pedimos
        aoVoltarNoNavegador {
            if (ignorar) { ignorar = false; return@aoVoltarNoNavegador }
            armado = false
            app.back()
            if (app.stack.size > 1) { empilhaEntrada(); armado = true }
        }
        snapshotFlow { app.stack.size }.collect { n ->
            if (n > 1 && !armado) { empilhaEntrada(); armado = true }
            else if (n <= 1 && armado) { armado = false; ignorar = true; voltaNoNavegador() }
        }
    }
}
