package com.compilandia

import com.compilandia.content.episodes.ATTR_EXEMPLO_2
import com.compilandia.content.episodes.ATTR_EXEMPLO_2_ERRO
import com.compilandia.content.episodes.ESCOPO_SEMENTE_2
import com.compilandia.content.episodes.OTIM_EXEMPLO_2
import com.compilandia.content.episodes.REG_EXEMPLO_2
import com.compilandia.content.episodes.RT_ARGUMENTO_2
import com.compilandia.content.episodes.TAC_DESTINO_2
import com.compilandia.content.episodes.TAC_EXEMPLO_2
import com.compilandia.content.episodes.escopoExemplo2
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.RtEvent
import com.compilandia.engine.RuntimeSim
import com.compilandia.engine.TacGen
import com.compilandia.engine.Type
import com.compilandia.engine.TypeChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A9: o que os segundos exemplos das aulas longas de semântica e back-end afirmam, o motor confirma. */
class A9BackendFactsTest {

    /** "c vezes dois" é int; "int mais float" na raiz é float; com ok (bool) no lugar do y, a raiz é erro. */
    @Test fun atributosETipos() {
        val e = ExprParser.parse(ATTR_EXEMPLO_2)
        val at = TypeChecker.evaluate(e, ExprGenerator.env)
        assertEquals("+", e.text)
        val vezes = e.children[0]
        assertEquals("*", vezes.text)
        assertEquals(Type.INT, at.getValue(vezes.children[0].id).type, "c é int (regra dois)")
        assertEquals(Type.INT, at.getValue(vezes.children[1].id).type, "dois é int (regra um)")
        assertEquals(Type.FLOAT, at.getValue(e.children[1].id).type, "y é float (regra dois)")
        assertEquals(Type.INT, at.getValue(vezes.id).type, "inteiro com inteiro fica inteiro")
        assertEquals(Type.FLOAT, at.getValue(e.id).type, "inteiro mais real dá real")
        assertTrue(at.getValue(e.id).coerced, "o inteiro vira real antes da soma")

        val e2 = ExprParser.parse(ATTR_EXEMPLO_2_ERRO)
        val at2 = TypeChecker.evaluate(e2, ExprGenerator.env)
        assertEquals(Type.BOOL, at2.getValue(e2.children[1].id).type, "ok é bool")
        assertEquals(Type.ERROR, at2.getValue(e2.id).type, "bool numa conta é erro (regra quatro)")
        val cmp = ExprParser.parse("c < y")
        assertEquals(Type.BOOL, TypeChecker.evaluate(cmp, ExprGenerator.env).getValue(cmp.id).type, "comparação dá bool")
    }

    /** O programa do ScopeGen tem a sombra, a busca que desce e o print depois do bloco, como a aula diz. */
    @Test fun escopoDoSegundoExemplo() {
        val ex = escopoExemplo2()
        val l = ex.prog.lines
        assertTrue(l.size <= 12, "o programa cabe no palco: ${l.size} linhas")
        assertEquals(2, l.maxOf { it.indent }, "um bloco dentro do outro")
        assertTrue(l.all { it.use == null || it.resolvesTo != null }, "nenhum uso sem declaração")
        // Pergunta 1: a tabela de dentro responde, e o nome também existe fora (o de dentro esconde o de fora).
        val dentro = l[ex.usoDentro]
        assertEquals(2, l[dentro.resolvesTo!!].indent)
        assertTrue(l.any { it.indent == 1 && it.declId != null && it.text.endsWith(" ${dentro.use};") }, "o nome também está fora")
        // Pergunta 2: no bloco de dentro, a busca desce até a tabela de fora.
        val fora = l[ex.usoFora]
        assertEquals(2, fora.indent)
        assertEquals(1, l[fora.resolvesTo!!].indent)
        assertFalse(l.any { it.indent == 2 && it.declId != null && it.text.endsWith(" ${fora.use};") }, "a tabela de dentro não tem esse nome")
        // Pergunta 3 e fim: a chave fecha o bloco de dentro; o print depois dela usa a tabela de fora.
        assertTrue(ex.fecha > ex.usoDentro && ex.fecha > ex.usoFora)
        assertTrue(ex.depois > ex.fecha)
        assertTrue(l[ex.depois].text.startsWith("print"), l[ex.depois].text)
        assertEquals(1, l[l[ex.depois].resolvesTo!!].indent)
        assertTrue(ex.tabela(2).isNotEmpty() && ex.tabela(1).isNotEmpty())
        assertEquals(ESCOPO_SEMENTE_2, 4881)
    }

    /** As quatro linhas de TAC, na ordem que a aula pede: soma, subtração, vezes, cópia para x. */
    @Test fun tacDoSegundoExemplo() {
        val tac = TacGen.generate(ExprParser.parse(TAC_EXEMPLO_2), assignTo = TAC_DESTINO_2).map { it.toString() }
        assertEquals(listOf("t1 = a + b", "t2 = c - d", "t3 = t1 * t2", "x = t3"), tac)
        // O primeiro exemplo: o vezes sai antes do mais, e a segunda linha usa o t1.
        assertEquals(listOf("t1 = b * c", "t2 = a + t1"), TacGen.generate(ExprParser.parse("a + b * c")).map { it.toString() })
    }

    /** Dois vezes três vira seis, dez menos seis vira quatro, e sobra uma linha só. */
    @Test fun dobraDoSegundoExemplo() {
        val e = ExprParser.parse(OTIM_EXEMPLO_2)
        assertEquals(3, TacGen.generate(e).size, "sem otimizar, três linhas")
        val menos = e.children[0]
        val vezes = menos.children[1]
        assertEquals("*", vezes.text)
        assertTrue(vezes.children.all { it.kind == Expr.Kind.NUM }, "o vezes tem dois números embaixo")
        assertEquals("6", TacGen.constantFold(vezes).text)
        assertEquals("4", TacGen.constantFold(menos).text)
        val dobrada = TacGen.constantFold(e)
        assertEquals(Expr.Kind.BINOP, dobrada.kind, "o x segura a conta da raiz")
        assertEquals(listOf("t1 = 4 * x"), TacGen.generate(dobrada).map { it.toString() })
    }

    /** O fat do motor para em n = 1; no segundo exemplo, f chama g, g chama h(2), e f devolve cinco. */
    @Test fun registrosDeAtivacao() {
        val fat = RuntimeSim.fatorial(2).events.filterIsInstance<RtEvent.Call>().map { it.frame }
        assertEquals(listOf("main", "fat", "fat"), fat.map { it.fn })
        assertEquals(listOf(2, 1), fat.drop(1).map { it.args.getValue("n") }, "o fat de um não chama ninguém")

        val prog = RuntimeSim.chain(RT_ARGUMENTO_2)
        val calls = prog.events.filterIsInstance<RtEvent.Call>()
        assertEquals(listOf("main", "f", "g", "h", "h"), calls.map { it.frame.fn })
        assertEquals(1, calls[1].frame.args.getValue("c"), "o main chamou f com c igual a um")
        assertEquals(2, calls[3].frame.args.getValue("a"), "h recebe b vezes dois")
        assertEquals(4, calls[3].stack.size, "quatro registros, contando o main")
        val depoisDoG = prog.events.filterIsInstance<RtEvent.Return>().first { it.fn == "g" }.stack
        assertEquals(listOf("main", "f"), depoisDoG.map { it.fn }, "h e g retornaram; f roda")
        assertEquals(1, calls[4].frame.args.getValue("a"), "o segundo h tem a igual a um")
        assertEquals(5, prog.result, "f devolve cinco")
    }

    /** Vivacidade, fios e cores do segundo exemplo de alocação. */
    @Test fun alocacaoDoSegundoExemplo() {
        val code = TacGen.generate(ExprParser.parse(REG_EXEMPLO_2))
        assertEquals(listOf("t1 = a * b", "t2 = c - d", "t3 = t2 * e", "t4 = t1 + t3"), code.map { it.toString() })
        val vivo = RegAlloc.liveness(code).liveOut
        assertEquals(setOf("t1", "t2"), vivo[1], "depois da linha do t2")
        assertEquals(setOf("t1", "t3"), vivo[2], "depois da linha do t3: o t2 já foi lido")
        val g = RegAlloc.interference(code)
        val fios = g.flatMap { (v, nbs) -> nbs.map { setOf(v, it) } }.toSet()
        assertEquals(setOf(setOf("t1", "t2"), setOf("t1", "t3")), fios, "os fios são só os dois que a aula liga")
        assertEquals(2, RegAlloc.chromatic(g), "dois registradores bastam")
        val alloc = RegAlloc.color(g, 2)
        assertTrue(alloc.spilled.isEmpty(), "sem spill")
        assertTrue(RegAlloc.isValidColoring(g, alloc.colors))
        assertEquals(4, alloc.colors.size, "quatro temporários em dois registradores")
    }
}
