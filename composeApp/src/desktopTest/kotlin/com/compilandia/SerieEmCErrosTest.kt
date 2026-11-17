package com.compilandia

import com.compilandia.content.Temporada
import com.compilandia.content.episodes.EMC2_ERRO_ENTRADA
import com.compilandia.content.episodes.EMC3_SEGUNDA
import com.compilandia.content.episodes.EMC5_SEGUNDA
import com.compilandia.content.episodes.Episodes
import com.compilandia.content.episodes.SPEC_CALC
import com.compilandia.content.episodes.SPEC_SEM_LEFT
import com.compilandia.content.episodes.emC3Opcoes
import com.compilandia.content.episodes.emC5Opcoes
import com.compilandia.engine.ErrosDaSerieC
import com.compilandia.engine.MiniBison
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Os erros comuns das Em C 2, 3 e 5: o que as falas dizem, o motor confere. */
class SerieEmCErrosTest {
    /** Em C 2: o expr que chama a si mesmo estoura a pilha sem ler um token. */
    @Test fun recursaoAEsquerdaEstoura() {
        val r = ErrosDaSerieC.exprComRecursaoAEsquerda(EMC2_ERRO_ENTRADA)
        assertTrue(r.estourou)
        assertEquals(0, r.lidos)
        assertEquals(ErrosDaSerieC.LIMITE + 1, r.chamadas)
    }

    /** Em C 3: com o e =, 1 + 2 + 3 junta pela esquerda; sem ele, sobra a folha 1. */
    @Test fun semGuardarONo() {
        assertEquals("(1 + 2) + 3", ErrosDaSerieC.exprMontaArvore(EMC3_SEGUNDA).texto())
        assertEquals("1", ErrosDaSerieC.exprMontaArvore(EMC3_SEGUNDA, guardaNo = false).texto())
        assertEquals(listOf("1", "(1 + 2) + 3", "1 + (2 + 3)"), emC3Opcoes())
        assertEquals("2 + 3", ErrosDaSerieC.exprMontaArvore(listOf("2", "+", "3")).texto())
    }

    /** Em C 5: sem o %left, 4 conflitos de empilha-reduz, o Bison empilha e 2*3+4 dá 14; com ele, 10. */
    @Test fun semOLeft() {
        val sem = MiniBison.compile(SPEC_SEM_LEFT.joinToString("\n"))
        assertEquals(4, sem.conflicts.size)
        assertTrue(sem.conflicts.all { it.kind == "shift/reduce" })
        assertEquals(14L, sem.run(EMC5_SEGUNDA).value, "três mais quatro, vezes dois")
        assertEquals(10L, MiniBison.compile(SPEC_CALC.joinToString("\n")).run(EMC5_SEGUNDA).value)
        assertEquals(listOf("= 10", "= 14"), emC5Opcoes().take(2))
        assertEquals(2, SPEC_CALC.size - SPEC_SEM_LEFT.size, "só as duas linhas do %left saem")
    }

    /** Toda aula da série segue no teto de 10 passos. */
    @Test fun serieNoTeto() {
        for (e in Episodes.all.filter { it.temporada == Temporada.EM_C }) {
            assertTrue(e.passos.size <= 10, "${e.title}: ${e.passos.size} passos")
        }
    }
}
