package com.compilandia.tutor

import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.EPS
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRTable
import com.compilandia.engine.Production
import com.compilandia.engine.SentenceGenerator
import kotlin.random.Random

enum class Falha { IMPRODUTIVO, INALCANCAVEL, RECURSAO, RECURSAO_INDIRETA, PREFIXO, AMBIGUA, CONFLITO_LL, CONFLITO_SLR }

/**
 * Um problema achado pelo motor. Os textos são os degraus da dica:
 * [aponta] diz onde, [porque] diz por quê, [faca] diz o que tentar.
 * O conserto pronto só sai pelo [Degraus], que confere se a pessoa tentou.
 */
data class Problema(
    val falha: Falha,
    val linha: Int,
    val nt: String?,
    val aponta: String,
    val porque: String,
    val faca: String,
    /** A frase ambígua, quando for o caso. */
    val frase: List<String>? = null,
)

data class Laudo(
    val g: Grammar?,
    val erro: String?,
    val problemas: List<Problema>,
    val ll1: Boolean,
    val slr: Boolean,
    val lalr: Boolean,
    val frases: List<List<String>>,
) {
    val limpa: Boolean get() = g != null && problemas.isEmpty()
}

/** Lê a gramática com o motor e diz o que achou. Nunca adivinha: todo problema sai de um cálculo. */
object Diagnostico {
    /** Aceita o que costuma vir de lista de exercício: seta de texto, λ, espaços grudados. */
    fun normaliza(texto: String): String = texto
        .replace("→", "->").replace("::=", "->").replace("–>", "->").replace("—>", "->")
        .replace("Ɛ", "ε")
        .lines().joinToString("\n") { l ->
            val i = l.indexOf("->")
            if (i < 0) l else l.substring(0, i).trim() + " -> " + l.substring(i + 2).replace("|", " | ").trim().replace(Regex("\\s+"), " ")
        }

    fun analisa(texto: String, rnd: Random = Random(7)): Laudo {
        val g = try {
            Grammar.parse(normaliza(texto))
        } catch (e: Exception) {
            return Laudo(null, e.message ?: "não entendi esse texto", emptyList(), false, false, false, emptyList())
        }
        val problemas = mutableListOf<Problema>()
        fun linha(nt: String) = g.nonTerminals.indexOf(nt) + 1

        val semFim = improdutivos(g)
        for (nt in semFim) problemas += Problema(
            Falha.IMPRODUTIVO, linha(nt), nt,
            "$nt nunca termina: toda regra dele volta para ele.",
            "Uma derivação que passa por $nt não chega numa frase. Fica sempre um não terminal sobrando.",
            "Dê a $nt uma opção que só use terminais.",
        )
        for (nt in inalcancaveis(g)) problemas += Problema(
            Falha.INALCANCAVEL, linha(nt), nt,
            "Ninguém chama $nt. Essa regra nunca roda.",
            "A derivação começa em ${g.start}, e nenhum caminho chega em $nt.",
            "Use $nt em alguma regra ou apague ela.",
        )
        for (nt in GrammarTransform.leftRecursiveNTs(g)) problemas += Problema(
            Falha.RECURSAO, linha(nt), nt,
            "Na linha ${linha(nt)}, $nt começa com $nt.",
            "Para decidir $nt, o parser precisa de $nt. Ele entra nisso e não sai mais.",
            "Crie $nt' com o que vem depois de $nt, e termine $nt' com ε.",
        )
        for (nt in recursaoIndireta(g)) problemas += Problema(
            Falha.RECURSAO_INDIRETA, linha(nt), nt,
            "$nt volta para $nt passando por outra regra.",
            "A recursão à esquerda está escondida: o parser dá a volta e chega no mesmo lugar.",
            "Substitua a regra do meio dentro de $nt. Assim a recursão fica à vista.",
        )
        if (GrammarTransform.needsLeftFactoring(g)) {
            val (nt, inicio) = prefixoComum(g)
            problemas += Problema(
                Falha.PREFIXO, linha(nt), nt,
                "Na linha ${linha(nt)}, duas opções de $nt começam com $inicio.",
                "Olhando um símbolo só, o parser não sabe qual opção escolher.",
                "Junte o começo igual e jogue a diferença para uma regra nova.",
            )
        }
        val frases = frases(g, rnd)
        ambigua(g, frases)?.let { f ->
            problemas += Problema(
                Falha.AMBIGUA, 1, null,
                "A frase ${f.joinToString(" ")} tem duas árvores.",
                "Duas árvores são dois sentidos. O compilador precisa de um só.",
                "Separe os operadores em níveis, ou declare precedência no Bison.",
                f,
            )
        }
        val ll = LL1Table(g)
        val temGrave = problemas.any { it.falha in setOf(Falha.RECURSAO, Falha.RECURSAO_INDIRETA, Falha.PREFIXO, Falha.AMBIGUA, Falha.IMPRODUTIVO) }
        if (!temGrave) ll.conflicts.firstOrNull()?.let { (a, t) ->
            val regras = ll.cell(a, t)
            problemas += Problema(
                Falha.CONFLITO_LL, linha(a), a,
                "Na tabela, a casa de $a com $t recebeu ${regras.size} regras.",
                "Com $t na entrada, valem ${regras.joinToString(" e ") { curta(it) }}. Só pode valer uma.",
                motivoConflito(ll, a, t),
            )
        }
        val slr = semFim.isEmpty() && !LRTable(g, LRKind.SLR1).hasConflicts
        val lalr = semFim.isEmpty() && !LRTable(g, LRKind.LALR1).hasConflicts
        if (!slr && problemas.none { it.falha == Falha.AMBIGUA || it.falha == Falha.IMPRODUTIVO }) {
            val tb = LRTable(g, LRKind.SLR1)
            tb.conflicts.firstOrNull()?.let { c ->
                problemas += Problema(
                    Falha.CONFLITO_SLR, 1, null,
                    "No estado ${c.state}, com ${c.terminal}, a tabela SLR tem conflito ${c.kind}.",
                    "${descreve(tb, c.actions)}. Olhar só o FOLLOW não basta aqui.",
                    if (lalr) "LALR(1) resolve: ele guarda o que pode vir em cada estado." else "Reescreva a gramática ou declare precedência no Bison.",
                )
            }
        }
        return Laudo(g, null, problemas, ll.conflicts.isEmpty(), slr, lalr, frases)
    }

    private fun curta(p: Production) = "${p.lhs} para ${p.rhsText()}"

    private fun motivoConflito(t: LL1Table, a: String, c: String): String {
        val r = t.reasons[a to c].orEmpty()
        return if (r.any { "FOLLOW" in it }) "Uma opção de $a pode sumir, e $c também vem depois de $a. Mude a regra para separar os dois casos."
        else "Duas opções de $a começam com $c. Fatore ou reescreva a regra."
    }

    private fun descreve(t: LRTable, acoes: List<LRAction>): String = acoes.joinToString(" ou ") {
        when (it) {
            is LRAction.Shift -> "empilhar"
            is LRAction.Reduce -> "reduzir por ${t.grammar.productions[it.prodId]}"
            else -> "aceitar"
        }
    }

    fun improdutivos(g: Grammar): List<String> {
        val bons = mutableSetOf<String>()
        var mudou = true
        while (mudou) {
            mudou = false
            for (p in g.productions) if (p.lhs !in bons && p.rhs.all { !g.isNonTerminal(it) || it in bons }) { bons += p.lhs; mudou = true }
        }
        return g.nonTerminals.filter { it !in bons }
    }

    fun inalcancaveis(g: Grammar): List<String> {
        val vistos = mutableSetOf(g.start)
        val fila = ArrayDeque(listOf(g.start))
        while (fila.isNotEmpty()) for (p in g.productionsOf(fila.removeFirst())) for (s in p.rhs) {
            if (g.isNonTerminal(s) && vistos.add(s)) fila += s
        }
        return g.nonTerminals.filter { it !in vistos }
    }

    /** A chega em A pela esquerda passando por outro não terminal. */
    fun recursaoIndireta(g: Grammar): List<String> {
        val nulos = FirstFollow(g).nullable
        val canto = g.nonTerminals.associateWith { a ->
            buildSet {
                for (p in g.productionsOf(a)) for (s in p.rhs) {
                    if (g.isNonTerminal(s)) add(s)
                    if (s !in nulos) break
                }
            }
        }
        return g.nonTerminals.filter { a ->
            if (a in canto.getValue(a)) return@filter false
            val vistos = mutableSetOf<String>()
            val fila = ArrayDeque(canto.getValue(a))
            while (fila.isNotEmpty()) {
                val b = fila.removeFirst()
                if (b == a) return@filter true
                if (vistos.add(b)) fila += canto[b].orEmpty()
            }
            false
        }
    }

    private fun prefixoComum(g: Grammar): Pair<String, String> {
        for (nt in g.nonTerminals) {
            val grupos = g.productionsOf(nt).filter { it.rhs.isNotEmpty() }.groupBy { it.rhs.first() }
            grupos.entries.firstOrNull { it.value.size > 1 }?.let { return nt to it.key }
        }
        return g.start to "o mesmo símbolo"
    }

    fun frases(g: Grammar, rnd: Random, n: Int = 6): List<List<String>> {
        if (improdutivos(g).contains(g.start)) return emptyList()
        return List(n * 3) { SentenceGenerator.generate(g, rnd, maxDepth = 6, maxTokens = 9, tries = 12) }
            .filterNotNull().distinct().sortedBy { it.size }.take(n)
    }

    /** Procura uma frase com duas árvores entre as geradas. Achar prova; não achar não prova nada. */
    fun ambigua(g: Grammar, frases: List<List<String>>): List<String>? {
        if (frases.isEmpty()) return null
        return frases.firstOrNull { f -> f.size <= 9 && DerivationHelper.leftmostDerivations(g, f, limit = 2).size >= 2 }
    }

    /** Conserto que o motor sabe fazer: tira recursão direta e fatora. Null se não mudou nada. */
    fun conserto(g: Grammar): Grammar? {
        val semRec = if (GrammarTransform.leftRecursiveNTs(g).isNotEmpty()) GrammarTransform.eliminateImmediateLeftRecursion(g) else g
        val fat = if (GrammarTransform.needsLeftFactoring(semRec)) GrammarTransform.leftFactor(semRec) else semRec
        return if (fat === g) null else fat
    }
}

/**
 * A escada de dicas. A trava mora aqui, no código: o último degrau não abre sem uma tentativa.
 * Instrução em prompt vaza (o tutor do CS50 entrega código em até 1/4 das vezes); um if não vaza.
 */
class Degraus(val problema: Problema) {
    var degrau = 1
        private set
    var tentativas = 0
        private set

    val podeSubir: Boolean get() = degrau < 2 || (degrau == 2 && tentativas > 0)
    val travado: Boolean get() = degrau == 2 && tentativas == 0

    fun tentou() { tentativas++ }

    /** Sobe um degrau. Devolve false quando falta tentar. */
    fun sobe(): Boolean {
        if (!podeSubir) return false
        degrau++
        return true
    }

    val texto: String get() = when (degrau) {
        1 -> problema.aponta
        2 -> problema.porque + " " + problema.faca
        else -> problema.faca
    }
}

@Suppress("unused") private val EPSILON = EPS
