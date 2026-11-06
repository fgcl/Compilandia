package com.compilandia.ui.corrida

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.learning.Mission
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
import com.compilandia.ui.pegboard
import kotlin.random.Random
import com.compilandia.textos.tx

private const val SALAS = 9
private const val INTEGRIDADE = 5

private enum class Peca(val nome: String, val desc: String) {
    ESCUDO(tx("corridascreen_escudo"), tx("corridascreen_ignora_falha")),
    DICA(tx("corridascreen_dica_extra"), tx("corridascreen_cada_sala_comeca_com_dica")),
    REPARO(tx("corridascreen_reparo"), tx("corridascreen_devolve_2_integridade")),
    IMA(tx("corridascreen_ima"), tx("corridascreen_rende_50_mais_engrenagens")),
}

private enum class Fase { PORTAS, OFICINA, SALA, BALANCO, FIM }

private data class Porta(val titulo: String, val desc: String, val premio: String, val cor: Color, val corEscura: Color, val perigosa: Boolean, val oficina: Boolean)

/**
 * Corrida de portas: nove salas com assuntos misturados. Cada escolha muda o risco e o prêmio.
 * Misturar assuntos é o ponto: intercalar faz lembrar mais do que treinar um de cada vez.
 * Perder a corrida não bloqueia nada: dá para recomeçar na hora.
 */
@Composable
fun CorridaScreen(app: AppState) {
    val l = app.learner
    val plano = remember { l.dailyMix(nowMs(), SALAS).let { m -> List(SALAS) { m[it % m.size] } } }
    var sala by remember { mutableIntStateOf(0) }
    var vida by remember { mutableIntStateOf(INTEGRIDADE) }
    var fase by remember { mutableStateOf(Fase.PORTAS) }
    var perigosa by remember { mutableStateOf(false) }
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
        Fx.porta(tx("corridascreen_sala_1_2", prox + 1, SALAS), Palette.drawer) { sala = prox; fase = Fase.PORTAS }
    }

    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        Hud(sala, vida, pecas)
        HazardStripe()
        when (fase) {
            Fase.PORTAS -> Portas(plano[sala], onEscolhe = { p ->
                perigosa = p.perigosa
                if (p.oficina) fase = Fase.OFICINA else fase = Fase.SALA
            })
            Fase.OFICINA -> Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ByteFigure(Modifier.height(150.dp), Mood.HAPPY)
                Text(tx("corridascreen_oficina_escolha_peca"), style = MaterialTheme.typography.headlineSmall)
                for (o in ofertas) Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Palette.white).clickable {
                        Juice.drop()
                        if (o == Peca.REPARO) vida = (vida + 2).coerceAtMost(INTEGRIDADE) else pecas += o
                        avanca()
                    }.padding(14.dp),
                ) {
                    Text(o.nome, style = MaterialTheme.typography.titleMedium)
                    Text(o.desc, style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
                }
            }
            Fase.SALA -> {
                val m = plano[sala]
                val nivel = (m.level + if (perigosa) 1 else 0).coerceAtMost(5)
                val dicas = if (Peca.DICA in pecas && !perigosa) 1 else 0
                DisposableEffect(sala) {
                    Juice.arenas++
                    onDispose {
                        Juice.arenas--
                        Juice.combo = 0
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${m.game.title}: ${m.skill.title}", style = MaterialTheme.typography.titleSmall)
                    key(sala) {
                        GameHost(m.game, GameCtx(m.skill, nivel, Random(semente + sala), dicas) { score ->
                            l.recordAttempt(m.skill, m.game, nivel, score, null, 40_000, dicas, nowMs())
                            ultimo = score
                            val passou = score >= 0.6
                            val base = if (perigosa) 20 else 10
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
                    }
                }
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
            for (p in pecas) Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Palette.white).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(p.nome, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.Portas(m: Mission, onEscolhe: (Porta) -> Unit) {
    val portas = listOf(
        Porta(tx("corridascreen_tranquila"), tx("corridascreen_1_nivel_2", m.game.title, m.level), "+10", Color(0xFF2E7D4F), Color(0xFF17472D), false, false),
        Porta(tx("corridascreen_perigosa"), tx("corridascreen_mais_dificil_sem_dica"), "+20", Color(0xFFB23A2A), Color(0xFF6A1F15), true, false),
        Porta(tx("comum_oficina"), tx("corridascreen_pule_sala_pegue_peca"), tx("corridascreen_peca"), Color(0xFF1F6F8F), Color(0xFF10435A), false, true),
    )
    Row(
        Modifier.weight(1f).fillMaxWidth().pegboard(Palette.drawerHi.copy(alpha = 0.14f)).padding(horizontal = 10.dp).padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom,
    ) {
        portas.forEachIndexed { i, p ->
            Box(
                Modifier.weight(1f).fillMaxHeight(if (i == 1) 0.96f else 0.84f).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(p.cor, p.corEscura))).clickable { Juice.click(); onEscolhe(p) }.padding(9.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(p.titulo, color = Color.White, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(p.desc, color = Color.White, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    Text(p.premio, color = Palette.hazard, style = MaterialTheme.typography.headlineSmall)
                }
                Box(Modifier.align(Alignment.CenterEnd).size(13.dp).clip(CircleShape).background(Palette.hazard))
            }
        }
    }
    Text(
        tx("corridascreen_escolha_porta_assuntos_misturados_ficam"),
        Modifier.fillMaxWidth().background(Palette.paper).padding(12.dp), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
    )
    @Suppress("UNUSED_EXPRESSION") Offset.Zero
    @Suppress("UNUSED_EXPRESSION") Size.Zero
    Spacer(Modifier.width(0.dp))
}
