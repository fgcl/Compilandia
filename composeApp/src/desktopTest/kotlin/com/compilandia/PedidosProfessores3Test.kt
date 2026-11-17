package com.compilandia

import com.compilandia.content.Guiado
import com.compilandia.content.Prova
import com.compilandia.engine.ExprParser
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import com.compilandia.tutor.ProvaForja
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Terceira leva dos pedidos dos professores 5 e 6: Questão de prova na Forja, Fazer com ajuda da Alocação. */
class PedidosProfessores3Test {
    private fun tac(src: String) = TacGen.generate(ExprParser.parse(src), ProvaForja.DESTINO)

    /** As quádruplas e as triplas batem com a tabela da aula de texto de TAC. */
    @Test fun quadruplasETriplasDaAula() {
        val c = tac("( a + b ) * ( a - c )")
        assertEquals(listOf(listOf("+", "a", "b", "t1"), listOf("-", "a", "c", "t2"), listOf("*", "t1", "t2", "t3"), listOf("=", "t3", "", "x")), ProvaForja.quadruplas(c))
        assertEquals(listOf(listOf("+", "a", "b"), listOf("-", "a", "c"), listOf("*", "(0)", "(1)"), listOf("=", "x", "(2)")), ProvaForja.triplas(c))
        assertEquals(listOf("+", "(0)", "(1)"), ProvaForja.triplas(tac("a * b + a * b"))[2], "as duas contas iguais viram duas linhas")
    }

    /** in = use ∪ (out − def), out é o in da linha de baixo, e a terceira questão precisa de três cores. */
    @Test fun vivacidadeECores() {
        for (src in ProvaForja.ALOCACAO) {
            val v = ProvaForja.tabelaViva(tac(src))
            v.forEachIndexed { i, l ->
                assertEquals(l.use + (l.out - l.def), l.inn, "$src linha $i")
                assertEquals(v.getOrNull(i + 1)?.inn ?: emptySet(), l.out, "$src linha $i")
            }
        }
        assertEquals(listOf(1, 2, 3), ProvaForja.ALOCACAO.map { RegAlloc.chromatic(RegAlloc.interference(tac(it))) })
    }

    @Test fun provasEFazerComAjudaDaForja() {
        for (s in listOf(Skill.TAC, Skill.REG_ALLOC)) {
            assertTrue(Prova.disponivel(s))
            val ps = Prova.problemas(s, 7)
            assertEquals(Prova.PROBLEMAS, ps.size)
            for (p in ps) for (st in p.passos) st.ask?.let { a ->
                assertTrue(a.opcoes.size >= 2 && a.opcoes.distinct().size == a.opcoes.size, "$s: ${a.pergunta} ${a.opcoes}")
                assertTrue(a.certa in a.opcoes.indices)
            }
        }
        assertTrue(Guiado.disponivel(Skill.REG_ALLOC), "a Alocação ganha o Fazer com ajuda")
    }

    /** A aula diz "quatro líderes" e que só o t2 é morto: o motor confere, e cada bloco termina antes do próximo líder. */
    @Test fun lideresECodigoMorto() {
        val laco = com.compilandia.engine.MiniLang.tac(com.compilandia.content.episodes.OTIM_LACO)
        val l = com.compilandia.engine.BlocosBasicos.lideres(laco)
        assertEquals(4, l.size, laco.toString())
        assertTrue(laco[l.elementAt(1)].endsWith(":") && laco[l.elementAt(2) - 1].startsWith("ifFalse"))
        assertEquals(laco.size, com.compilandia.engine.BlocosBasicos.blocos(laco).sumOf { it.count() })
        assertEquals(setOf(1), com.compilandia.engine.BlocosBasicos.mortas(com.compilandia.content.episodes.OTIM_MORTO, setOf("x")))
    }

    /** Cap. 11 do guia: os dois .asm rodam, imprimem o que o texto diz, e o fat devolve a pilha como achou. */
    @Test fun vetoresEFuncoesRodam() {
        val v = com.compilandia.engine.MipsVM(com.compilandia.content.TP_VETOR_ASM).run()
        assertEquals("soma = 30\n", v.output.toString())
        val f = com.compilandia.engine.MipsVM(com.compilandia.content.TP_FAT_ASM).run()
        assertEquals("fat(5) = 120\n", f.output.toString())
        assertEquals(com.compilandia.engine.MipsVM.STACK_TOP, f.regs["\$sp"])
        assertEquals(11, com.compilandia.content.Library.guide.last().number)
    }
}
