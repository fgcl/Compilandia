package com.compilandia.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** J8: a reação dos jogos de classificar vem do motor. */
class ReacoesTest {
    @Test fun porteiroMostraPrimeiroOQueAPessoaErrou() {
        val r = Reacoes.porteiro("(a|b)*abb", listOf("abb" to false, "ab" to false), listOf("babb", "ba"))
        assertEquals(1 + 1 + 2, r.size)
        assertTrue(r[1].linha.startsWith("abb:"))
        assertTrue(r[1].nota.startsWith("Você barrou abb."), r[1].nota)
        assertEquals(false, r[1].ok)
        assertEquals(null, r[2].ok, "babb é nova")
        assertTrue(r[2].linha.endsWith("entra"), r[2].linha)
    }

    @Test fun esteiraRodaAteOMips() {
        val nomes = listOf("L", "S", "Se", "I", "O", "G")
        val r = Reacoes.esteira(nomes)
        assertEquals(8, r.size)
        assertTrue("t1 = a + 6" in r[5].linha || "a + 6" in r[5].linha, r[5].linha)
        assertTrue(r.last().linha.endsWith("x = 10"), r.last().linha)
    }

    @Test fun detetiveParaNaFaseCerta() {
        val r = Reacoes.detetive("int x; x = \"oi\";", 2, 1, listOf("léxico", "sintático", "semântico", "execução"))
        assertEquals(listOf(null, true, true, false, null), r.map { it.ok })
        assertEquals("Você apontou sintático. Quem pega é semântico.", r.last().nota)
    }

    @Test fun fatiadorMandaIdParaATabela() {
        val toks = MiniLexer.tokenize("while (x < 10)")
        val classes = toks.indices.associateWith { toks[it].cls }.toMutableMap()
        classes[0] = TokenClass.ID
        val r = Reacoes.fatiador(toks, classes)
        assertEquals(false, r[0].ok)
        assertTrue(r[0].nota.startsWith("while entra na tabela de símbolos."), r[0].nota)
        assertEquals("id ( id < num )", r[r.size - 2].linha)
    }

    @Test fun duasArvoresDaoValoresDiferentes() {
        val g = GrammarBank.exprAmbiguous.grammar
        val t1 = Earley(g).let { LRParser.parse(LRTable(g, LRKind.SLR1, mapOf("+" to Precedence(1, Assoc.LEFT), "*" to Precedence(2, Assoc.LEFT)), resolveConflicts = true), listOf("id", "+", "id", "*", "id")).tree!! }
        val t2 = LRParser.parse(LRTable(g, LRKind.SLR1, mapOf("+" to Precedence(2, Assoc.LEFT), "*" to Precedence(1, Assoc.LEFT)), resolveConflicts = true), listOf("id", "+", "id", "*", "id")).tree!!
        val r = Reacoes.duasArvores(t1, t2)
        assertEquals("2 + (3 * 4) = 14", r[1].linha)
        assertEquals("(2 + 3) * 4 = 20", r[2].linha)
        assertEquals(false, r[3].ok)
    }

    @Test fun cirurgiaCertaAnalisaSemTravar() {
        val g = Grammar.parse("E -> E + T | T\nT -> id")
        val certa = GrammarTransform.eliminateImmediateLeftRecursion(g)
        val frases = listOf(listOf("id"), listOf("id", "+", "id"))
        val r = Reacoes.cirurgia(g, certa, frases)
        assertTrue(r.drop(1).all { it.ok == true }, r.joinToString { it.nota })
        // Esquecer a regra vazia: a frase id para de sair.
        val torta = Grammar.parse("E -> T E'\nE' -> + T E'\nT -> id")
        val r2 = Reacoes.cirurgia(g, torta, frases)
        assertEquals(false, r2[1].ok)
        // Deixar a recursão à esquerda: duas regras na mesma célula.
        val r3 = Reacoes.cirurgia(g, g, frases)
        assertEquals(2, r3.size)
        assertTrue(r3[1].nota.startsWith("A célula M[E, id] ficou com duas regras."), r3[1].nota)
    }

    @Test fun montaGramaticaJuntaFixasEMontadas() {
        val g = Reacoes.montaGramatica("E", listOf(Production(0, "T", listOf("id"))), listOf("E" to listOf("T", "E'"), "E'" to listOf("+", "T", "E'"), "E'" to emptyList()))!!
        assertEquals("E", g.start)
        assertTrue(LL1Table(g).isLL1)
    }

    @Test fun conflitoParaNaCelulaDupla() {
        val g = GrammarBank.exprAmbiguous.grammar
        val t = LRTable(g, LRKind.SLR1)
        val r = Reacoes.conflito(t, g)
        assertTrue(r.isNotEmpty())
        assertEquals(false, r.last().ok)
        assertTrue("id + id" in r.first().linha || "id * id" in r.first().linha, r.first().linha)
        val semConflito = Grammar.parse("E -> E + T | T\nT -> id")
        val r2 = Reacoes.conflito(LRTable(semConflito, LRKind.SLR1), semConflito)
        assertEquals(1, r2.size)
        assertEquals(true, r2[0].ok)
    }

    @Test fun escopoAchaOMesmoQueOGerador() {
        repeat(40) { s ->
            val p = ScopeGen.random(Random(s))
            for (u in p.uses) assertEquals(u.value.resolvesTo, Reacoes.resolve(p, u.index, u.value.use!!).second, "semente $s, linha ${u.index}")
            val r = Reacoes.escopo(p, emptyMap())
            assertTrue(r.isNotEmpty() || p.uses.isEmpty())
        }
    }

    @Test fun panicoChegaAoFim() {
        val g = Grammar.parse("P -> S ; P | ε\nS -> id = E | print E\nE -> T E'\nE' -> + T E' | ε\nT -> id | num")
        val inp = listOf("id", "=", "=", "num", ";", "id", "=", "num", ";")
        val tr = PanicMode.trace(LL1Table(g), inp)
        val r = Reacoes.panico(inp, tr)
        assertTrue(r.size >= 3)
        assertFalse(r[1].ok!!)
        assertTrue("[" in r[1].linha, r[1].linha)
        // Um erro só: a frase do fim fica no singular ("Achou 1 erros" apareceu na captura).
        assertEquals(1, tr.events.size)
        assertEquals(com.compilandia.textos.tx("reacao_panico_fim_um"), r.last().nota)
    }

    @Test fun fatiadorComUmNomeSoFicaNoSingular() {
        val toks = MiniLexer.tokenize("x = 1;")
        val r = Reacoes.fatiador(toks, toks.indices.associateWith { toks[it].cls })
        assertEquals("x", r.last().linha)
        assertEquals(com.compilandia.textos.tx("reacao_fatiador_tabela_um"), r.last().nota)
    }
}
