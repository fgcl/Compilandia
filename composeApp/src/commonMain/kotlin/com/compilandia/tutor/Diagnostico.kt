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
import com.compilandia.textos.tx

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
            return Laudo(null, e.message ?: tx("diagnostico_nao_entendi_esse_texto"), emptyList(), false, false, false, emptyList())
        }
        val problemas = mutableListOf<Problema>()
        fun linha(nt: String) = g.nonTerminals.indexOf(nt) + 1

        val semFim = improdutivos(g)
        for (nt in semFim) problemas += Problema(
            Falha.IMPRODUTIVO, linha(nt), nt,
            tx("diagnostico_1_nunca_termina_toda_regra", nt),
            tx("diagnostico_derivacao_que_passa_por_1", nt),
            tx("diagnostico_1_opcao_que_so_use", nt),
        )
        for (nt in inalcancaveis(g)) problemas += Problema(
            Falha.INALCANCAVEL, linha(nt), nt,
            tx("diagnostico_ninguem_chama_1_essa_regra", nt),
            tx("diagnostico_derivacao_comeca_em_1_nenhum", g.start, nt),
            tx("diagnostico_use_1_em_alguma_regra", nt),
        )
        for (nt in GrammarTransform.leftRecursiveNTs(g)) problemas += Problema(
            Falha.RECURSAO, linha(nt), nt,
            tx("diagnostico_na_linha_1_2_comeca", linha(nt), nt, nt),
            tx("diagnostico_para_decidir_1_parser_precisa", nt, nt),
            tx("diagnostico_crie_1_com_que_vem", nt, nt, nt),
        )
        for (nt in recursaoIndireta(g)) problemas += Problema(
            Falha.RECURSAO_INDIRETA, linha(nt), nt,
            tx("diagnostico_1_volta_para_2_passando", nt, nt),
            tx("diagnostico_recursao_esquerda_esta_escondida_parser"),
            tx("diagnostico_substitua_regra_meio_dentro_1", nt),
        )
        if (GrammarTransform.needsLeftFactoring(g)) {
            val (nt, inicio) = prefixoComum(g)
            problemas += Problema(
                Falha.PREFIXO, linha(nt), nt,
                tx("diagnostico_na_linha_1_duas_opcoes", linha(nt), nt, inicio),
                tx("diagnostico_olhando_simbolo_so_parser_nao"),
                tx("diagnostico_junte_comeco_igual_jogue_diferenca"),
            )
        }
        val frases = frases(g, rnd)
        ambigua(g, frases)?.let { f ->
            problemas += Problema(
                Falha.AMBIGUA, 1, null,
                tx("diagnostico_frase_1_tem_duas_arvores", f.joinToString(" ")),
                tx("diagnostico_duas_arvores_sao_dois_sentidos"),
                tx("diagnostico_separe_operadores_em_niveis_ou"),
                f,
            )
        }
        val ll = LL1Table(g)
        val temGrave = problemas.any { it.falha in setOf(Falha.RECURSAO, Falha.RECURSAO_INDIRETA, Falha.PREFIXO, Falha.AMBIGUA, Falha.IMPRODUTIVO) }
        if (!temGrave) ll.conflicts.firstOrNull()?.let { (a, t) ->
            val regras = ll.cell(a, t)
            problemas += Problema(
                Falha.CONFLITO_LL, linha(a), a,
                tx("diagnostico_na_tabela_casa_1_com", a, t, regras.size),
                tx("diagnostico_com_1_na_entrada_valem", t, regras.joinToString(" e ") { curta(it) }),
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
                    tx("diagnostico_no_estado_1_com_2", c.state, c.terminal, c.kind),
                    tx("diagnostico_1_olhar_so_follow_nao", descreve(tb, c.actions)),
                    if (lalr) tx("diagnostico_lalr_1_resolve_ele_guarda") else tx("diagnostico_reescreva_gramatica_ou_declare_precedencia"),
                )
            }
        }
        return Laudo(g, null, problemas, ll.conflicts.isEmpty(), slr, lalr, frases)
    }

    private fun curta(p: Production) = tx("diagnostico_1_para_2", p.lhs, p.rhsText())

    private fun motivoConflito(t: LL1Table, a: String, c: String): String {
        val r = t.reasons[a to c].orEmpty()
        return if (r.any { "FOLLOW" in it }) tx("diagnostico_opcao_1_pode_sumir_2", a, c, a)
        else tx("diagnostico_duas_opcoes_1_comecam_com", a, c)
    }

    private fun descreve(t: LRTable, acoes: List<LRAction>): String = acoes.joinToString(tx("diagnostico_ou")) {
        when (it) {
            is LRAction.Shift -> tx("diagnostico_empilhar")
            is LRAction.Reduce -> tx("diagnostico_reduzir_por_1", t.grammar.productions[it.prodId])
            else -> tx("ll1_aceitar")
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
        return g.start to tx("diagnostico_mesmo_simbolo")
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
