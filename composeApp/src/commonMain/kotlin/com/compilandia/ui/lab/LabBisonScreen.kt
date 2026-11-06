package com.compilandia.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.compilandia.content.linhasDoEstado
import com.compilandia.engine.LRAction
import com.compilandia.engine.MiniBison
import com.compilandia.engine.isComplete
import com.compilandia.engine.nextSymbol
import com.compilandia.engine.render
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.ui.AppState
import com.compilandia.ui.BigButton
import com.compilandia.ui.Header
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.Palette
import com.compilandia.ui.VSpace
import com.compilandia.ui.cast.ByteFigure
import com.compilandia.ui.cast.Mood
import com.compilandia.textos.tx

/** Os .y prontos do laboratório. Cada um mostra uma coisa diferente do Bison. */
object BisonExemplos {
    val calculadora = """
        %token NUM
        %left '+' '-'
        %left '*' '/'
        %%
        e: e '+' e { ${'$'}${'$'} = ${'$'}1 + ${'$'}3; }
         | e '-' e { ${'$'}${'$'} = ${'$'}1 - ${'$'}3; }
         | e '*' e { ${'$'}${'$'} = ${'$'}1 * ${'$'}3; }
         | e '/' e { ${'$'}${'$'} = ${'$'}1 / ${'$'}3; }
         | NUM ;
    """.trimIndent()

    val semPrecedencia = """
        %token NUM
        %%
        e: e '+' e { ${'$'}${'$'} = ${'$'}1 + ${'$'}3; }
         | e '*' e { ${'$'}${'$'} = ${'$'}1 * ${'$'}3; }
         | NUM ;
    """.trimIndent()

    val potencia = """
        %token NUM
        %left '+'
        %left '*'
        %right '^'
        %%
        e: e '+' e { ${'$'}${'$'} = ${'$'}1 + ${'$'}3; }
         | e '*' e { ${'$'}${'$'} = ${'$'}1 * ${'$'}3; }
         | e '^' e { ${'$'}${'$'} = ${'$'}1 ^ ${'$'}3; }
         | NUM ;
    """.trimIndent()

    val menosUnario = """
        %token NUM
        %left '+' '-'
        %left '*'
        %right UMINUS
        %%
        e: e '+' e { ${'$'}${'$'} = ${'$'}1 + ${'$'}3; }
         | e '-' e { ${'$'}${'$'} = ${'$'}1 - ${'$'}3; }
         | e '*' e { ${'$'}${'$'} = ${'$'}1 * ${'$'}3; }
         | '-' e %prec UMINUS { ${'$'}${'$'} = -${'$'}2; }
         | NUM ;
    """.trimIndent()

    val todos = listOf(
        Triple(tx("labbisonscreen_calculadora"), calculadora, "2+3*4"),
        Triple(tx("labbisonscreen_sem_precedencia"), semPrecedencia, "2+3*4"),
        Triple(tx("labbisonscreen_potencia"), potencia, "2^3^2"),
        Triple(tx("labbisonscreen_menos_unario"), menosUnario, "-5+2"),
    )
}

private val PREC = listOf("%left", "%right", "%nonassoc")

/** Índices das linhas de precedência, na ordem do arquivo (de cima para baixo = da mais fraca para a mais forte). */
private fun linhasDePrecedencia(spec: String): List<Int> {
    val linhas = spec.lines()
    val fim = linhas.indexOfFirst { it.trim() == "%%" }.let { if (it < 0) linhas.size else it }
    return (0 until fim).filter { i -> PREC.any { linhas[i].trim().startsWith(it) } }
}

private fun trocaLinhas(spec: String, a: Int, b: Int): String {
    val l = spec.lines().toMutableList()
    val t = l[a]; l[a] = l[b]; l[b] = t
    return l.joinToString("\n")
}

/** %left vira %right, que vira %nonassoc, que volta a %left. */
private fun giraAssociatividade(spec: String, i: Int): String {
    val l = spec.lines().toMutableList()
    val atual = PREC.first { l[i].trim().startsWith(it) }
    val prox = PREC[(PREC.indexOf(atual) + 1) % PREC.size]
    l[i] = l[i].replaceFirst(atual, prox)
    return l.joinToString("\n")
}

private enum class Aba(val titulo: String) { PASSOS(tx("labbisonscreen_passo_passo")), CONFLITOS(tx("comum_conflitos")), ESTADOS(tx("labbisonscreen_estados")) }

/**
 * O Laboratório Bison: o .y em blocos, o veredito ao vivo e três jeitos de olhar o parser.
 * Toda mudança no arquivo roda o motor de novo na hora.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabBisonScreen(app: AppState, inicial: String?, abaInicial: Int = 0, passoInicial: Int = 0) {
    val fonts = LocalFonts.current
    var spec by remember { mutableStateOf(inicial ?: BisonExemplos.calculadora) }
    var entrada by remember { mutableStateOf(BisonExemplos.todos.firstOrNull { it.second == inicial }?.third ?: "2+3*4") }
    var editando by remember { mutableStateOf(false) }
    var aba by remember { mutableStateOf(Aba.entries[abaInicial]) }
    var passo by remember { mutableIntStateOf(passoInicial) }

    val compilado = remember(spec) { runCatching { MiniBison.compile(spec) } }
    val bison = compilado.getOrNull()
    val quadros = remember(bison, entrada) { bison?.quadros(entrada).orEmpty() }
    val resultado = remember(bison, entrada) { bison?.run(entrada) }
    if (passo > quadros.lastIndex) passo = quadros.lastIndex.coerceAtLeast(0)

    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        Header(tx("comum_laboratorio_bison"), tx("labbisonscreen_mexa_no_y_veja_parser"))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Exemplos prontos
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((nome, s, e) in BisonExemplos.todos) {
                    EtiquetaBison(nome, spec == s) { spec = s; entrada = e; passo = 0; Sound.play(Sfx.CLICK) }
                }
            }

            // O veredito: o Byte lê o resultado do motor
            Veredito(bison, compilado.exceptionOrNull()?.message, entrada, resultado)

            // Declarações: a régua de precedência
            Bloco(tx("labbisonscreen_declaracoes")) {
                val linhas = spec.lines()
                val fim = linhas.indexOfFirst { it.trim() == "%%" }.let { if (it < 0) linhas.size else it }
                for (i in 0 until fim) {
                    val t = linhas[i].trim()
                    if (t.isNotEmpty() && PREC.none { t.startsWith(it) }) LinhaMono(t, Palette.inkSoft)
                }
                val prec = linhasDePrecedencia(spec)
                if (prec.isEmpty()) Text(tx("labbisonscreen_sem_left_nem_right_toque"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
                else Text(tx("labbisonscreen_arraste_pela_alca_mais_embaixo"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
                Regua(spec, prec, onTroca = { a, b -> spec = trocaLinhas(spec, a, b); passo = 0; Sound.play(Sfx.DROP) }, onGira = { spec = giraAssociatividade(spec, it); passo = 0; Sound.play(Sfx.CLICK) })
            }

            // Regras em blocos
            Bloco("REGRAS") {
                val linhas = spec.lines()
                val ini = linhas.indexOfFirst { it.trim() == "%%" }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (l in (if (ini < 0) emptyList() else linhas.drop(ini + 1)).map { it.trim() }.filter { it.isNotEmpty() && it != "%%" }) {
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Palette.teal.copy(alpha = 0.18f)).border(1.dp, Palette.teal.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Text(l, fontFamily = fonts.mono, fontSize = 12.sp, color = Palette.ink)
                        }
                    }
                }
                Text(if (editando) tx("labbisonscreen_fechar_texto") else tx("labbisonscreen_editar_texto_y"), color = Palette.yellow, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable { editando = !editando }.padding(vertical = 4.dp))
                if (editando) {
                    BasicTextField(
                        spec, { spec = it; passo = 0 },
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(10.dp).testTag("lab-bison-texto"),
                        textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 13.sp, color = Color(0xFFE8E4D8)),
                        cursorBrush = SolidColor(Palette.yellow),
                    )
                }
            }

            // Entrada
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tx("comum_entrada"), color = Palette.inkSoft, style = MaterialTheme.typography.labelMedium)
                BasicTextField(
                    entrada, { entrada = it.take(24); passo = 0 },
                    Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(horizontal = 10.dp, vertical = 8.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 16.sp, color = Color(0xFFE8E4D8)),
                    cursorBrush = SolidColor(Palette.yellow),
                )
            }

            // As três abas
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (a in Aba.entries) {
                    val n = if (a == Aba.CONFLITOS) bison?.table?.rawConflictCells?.size ?: 0 else -1
                    EtiquetaBison(if (n > 0) "${a.titulo} ($n)" else a.titulo, aba == a) { aba = a; Sound.play(Sfx.CLICK) }
                }
            }
            if (bison != null) when (aba) {
                Aba.PASSOS -> TresPilhas(quadros, passo, onPasso = { passo = it.coerceIn(0, quadros.lastIndex.coerceAtLeast(0)) })
                Aba.CONFLITOS -> Conflitos(bison)
                Aba.ESTADOS -> Estados(bison, quadros.getOrNull(passo))
            }
            VSpace(30)
        }
    }
}

@Composable
private fun EtiquetaBison(texto: String, ligada: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(6.dp)).background(if (ligada) Palette.hazard else Palette.paper)
            .border(1.5.dp, if (ligada) Palette.hazard else Palette.drawer, RoundedCornerShape(6.dp))
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp),
    ) { Text(texto, color = if (ligada) Palette.hazardDark else Palette.ink, style = MaterialTheme.typography.labelMedium) }
}

@Composable
private fun Bloco(titulo: String, conteudo: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(titulo, color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall)
        conteudo()
    }
}

@Composable
private fun LinhaMono(t: String, cor: Color = Palette.ink, negrito: Boolean = false) {
    Text(t, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = cor, fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal)
}

/** A régua: cada linha de precedência é uma barra que se arrasta pela alça. */
@Composable
private fun Regua(spec: String, prec: List<Int>, onTroca: (Int, Int) -> Unit, onGira: (Int) -> Unit) {
    val linhas = spec.lines()
    val alturaPx = with(LocalDensity.current) { 46.dp.toPx() }
    var arrastando by remember { mutableStateOf<String?>(null) }
    var desloc by remember { mutableFloatStateOf(0f) }
    val atual by androidx.compose.runtime.rememberUpdatedState(prec)
    val textoAtual by androidx.compose.runtime.rememberUpdatedState(linhas)
    prec.forEachIndexed { pos, i ->
        val texto = linhas[i].trim()
        key(texto) {
            val forte = pos == prec.lastIndex && prec.size > 1
            Row(
                Modifier.fillMaxWidth().height(42.dp).zIndex(if (arrastando == texto) 1f else 0f)
                    .graphicsLayer { translationY = if (arrastando == texto) desloc else 0f }
                    .clip(RoundedCornerShape(6.dp)).background(if (arrastando == texto) Palette.hazard.copy(alpha = 0.3f) else Palette.yellow.copy(alpha = 0.10f + 0.12f * pos / prec.size.coerceAtLeast(1)))
                    .border(1.5.dp, Palette.yellow.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(42.dp).testTag("alca-$pos").pointerInput(texto) {
                        detectDragGestures(
                            onDragStart = { arrastando = texto; desloc = 0f },
                            onDragEnd = { arrastando = null; desloc = 0f },
                            onDragCancel = { arrastando = null; desloc = 0f },
                        ) { ch, d ->
                            ch.consume()
                            desloc += d.y
                            val agora = atual.indexOfFirst { textoAtual[it].trim() == texto }
                            if (desloc > alturaPx / 2 && agora in 0 until atual.lastIndex) { onTroca(atual[agora], atual[agora + 1]); desloc -= alturaPx }
                            else if (desloc < -alturaPx / 2 && agora > 0) { onTroca(atual[agora], atual[agora - 1]); desloc += alturaPx }
                        }
                    },
                    contentAlignment = Alignment.Center,
                ) { Text("≡", color = Palette.ink, fontSize = 22.sp) }
                val kind = PREC.first { texto.startsWith(it) }
                Box(Modifier.clip(RoundedCornerShape(5.dp)).background(Palette.hazard).clickable { onGira(i) }.padding(horizontal = 7.dp, vertical = 3.dp)) {
                    Text(kind, fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Palette.hazardDark, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(texto.removePrefix(kind).trim(), Modifier.weight(1f), fontFamily = LocalFonts.current.mono, fontSize = 14.sp, color = Palette.ink, fontWeight = FontWeight.Bold)
                Text(if (forte) "forte" else if (pos == 0) "fraco" else "", color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 10.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** O Byte lê o que o motor disse e fala em uma linha. */
@Composable
private fun Veredito(bison: MiniBison?, erro: String?, entrada: String, r: MiniBison.RunResult?) {
    val ok = bison != null && bison.conflicts.isEmpty() && r?.accepted == true
    val cor = when {
        bison == null -> Palette.red
        bison.conflicts.isNotEmpty() -> Palette.yellow
        r?.accepted == true -> Palette.green
        else -> Palette.red
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper).border(2.dp, cor, RoundedCornerShape(8.dp)).padding(10.dp).testTag("lab-bison-veredito"),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ByteFigure(Modifier.width(52.dp), if (ok) Mood.HAPPY else if (bison == null) Mood.SAD else Mood.THINK, animate = false)
        Column(Modifier.weight(1f)) {
            if (bison == null) {
                Text(tx("labbisonscreen_y_nao_compilou"), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
                Text(erro ?: "", style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
            } else {
                val n = bison.conflicts.size
                val resolvidos = bison.resolvedByPrecedence
                Text(
                    tx("labbisonscreen_1_2_estados", if (n == 0) tx("labbisonscreen_0_conflitos") else tx("labbisonscreen_1_conflito_2", n, if (n > 1) "s" else ""), bison.table.automaton.states.size),
                    style = MaterialTheme.typography.titleMedium, color = Palette.ink,
                )
                if (resolvidos > 0) Text(tx("labbisonscreen_precedencia_decidiu_1_celula_2", resolvidos, if (resolvidos > 1) "s" else ""), style = MaterialTheme.typography.bodySmall, color = Palette.inkSoft)
                val res = if (r == null) "" else if (r.accepted) "$entrada  →  ${r.value}" else "$entrada  →  ${r.error}"
                Text(res, fontFamily = LocalFonts.current.mono, fontSize = 14.sp, color = if (r?.accepted == true) Palette.yellow else Palette.red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** B2: as três pilhas lado a lado, como no Visual Yacc: estados, símbolos e valores. */
@Composable
private fun TresPilhas(quadros: List<MiniBison.Quadro>, passo: Int, onPasso: (Int) -> Unit) {
    val q = quadros.getOrNull(passo)
    if (q == null) {
        Text(tx("labbisonscreen_entrada_nao_virou_tokens_use"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
        return
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper).padding(10.dp).testTag("lab-bison-pilhas"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tx("labbisonscreen_falta_ler"), color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { q.entrada.forEachIndexed { i, t -> Celula(t, if (i == 0) Palette.hazard else Palette.paperDark) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Pilha("ESTADOS", q.estados.map { it.toString() }, Palette.drawer, Modifier.weight(1f))
            Pilha(tx("labbisonscreen_simbolos"), listOf("$") + q.simbolos, Palette.orange, Modifier.weight(1f))
            Pilha("VALORES", listOf("·") + q.valores.map { it?.toString() ?: "·" }, Palette.yellow, Modifier.weight(1f), destaqueTopo = q.acao is LRAction.Reduce)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Palette.codeBg).padding(8.dp)) {
            Text(tx("comum_passo_1_2", passo + 1, quadros.size), color = Color(0xFFA9B3B0), style = MaterialTheme.typography.labelSmall)
            Text(q.texto, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = Color(0xFFE8E4D8))
            if (q.conta != null) Text(q.conta, fontFamily = LocalFonts.current.mono, fontSize = 14.sp, color = Palette.hazard, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BigButton(tx("comum_voltar"), Modifier.weight(1f), color = Palette.drawer, small = true, enabled = passo > 0) { onPasso(passo - 1); Sound.play(Sfx.TICK) }
            BigButton(tx("comum_proximo"), Modifier.weight(2f), enabled = passo < quadros.lastIndex) { onPasso(passo + 1); Sound.play(Sfx.DROP) }
            BigButton(tx("labbisonscreen_ate_fim"), Modifier.weight(1f), color = Palette.drawer, small = true, enabled = passo < quadros.lastIndex) { onPasso(quadros.lastIndex) }
        }
    }
}

@Composable
private fun Celula(t: String, cor: Color) {
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(cor).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Text(t, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = Palette.on(cor), fontWeight = FontWeight.Bold)
    }
}

/** Uma pilha desenhada de cima para baixo: o topo fica em cima e aceso. */
@Composable
private fun Pilha(titulo: String, itens: List<String>, cor: Color, modifier: Modifier, destaqueTopo: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall)
        for ((k, t) in itens.withIndex().reversed().take(7)) {
            val topo = k == itens.lastIndex
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(5.dp)).background(if (topo) cor else cor.copy(alpha = 0.35f))
                    .then(if (topo && destaqueTopo) Modifier.border(2.dp, Palette.ink, RoundedCornerShape(5.dp)) else Modifier)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) { Text(t, fontFamily = LocalFonts.current.mono, fontSize = 13.sp, color = if (topo) Palette.on(cor) else Palette.ink, fontWeight = FontWeight.Bold, maxLines = 1) }
        }
    }
}

/** B3: cada célula com duas ações vira um duelo: o item que empilha contra o item que reduz. */
@Composable
private fun Conflitos(bison: MiniBison) {
    val t = bison.table
    val g = t.grammar
    val celulas = t.rawConflictCells
    if (celulas.isEmpty()) {
        Text(tx("labbisonscreen_nenhuma_celula_tabela_tem_duas"), color = Palette.inkSoft, style = MaterialTheme.typography.bodyMedium)
        return
    }
    for ((s, a) in celulas) {
        val st = t.automaton.states[s]
        val empilha = st.items.filter { it.nextSymbol(g) == a }.map { it.core().render(g) }.distinct()
        val reduz = st.items.filter { it.isComplete(g) && (it.lookahead == null || it.lookahead == a) }.map { it.core().render(g) }.distinct()
        val decisao = t.resolutions[s to a]
        val sobrou = bison.conflicts.any { it.state == s && it.terminal == a }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.paper)
                .border(2.dp, if (sobrou) Palette.red else Palette.green, RoundedCornerShape(8.dp)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(tx("labbisonscreen_estado_1_chega_2", s, a), style = MaterialTheme.typography.titleSmall, color = Palette.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (empilha.isNotEmpty()) RegistroCard("EMPILHAR", empilha, Palette.teal, Modifier.weight(1f))
                for (r in reduz) RegistroCard("REDUZIR", listOf(r), Palette.orange, Modifier.weight(1f))
            }
            Text(
                if (sobrou) tx("labbisonscreen_sobrou_1", decisao ?: tx("labbisonscreen_bison_escolhe_avisa")) else tx("labbisonscreen_resolvido_1", decisao ?: ""),
                color = if (sobrou) Palette.red else Palette.green, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** B4: os estados do relatório do bison -v. O topo da pilha no passo atual acende. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Estados(bison: MiniBison, q: MiniBison.Quadro?) {
    val t = bison.table
    val topo = q?.estados?.lastOrNull()
    val naPilha = q?.estados.orEmpty().toSet()
    Text(tx("labbisonscreen_como_no_arquivo_output_amarelo"), color = Palette.inkSoft, style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (s in t.automaton.states.indices) {
            val cor = when (s) { topo -> Palette.hazard; in naPilha -> Palette.teal; else -> Palette.drawer }
            RegistroCard(tx("comum_estado_1", s), linhasDoEstado(t, s), cor, Modifier.widthIn(min = 150.dp, max = 330.dp))
        }
    }
}

/** A caixa com compartimentos, em Compose: o nome numa faixa e uma linha por item. */
@Composable
fun RegistroCard(nome: String, linhas: List<String>, cor: Color, modifier: Modifier = Modifier) {
    val mono = LocalFonts.current.mono
    Column(modifier.clip(RoundedCornerShape(6.dp)).background(Palette.paperDark).border(1.5.dp, cor, RoundedCornerShape(6.dp))) {
        Box(Modifier.fillMaxWidth().background(cor).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(nome, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.on(cor))
        }
        for ((i, l) in linhas.withIndex()) {
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.drawerLo))
            Text(l, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontFamily = mono, fontSize = 12.sp, color = Palette.ink)
        }
    }
}
