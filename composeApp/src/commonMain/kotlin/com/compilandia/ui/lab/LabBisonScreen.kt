package com.compilandia.ui.lab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.compilandia.engine.MiniBison
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.textos.tx
import com.compilandia.ui.AppState
import com.compilandia.ui.Header
import com.compilandia.ui.LISTA_MAX
import com.compilandia.ui.LocalFonts
import com.compilandia.ui.LocalJanela
import com.compilandia.ui.Palette
import com.compilandia.ui.TreeView
import com.compilandia.ui.VSpace
import com.compilandia.ui.placaDeMetal

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

    /** Sem precedência, 2*3+4 dá 14: o Bison empilha sempre e a soma fica embaixo da vezes. */
    val todos = listOf(
        Triple(tx("labbisonscreen_calculadora"), calculadora, "2+3*4"),
        Triple(tx("labbisonscreen_sem_precedencia"), semPrecedencia, "2*3+4"),
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
    l[i] = l[i].replaceFirst(atual, PREC[(PREC.indexOf(atual) + 1) % PREC.size])
    return l.joinToString("\n")
}

internal enum class AbaBison(val titulo: String) {
    PASSOS(tx("labbisonscreen_passo_passo")), CONFLITOS(tx("comum_conflitos")), ESTADOS(tx("labbisonscreen_estados")), ARQUIVO(tx("labbison_arquivo_y")),
}

/**
 * O Laboratório Bison. No alto, a conta e a árvore que o .y montou; embaixo, a régua de precedência.
 * Mexer na régua muda a árvore na hora. As abas mostram o parser por dentro: a pilha, os conflitos,
 * os estados e o próprio arquivo .y.
 */
@Composable
fun LabBisonScreen(app: AppState, inicial: String?, abaInicial: Int = 0, passoInicial: Int = 0) {
    var spec by remember { mutableStateOf(inicial ?: BisonExemplos.calculadora) }
    var entrada by remember { mutableStateOf(BisonExemplos.todos.firstOrNull { it.second == inicial }?.third ?: "2+3*4") }
    var aba by remember { mutableStateOf(AbaBison.entries[abaInicial]) }
    var passo by remember { mutableIntStateOf(passoInicial) }

    val compilado = remember(spec) { runCatching { MiniBison.compile(spec) } }
    val bison = compilado.getOrNull()
    val quadros = remember(bison, entrada) { bison?.quadros(entrada).orEmpty() }
    val resultado = remember(bison, entrada) { bison?.run(entrada) }
    if (passo > quadros.lastIndex) passo = quadros.lastIndex.coerceAtLeast(0)
    val muda: (String) -> Unit = { spec = it; passo = 0 }

    val topo: @Composable ColumnScope.() -> Unit = {
        Exemplos(spec) { s, e -> spec = s; entrada = e; passo = 0 }
        // No passo a passo, a árvore cresce junto com a pilha: o que ainda não foi montado fica fantasma.
        // No passo 0 ela aparece inteira, que é a resposta da conta; o primeiro Próximo começa a montagem.
        val progresso = if (aba == AbaBison.PASSOS && passo > 0) progressoNoPasso(quadros, passo) else null
        Placa(bison, compilado.exceptionOrNull()?.message, entrada, resultado, progresso) { entrada = it.take(24); passo = 0 }
        if (bison != null || linhasDePrecedencia(spec).isNotEmpty()) Regua(spec, muda)
    }
    val baixo: @Composable ColumnScope.() -> Unit = {
        Abas(aba, bison?.table?.rawConflictCells?.size ?: 0) { aba = it; Sound.play(Sfx.CLICK) }
        when (aba) {
            AbaBison.ARQUIVO -> ArquivoY(spec, muda)
            else -> if (bison != null) when (aba) {
                AbaBison.PASSOS -> PilhaPasso(quadros, passo) { passo = it.coerceIn(0, quadros.lastIndex.coerceAtLeast(0)) }
                AbaBison.CONFLITOS -> Conflitos(bison)
                else -> Estados(bison, quadros.getOrNull(passo))
            }
        }
        VSpace(30)
    }

    Column(Modifier.fillMaxSize().background(Palette.paperDark)) {
        Header(tx("comum_laboratorio_bison"))
        val coluna = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        val espaco = Arrangement.spacedBy(10.dp)
        if (LocalJanela.current.ladoALado) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).then(coluna), verticalArrangement = espaco, content = topo)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).then(coluna), verticalArrangement = espaco, content = baixo)
            }
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = LISTA_MAX).fillMaxWidth().verticalScroll(rememberScrollState()).then(coluna), verticalArrangement = espaco) {
                    topo(); baixo()
                }
            }
        }
    }
}

@Composable
internal fun EtiquetaBison(texto: String, ligada: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(6.dp)).background(if (ligada) Palette.hazard else Palette.paper)
            .border(1.5.dp, if (ligada) Palette.hazard else Palette.drawer, RoundedCornerShape(6.dp))
            .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp),
    ) { Text(texto, color = if (ligada) Palette.hazardDark else Palette.ink, style = MaterialTheme.typography.labelMedium) }
}

@Composable
private fun Exemplos(spec: String, onEscolhe: (String, String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for ((nome, s, e) in BisonExemplos.todos) EtiquetaBison(nome, spec == s) { onEscolhe(s, e); Sound.play(Sfx.CLICK) }
    }
}

@Composable
private fun Abas(aba: AbaBison, conflitos: Int, onAba: (AbaBison) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (a in AbaBison.entries) EtiquetaBison(if (a == AbaBison.CONFLITOS && conflitos > 0) "${a.titulo} ($conflitos)" else a.titulo, aba == a) { onAba(a) }
    }
}

/**
 * A placa da conta: a entrada, o valor que o .y deu e a árvore. Se o valor não bate com a conta
 * da matemática, ele fica vermelho e a conta certa aparece embaixo.
 */
@Composable
private fun Placa(bison: MiniBison?, erro: String?, entrada: String, r: MiniBison.RunResult?, progresso: Progresso?, onEntrada: (String) -> Unit) {
    val fonts = LocalFonts.current
    val certa = remember(entrada) { contaCerta(entrada) }
    val bate = r?.accepted == true && (certa == null || certa == r.value)
    val corValor = if (bate) Palette.green else Palette.red
    val onPlaca = Palette.on(Palette.drawer)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).placaDeMetal(Palette.drawer, Palette.drawerHi, Palette.drawerLo, Palette.drawerHi)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp).testTag("lab-bison-veredito"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicTextField(
                entrada, onEntrada,
                Modifier.weight(1f).clip(RoundedCornerShape(7.dp)).background(Palette.codeBg).padding(horizontal = 12.dp, vertical = 9.dp).testTag("lab-bison-entrada"),
                singleLine = true,
                textStyle = TextStyle(fontFamily = fonts.mono, fontSize = 20.sp, color = Palette.onAccent, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(Palette.hazard),
            )
            Text("=", color = onPlaca, fontFamily = fonts.mono, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.widthIn(min = 64.dp).clip(RoundedCornerShape(7.dp)).background(corValor).padding(horizontal = 12.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(if (r?.accepted == true) "${r.value}" else "?", color = Palette.on(corValor), fontFamily = fonts.mono, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bison != null) SeloConflitos(bison.conflicts.size)
            val nota = when {
                bison == null -> tx("labbisonscreen_y_nao_compilou")
                r?.accepted != true -> r?.error ?: ""
                certa != null && certa != r.value -> tx("labbison_conta_certa", certa)
                else -> ""
            }
            Text(nota, Modifier.weight(1f), color = onPlaca, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
        if (bison == null && erro != null) Text(erro, color = onPlaca.copy(alpha = 0.8f), fontFamily = fonts.mono, fontSize = 12.sp)
        val arvore = remember(bison, r, progresso) { r?.tree?.let { t -> bison?.arvoreDaConta(entrada, t, progresso) } }
        if (arvore != null) {
            Box(Modifier.fillMaxWidth().heightIn(min = 120.dp).clip(RoundedCornerShape(8.dp)).background(Palette.paper).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                TreeView(arvore, Modifier.testTag("lab-bison-arvore"), centro = true)
            }
        }
    }
}

@Composable
private fun SeloConflitos(n: Int) {
    val cor = if (n == 0) Palette.green else Palette.red
    Box(Modifier.clip(RoundedCornerShape(50)).background(cor).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(if (n == 0) tx("labbisonscreen_0_conflitos") else tx("labbisonscreen_1_conflito_2", n, if (n > 1) "s" else ""), color = Palette.on(cor), style = MaterialTheme.typography.labelMedium)
    }
}

/** A alça desenhada: três riscos, porque o glifo pode faltar na fonte do navegador. */
@Composable
private fun Alca(cor: Color) {
    Canvas(Modifier.size(18.dp, 14.dp)) {
        for (k in 0..2) {
            val y = size.height * (0.15f + 0.35f * k)
            drawLine(cor, Offset(0f, y), Offset(size.width, y), 2.5.dp.toPx(), StrokeCap.Round)
        }
    }
}

/**
 * A régua: cada linha de precedência é uma barra que se arrasta pela alça.
 * Mais embaixo liga mais forte. Tocar no %left gira a associatividade.
 * Sem nenhuma linha, a régua vira um espaço tracejado que põe um %left por operador.
 */
@Composable
private fun Regua(spec: String, onMuda: (String) -> Unit) {
    val prec = linhasDePrecedencia(spec)
    if (prec.isEmpty()) {
        val cor = Palette.hazard
        Box(
            Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).clickable { onMuda(pontoDePrecedencia(spec)); Sound.play(Sfx.DROP) }
                .testTag("lab-bison-por-left"),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawRoundRect(cor, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
            }
            Text(tx("labbison_por_left"), color = cor, style = MaterialTheme.typography.labelLarge)
        }
        return
    }
    val linhas = spec.lines()
    val alturaPx = with(LocalDensity.current) { 46.dp.toPx() }
    var arrastando by remember { mutableStateOf<String?>(null) }
    var desloc by remember { mutableFloatStateOf(0f) }
    val atual by rememberUpdatedState(prec)
    val textoAtual by rememberUpdatedState(linhas)
    val specAtual by rememberUpdatedState(spec)
    Column {
        prec.forEachIndexed { pos, i ->
            val texto = linhas[i].trim()
            key(texto) {
                val emCima = arrastando == texto
                Row(
                    Modifier.fillMaxWidth().height(42.dp).zIndex(if (emCima) 1f else 0f)
                        .graphicsLayer { translationY = if (emCima) desloc else 0f }
                        .clip(RoundedCornerShape(6.dp)).background(if (emCima) Palette.hazard.copy(alpha = 0.3f) else Palette.yellow.copy(alpha = 0.10f + 0.14f * pos / prec.size.coerceAtLeast(1)))
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
                                if (desloc > alturaPx / 2 && agora in 0 until atual.lastIndex) { onMuda(trocaLinhas(specAtual, atual[agora], atual[agora + 1])); desloc -= alturaPx; Sound.play(Sfx.DROP) }
                                else if (desloc < -alturaPx / 2 && agora > 0) { onMuda(trocaLinhas(specAtual, atual[agora], atual[agora - 1])); desloc += alturaPx; Sound.play(Sfx.DROP) }
                            }
                        },
                        contentAlignment = Alignment.Center,
                    ) { Alca(Palette.ink) }
                    val kind = PREC.first { texto.startsWith(it) }
                    Box(Modifier.clip(RoundedCornerShape(5.dp)).background(Palette.hazard).clickable { onMuda(giraAssociatividade(specAtual, i)); Sound.play(Sfx.CLICK) }.padding(horizontal = 7.dp, vertical = 3.dp)) {
                        Text(kind, fontFamily = LocalFonts.current.mono, fontSize = 12.sp, color = Palette.hazardDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(texto.removePrefix(kind).trim(), Modifier.weight(1f), fontFamily = LocalFonts.current.mono, fontSize = 14.sp, color = Palette.ink, fontWeight = FontWeight.Bold)
                    val ponta = when { prec.size < 2 -> ""; pos == prec.lastIndex -> tx("labbison_forte"); pos == 0 -> tx("labbison_fraco"); else -> "" }
                    Text(ponta, color = Palette.inkSoft, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 10.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** O .y inteiro, como o Bison lê. Um toque em Editar abre o texto. */
@Composable
private fun ArquivoY(spec: String, onMuda: (String) -> Unit) {
    var editando by remember { mutableStateOf(false) }
    val fonts = LocalFonts.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Palette.codeBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val estilo = TextStyle(fontFamily = fonts.mono, fontSize = 13.sp, lineHeight = 19.sp, color = Palette.onAccent)
        if (editando) BasicTextField(spec, onMuda, Modifier.fillMaxWidth().testTag("lab-bison-texto"), textStyle = estilo, cursorBrush = SolidColor(Palette.hazard))
        else Text(spec, style = estilo)
        Text(
            if (editando) tx("labbisonscreen_fechar_texto") else tx("labbison_editar"), color = Palette.hazard, style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { editando = !editando }.padding(vertical = 4.dp),
        )
    }
}
