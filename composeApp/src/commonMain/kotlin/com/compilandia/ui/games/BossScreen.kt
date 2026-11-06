package com.compilandia.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.learning.BOSS_GEARS
import com.compilandia.learning.Boss
import com.compilandia.learning.GameId
import com.compilandia.learning.Region
import com.compilandia.learning.Skill
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.VSpace
import com.compilandia.ui.fx.ExplosaoChefao
import kotlin.random.Random
import com.compilandia.textos.tx

/**
 * Chefão da região: 3 desafios seguidos, um pouco acima do seu nível, que religam a máquina.
 * É um simulado disfarçado. Errar não bloqueia nada: dá para tentar de novo na hora.
 */
@Composable
fun BossScreen(app: AppState, region: Region) {
    val l = app.learner
    val boss = Boss.of(region)
    val color = Palette.region(region)
    val plan = remember {
        val skills = Skill.entries.filter { it.region == region }.shuffled(Random(nowMs()))
        val used = mutableSetOf<GameId>()
        val out = mutableListOf<Pair<Skill, GameId>>()
        var i = 0
        while (out.size < 3 && i < 12) {
            val s = skills[i % skills.size]
            val g = GameId.forSkill(s).firstOrNull { it !in used } ?: GameId.forSkill(s).first()
            used += g; out += s to g; i++
        }
        out
    }
    var round by remember { mutableIntStateOf(-1) }   // -1 = apresentação
    var hp by remember { mutableFloatStateOf(1f) }
    var lastScore by remember { mutableStateOf<Double?>(null) }
    val hpAnim by animateFloatAsState(hp)
    val seed = remember { nowMs() }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Header("${boss.emoji} ${boss.bossName}", tx("bossscreen_chefao_1", region.title), color)
        val won = hp <= 0.001f
        val lost = round >= plan.size && !won
        MachinePanel(((1f - hpAnim) * MACHINE_LAMPS + 0.01f).toInt(), won, region.title)
        when {
            round == -1 -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(tx("bossscreen_1_quebrou_esta_parte_maquina", boss.bossName), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(tx("bossscreen_tres_desafios_seguidos_pouco_acima"), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                for ((s, g) in plan) Text("${g.title}: ${s.title}", style = MaterialTheme.typography.bodyMedium, color = Palette.inkSoft)
                BigButton(tx("bossscreen_religar"), Modifier.fillMaxWidth(), color = Palette.action) { round = 0; Sound.play(Sfx.WHOOSH) }
            }
            won || lost -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (won) {
                    LaunchedEffect(Unit) { Juice.victory() }
                    com.compilandia.ui.cast.ByteFigure(Modifier.height(170.dp), com.compilandia.ui.cast.Mood.HAPPY, armsUp = 1f, hop = 1)
                    Text(tx("comum_maquina_religada"), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(tx("bossscreen_150_xp_1_engrenagens_2", BOSS_GEARS, region.title), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                } else {
                    com.compilandia.ui.cast.ByteFigure(Modifier.height(170.dp), com.compilandia.ui.cast.Mood.SAD)
                    Text(tx("bossscreen_faltou_pouco"), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(tx("bossscreen_agora_voce_sabe_que_revisar"), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
                BigButton(tx("comum_voltar_oficina"), Modifier.fillMaxWidth()) { app.home() }
                if (!won) BigButton(tx("comum_tentar_novo"), Modifier.fillMaxWidth(), color = Palette.drawer) { app.replace(com.compilandia.ui.Screen.BossFight(region)) }
            }
            lastScore != null -> Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val sc = lastScore!!
                VSpace(20)
                com.compilandia.ui.cast.ByteFigure(Modifier.height(150.dp), if (sc >= 0.5) com.compilandia.ui.cast.Mood.HAPPY else com.compilandia.ui.cast.Mood.SAD, hop = 1)
                Text(if (sc >= 0.5) tx("bossscreen_boa_1_acerto", (sc * 100).toInt()) else tx("bossscreen_essa_nao_acendeu_nada"), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                BigButton(tx("bossscreen_proximo_desafio"), Modifier.fillMaxWidth()) { lastScore = null; round++ }
            }
            else -> {
                val (s, g) = plan[round]
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tx("bossscreen_desafio_1_3_2", round + 1, g.title), style = MaterialTheme.typography.titleMedium)
                    Text(com.compilandia.content.comoJogar[g]?.text ?: "", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                    val level = (l.chooseLevel(s, nowMs()) + 1).coerceAtMost(5)
                    key(round) {
                        GameHost(g, GameCtx(s, level, Random(seed + round), 0) { score ->
                            l.recordAttempt(s, g, level, score, null, 60_000, 0, nowMs())
                            hp = (hp - (score * 0.4).toFloat()).coerceAtLeast(0f)
                            if (hp <= 0.001f) { l.defeatBoss(region); Juice.levelUp() } else if (score >= 0.5) Juice.correct() else Juice.wrong()
                            app.commit()
                            lastScore = score
                            if (hp <= 0.001f || round == plan.lastIndex) { lastScore = null; round = plan.size.coerceAtLeast(round + 1) }
                        })
                    }
                    VSpace(60)
                }
            }
        }
    }
    if (hp <= 0.001f) ExplosaoChefao()
    }
}
