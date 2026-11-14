package com.compilandia

import com.compilandia.content.TP_ASM_CONTAGEM
import com.compilandia.content.TP_TAC_COLUNA
import com.compilandia.content.TP_TAC_SOMA
import com.compilandia.content.episodes.EMC2_PARENTESES
import com.compilandia.content.episodes.segundoExpr
import com.compilandia.content.episodes.valoresSdd
import com.compilandia.content.rastroDescida
import com.compilandia.content.tpCap8TacEnquanto
import com.compilandia.content.tpCap8TacExpr
import com.compilandia.engine.ExprParser
import com.compilandia.engine.TacGen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Confere contra o motor o que entrou pelos pedidos dos professores dos blocos 5 e 6 (TAREFAS, seção 7). */
class PedidosProfessoresTest {
    @Test fun tacDoGuiaEhODoMotor() {
        val tac = TacGen.generate(ExprParser.parse("soma + n * 2"), "soma").map { it.toString() }
        assertEquals(TP_TAC_SOMA, tac)
        assertEquals("t1 = n > 0", TacGen.generate(ExprParser.parse("n > 0")).single().toString())
        assertTrue("t1 = n > 0" in tpCap8TacEnquanto.code)
    }

    /** As seis instruções ao lado do TAC aparecem juntas e nessa ordem no saida.asm do capítulo. */
    @Test fun mipsDoLadoDoTacEhODoSaidaAsm() {
        val mips = tpCap8TacExpr.code.lines().drop(2).map { it.drop(TP_TAC_COLUNA).trim().replace(Regex("\\s+"), " ") }
        val asm = TP_ASM_CONTAGEM.lines().map { it.trim().replace(Regex("\\s+"), " ") }
        val i = asm.indexOf(mips.first())
        assertTrue(i >= 0, "não achei ${mips.first()}")
        assertEquals(mips, asm.subList(i, i + mips.size))
    }

    @Test fun descidaComParentesesChamaExprDuasVezes() {
        val r = rastroDescida(EMC2_PARENTESES)
        assertEquals(EMC2_PARENTESES.size, r.last().lidos, "a descida lê a conta inteira")
        assertEquals(listOf("main", "expr", "termo", "fator", "expr"), r[segundoExpr(r)].pilha)
        assertEquals(7, r.maxOf { it.pilha.size }, "no fundo: main expr termo fator expr termo fator")
    }

    @Test fun sddDeValorSomaOsFilhos() {
        assertEquals(Triple(2, 3, 5), valoresSdd())
    }
}
