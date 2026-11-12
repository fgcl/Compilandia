package com.compilandia.content

import com.compilandia.engine.ParseNode
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Papel de cor de uma peça no palco. Cada tema resolve a cor certa. */
enum class Tint { NONE, TEAL, ORANGE, YELLOW, RED, GREEN, BLUE, PINK, DARK }

/** Como a peça é desenhada. */
enum class Look {
    CELL,   // casinha de caractere ou de tabela
    TOKEN,  // bloco colorido
    NODE,   // cápsula de árvore
    STATE,  // estado de autômato
    ACCEPT, // estado final (círculo duplo)
    LABEL,  // texto solto
    TITLE,  // título grande
    CODE,   // linha de código, alinhada à esquerda em x
    GAP,    // fenda tocável entre duas peças
    TRAIN,  // marcador que anda sobre o desenho
    BIN,    // caixa onde coisas caem
    GHOST,  // contorno tracejado: onde a peça pode cair
    BANG,   // palavra que cai letra por letra e levanta poeira
    RECORD, // caixa com compartimentos: nome em cima e uma linha por item (estado, struct, registro)
    CARD,   // cartão de frase: texto de ler, que quebra em linhas dentro da caixa
}

/**
 * Uma peça do palco. O palco tem 360 de largura; x e y são o centro da peça (CODE usa x como margem esquerda).
 * Peças com o mesmo [id] em duas cenas seguidas deslizam de uma posição para a outra.
 */
data class Box(
    val id: String,
    val text: String = "",
    val x: Float,
    val y: Float,
    val w: Float = 40f,
    val h: Float = 40f,
    val look: Look = Look.CELL,
    val tint: Tint = Tint.NONE,
    val hot: Boolean = false,
    val size: Float = 16f,
    /** Só em RECORD: as linhas abaixo do nome. */
    val rows: List<String> = emptyList(),
    /** Só em RECORD: a linha acesa, ou -1. */
    val lit: Int = -1,
)

/** Fio entre duas peças. [from] igual a [to] desenha um laço. */
data class Wire(
    val from: String,
    val to: String,
    val label: String = "",
    val hot: Boolean = false,
    val bend: Float = 0f,
    val arrow: Boolean = true,
    /** Símbolos que correm pelo fio, da origem ao destino. */
    val flow: List<String> = emptyList(),
    /** Se a origem é RECORD, o fio sai da lateral desta linha, e não do meio da caixa. */
    val fromRow: Int = -1,
)

/** Byte dentro da cena: x e y em unidades do palco, no centro da base dele. */
data class Actor(val x: Float, val y: Float, val scale: Float = 1f)

/** Para onde a câmera olha: x e y em unidades do palco, e quanto ela aproxima. */
data class Camera(val x: Float = 180f, val y: Float = 205f, val zoom: Float = 1f)

/** Uma imagem do palco. Entre duas imagens, o motor anima o que mudou. [ms] é o tempo dessa animação. */
data class Shot(
    val boxes: List<Box>,
    val wires: List<Wire> = emptyList(),
    val actor: Actor? = null,
    val camera: Camera? = null,
    val ms: Int = 820,
)

/** Pede um toque no próprio desenho. Todos os ids em [right] precisam ser tocados. */
data class Ask(val right: Set<String>, val wrong: Set<String>, val hint: Fala, val got: Fala)

/**
 * Um brinquedo: a pessoa aperta teclas e o palco responde. Quem calcula é o motor, nunca o desenho.
 * [dica] toca quando ela erra e [acerto] quando ela chega ao objetivo.
 */
class Brinquedo(val dica: Fala, val acerto: Fala, val novo: () -> Toy)

/** Um passo do episódio: o que se vê, o que se ouve e, às vezes, o que se pede. */
data class Passo(
    val shot: Shot,
    val fala: Fala,
    val ask: Ask? = null,
    val mood: EpMood = EpMood.NEUTRAL,
    val brinquedo: Brinquedo? = null,
)

enum class EpMood { NEUTRAL, HAPPY, THINK, SURPRISED }

/** Cada região tem três temporadas. O clipe é a aula de 30 segundos do feed. */
enum class Temporada(val titulo: String) {
    BASICO(tx("episode_basico")),
    PEGADINHA(tx("episode_pegadinhas")),
    PRATICA(tx("episode_pratica")),
    CLIPE(tx("episode_clipe")),
    /** Diagramas de tabela: o mesmo assunto desenhado como caixas com compartimentos. */
    TABELAS(tx("episode_tabelas")),
    /** A série que escreve um compilador em C, do getchar ao MIPS. Aparece inteira em toda região. */
    EM_C(tx("episode_em_c")),
}

/** [inicioDoExemplo]: a cena em que o primeiro exemplo começa, quando o roteiro marca (veja [EpisodeBuilder.comecaExemplo]). */
data class Episode(val skill: Skill, val title: String, val passos: List<Passo>, val temporada: Temporada = Temporada.BASICO, val inicioDoExemplo: Int? = null) {
    /** O básico fica com o nome do assunto, para os saves antigos continuarem certos. */
    val id: String get() = if (temporada == Temporada.BASICO) skill.name else "${skill.name}/${com.compilandia.textos.Txt.paraPt(title).lowercase().filter { it.isLetterOrDigit() }}"
    /** Duração aproximada da aula, para a lista. */
    val segundos: Int get() = passos.sumOf { 2 + it.fala.text.length / 14 }
}

/**
 * Monta um episódio cena a cena. Cada cena herda a anterior: basta dizer o que muda.
 * Assim o Byte explica um algoritmo como quem mexe peças numa mesa.
 */
class EpisodeBuilder(private val skill: Skill, private val title: String, private val temporada: Temporada = Temporada.BASICO) {
    private val boxes = linkedMapOf<String, Box>()
    private var wires = mutableListOf<Wire>()
    private var actor: Actor? = null
    private var camera: Camera? = null
    private val passos = mutableListOf<Passo>()

    fun add(vararg b: Box) { for (x in b) boxes[x.id] = x }
    fun add(b: List<Box>) { for (x in b) boxes[x.id] = x }
    fun wire(w: Wire) { wires.add(w) }
    fun wire(from: String, to: String, label: String = "", hot: Boolean = false, bend: Float = 0f, arrow: Boolean = true) {
        wires.add(Wire(from, to, label, hot, bend, arrow))
    }
    fun wires(ws: List<Wire>) { wires.addAll(ws) }
    fun clearWires() { wires = mutableListOf() }
    fun drop(vararg ids: String) { for (i in ids) boxes.remove(i); wires = wires.filterNot { it.from in ids || it.to in ids }.toMutableList() }
    fun dropAll(prefix: String) { drop(*boxes.keys.filter { it.startsWith(prefix) }.toTypedArray()) }
    fun clear() { boxes.clear(); wires.clear() }
    /** Aponta a câmera. Sem argumento, ela volta a mostrar o palco inteiro. */
    fun camera(x: Float = 180f, y: Float = 205f, zoom: Float = 1f) { camera = if (zoom == 1f) null else Camera(x, y, zoom) }
    /** Faz os símbolos correrem pelo fio. */
    fun flow(from: String, to: String, vararg simbolos: String) {
        wires = wires.map { if (it.from == from && it.to == to) it.copy(flow = simbolos.toList(), hot = true) else it.copy(flow = emptyList(), hot = false) }.toMutableList()
    }
    fun change(id: String, f: (Box) -> Box) { boxes[id]?.let { boxes[id] = f(it) } }
    fun move(id: String, x: Float, y: Float) = change(id) { it.copy(x = x, y = y) }
    fun text(id: String, t: String) = change(id) { it.copy(text = t) }
    fun tint(id: String, t: Tint) = change(id) { it.copy(tint = t) }
    /** Acende uma linha de uma caixa-registro (-1 apaga). */
    fun lit(id: String, row: Int) = change(id) { it.copy(lit = row) }
    /** Liga só os fios que saem desta linha da caixa. */
    fun hotRow(from: String, row: Int) { wires = wires.map { it.copy(hot = it.from == from && it.fromRow == row) }.toMutableList() }
    /** Liga o destaque só nestas peças. */
    fun hot(vararg ids: String) {
        for ((k, v) in boxes.toList()) boxes[k] = v.copy(hot = k in ids)
        wires = wires.map { it.copy(hot = false) }.toMutableList()
    }
    fun hotWire(from: String, to: String) { wires = wires.map { if (it.from == from && it.to == to) it.copy(hot = true) else it.copy(hot = false) }.toMutableList() }
    fun has(id: String) = id in boxes
    fun actor(a: Actor?) { actor = a }

    private fun shot(ms: Int = 820) = Shot(boxes.values.toList(), wires.toList(), actor, camera, ms)

    fun cena(fala: String, who: Speaker = Speaker.BYTE, mood: EpMood = EpMood.NEUTRAL, change: EpisodeBuilder.() -> Unit = {}) {
        change()
        passos += Passo(shot(), Fala(who, fala), null, mood)
    }

    /** Para e pede toque: [right] é o que precisa ser tocado, [wrong] o que dá para errar. */
    fun pergunta(fala: String, right: Set<String>, wrong: Set<String>, dica: String, acerto: String, who: Speaker = Speaker.BYTE, change: EpisodeBuilder.() -> Unit = {}) {
        change()
        passos += Passo(shot(), Fala(who, fala), Ask(right, wrong, Fala(who, dica), Fala(who, acerto)), EpMood.THINK)
    }

    /** A palavra cai letra por letra, e uma frase curta fica embaixo dela. */
    fun palavra(termo: String, fala: String, who: Speaker = Speaker.BYTE, mood: EpMood = EpMood.SURPRISED, change: EpisodeBuilder.() -> Unit = {}) {
        clear()
        add(Box("BANG", termo, 180f, 190f, 340f, 90f, Look.BANG, Tint.YELLOW, size = minOf(82f, 720f / termo.length)))
        change()
        passos += Passo(shot(1500), Fala(who, fala), null, mood)
    }

    /** Dá o controle para a pessoa: o palco vira o brinquedo até ela chegar ao objetivo. */
    fun brinquedo(fala: String, dica: String, acerto: String, who: Speaker = Speaker.BYTE, novo: () -> Toy) {
        passos += Passo(novo().shot, Fala(who, fala), null, EpMood.THINK, Brinquedo(Fala(who, dica), Fala(who, acerto), novo))
    }

    private var inicioDoExemplo: Int? = null
    /** Marca que a próxima cena abre o primeiro exemplo, para a fita da aula saber onde ele começa. */
    fun comecaExemplo() { inicioDoExemplo = passos.size }
    fun build() = Episode(skill, title, passos.toList(), temporada, inicioDoExemplo)
}

fun episodio(skill: Skill, title: String, temporada: Temporada = Temporada.BASICO, f: EpisodeBuilder.() -> Unit): Episode =
    EpisodeBuilder(skill, title, temporada).apply(f).build()

// ------------------------------------------------------------ peças prontas

const val TITULO_ID = "HEAD"

fun titulo(text: String, y: Float = 26f, id: String = TITULO_ID, size: Float = 26f) = Box(id, text, 180f, y, 340f, 34f, Look.TITLE, size = size)
fun rotulo(id: String, text: String, x: Float, y: Float, tint: Tint = Tint.NONE, size: Float = 15f) = Box(id, text, x, y, text.length * size * 0.5f, 22f, Look.LABEL, tint, size = size)

/** Fita de caracteres: ids c0, c1... */
fun fita(src: String, y: Float, cell: Float = 22f, prefix: String = "c"): List<Box> {
    val x0 = 180f - (src.length - 1) * cell / 2
    return src.mapIndexed { i, ch -> Box("$prefix$i", ch.toString(), x0 + i * cell, y, cell - 2f, cell * 1.5f, Look.CELL, size = maxOf(15f, cell * 0.62f)) }
}

/** Fileira de blocos centrada. */
fun fileira(prefix: String, itens: List<Pair<String, Tint>>, y: Float, w: Float = 52f, h: Float = 34f, gap: Float = 6f, look: Look = Look.TOKEN, size: Float = 15f): List<Box> {
    val x0 = 180f - (itens.size - 1) * (w + gap) / 2
    return itens.mapIndexed { i, (t, c) -> Box("$prefix$i", t, x0 + i * (w + gap), y, w, h, look, c, size = size) }
}

/** Pilha de baixo para cima: o último item é o topo. */
fun pilha(prefix: String, itens: List<String>, x: Float, base: Float, w: Float = 70f, h: Float = 30f, tint: Tint = Tint.TEAL, gap: Float = 3f): List<Box> =
    itens.mapIndexed { i, t -> Box("$prefix$i", t, x, base - i * (h + gap), w, h, Look.TOKEN, tint) }

/** Onde a peça pode cair: tracejado, tocável, com um ponto de interrogação. */
fun alvo(id: String, x: Float, y: Float, w: Float = 70f, h: Float = 34f, texto: String = "?") = Box(id, texto, x, y, w, h, Look.GHOST, size = 15f)

/** Código: uma linha por caixa, destacável. */
fun codigo(prefix: String, linhas: List<String>, y0: Float, x: Float = 14f, passo: Float = 25f, size: Float = 13f): List<Box> =
    linhas.mapIndexed { i, t -> Box("$prefix$i", t, x, y0 + i * passo, 346f - x, passo - 3f, Look.CODE, size = size) }

/** Tabela: ids <prefix>l<linha>c<coluna>. A primeira linha e a primeira coluna são cabeçalho. */
fun tabela(prefix: String, linhas: List<List<String>>, y0: Float, cw: Float = 60f, ch: Float = 30f, size: Float = 12f): List<Box> {
    val cols = linhas.maxOf { it.size }
    val x0 = 180f - (cols - 1) * cw / 2
    return linhas.flatMapIndexed { l, row ->
        row.mapIndexed { c, t ->
            Box("${prefix}l${l}c$c", t, x0 + c * cw, y0 + l * ch, cw - 3f, ch - 3f, Look.CELL, if (l == 0 || c == 0) Tint.DARK else Tint.NONE, size = size)
        }
    }
}

/** Árvore: devolve peças e fios, com ids pelo caminho no nó (t, t0, t01...). */
fun arvore(raiz: ParseNode, y0: Float, passoY: Float = 52f, largura: Float = 340f, prefix: String = "t"): Pair<List<Box>, List<Wire>> {
    // A posição vai pelo caminho no nó, não pelo nó: duas folhas "n" são iguais, mas moram em lugares diferentes.
    val colunas = mutableMapOf<String, Float>()
    var folhas = 0
    fun mede(x: ParseNode, id: String): Float {
        val c = if (x.children.isEmpty()) (folhas++).toFloat() else x.children.mapIndexed { i, f -> mede(f, "$id$i") }.average().toFloat()
        colunas[id] = c
        return c
    }
    mede(raiz, prefix)
    val passoX = largura / maxOf(folhas, 1)
    val boxes = mutableListOf<Box>()
    val wires = mutableListOf<Wire>()
    fun vai(x: ParseNode, id: String, d: Int) {
        val folha = x.children.isEmpty()
        boxes += Box(
            id, x.symbol, (180f - largura / 2) + passoX * (colunas.getValue(id) + 0.5f), y0 + d * passoY,
            maxOf(34f, x.symbol.length * 11f + 12f), 30f, Look.NODE, if (folha) Tint.TEAL else Tint.ORANGE, size = 14f,
        )
        x.children.forEachIndexed { i, c -> val cid = "$id$i"; wires += Wire(id, cid, arrow = false); vai(c, cid, d + 1) }
    }
    vai(raiz, prefix, 0)
    return boxes to wires
}

/** Estados em posições dadas. Ids: q<n>. */
fun estados(pos: List<Pair<Float, Float>>, finais: Set<Int> = emptySet(), nomes: List<String>? = null): List<Box> = pos.mapIndexed { i, (x, y) ->
    Box("q$i", nomes?.get(i) ?: "q$i", x, y, 46f, 46f, if (i in finais) Look.ACCEPT else Look.STATE, size = 14f)
}

// ------------------------------------------------------------ fecho da aula

/** A primeira peça do fecho. É por ela que a fita da aula acha a quinta parte. */
const val CUIDADO_ID = "cu1"

/** Cartão de frase: o texto quebra em linhas dentro da caixa. */
fun cartao(id: String, text: String, y: Float, tint: Tint = Tint.NONE, h: Float = 66f) = Box(id, text, 180f, y, 336f, h, Look.CARD, tint, size = 16.5f)

/**
 * O fecho de uma aula básica: os erros que mais derrubam na prova e o resumo.
 * Primeiro a pessoa julga uma afirmação; depois vê a versão certa, um segundo erro ao lado do conserto, e três linhas de resumo.
 */
class Fecho(
    /** A afirmação que a pessoa julga, e se ela vale. */
    val afirmacao: String, val vale: Boolean,
    val pergunta: String, val dica: String, val porque: String,
    /** A afirmação consertada. Quando [vale], é a própria afirmação. */
    val certo: String,
    /** O segundo erro e o conserto dele, com a fala que explica. */
    val erro2: String, val certo2: String, val fala2: String,
    val resumo: List<String>, val falaResumo: String,
)

fun EpisodeBuilder.fecho(f: Fecho) {
    pergunta(f.pergunta, setOf(if (f.vale) "cuS" else "cuN"), setOf(if (f.vale) "cuN" else "cuS"), f.dica, f.porque) {
        clear()
        add(titulo(tx("aula_titulo_erros")))
        add(cartao(CUIDADO_ID, f.afirmacao, 130f))
        add(Box("cuS", tx("aula_certo"), 110f, 240f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 16f))
        add(Box("cuN", tx("aula_errado"), 250f, 240f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 16f))
    }
    cena(f.fala2) {
        drop("cuS", "cuN")
        change(CUIDADO_ID) { it.copy(text = tx("aula_certo_prefixo", f.certo), tint = Tint.GREEN, y = 104f) }
        add(cartao("cu2", tx("aula_errado_prefixo", f.erro2), 206f, Tint.RED))
        add(cartao("cu3", tx("aula_certo_prefixo", f.certo2), 284f, Tint.GREEN))
        hot("cu2", "cu3")
    }
    cena(f.falaResumo, mood = EpMood.HAPPY) {
        clear()
        add(titulo(tx("aula_titulo_resumo")))
        f.resumo.forEachIndexed { i, t -> add(cartao("z${i + 1}", t, 100f + i * 78f, h = 68f)) }
    }
}
