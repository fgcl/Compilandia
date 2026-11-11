package com.compilandia.ui.corrida

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.HazardStripe
import com.compilandia.ui.Juice
import com.compilandia.ui.Palette
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.ui.fx.Fx
import com.compilandia.ui.games.GameCtx
import com.compilandia.ui.games.GameHost
import kotlin.random.Random
import com.compilandia.textos.tx

private const val SALAS = 9
private const val INTEGRIDADE = 5

/** A Oficina aparece sozinha antes destas salas (contando de zero): depois da terceira e da sexta. */
private val SALAS_COM_OFICINA = setOf(3, 6)

/** Escudo ignora uma falha, Dica abre uma dica em cada sala, Reparo devolve 2 de integridade, Ímã rende 50% mais. */
internal enum class Peca(val nome: String) {
    ESCUDO(tx("corridascreen_escudo")),
    DICA(tx("corridascreen_dica_extra")),
    REPARO(tx("corridascreen_reparo")),
    IMA(tx("corridascreen_ima")),
}

private enum class Fase { OFICINA, SALA, BALANCO, FIM }


/**
 * Corrida: nove salas com assuntos misturados, uma atrás da outra, sem porta para escolher
 * (mural, rodada 10). O nível de cada sala vem do modelo do aluno. Depois da terceira e da sexta
 * sala, a Oficina oferece uma peça. Misturar assuntos é o ponto: intercalar faz lembrar mais
 * do que treinar um de cada vez. Perder não bloqueia nada: dá para recomeçar na hora.
 */
@Composable
fun CorridaScreen(app: AppState, comecaNaOficina: Boolean = false) {
    val l = app.learner
    val plano = remember { l.dailyMix(nowMs(), SALAS).let { m -> List(SALAS) { m[it % m.size] } } }
    var sala by remember { mutableIntStateOf(0) }
    var vida by remember { mutableIntStateOf(INTEGRIDADE) }
    var fase by remember { mutableStateOf(if (comecaNaOficina) Fase.OFICINA else Fase.SALA) }
    var ganhos by remember { mutableIntStateOf(0) }
    var ultimo by remember { mutableStateOf(0.0) }
    var ganhouSala by remember { mutableIntStateOf(0) }
    val pecas = remember { mutableStateListOf<Peca>() }
    val semente = remember { nowMs() }
    val ofertas = remember(sala) { Peca.entries.shuffled(Random(semente + sala)).take(2) }

    fun avanca() {
        if (vida <= 0 || sala + 1 >= SALAS) {
            fase = Fase.FIM
            l.fimCorrida(sala + 1, ganhos)
            app.commit()
            return
        }
        val prox = sala + 1
        Fx.porta(tx("corridascreen_sala_1_2", prox + 1, SALAS), Palette.drawer) { sala = prox; fase = if (prox in SALAS_COM_OFICINA) Fase.OFICINA else Fase.SALA }
    }

    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        Hud(sala, vida, pecas)
        HazardStripe()
        when (fase) {
            Fase.OFICINA -> Oficina(ofertas) { o ->
                Juice.drop()
                if (o == Peca.REPARO) vida = (vida + 2).coerceAtMost(INTEGRIDADE) else pecas += o
                fase = Fase.SALA
            }
            Fase.SALA -> {
                val m = plano[sala]
                val nivel = m.level.coerceAtMost(5)
                val dicas = if (Peca.DICA in pecas) 1 else 0
                DisposableEffect(sala) {
                    Juice.arenas++
                    onDispose {
                        Juice.arenas--
                        Juice.combo = 0
                    }
                }
                val barra = remember(sala) { com.compilandia.ui.games.BarraDoJogo() }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${m.game.title}: ${m.skill.title}", style = MaterialTheme.typography.titleSmall)
                    key(sala) { androidx.compose.runtime.CompositionLocalProvider(com.compilandia.ui.games.LocalBarraDoJogo provides barra) {
                        GameHost(m.game, GameCtx(m.skill, nivel, Random(semente + sala), dicas) { score ->
                            l.recordAttempt(m.skill, m.game, nivel, score, null, 40_000, dicas, nowMs())
                            ultimo = score
                            val passou = score >= 0.6
                            val base = 10
                            val ima = if (Peca.IMA in pecas) 1.5 else 1.0
                            ganhouSala = if (passou) (base * ima).toInt() else 0
                            ganhos += ganhouSala
                            if (!passou) {
                                if (Peca.ESCUDO in pecas) pecas.remove(Peca.ESCUDO) else vida--
                                Juice.wrong()
                            } else Juice.correct()
                            app.commit()
                            fase = Fase.BALANCO
                        })
                    } }
                }
                com.compilandia.ui.games.AcaoDaBarra(barra)
            }
            Fase.BALANCO -> Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val passou = ultimo >= 0.6
                ByteFigure(Modifier.height(170.dp), if (passou) Mood.HAPPY else Mood.SAD, hop = 1)
                Text(if (passou) tx("corridascreen_sala_limpa_1", ganhouSala) else tx("corridascreen_essa_sala_te_acertou"), style = MaterialTheme.typography.headlineSmall)
                Text(tx("comum_1_acerto", (ultimo * 100).toInt()), style = MaterialTheme.typography.bodyLarge, color = Palette.inkSoft)
                Spacer(Modifier.weight(1f))
                BigButton(if (vida <= 0 || sala + 1 >= SALAS) tx("corridascreen_ver_resultado") else tx("corridascreen_proxima_sala"), Modifier.fillMaxWidth()) { avanca() }
            }
            Fase.FIM -> Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val venceu = vida > 0
                ByteFigure(Modifier.height(190.dp), if (venceu) Mood.HAPPY else Mood.SAD, armsUp = if (venceu) 1f else 0f, hop = 1)
                Text(if (venceu) tx("corridascreen_corrida_completa") else tx("corridascreen_parou_na_sala_1", sala + 1), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(tx("corridascreen_1_engrenagens_melhor_corrida_2", ganhos, app.ls.melhorCorrida), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f))
                BigButton(tx("corridascreen_correr_novo"), Modifier.fillMaxWidth()) { app.replace(com.compilandia.ui.Screen.Corrida) }
                BigButton(tx("comum_voltar_oficina"), Modifier.fillMaxWidth(), color = Palette.drawer) { app.home() }
            }
        }
    }
}

@Composable
private fun Hud(sala: Int, vida: Int, pecas: List<Peca>) {
    Column(Modifier.fillMaxWidth().background(Palette.paper).padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tx("corridascreen_corrida_sala_1_2", sala + 1, SALAS), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 0 until INTEGRIDADE) Box(Modifier.size(22.dp, 13.dp).clip(RoundedCornerShape(3.dp)).background(if (i < vida) Palette.green else Palette.drawerLo))
            }
        }
        if (pecas.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (p in pecas) PecaIcone(p, 26.dp)
        }
    }
}
