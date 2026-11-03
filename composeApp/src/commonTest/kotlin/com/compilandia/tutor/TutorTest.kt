package com.compilandia.tutor

import com.compilandia.content.Album
import com.compilandia.content.episodes.Aberturas
import com.compilandia.content.episodes.Episodes
import com.compilandia.engine.DFA
import com.compilandia.engine.DfaCheck
import com.compilandia.engine.Grammar
import com.compilandia.engine.GrammarBank
import com.compilandia.engine.SetStep
import com.compilandia.learning.Skill
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EntradaTest {
    @Test fun descobreOQueFoiColado() {
        assertEquals(Tipo.GRAMATICA, Entrada.tipo("E -> E + T | T"))
        assertEquals(Tipo.GRAMATICA, Entrada.tipo("E → T E'"))
        assertEquals(Tipo.REGEX, Entrada.tipo("(a|b)*abb"))
        assertEquals(Tipo.REGEX, Entrada.tipo("ab*"))
        assertEquals(Tipo.EXPRESSAO, Entrada.tipo("a + b * c"))
        assertEquals(Tipo.CODIGO, Entrada.tipo("if (x >= 10) y = 1;"))
        assertEquals(Tipo.PERGUNTA, Entrada.tipo("o que é handle?"))
        assertEquals(Tipo.PERGUNTA, Entrada.tipo("como calculo o follow"))
        assertEquals(Tipo.MIPS, Entrada.tipo(".text\nmain:\n li \$v0, 10"))
        assertEquals(Tipo.BISON, Entrada.tipo("%token NUM\n%%\nexpr : NUM ;\n"))
        assertEquals(Tipo.VAZIO, Entrada.tipo("   "))
    }
}

class DiagnosticoTest {
    private fun falhas(t: String) = Diagnostico.analisa(t).problemas.map { it.falha }

    @Test fun achaRecursaoEFatoracao() {
        assertEquals(Falha.RECURSAO, falhas("E -> E + T | T\nT -> id").first())
        assertTrue(Falha.PREFIXO in falhas("A -> a b | a c"))
    }

    @Test fun achaRecursaoIndireta() {
        assertEquals(listOf("S", "A"), Diagnostico.recursaoIndireta(Grammar.parse("S -> A a | b\nA -> S c | d")))
        assertTrue(Falha.RECURSAO_INDIRETA in falhas("S -> A a | b\nA -> S c | d"))
    }

    @Test fun achaAmbiguidade() {
        val l = Diagnostico.analisa(GrammarBank.exprAmbiguous.text)
        assertTrue(l.problemas.any { it.falha == Falha.AMBIGUA }, l.problemas.toString())
    }

    @Test fun achaConflitoDaCasaDupla() {
        val l = Diagnostico.analisa("S -> A b\nA -> b | ε")
        assertEquals(Falha.CONFLITO_LL, l.problemas.first().falha)
        assertFalse(l.ll1)
    }

    @Test fun achaRegraSemFimESolta() {
        assertTrue(Falha.IMPRODUTIVO in falhas("S -> a A\nA -> b A"))
        assertTrue(Falha.INALCANCAVEL in falhas("S -> a\nB -> b"))
    }

    @Test fun gramaticaBoaPassaLimpa() {
        val l = Diagnostico.analisa(GrammarBank.exprLL1.text)
        assertTrue(l.limpa, l.problemas.toString())
        assertTrue(l.ll1 && l.slr)
        assertTrue(l.frases.isNotEmpty())
    }

    @Test fun lrSemConflitoNaoViraProblema() {
        val l = Diagnostico.analisa(GrammarBank.lalrNotSlr.text)
        assertFalse(l.slr)
        assertTrue(l.lalr)
    }

    @Test fun aceitaAFormaDeListaDeExercicio() {
        assertTrue(Diagnostico.analisa("E→E+T|T\nT→id").g != null)
        assertEquals("E -> E + T | T", Diagnostico.normaliza("E→E + T|T"))
    }

    @Test fun textoRuimDizOErro() {
        val l = Diagnostico.analisa("isso não é regra")
        assertNull(l.g)
        assertNotNull(l.erro)
    }

    @Test fun consertoTiraARecursao() {
        val g = Grammar.parse("E -> E + T | T\nT -> id")
        val c = assertNotNull(Diagnostico.conserto(g))
        assertTrue(Diagnostico.analisa(c.toText()).limpa)
    }
}

class DegrausTest {
    private val p = Diagnostico.analisa("E -> E + T | T\nT -> id").problemas.first()

    @Test fun ultimoDegrauSoAbreDepoisDeTentar() {
        val d = Degraus(p)
        assertEquals(1, d.degrau)
        assertTrue(d.sobe())
        assertEquals(2, d.degrau)
        assertTrue(d.travado)
        assertFalse(d.sobe(), "abriu sem tentativa")
        assertEquals(2, d.degrau)
        d.tentou()
        assertTrue(d.sobe())
        assertEquals(3, d.degrau)
    }

    @Test fun primeiroDegrauApontaESegundoExplica() {
        val d = Degraus(p)
        assertTrue("linha 1" in d.texto)
        d.sobe()
        assertTrue("parser" in d.texto)
    }
}

class SimsTest {
    private val g = GrammarBank.exprLL1.grammar
    private val rec = GrammarBank.exprLeftRec.grammar

    @Test fun todasRodamComGramaticaDeVerdade() {
        val tokens = listOf("id", "+", "id", "*", "id")
        val sims = listOf(
            Sims.conjuntos(g, SetStep.Kind.FIRST), Sims.conjuntos(g, SetStep.Kind.FOLLOW), Sims.tabelaLL(g),
            Sims.parseLL(g, tokens), Sims.parseLR(rec, tokens), assertNotNull(Sims.derivacao(g, tokens)),
            assertNotNull(Sims.recursao(rec)), Sims.lr0(Grammar.parse("E -> E + n | n")),
            Sims.afd("(a|b)*abb", "aabb"), Sims.tokens("if (x>=10) y = 2;"), Sims.expressao("a + b * c"),
            assertNotNull(Sims.ambigua(GrammarBank.exprAmbiguous.grammar, tokens)),
            assertNotNull(Sims.fatorar(Grammar.parse("A -> a b | a c"))),
            assertNotNull(Sims.laco(rec, "E")),
        )
        for (s in sims) {
            assertTrue(s.passos.isNotEmpty(), s.titulo)
            for (p in s.passos) {
                assertTrue(p.say.isNotBlank(), s.titulo)
                p.ask?.let { a -> assertTrue(a.certa in a.opcoes.indices, "${s.titulo}: ${a.pergunta}") }
            }
        }
    }

    @Test fun parserTerminaAceitandoOuTravando() {
        val tokens = listOf("id", "+", "id")
        assertTrue("vale" in Sims.parseLL(g, tokens).passos.last().say)
        assertTrue(Sims.parseLL(g, listOf("id", "+")).passos.last().erro)
        assertTrue("vale" in Sims.parseLR(rec, tokens).passos.last().say)
        assertTrue(Sims.parseLR(rec, listOf("+")).passos.last().erro)
    }

    @Test fun pecasDaSimulacaoCabemNoPalco() {
        val g2 = Grammar.parse("S -> a S b | c")
        val sims = listOf(Sims.conjuntos(g, SetStep.Kind.FOLLOW), Sims.tabelaLL(g), Sims.parseLL(g2, listOf("a", "a", "c", "b", "b")), Sims.afd("(a|b)*abb", "abb"), Sims.expressao("a + b"))
        for (s in sims) for (p in s.passos) for (b in p.shot.boxes) {
            if (b.look == com.compilandia.content.Look.CODE) continue
            assertTrue(b.x - b.w / 2 >= -14f && b.x + b.w / 2 <= 374f, "${s.titulo}: ${b.text} em x=${b.x} w=${b.w}")
            assertTrue(b.y + b.h / 2 <= 420f, "${s.titulo}: ${b.text} em y=${b.y}")
        }
    }

    @Test fun outroExemploVariaEDaCertoEmTodoAssuntoComSimulacao() {
        val assuntos = listOf(Skill.TOKENS, Skill.REGEX_DFA, Skill.FIRST, Skill.FOLLOW, Skill.LL1_TABLE, Skill.LL1_PARSING, Skill.CFG_DERIVATION, Skill.AMBIGUITY, Skill.LEFT_RECURSION, Skill.LEFT_FACTORING, Skill.SHIFT_REDUCE, Skill.LR0_ITEMS, Skill.TAC)
        for (s in assuntos) repeat(6) { i -> assertNotNull(Sims.exemplo(s, Random(i)), "$s semente $i") }
    }
}

class DesafioTest {
    @Test fun oMesmoDiaDaOMesmoSegredo() {
        assertEquals(Desafio.doDia(20800).segredo, Desafio.doDia(20800).segredo)
    }

    @Test fun todoDiaTemFraseValida() {
        for (dia in 20759L..21124L) {
            val d = Desafio.doDia(dia)
            assertTrue(d.tamanho in 5..7, "dia $dia")
            assertTrue(Desafio.valido(d, d.segredo), "segredo inválido no dia $dia")
        }
    }

    @Test fun coresSeguemARegraDoWordle() {
        val s = listOf("(", "id", "+", "id", ")")
        assertEquals(listOf(Cor.AMARELO, Cor.AMARELO, Cor.AMARELO, Cor.CINZA, Cor.CINZA), Desafio.cores(listOf("id", "+", "id", "+", "id"), s))
        assertEquals(listOf(Cor.VERDE, Cor.VERDE, Cor.AMARELO, Cor.AMARELO, Cor.AMARELO), Desafio.cores(listOf("(", "id", ")", "+", "id"), s))
        assertEquals(List(5) { Cor.VERDE }, Desafio.cores(s, s))
    }

    @Test fun recusaFraseQueAGramaticaNaoAceita() {
        val d = Desafio.doDia(20800)
        assertFalse(Desafio.valido(d, List(d.tamanho) { ")" }))
    }

    @Test fun oResultadoNaoEntregaAFrase() {
        val d = Desafio.doDia(20800)
        val r = Desafio.resultado(d, listOf(d.segredo), true)
        assertTrue("1/6" in r)
        assertFalse(d.segredo.joinToString(" ") in r)
    }
}

class DfaCheckTest {
    @Test fun acuseAMenorDiferenca() {
        val a = DFA.fromRegex("ab*")
        val b = DFA.fromRegex("ab")
        assertEquals("a", DfaCheck.diferenca(a, b))
        assertNull(DfaCheck.diferenca(a, DFA.fromRegex("ab*")))
    }

    @Test fun autômatoParcialFuncionaNaBancada() {
        // q0 -a-> q1 -b-> q2, com laço de b em q2: aceita abb, que não termina em ab
        val montado = DFA(3, 0, setOf(2), listOf('a', 'b'), mapOf((0 to 'a') to 1, (1 to 'b') to 2, (2 to 'b') to 2))
        val alvo = DFA.fromRegex("(a|b)*ab")
        val c = assertNotNull(DfaCheck.diferenca(montado, alvo))
        assertTrue(montado.accepts(c) != alvo.accepts(c))
    }

    @Test fun pecasMinimasNaoContamOBuraco() {
        assertEquals(2, DfaCheck.pecasMinimas(DFA.fromRegex("ab*")))
        assertEquals(3, DfaCheck.pecasMinimas(DFA.fromRegex("(a|b)*ab")))
    }
}

class RespostasTest {
    @Test fun perguntaLevaAoClipeCerto() {
        assertEquals("O que é handle", Respostas.busca("o que é handle?")?.clipe?.title)
        assertEquals("O que é FOLLOW", Respostas.busca("me explica follow")?.clipe?.title)
        assertEquals("Maior casamento", Respostas.busca("o que é maior casamento?")?.clipe?.title)
        assertEquals("Para que serve o épsilon", Respostas.busca("para que serve o épsilon?")?.clipe?.title)
        assertEquals("Conflito shift-reduce", Respostas.busca("o que é conflito shift-reduce?")?.clipe?.title)
    }

    @Test fun todoClipeTemTermosEDuasFalas() {
        for (c in Episodes.clipes) {
            assertTrue(c.passos.size in 2..4, c.title)
            assertNotNull(Respostas.busca(c.title), c.title)
        }
    }

    @Test fun oQueNaoSabeNaoInventa() {
        assertNull(Respostas.busca("qual a capital da Mongólia"))
    }
}

class AlbumTest {
    @Test fun todoBugEstaCompleto() {
        assertEquals(Album.especies.size, Album.especies.map { it.id }.distinct().size)
        for (e in Album.especies) {
            assertEquals(3, e.testes.size, e.id)
            assertTrue(e.testes.any { it.verdade } && e.testes.any { !it.verdade }, "${e.id} precisa de verdade e de mentira")
            assertTrue(e.testes.all { it.texto.length <= 84 }, "${e.id}: frase longa para a carta")
        }
    }

    @Test fun todoAssuntoTemAbertura() {
        for (s in Skill.entries) assertNotNull(Aberturas.of(s), "$s sem abertura")
        for (a in Aberturas.todas) assertTrue(a.certa in a.opcoes.indices, a.manchete)
    }
}
