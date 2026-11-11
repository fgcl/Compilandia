package com.compilandia.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.compilandia.learning.Attempt
import com.compilandia.learning.GameId
import com.compilandia.learning.Skill
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.Juice
import com.compilandia.platform.nowMs
import com.compilandia.ui.AppState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import com.compilandia.content.comoJogar
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.Palette
import com.compilandia.ui.PaperCard
import com.compilandia.ui.RichText
import com.compilandia.ui.Screen
import com.compilandia.ui.VSpace
import com.compilandia.ui.screens.playerLevel
import kotlin.random.Random
import com.compilandia.textos.tx

/** O que todo minijogo recebe. */
class GameCtx(
    val skill: Skill,
    val level: Int,
    val rnd: Random,
    val hints: Int,
    val finish: (Double) -> Unit,
)

/** Jogos em que a própria esteira é o palco: fila e pilha. */
val beltGames = setOf(GameId.SHIFT_REDUCE_RUSH, GameId.STACK_TOWER)

private enum class Phase { CONFIDENCE, PLAYING, RESULT }

@Composable
fun PlayScreen(app: AppState, s: Screen.Play) {
    val l = app.learner
    var phase by remember { mutableStateOf(if (app.ls.askConfidence) Phase.CONFIDENCE else Phase.PLAYING) }
    var confidence by remember { mutableStateOf<Double?>(null) }
    var hints by remember { mutableIntStateOf(0) }
    var score by remember { mutableDoubleStateOf(0.0) }
    var attempt by remember { mutableStateOf<Attempt?>(null) }
    var xpBefore by remember { mutableIntStateOf(app.ls.xp) }
    var gearsBefore by remember { mutableIntStateOf(app.ls.gears) }
    var ending by remember { mutableStateOf(false) }
    var melhorSeq by remember { mutableIntStateOf(0) }
    var bonusSeq by remember { mutableIntStateOf(0) }
    // Tentativas desde a última dica: o degrau seguinte só abre depois de tentar.
    val jogadas = remember { Juice.hitTick + Juice.missTick }
    var jogadasNaDica by remember { mutableIntStateOf(jogadas) }
    val start = remember { nowMs() }
    val predicted = remember { l.predict(s.skill, s.level, nowMs()) }
    val color = Palette.region(s.skill.region)
    val rnd = remember(s) { Random(s.seed xor s.game.ordinal.toLong() xor (s.level * 7919L)) }
    val useArena = s.game !in beltGames

    androidx.compose.runtime.DisposableEffect(Unit) {
        Juice.combo = 0
        Juice.bestCombo = 0
        Juice.arenas++
        onDispose {
            Juice.arenas--
            Juice.combo = 0   // a sequência morre com a partida: não aparece em outra tela
        }
    }
    LaunchedEffect(Unit) {
        comoJogar[s.game]?.let { com.compilandia.ui.voice.Narrator.say(it) }
        if (phase == Phase.CONFIDENCE) com.compilandia.ui.voice.Narrator.say(com.compilandia.content.Falas.confianca)
    }
    LaunchedEffect(ending) {
        if (ending) {
            com.compilandia.ui.voice.Narrator.stop()
            kotlinx.coroutines.delay(if (useArena) 800 else 200)
            phase = Phase.RESULT
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Sem subtítulo (mural rodada 4): o nome do jogo basta, e a dica já mostra a conta no botão Dica.
        Header(s.game.title, if (s.queue.isNotEmpty()) tx("playscreen_faltam_1", s.queue.size) else null, color)
        when (phase) {
            Phase.CONFIDENCE -> Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                com.compilandia.ui.cast.ByteFigure(Modifier.padding(top = 10.dp).height(170.dp))
                Text(comoJogar[s.game]?.text ?: "", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Text(tx("playscreen_voce_sabe_isso"), style = MaterialTheme.typography.headlineSmall)
                val opts = listOf(
                    Triple(tx("playscreen_vou_chutar"), 0.2, Palette.red),
                    Triple(tx("comum_acho_que_sei"), 0.6, Palette.yellow),
                    Triple(tx("comum_tenho_certeza"), 0.9, Palette.green),
                )
                for ((label, v, c) in opts) BigButton(label, Modifier.fillMaxWidth(), color = c) {
                    confidence = v; phase = Phase.PLAYING; Sound.play(Sfx.CLICK)
                }
                Text(
                    tx("comum_pular"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable { phase = Phase.PLAYING }.padding(6.dp),
                )
            }
            Phase.PLAYING -> {
                if (useArena) GameArena(defeated = ending && score >= 0.6, escaped = ending && score < 0.6)
                val barra = remember { BarraDoJogo() }
                Box(Modifier.weight(1f)) {
                    val ctx = GameCtx(s.skill, s.level, rnd, hints) { sc ->
                        if (!ending) {
                            score = sc.coerceIn(0.0, 1.0)
                            xpBefore = app.ls.xp
                            gearsBefore = app.ls.gears
                            attempt = l.recordAttempt(s.skill, s.game, s.level, score, confidence, nowMs() - start, hints, nowMs())
                            l.noteCombo(Juice.bestCombo, nowMs())
                            // o multiplicador da melhor sequência vira engrenagens
                            melhorSeq = Juice.bestCombo
                            bonusSeq = com.compilandia.ui.multDe(melhorSeq) - 1
                            l.bonusOverclock(bonusSeq)
                            app.commit()
                            if (score >= 0.6) Juice.levelUp() else Juice.wrong()
                            ending = true
                        }
                    }
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.runtime.CompositionLocalProvider(LocalBarraDoJogo provides barra) { GameHost(s.game, ctx) }
                        VSpace(24)
                    }
                }
                // A ação da rodada (Conferir, Continuar) fica em cima da barra, perto do polegar.
                barra.acao?.let { a ->
                    Box(Modifier.fillMaxWidth().background(Palette.paperDark).padding(start = 8.dp, end = 8.dp, top = 8.dp)) {
                        BigButton(a.texto, Modifier.fillMaxWidth().testTag("acao-do-jogo"), color = a.cor, textColor = a.corTexto, enabled = a.ativo) { a.faz() }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().background(Palette.paperDark).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val tentou = Juice.hitTick + Juice.missTick > jogadasNaDica
                    val travada = hints >= 1 && !tentou
                    BigButton(if (hints == 0) tx("comum_dica") else tx("playscreen_dica_1", hints + 1), small = true, color = if (travada) Palette.drawer else Palette.yellow) {
                        when {
                            travada -> app.say(tx("playscreen_tente_vez_proxima_dica_abre"))
                            hints >= 2 -> app.go(Screen.Episode(s.skill, returnToGame = true))
                            else -> {
                                hints++
                                jogadasNaDica = Juice.hitTick + Juice.missTick
                                Sound.play(Sfx.WHOOSH)
                            }
                        }
                    }
                    BigButton(tx("comum_nao_entendi"), small = true, color = Palette.white, textColor = Palette.ink) {
                        app.go(Screen.Episode(s.skill, returnToGame = true))
                    }
                    BigButton(tx("playscreen_trocar_jogo"), small = true, color = Palette.white, textColor = Palette.ink) {
                        val next = l.bored(s.skill, s.game, nowMs())
                        app.commit()
                        app.say(tx("playscreen_mesma_materia_outro_jogo_1", next.title))
                        app.replace(s.copy(game = next, seed = nowMs()))
                    }
                }
            }
            Phase.RESULT -> ResultView(app, s, score, confidence, predicted, attempt, app.ls.xp - xpBefore, app.ls.gears - gearsBefore, melhorSeq, bonusSeq)
        }
    }
}

@Composable
private fun ResultView(app: AppState, s: Screen.Play, score: Double, confidence: Double?, predicted: Double, attempt: Attempt?, xpGained: Int, gearsGained: Int, sequencia: Int, bonus: Int) {
    val l = app.learner
    val stars = when { score >= 0.95 -> 3; score >= 0.7 -> 2; score >= 0.4 -> 1; else -> 0 }
    val levelNow = playerLevel(app.ls.xp)
    val levelBefore = playerLevel(app.ls.xp - xpGained)
    var details by remember { mutableStateOf(false) }
    val won = score >= 0.6
    LaunchedEffect(Unit) { com.compilandia.ui.voice.Narrator.say((if (won) com.compilandia.content.Falas.vitoria else com.compilandia.content.Falas.derrota).random()) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RewardHero(stars, xpGained, gearsGained, if (won) tx("playscreen_bug_eliminado") else tx("playscreen_bug_fugiu"), sequencia = sequencia, bonus = bonus)
        if (levelNow > levelBefore) Text(tx("playscreen_nivel_1", levelNow), style = MaterialTheme.typography.headlineSmall, color = Palette.yellow)
        val offer = remember { l.affordableItem() }
        if (offer != null) RoomOffer(offer) {
            if (l.buy(offer)) {
                app.justBought = offer.id
                app.commit()
                app.home()
            }
        }
        if (score < 0.6) {
            BigButton(tx("playscreen_ver_aula_novo"), Modifier.fillMaxWidth(), color = Palette.region(s.skill.region)) {
                app.replace(Screen.Episode(s.skill))
            }
        }
        if (s.queue.isNotEmpty()) {
            val n = s.queue.first()
            BigButton(tx("playscreen_proximo_1", n.skill.title), Modifier.fillMaxWidth()) {
                app.replace(Screen.Play(n.skill, n.game, n.level, s.queue.drop(1), nowMs()))
            }
        } else {
            BigButton(tx("playscreen_coletar"), Modifier.fillMaxWidth()) { app.home() }
            BigButton(tx("playscreen_mais"), Modifier.fillMaxWidth(), color = Palette.drawer) {
                app.replace(s.copy(level = l.chooseLevel(s.skill, nowMs()), seed = nowMs(), game = l.chooseGame(s.skill)))
            }
        }
        Text(
            if (details) tx("playscreen_esconder_detalhes") else tx("playscreen_ver_detalhes"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { details = !details }.padding(6.dp),
        )
        if (details) PaperCard(Modifier.fillMaxWidth(), color = Palette.paper) {
            Text(tx("comum_1_acerto", (score * 100).toInt()), style = MaterialTheme.typography.titleSmall)
            if (confidence != null) {
                val gap = confidence - score
                Text(
                    when {
                        gap > 0.4 -> tx("playscreen_voce_achou_que_sabia_mais")
                        gap < -0.4 -> tx("playscreen_voce_sabia_mais_que_imaginava")
                        else -> tx("playscreen_sua_confianca_bateu_com_resultado")
                    }, style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(tx("playscreen_rede_previa_1_chance_voce", (predicted * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            val mem = l.skill(s.skill).memory
            Text(tx("playscreen_proxima_revisao_em_1", formatDays(mem.intervalDays())), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            Text(tx("playscreen_dominio_1", (l.mastery(s.skill) * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
        }
    }
    @Suppress("UNUSED_EXPRESSION") attempt
}

@Composable
private fun RoomOffer(item: com.compilandia.learning.RoomItem, onPlace: () -> Unit) {
    val layer = com.compilandia.ui.room.roomLayers[item.id]
    PaperCard(Modifier.fillMaxWidth(), color = Palette.yellow.copy(alpha = 0.12f), border = Palette.yellow) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (layer != null) androidx.compose.foundation.Image(org.jetbrains.compose.resources.painterResource(layer.art), null, Modifier.size(72.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(tx("playscreen_novo_na_oficina"), style = MaterialTheme.typography.labelMedium, color = Palette.yellow)
                Text(item.title, style = MaterialTheme.typography.titleMedium)
            }
        }
        BigButton(tx("playscreen_colocar_na_sala_1", item.price), Modifier.fillMaxWidth(), color = Palette.hazard, onClick = onPlace)
    }
}

fun formatDays(d: Double): String = when {
    d < 1.0 / 24 -> tx("playscreen_alguns_minutos")
    d < 1.0 -> "${(d * 24).toInt().coerceAtLeast(1)} h"
    d < 2.0 -> "1 dia"
    else -> tx("playscreen_1_dias", d.toInt())
}

/** Despacha para o minijogo certo. */
@Composable
fun GameHost(game: GameId, ctx: GameCtx) {
    when (game) {
        GameId.PIPELINE -> PipelineGame(ctx)
        GameId.PHASE_DETECTIVE -> PhaseDetectiveGame(ctx)
        GameId.TOKEN_SLICER -> TokenSlicerGame(ctx)
        GameId.DFA_TRAIN -> DfaTrainGame(ctx)
        GameId.REGEX_SORTER -> RegexSorterGame(ctx)
        GameId.DERIVATION_RACE -> DerivationRaceGame(ctx)
        GameId.TWO_TREES -> TwoTreesGame(ctx)
        GameId.GRAMMAR_SURGERY -> GrammarSurgeryGame(ctx)
        GameId.FIRST_FOLLOW_ALCHEMY -> AlchemyGame(ctx)
        GameId.LL1_SUDOKU -> LL1SudokuGame(ctx)
        GameId.STACK_TOWER -> StackTowerGame(ctx)
        GameId.SHIFT_REDUCE_RUSH -> ShiftReduceGame(ctx)
        GameId.ITEM_BUILDER -> ItemBuilderGame(ctx)
        GameId.CONFLICT_HUNTER -> ConflictHunterGame(ctx)
        GameId.BISON_WORKSHOP -> BisonWorkshopGame(ctx)
        GameId.ATTRIBUTE_TREE -> AttributeTreeGame(ctx)
        GameId.SCOPE_DETECTIVE -> ScopeDetectiveGame(ctx)
        GameId.TAC_ASSEMBLY -> TacAssemblyGame(ctx)
        GameId.SUBSET_FACTORY -> SubsetFactoryGame(ctx)
        GameId.FLEX_WORKSHOP -> FlexWorkshopGame(ctx)
        GameId.PANIC_ROOM -> PanicRoomGame(ctx)
        GameId.CALL_STACK -> CallStackGame(ctx)
        GameId.MIPS_FORGE -> MipsForgeGame(ctx)
        GameId.DESCENT_CODER -> DescentCoderGame(ctx)
        GameId.MIN_TABLE -> MinTableGame(ctx)
        GameId.AST_FACTORY -> AstFactoryGame(ctx)
        GameId.REG_COLORING -> RegColoringGame(ctx)
        GameId.BRANCH_MAZE -> BranchMazeGame(ctx)
        GameId.AUTOMATON_FACTORY -> AutomatonFactoryGame(ctx)
        GameId.FLASHCARDS -> Text(tx("playscreen_use_revisao_relampago_no_mapa"))
    }
}

/** Gerencia N rodadas e devolve a média para ctx.finish. */
@Composable
fun Rounds(ctx: GameCtx, total: Int, content: @Composable (round: Int, done: (Double) -> Unit) -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val scores = remember { mutableListOf<Double>() }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(tx("playscreen_rodada_1_2", round + 1, total), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.weight(1f))
        for (i in 0 until total) Box(
            Modifier.padding(2.dp).clip(RoundedCornerShape(50)).background(
                when {
                    i < scores.size -> if (scores[i] >= 0.6) Palette.green else Palette.red
                    i == round -> Palette.yellow
                    else -> Palette.paperDark
                }
            ).border(1.dp, Palette.ink, RoundedCornerShape(50)).padding(6.dp)
        )
    }
    androidx.compose.runtime.key(round) {
        content(round) { sc ->
            scores += sc
            if (scores.size >= total) ctx.finish(scores.average()) else round++
        }
    }
}

/** Barra inferior padrão de "conferir / próxima" dentro de um jogo. */
@Composable
fun CheckOrNext(checked: Boolean, canCheck: Boolean = true, onCheck: () -> Unit, onNext: () -> Unit) {
    if (!checked) BotaoDoJogo(tx("comum_conferir_2"), Modifier.fillMaxWidth(), color = Palette.teal, enabled = canCheck) { onCheck() }
    else BotaoDoJogo(tx("comum_continuar_2"), Modifier.fillMaxWidth(), color = Palette.orange) { onNext() }
}

@Composable
fun HintBox(ctx: GameCtx, vararg hints: String) {
    if (ctx.hints <= 0 || hints.isEmpty()) return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        // Só as dicas abertas. O próximo degrau mora no botão Dica, que fica cinza até você tentar.
        hints.take(ctx.hints).forEachIndexed { i, h -> Degrau(i + 1, h) }
    }
}

@Composable
private fun Degrau(n: Int, texto: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Palette.white).padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(26.dp).clip(RoundedCornerShape(13.dp)).background(Palette.hazard), contentAlignment = Alignment.Center) {
            Text("$n", color = Palette.hazardDark, style = MaterialTheme.typography.labelMedium)
        }
        RichText(texto, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Palette.ink)
    }
}
