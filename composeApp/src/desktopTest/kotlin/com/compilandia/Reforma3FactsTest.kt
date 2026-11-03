package com.compilandia

import com.compilandia.content.Look
import com.compilandia.content.Temporada
import com.compilandia.content.ToyChamadas
import com.compilandia.content.ToyClosure
import com.compilandia.content.ToyTabelaViva
import com.compilandia.content.caixasDeItens
import com.compilandia.content.caixasDoAfd
import com.compilandia.content.emOrdem
import com.compilandia.content.episodes.Episodes
import com.compilandia.content.linhasDoEstado
import com.compilandia.content.rastroDescida
import com.compilandia.content.structsDaExpr
import com.compilandia.engine.DFA
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.MiniBison
import com.compilandia.engine.Mips
import com.compilandia.engine.TacGen
import com.compilandia.learning.Region
import com.compilandia.ui.lab.BisonExemplos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** O que a terceira reforma afirma (caixas, laboratório Bison e série em C), conferido contra o motor. */
class Reforma3FactsTest {

    @Test fun afdEmOrdemComoNaAula() {
        // A aula diz: q0 lê a e vai a q1; q1 lê b e vai a q2; q2 lê b e vai a q3, que é final.
        val d = DFA.fromRegex("(a|b)*abb").minimize().emOrdem()
        assertEquals(4, d.stateCount)
        assertEquals(0, d.start)
        assertEquals(1, d.delta[0 to 'a']); assertEquals(0, d.delta[0 to 'b'])
        assertEquals(2, d.delta[1 to 'b']); assertEquals(3, d.delta[2 to 'b'])
        assertEquals(setOf(3), d.accepting)
        for (w in listOf("abb", "aabb", "babb", "ababb")) assertTrue(d.accepts(w), w)
        for (w in listOf("ab", "abba", "bbb")) assertTrue(!d.accepts(w), w)
    }

    @Test fun caixaDoEstadoTemUmaLinhaPorLetraEOFioSaiDela() {
        val d = DFA.fromRegex("(a|b)*abb").minimize().emOrdem()
        val (boxes, wires) = caixasDoAfd(d, List(4) { 100f to 100f })
        assertTrue(boxes.all { it.look == Look.RECORD && it.rows.size == 2 })
        assertEquals("b → q2", boxes.first { it.id == "q1" }.rows[1])
        assertEquals(8, wires.size)
        assertTrue(wires.all { it.fromRow in 0..1 })
    }

    @Test fun caixasDeItensSaoOsConjuntosDoMotor() {
        val aut = LRAutomaton.lr0(Grammar.parse("E -> E + T | T\nT -> id"))
        val (boxes, wires) = caixasDeItens(aut, aut.states.map { 0f to 0f })
        assertEquals(aut.states.size, boxes.size)
        assertEquals(aut.states[0].items.size, boxes.first { it.id == "I0" }.rows.size)
        // Cada fio sai de uma linha cujo item tem o ponto antes do símbolo do fio.
        for (w in wires) {
            val linha = boxes.first { it.id == w.from }.rows[w.fromRow]
            assertTrue(linha.contains("• ${w.label}"), "$linha / ${w.label}")
        }
    }

    @Test fun relatorioDoBisonComoNaAula() {
        val b = MiniBison.compile(com.compilandia.content.episodes.SPEC_CALC.joinToString("\n"))
        assertTrue(b.conflicts.isEmpty())
        assertTrue(b.resolvedByPrecedence > 0)
        val s0 = linhasDoEstado(b.table, 0)
        assertTrue(s0.any { it.startsWith("NUM empilha") }, s0.toString())
        assertTrue(s0.any { it.startsWith("e vai") }, s0.toString())
        // A aula diz: no estado do conflito, o vezes empilha.
        val cf = b.table.rawConflictCells.first { it.second == "'*'" && b.table.automaton.states[it.first].kernel.any { k -> b.table.grammar.productions[k.prodId].rhs.contains("'+'") && k.dot == 3 } }
        assertTrue(b.table.resolutions.getValue(cf).endsWith("shift"), b.table.resolutions.getValue(cf))
        assertEquals(14L, b.run("2+3*4").value)
    }

    @Test fun quadrosDoLaboratorioTemAsTresPilhas() {
        val b = MiniBison.compile(BisonExemplos.calculadora)
        val q = b.quadros("2+3*4")
        assertTrue(q.last().texto.startsWith("aceita"))
        assertTrue(q.last().texto.endsWith("14"))
        assertTrue(q.any { it.conta == "\$\$ = 3 * 4 = 12" }, q.mapNotNull { it.conta }.toString())
        assertTrue(q.any { it.conta == "\$\$ = 2 + 12 = 14" })
        // Em todo quadro, as três pilhas andam juntas: um valor por símbolo.
        for (f in q) assertEquals(f.simbolos.size, f.valores.size)
        for (f in q) assertEquals(f.simbolos.size + 1, f.estados.size)
    }

    @Test fun exemplosDoLaboratorioFazemOQuePrometem() {
        assertEquals(14L, MiniBison.compile(BisonExemplos.calculadora).run("2+3*4").value)
        assertEquals(3L, MiniBison.compile(BisonExemplos.calculadora).run("8-3-2").value)
        assertTrue(MiniBison.compile(BisonExemplos.semPrecedencia).conflicts.isNotEmpty())
        assertEquals(512L, MiniBison.compile(BisonExemplos.potencia).run("2^3^2").value)
        assertEquals(-3L, MiniBison.compile(BisonExemplos.menosUnario).run("-5+2").value)
        // Trocar as duas linhas %left de lugar muda a conta: é o que a régua mostra.
        val trocado = BisonExemplos.calculadora.replace("%left '+' '-'\n%left '*' '/'", "%left '*' '/'\n%left '+' '-'")
        assertEquals(20L, MiniBison.compile(trocado).run("2+3*4").value)
    }

    @Test fun descidaRecursivaComoNaAula() {
        val r = rastroDescida(listOf("2", "+", "3", "*", "4"))
        assertEquals(listOf("main", "expr"), r.first().pilha)
        assertEquals(listOf("main", "expr", "termo", "fator"), r[2].pilha)
        // O mais é casado com expr no topo; o vezes, com termo.
        assertEquals("expr", r.first { it.acao == "casar" && it.alvo == "+" }.pilha.last())
        assertEquals("termo", r.first { it.acao == "casar" && it.alvo == "*" }.pilha.last())
        assertEquals(listOf("main"), r.last().pilha)
        assertEquals(5, r.count { it.acao == "casar" })
    }

    @Test fun arvoreDeStructsAponta() {
        val (nos, fios) = structsDaExpr(ExprParser.parse("2 + 3 * 4"), 100f)
        assertEquals(5, nos.size)
        assertEquals("op: '+'", nos.first { it.id == "t" }.rows[0])
        val dir = fios.first { it.from == "t" && it.fromRow == 2 }
        assertEquals("op: '*'", nos.first { it.id == dir.to }.rows[0])
    }

    @Test fun mipsDaAulaQuatro() {
        // A aula diz: o vezes vira quatro linhas, carrega b, carrega c, multiplica e guarda.
        val m = Mips.fromTac(TacGen.generate(ExprParser.parse("a + b * c")))
        assertEquals(8, m.size)
        assertTrue(m[0].startsWith("lw") && m[0].endsWith("b"), m.toString())
        assertTrue(m[1].startsWith("lw") && m[1].endsWith("c"), m.toString())
        assertTrue(m[2].startsWith("mul"))
        assertTrue(m[3].startsWith("sw"))
    }

    @Test fun brinquedosNovosChegamAoFim() {
        val d = DFA.fromRegex("(a|b)*abb").minimize().emOrdem()
        val tv = ToyTabelaViva(d, 4, "")
        for (c in "aabb") assertTrue(tv.aperta(c.toString()) || tv.pronto)
        assertTrue(tv.pronto)

        val tc = ToyClosure(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> ( E ) | id"), "")
        var guarda = 0
        while (!tc.pronto && guarda++ < 20) {
            val certa = tc.teclas.first { tecla -> tecla.contains("→ •") }
            assertTrue(tc.aperta(certa), certa)
        }
        assertTrue(tc.pronto)
        assertEquals(7, tc.shot.boxes.first { it.id == "I0" }.rows.size)

        val ch = ToyChamadas(listOf("1", "*", "2", "+", "3"), "")
        for (p in rastroDescida(listOf("1", "*", "2", "+", "3"))) {
            assertTrue(ch.aperta(when (p.acao) { "chamar" -> "Chamar"; "casar" -> "Casar"; else -> "Voltar" }))
        }
        assertTrue(ch.pronto)
    }

    @Test fun temporadasNovasAparecemNaRegiao() {
        assertTrue(Episodes.daRegiao(Region.FLORESTA_LEXICA, Temporada.TABELAS).isNotEmpty())
        // A série em C é uma só: aparece inteira em qualquer região, na ordem.
        val serie = Episodes.daRegiao(Region.PORTO, Temporada.EM_C)
        assertEquals(5, serie.size)
        assertEquals(serie, Episodes.daRegiao(Region.FORJA_CODIGO, Temporada.EM_C))
        assertTrue(serie.first().title.startsWith("Em C 1"))
        // Toda aula nova termina pedindo algo da pessoa: um toque ou um brinquedo, em algum ponto.
        for (e in Episodes.all.filter { it.temporada == Temporada.TABELAS || it.temporada == Temporada.EM_C }) {
            assertTrue(e.passos.any { it.ask != null || it.brinquedo != null }, e.title)
        }
    }
}
