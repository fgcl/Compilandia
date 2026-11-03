package com.compilandia.ui.desafio

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.tutor.Cor
import com.compilandia.tutor.Desafio
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Chip
import com.compilandia.ui.Header
import com.compilandia.ui.Juice
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.ThemeState
import com.compilandia.ui.pegboard
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesafioScreen(app: AppState) {
    val hoje = remember { nowMs() / 86_400_000L }
    val d = remember { Desafio.doDia(hoje) }
    val palpites = remember { mutableStateListOf<List<String>>() }
    val atual = remember { mutableStateListOf<String>() }
    var venceu by remember { mutableStateOf(false) }
    val jaJogou = app.ls.desafioDia == hoje
    val acabou = venceu || palpites.size >= Desafio.TENTATIVAS || jaJogou
    val clip = LocalClipboardManager.current
    val mono = LocalFonts.current.mono
    Column(Modifier.fillMaxSize()) {
        Header("Desafio do dia", "nº ${d.numero} · sequência ${app.ls.desafioSequencia}", Palette.drawer)
        Column(
            Modifier.weight(1f).fillMaxWidth().background(Palette.paperDark).pegboard(Palette.drawerHi.copy(alpha = 0.14f)).verticalScroll(rememberScrollState()).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Ache a frase secreta de ${d.tamanho} tokens. Só vale frase que a gramática aceita.", style = MaterialTheme.typography.titleSmall)
            Text(d.texto, Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Palette.codeBg).padding(12.dp), style = TextStyle(fontFamily = mono, fontSize = 15.sp, lineHeight = 22.sp, color = Palette.onAccent))
            val lado = (340.dp / d.tamanho).coerceAtMost(56.dp)
            for (l in 0 until Desafio.TENTATIVAS) {
                val p = palpites.getOrNull(l)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (c in 0 until d.tamanho) {
                        if (p != null) {
                            Casa(p[c], Desafio.cores(p, d.segredo)[c], c, lado)
                        } else {
                            val t = if (l == palpites.size) atual.getOrNull(c) else null
                            Casa(t ?: "", null, c, lado)
                        }
                    }
                }
            }
            if (acabou) {
                val texto = if (jaJogou && palpites.isEmpty()) "Você já jogou hoje. Volte amanhã." else if (venceu) "Compilou em ${palpites.size}." else "Era: ${d.segredo.joinToString(" ")}"
                Text(texto, style = MaterialTheme.typography.titleMedium, color = if (venceu) Palette.green else Palette.yellow)
            }
        }
        Column(Modifier.fillMaxWidth().background(Palette.paper).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (acabou) {
                if (palpites.isNotEmpty()) BigButton("Compartilhar", Modifier.fillMaxWidth(), color = Palette.hazard, textColor = Palette.hazardDark) {
                    val t = Desafio.resultado(d, palpites, venceu)
                    clip.setText(AnnotatedString(t))
                    com.compilandia.platform.Compartilha.texto(t)
                    app.say("Resultado copiado.")
                }
                BigButton("Voltar à oficina", Modifier.fillMaxWidth()) { app.home() }
            } else {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in d.teclas) Box(Modifier.testTag("tecla-$t")) {
                        Chip(t, color = Palette.teal) { if (atual.size < d.tamanho) { atual += t; Sound.play(Sfx.PICK) } }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BigButton("Apagar", Modifier.weight(1f), color = Palette.drawer, enabled = atual.isNotEmpty()) { atual.removeAt(atual.lastIndex) }
                    BigButton("Conferir", Modifier.weight(1f), enabled = atual.size == d.tamanho) {
                        val p = atual.toList()
                        if (!Desafio.valido(d, p)) {
                            Juice.wrong()
                            app.say("A gramática não aceita essa frase.")
                        } else {
                            palpites += p
                            atual.clear()
                            if (p == d.segredo) {
                                venceu = true
                                Juice.victory()
                            }
                            if (p == d.segredo || palpites.size >= Desafio.TENTATIVAS) {
                                app.learner.fimDesafio(hoje, palpites.size, p == d.segredo)
                                app.commit()
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Uma casa da grade. A cor aparece com a casa virando, uma de cada vez. */
@Composable
private fun Casa(t: String, cor: Cor?, ordem: Int, lado: Dp) {
    val gira = remember(cor) { Animatable(if (cor == null || ThemeState.reduceMotion) 0f else 90f) }
    var mostra by remember(cor) { mutableStateOf(cor == null || ThemeState.reduceMotion) }
    LaunchedEffect(cor) {
        if (cor != null && !mostra) {
            delay(ordem * 120L)
            Sound.play(Sfx.TICK, 0.9f + ordem * 0.08f)
            mostra = true
            gira.animateTo(0f, tween(240))
        }
    }
    val bg = when {
        !mostra || cor == null -> Palette.white
        cor == Cor.VERDE -> Palette.green
        cor == Cor.AMARELO -> Palette.hazard
        else -> Palette.drawerLo
    }
    val fg = if (mostra && cor != null) Palette.on(bg) else Palette.ink
    Box(Modifier.size(lado).graphicsLayer { rotationX = gira.value }.clip(RoundedCornerShape(6.dp)).background(bg), contentAlignment = Alignment.Center) {
        Text(t, color = fg, style = TextStyle(fontFamily = LocalFonts.current.mono, fontSize = (if (t.length > 3) 11 else 15).sp), maxLines = 1)
    }
}

@Suppress("unused") private val keepH = Modifier.height(0.dp).padding(0.dp)
