package com.compilandia

import com.compilandia.content.episodes.ARVORE_AMB_CONSERTO
import com.compilandia.content.episodes.ARVORE_AMB_DIR
import com.compilandia.content.episodes.ARVORE_AMB_ESQ
import com.compilandia.content.episodes.ARVORE_CFG_2
import com.compilandia.content.episodes.FRASE_CFG_2
import com.compilandia.content.episodes.FRASE_CFG_FORA
import com.compilandia.content.episodes.GRAMATICA_AMB_2
import com.compilandia.content.episodes.GRAMATICA_AMB_CONSERTO
import com.compilandia.content.episodes.GRAMATICA_CFG_2
import com.compilandia.content.episodes.GRAMATICA_FAT_2
import com.compilandia.content.episodes.GRAMATICA_FAT_2_DEPOIS
import com.compilandia.content.episodes.GRAMATICA_FAT_2_RODADA1
import com.compilandia.content.episodes.GRAMATICA_FAT_3
import com.compilandia.content.episodes.GRAMATICA_FAT_3_DEPOIS
import com.compilandia.content.episodes.GRAMATICA_REC_2
import com.compilandia.content.episodes.GRAMATICA_REC_2_DEPOIS
import com.compilandia.content.episodes.GRAMATICA_REC_3
import com.compilandia.content.episodes.GRAMATICA_REC_3_DEPOIS
import com.compilandia.engine.DerivationHelper
import com.compilandia.engine.Earley
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarTransform
import com.compilandia.engine.LL1Table
import com.compilandia.engine.ParseNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A9: o que o segundo exemplo das aulas longas de gramática afirma, o motor confirma. */
class A9GramaticaFactsTest {
    private fun gram(linhas: List<String>) = Grammar.parse(linhas.joinToString("\n"))
    /** Gramática como texto comparável: uma linha por não-terminal, sem espaços de alinhamento. */
    private fun texto(g: Grammar) = g.toText().replace("→", "->")
    private fun texto(linhas: List<String>) = texto(gram(linhas))
    private fun valor(t: ParseNode, nums: ArrayDeque<Int>): Int = when {
        t.symbol == "n" -> nums.removeFirst()
        t.children.size == 1 -> valor(t.children[0], nums)
        else -> { val a = valor(t.children[0], nums); a - valor(t.children[2], nums) }
    }

    @Test fun derivacaoDaDeclaracao() {
        val g = gram(GRAMATICA_CFG_2)
        val ds = DerivationHelper.leftmostDerivations(g, FRASE_CFG_2)
        assertEquals(1, ds.size, "uma derivação só")
        assertEquals(listOf("Decl → Tipo Var ;", "Tipo → float", "Var → * id"), ds[0].map { it.toString() }, "três trocas, nesta ordem")
        assertEquals(ARVORE_CFG_2, DerivationHelper.treeOf(g, ds[0]))
        // Depois da primeira troca, o mais à esquerda é Tipo.
        assertEquals("Tipo", ds[0][0].rhs.first { g.isNonTerminal(it) })
        assertFalse(Earley(g).accepts(FRASE_CFG_FORA), "int id id ; fica fora")
    }

    @Test fun ambiguidadeDoMenos() {
        val frase = listOf("n", "-", "n", "-", "n")
        val g = gram(GRAMATICA_AMB_2)
        val arvores = DerivationHelper.leftmostDerivations(g, frase, limit = 10).map { DerivationHelper.treeOf(g, it) }.toSet()
        assertEquals(setOf(ARVORE_AMB_ESQ, ARVORE_AMB_DIR), arvores, "exatamente as duas árvores da aula")
        assertEquals(2, valor(ARVORE_AMB_ESQ, ArrayDeque(listOf(9, 5, 2))))
        assertEquals(6, valor(ARVORE_AMB_DIR, ArrayDeque(listOf(9, 5, 2))))

        val c = gram(GRAMATICA_AMB_CONSERTO)
        val so = DerivationHelper.leftmostDerivations(c, frase, limit = 10)
        assertEquals(1, so.size, "consertada, uma árvore só")
        assertEquals(ARVORE_AMB_CONSERTO, DerivationHelper.treeOf(c, so[0]))
        assertEquals(2, valor(ARVORE_AMB_CONSERTO, ArrayDeque(listOf(9, 5, 2))), "agrupa pela esquerda")

        // Primeiro exemplo: n + n * n também tem duas árvores.
        val m = Grammar.parse("E -> E + E | E * E | n")
        assertEquals(2, DerivationHelper.leftmostDerivations(m, listOf("n", "+", "n", "*", "n"), limit = 10).size)
    }

    @Test fun recursaoDaLista() {
        val g = gram(GRAMATICA_REC_2)
        assertTrue(GrammarTransform.hasImmediateLeftRecursion(g, "L"))
        val depois = GrammarTransform.eliminateImmediateLeftRecursion(g)
        assertEquals(texto(GRAMATICA_REC_2_DEPOIS), texto(depois))
        assertTrue(GrammarTransform.leftRecursiveNTs(depois).isEmpty(), "nenhuma regra começa consigo mesma")
        assertTrue(Earley(depois).accepts(listOf("id", ",", "id")), "ainda gera id , id")
        // A linguagem é a mesma: confere todas as frases curtas.
        val simbolos = listOf("id", ",")
        var frases = listOf(emptyList<String>())
        repeat(6) {
            frases = frases.flatMap { f -> simbolos.map { f + it } }
            for (f in frases) assertEquals(Earley(g).accepts(f), Earley(depois).accepts(f), "$f")
        }
        // Primeiro exemplo: E -> E + n | n.
        val e = GrammarTransform.eliminateImmediateLeftRecursion(Grammar.parse("E -> E + n | n"))
        assertEquals(texto(listOf("E  -> n E'", "E' -> + n E' | ε")), texto(e))
    }

    /** Caso difícil: dois alfas (mais T e menos T) viram duas alternativas de E', e o ε entra uma vez. */
    @Test fun recursaoComDoisAlfas() {
        val g = gram(GRAMATICA_REC_3)
        val depois = GrammarTransform.eliminateImmediateLeftRecursion(g)
        assertEquals(texto(GRAMATICA_REC_3_DEPOIS), texto(depois))
        assertTrue(GrammarTransform.leftRecursiveNTs(depois).isEmpty())
        val frase = listOf("T", "-", "T", "+", "T")
        assertTrue(Earley(g).accepts(frase) && Earley(depois).accepts(frase))
        // As duas opções erradas da pergunta perdem frases: sem o menos, e sem o ε.
        assertFalse(Earley(gram(listOf("E -> T X", "X -> + T X | ε"))).accepts(frase), "sem o menos, perde a subtração")
        assertFalse(Earley(gram(listOf("E -> T X", "X -> + T X | - T X"))).accepts(frase), "sem o ε, nenhuma frase termina")
    }

    /** Fatoração com resto vazio: o if sem else deixa ε em S'; e o else solto continua em conflito. */
    @Test fun fatoracaoDoIfComRestoVazio() {
        val g = gram(GRAMATICA_FAT_3)
        val depois = GrammarTransform.leftFactor(g)
        // O motor põe o x antes do if S S'; a aula segue a ordem da gramática original. As regras são as mesmas.
        fun regras(x: com.compilandia.engine.Grammar) = x.productions.map { it.lhs to it.rhs }.toSet()
        assertEquals(regras(gram(GRAMATICA_FAT_3_DEPOIS)), regras(depois))
        assertFalse(GrammarTransform.needsLeftFactoring(depois))
        val t = LL1Table(depois)
        assertEquals(listOf("S'" to "else"), t.conflicts, "o else solto: S' com else tem duas regras")
        for (f in listOf(listOf("if", "x"), listOf("if", "x", "else", "x"), listOf("x")))
            assertTrue(Earley(depois).accepts(f), "$f")
        // As opções erradas da pergunta: sem o ε, o if sem else some da linguagem.
        assertFalse(Earley(gram(listOf("S -> if S S' | x", "S' -> else S"))).accepts(listOf("if", "x")))
    }

    @Test fun fatoracaoDosComandos() {
        val g = gram(GRAMATICA_FAT_2)
        assertTrue(GrammarTransform.needsLeftFactoring(g))
        assertFalse(LL1Table(g).isLL1, "sem fatorar, não é LL(1)")
        val depois = GrammarTransform.leftFactor(g)
        assertEquals(texto(GRAMATICA_FAT_2_DEPOIS), texto(depois))
        assertFalse(GrammarTransform.needsLeftFactoring(depois))
        // A primeira rodada, mostrada no meio da aula, é a fatoração só do C.
        val r1 = gram(GRAMATICA_FAT_2_RODADA1)
        assertTrue(GrammarTransform.needsLeftFactoring(r1), "na regra nova, duas alternativas ainda começam igual")
        assertEquals(texto(GRAMATICA_FAT_2_DEPOIS), texto(GrammarTransform.leftFactor(r1)))
        val t = LL1Table(depois)
        assertTrue(t.isLL1, "fatorada, é LL(1)")
        assertEquals(1, t.cell("C'", "(").size, "lendo abre parêntese, uma alternativa só")
        val frases = listOf("id = n", "id ( )", "id ( n )").map { it.split(" ") }
        for (f in frases) assertEquals(Earley(g).accepts(f), Earley(depois).accepts(f), "$f")
    }
}
