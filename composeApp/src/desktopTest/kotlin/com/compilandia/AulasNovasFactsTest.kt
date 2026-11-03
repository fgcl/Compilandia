package com.compilandia

import com.compilandia.content.Album
import com.compilandia.content.Temporada
import com.compilandia.content.ToyCorte
import com.compilandia.content.ToyDerivacao
import com.compilandia.content.ToyEmpilhaReduz
import com.compilandia.content.ToyMips
import com.compilandia.content.ToyPilhaLL
import com.compilandia.content.ToyTipos
import com.compilandia.content.ToyTrem
import com.compilandia.content.episodes.Episodes
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.FirstFollow
import com.compilandia.engine.Grammar
import com.compilandia.engine.LL1Table
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRKind
import com.compilandia.engine.LRParser
import com.compilandia.engine.LRTable
import com.compilandia.engine.MiniBison
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.Mips
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.TacGen
import com.compilandia.engine.tokensOf
import com.compilandia.learning.Region
import com.compilandia.tutor.Diagnostico
import com.compilandia.tutor.Sims
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** O que as aulas novas afirmam, conferido contra o motor. */
class AulasNovasFactsTest {
    @Test fun cadaRegiaoTemPegadinhaEPratica() {
        for (r in Region.entries) {
            assertTrue(Episodes.daRegiao(r, Temporada.PEGADINHA).isNotEmpty(), "$r sem pegadinhas")
            assertTrue(Episodes.daRegiao(r, Temporada.PRATICA).isNotEmpty(), "$r sem prática")
        }
    }

    @Test fun idsNaoColidem() {
        val ids = (Episodes.all + Episodes.clipes).map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(Episodes.basicos.all { it.id == it.skill.name }, "saves antigos guardam o nome do assunto")
    }

    @Test fun ifaEIdentificador() {
        val t = MiniLexer.tokenize("ifa=1;")
        assertEquals("ifa", t[0].lexeme)
        assertEquals(com.compilandia.engine.TokenClass.ID, t[0].cls)
        assertEquals(com.compilandia.engine.TokenClass.KEYWORD, MiniLexer.tokenize("if")[0].cls)
    }

    @Test fun estrelaMaisEInterrogacao() {
        assertTrue(DFA.fromRegex("a*").accepts("")); assertTrue(DFA.fromRegex("a*").accepts("aa"))
        assertFalse(DFA.fromRegex("a+").accepts("")); assertTrue(DFA.fromRegex("a+").accepts("aa"))
        assertTrue(DFA.fromRegex("a?").accepts("")); assertTrue(DFA.fromRegex("a?").accepts("a")); assertFalse(DFA.fromRegex("a?").accepts("aa"))
    }

    @Test fun fechoDoInicio() {
        val n = SmallNFA("t", "", 3, 0, setOf(2), listOf(Triple(0, null, 1), Triple(1, 'a', 2)))
        assertEquals(setOf(0, 1), n.epsClosure(setOf(0)))
    }

    @Test fun ordemDasRegrasNoFlex() {
        val nomePrimeiro = MiniFlex(listOf(MiniFlex.Rule("[a-z]+", "ID"), MiniFlex.Rule("\"if\"", "IF")))
        val ifPrimeiro = MiniFlex(listOf(MiniFlex.Rule("\"if\"", "IF"), MiniFlex.Rule("[a-z]+", "ID")))
        assertEquals("ID", nomePrimeiro.scan("if").tokens.single().token)
        assertEquals("IF", ifPrimeiro.scan("if").tokens.single().token)
    }

    @Test fun recursaoIndiretaESubstituicao() {
        val g = Grammar.parse("S -> A a | b\nA -> S c | d")
        assertTrue("S" in Diagnostico.recursaoIndireta(g))
        val depois = Grammar.parse("S -> S c a | d a | b")
        assertTrue("S" in com.compilandia.engine.GrammarTransform.leftRecursiveNTs(depois))
    }

    @Test fun prefixoEscondido() {
        val g = Grammar.parse("A -> a b | B\nB -> a c")
        assertFalse(com.compilandia.engine.GrammarTransform.needsLeftFactoring(g))
        assertEquals(2, LL1Table(g).cell("A", "a").size)
    }

    @Test fun firstDeRegraInteira() {
        val f = FirstFollow(Grammar.parse("S -> A B c\nA -> a | ε\nB -> b | ε"))
        assertEquals(setOf("a", "b", "c"), f.first["S"])
    }

    @Test fun followComEpsilon() {
        val f = FirstFollow(Grammar.parse("S -> A b\nA -> a B\nB -> d | ε"))
        assertEquals(setOf("b"), f.follow["B"])
    }

    @Test fun casaDuplaSemRecursao() {
        val t = LL1Table(Grammar.parse("S -> A b\nA -> b | ε"))
        assertEquals(2, t.cell("A", "b").size)
        assertFalse(t.isLL1)
    }

    @Test fun vezesEsperaOMais() {
        val g = Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> id")
        val r = LRParser.parse(LRTable(g, LRKind.SLR1), tokensOf("id + id * id"))
        val passo = r.steps.first { it.symbols == listOf("E", "+", "T") && it.input.first() == "*" }
        assertTrue(passo.action is LRAction.Shift)
    }

    @Test fun conflitoSemAmbiguidade() {
        val g = Grammar.parse("S -> L = R | R\nL -> * R | id\nR -> L")
        assertTrue(LRTable(g, LRKind.SLR1).hasConflicts)
        assertFalse(LRTable(g, LRKind.LALR1).hasConflicts)
    }

    @Test fun juntarEstadosCriaReduceReduce() {
        val g = Grammar.parse("S -> a A d | b B d | a B e | b A e\nA -> c\nB -> c")
        assertFalse(LRTable(g, LRKind.LR1).hasConflicts)
        val t = LRTable(g, LRKind.LALR1)
        assertTrue(t.conflicts.any { it.kind.contains("reduce/reduce") }, t.conflicts.toString())
    }

    @Test fun quemFicaEmbaixoManda() {
        val regras = com.compilandia.engine.BisonChallenges.all[0].starterSpec.substringAfter("%%")
        fun conta(prec: String) = MiniBison.compile("%token NUM\n$prec\n%%$regras").run("2+3*4").value
        assertEquals(14L, conta("%left '+'\n%left '*'"))
        assertEquals(20L, conta("%left '*'\n%left '+'"))
    }

    @Test fun temporariosNaOrdem() {
        assertEquals(listOf("t1 = b * c", "t2 = a + t1"), TacGen.generate(ExprParser.parse("a + b * c")).map { it.toString() })
    }

    @Test fun doisRegistradoresBastam() {
        val code = listOf(TacGen.Instr("t1", "a", "+", "b"), TacGen.Instr("t2", "c", "+", "d"), TacGen.Instr("t3", "t1", "*", "t2"))
        val g = RegAlloc.interference(code)
        assertTrue("t2" in g.getValue("t1"))
        assertFalse("t3" in g.getValue("t1"))
        assertEquals(2, RegAlloc.chromatic(g))
    }

    @Test fun errosDoAlbumBatemComOMotor() {
        assertFalse(DFA.fromRegex("(ab)*").accepts("aba"))
        assertTrue(Album.especies.any { it.id == "follow-vazio" })
        assertEquals(setOf("$"), FirstFollow(Grammar.parse("S -> a")).follow["S"])
        val ll = Grammar.parse("S -> A b\nA -> b | ε")
        assertTrue(com.compilandia.engine.GrammarTransform.leftRecursiveNTs(ll).isEmpty())
    }

    // ------------------------------------------------- os brinquedos chegam ao fim

    @Test fun tremChegaEmTresLetras() {
        val d = DFA.fromRegex("aa*b")
        val t = ToyTrem(d, Sims.posicoes(d.stateCount), "", 3)
        for (c in "aab") assertTrue(t.aperta(c.toString()), "recusou $c")
        assertTrue(t.pronto)
    }

    @Test fun corteDaLinha() {
        val t = ToyCorte("x>=10;", "")
        fun corta(letrasExtras: Int) { repeat(letrasExtras) { assertTrue(t.aperta("Mais uma letra")) }; assertTrue(t.aperta("Cortar aqui")) }
        assertFalse(t.aperta("Mais uma letra"), "x tem uma letra só")
        corta(0); corta(1); corta(1); corta(0)
        assertTrue(t.pronto)
    }

    @Test fun derivacaoPelasTeclas() {
        val g = Grammar.parse("E -> T X\nX -> + T X | ε\nT -> id")
        val t = ToyDerivacao(g, tokensOf("id + id"), "")
        for (r in listOf("E → T X", "T → id", "X → + T X", "T → id", "X → ε")) assertTrue(t.aperta(r), "recusou $r")
        assertTrue(t.pronto)
    }

    @Test fun pilhaLlAte0Fim() {
        val t = ToyPilhaLL(Grammar.parse("E -> n X\nX -> + n X | ε"), tokensOf("n + n"), "")
        var passos = 0
        while (!t.pronto && passos++ < 30) { if (!t.aperta("Trocar pela regra")) assertTrue(t.aperta("Casar")) }
        assertTrue(t.pronto)
    }

    @Test fun esteiraAteOFim() {
        val t = ToyEmpilhaReduz(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> id"), tokensOf("id + id * id"), "")
        var passos = 0
        while (!t.pronto && passos++ < 40) { if (!t.aperta("Reduzir")) assertTrue(t.aperta("Empilhar")) }
        assertTrue(t.pronto)
    }

    @Test fun tiposSobem() {
        val t = ToyTipos("a + x * 2", ExprGenerator.env, "")
        var passos = 0
        while (!t.pronto && passos++ < 20) { if (!t.aperta("float")) assertTrue(t.aperta("int")) }
        assertTrue(t.pronto)
    }

    @Test fun mipsNaOrdem() {
        val t = ToyMips("a + b", "")
        assertEquals(Mips.fromTac(TacGen.generate(ExprParser.parse("a + b"))).size, 4)
        for (k in listOf("lw", "lw", "add", "sw")) assertTrue(t.aperta(k), k)
        assertTrue(t.pronto)
    }
}
