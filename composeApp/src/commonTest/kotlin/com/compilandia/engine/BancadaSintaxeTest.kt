package com.compilandia.engine

import com.compilandia.content.Etapa
import com.compilandia.content.Robo
import com.compilandia.content.Trilha
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** J4, J5 e J6: toda fase tem resposta, e o erro aparece com a frase exata. */
class BancadaSintaxeTest {
    // ------------------------------------------------------------ Frases

    @Test fun frasesContaDerivacoes() {
        val g = Grammar.parse("E -> E + E | id")
        val out = Frases.geradas(g.productions, "E", setOf("E"), 5)
        assertEquals(1, out[listOf("id")])
        assertEquals(1, out[listOf("id", "+", "id")])
        assertEquals(2, out[listOf("id", "+", "id", "+", "id")])
        val eps = Grammar.parse("S -> ( S ) S | ε")
        val bal = Frases.geradas(eps.productions, "S", setOf("S"), 6)
        assertTrue(listOf("(", ")", "(", ")") in bal)
        assertEquals(5, bal.keys.count { it.size == 6 }, "Catalan(3)")
        // Um ciclo S ⇒ S S ⇒ S faz toda frase ter infinitas árvores.
        val ciclo = Grammar.parse("S -> S S | ( S ) | ε")
        assertEquals(2, Frases.geradas(ciclo.productions, "S", setOf("S"), 4)[listOf("(", ")")])
    }

    // ------------------------------------------------------------ J4

    @Test fun todaFasePilhaTemTabelaDaProvaQuePassa() {
        for (f in RoboPilha.fases) {
            assertTrue(f.certa.isLL1, "${f.id}: gramática não é LL(1)")
            val v = RoboPilha.confere(f, f.solucao)
            assertTrue(v.passou, "${f.id}: a tabela certa não passa: ${v.frase}")
            for (t in f.testes) assertTrue(tokensOf(t).size <= RoboPilha.BUSCA)
            assertTrue(f.testes.any { !f.deveAceitar(tokensOf(it)) }, "${f.id}: falta frase errada")
        }
    }

    @Test fun celulaVaziaFazOByteParar() {
        val f = RoboPilha.fases.first { it.id == "pilha_lista" }
        val semEps = f.solucao.filterValues { !f.g.productions[it].isEpsilon }
        val v = RoboPilha.confere(f, semEps)
        assertFalse(v.passou)
        assertEquals("id", v.testes[v.falha!!].entrada)
        assertEquals("Com id, o Byte parou. Não sei o que fazer com R e $.", v.frase)
        assertEquals("Olhe M[R, $]. A frase id passa por ali.", RoboPilha.ondeOlhar(f, semEps))
    }

    @Test fun regraSobrandoNaTabelaAparece() {
        val f = RoboPilha.fases.first { it.id == "pilha_parenteses" }
        val t = f.solucao + (("A" to ")") to f.solucao.getValue("A" to "x"))
        val v = RoboPilha.confere(f, t)
        assertFalse(v.passou)
        assertTrue(v.testes.dropLast(1).all { it.passou })
        assertEquals("M[A, )]", v.testes.last().entrada)
    }

    @Test fun regraTrocadaAchaFraseForaDaLista() {
        val f = RoboPilha.fases.first { it.id == "pilha_expressao" }
        // E' → ε no lugar de E' → + T E' na coluna +.
        val eps = f.g.productions.first { it.lhs == "E'" && it.isEpsilon }.id
        val v = RoboPilha.confere(f, f.solucao + (("E'" to "+") to eps))
        assertFalse(v.passou)
        assertEquals("id + id", v.testes[v.falha!!].entrada)
    }

    // ------------------------------------------------------------ J5

    @Test fun todaFaseGuindasteTemProgramaQuePassa() {
        for (f in Guindaste.fases) {
            val v = Guindaste.confere(f, f.solucao)
            assertTrue(v.passou, "${f.id}: a resposta não passa: ${v.frase}")
            assertTrue(f.solucao.size <= Guindaste.REGRAS_MAX)
            assertTrue(f.solucao.all { it.olha == null } || f.comOlhar)
        }
    }

    @Test fun ordemErradaReduzCedoDemais() {
        val f = Guindaste.fases.first { it.id == "guindaste_lista" }
        val v = Guindaste.confere(f, f.solucao.reversed())
        assertFalse(v.passou)
        assertEquals("id , id", v.testes[v.falha!!].entrada)
        assertEquals("Com id , id, a pilha travou em L , L. A última troca foi L → id.", v.frase)
        assertEquals("Na linha 1 do programa, ponha a regra L → L , id.", Guindaste.ondeOlhar(f, f.solucao.reversed()))
    }

    @Test fun regraForaDaGramaticaDeixaFraseErradaEntrar() {
        val f = Guindaste.fases.first { it.id == "guindaste_parenteses" }
        val v = Guindaste.confere(f, listOf(Guindaste.Regra(1), Guindaste.Regra(2), Guindaste.Regra(0)))
        assertFalse(v.passou)
        val t = v.testes[v.falha!!]
        assertEquals("( x )", t.entrada)
        val v2 = Guindaste.confere(f, listOf(Guindaste.Regra(0), Guindaste.Regra(1), Guindaste.Regra(2)))
        assertFalse(v2.testes.first { it.entrada == "( x" }.passou, "S → ( S deixa ( x entrar")
    }

    @Test fun semEsperarOGuindasteTrocaAntesDoVezes() {
        val f = Guindaste.fases.first { it.id == "guindaste_vezes" }
        val semOlhar = f.solucao.map { it.copy(olha = null) }
        val v = Guindaste.confere(f, semOlhar)
        assertFalse(v.passou)
        assertEquals("id * id", v.testes[v.falha!!].entrada)
        assertTrue(Guindaste.ondeOlhar(f, semOlhar)!!.contains("+ $"))
    }

    // ------------------------------------------------------------ J6

    @Test fun todaFaseFabricaTemRespostaQuePassa() {
        for (f in Fabrica.fases) {
            val v = Fabrica.confere(f, f.solucao)
            assertTrue(v.passou, "${f.id}: a resposta não passa: ${v.frase}")
            for (t in f.testes) assertTrue(tokensOf(t).size <= f.maxLen)
            assertTrue(f.testes.any { f.alvo(tokensOf(it)) } && f.testes.any { !f.alvo(tokensOf(it)) })
        }
    }

    @Test fun somaAmbiguaMostraAsDuasArvores() {
        val f = Fabrica.fases.first { it.id == "fabrica_soma" }
        val v = Fabrica.confere(f, setOf(0, 1))
        assertFalse(v.passou)
        assertEquals("id + id + id", v.testes[v.falha!!].entrada)
        val m = Fabrica.monta(f, setOf(0, 1), tokensOf("id + id + id"))
        assertEquals(2, m.arvores.size)
        assertTrue(m.arvores[0] != m.arvores[1])
        assertEquals("Desligue a máquina E → E + E.", Fabrica.ondeOlhar(f, setOf(0, 1)))
    }

    @Test fun maquinaErradaFazSairFraseProibida() {
        val f = Fabrica.fases.first { it.id == "fabrica_anbn" }
        val v = Fabrica.confere(f, setOf(1, 2))
        assertFalse(v.passou)
        val t = v.testes[v.falha!!]
        assertEquals("a a b b", t.entrada)
        assertEquals("não sai", t.obtido)
        // S → a S: sai "a", que não podia.
        val v2 = Fabrica.confere(f, setOf(0, 1, 4))
        assertEquals("a", v2.testes[v2.falha!!].entrada)
        assertNotNull(Fabrica.confere(f, emptySet()).falha)
    }

    // ------------------------------------------------------------ trilha

    @Test fun robosNovosEntramNaTrilha() {
        for (r in listOf(Robo.PILHA, Robo.GUINDASTE, Robo.FABRICA)) {
            assertEquals(r, Robo.de(r.skill))
            assertTrue(Trilha.passos(r.skill).any { it.etapa == Etapa.PROGRAMAR }, "${r.name} fora da trilha")
            assertEquals(r.fases, (0 until r.fases).map { r.idFase(it) }.toSet().size)
        }
        assertEquals(Skill.LL1_TABLE, Robo.PILHA.skill)
    }
}
