package com.compilandia.engine

import com.compilandia.content.Etapa
import com.compilandia.content.Robo
import com.compilandia.content.Robos
import com.compilandia.content.Trilha
import com.compilandia.learning.Learner
import com.compilandia.learning.LearnerState
import com.compilandia.learning.Skill
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** J1, J2 e J3: as fases têm resposta, e o erro aparece com a entrada exata. */
class BancadaTest {
    // ------------------------------------------------------------ Robô MIPS

    @Test fun todaFaseMipsTemRespostaFeitaDeCartas() {
        for (f in RoboMips.fases) {
            assertTrue(f.solucao.all { it in f.cartas }, "${f.id}: a resposta usa carta que não está na mesa")
            val v = RoboMips.confere(f, f.solucao)
            assertTrue(v.passou, "${f.id}: a resposta não passa: ${v.frase}")
            assertTrue(v.testes.size >= 3, "${f.id}: poucos testes")
            assertTrue(f.solucao.size <= RoboMips.LINHAS_MAX)
        }
    }

    @Test fun somaErradaMostraOContraexemplo() {
        val f = RoboMips.fases.first { it.id == "soma" }
        // Soma a com ele mesmo: passa por acaso só quando a = b.
        val v = RoboMips.confere(f, listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t0", "sw \$t2, x"))
        assertFalse(v.passou)
        val t = v.testes[v.falha!!]
        assertEquals("a = 2, b = 3", t.entrada)
        assertEquals("x = 4", t.obtido)
        assertEquals("x = 5", t.esperado)
        assertTrue("a = 2, b = 3" in v.frase && "x = 4" in v.frase && "x = 5" in v.frase, v.frase)
    }

    @Test fun trocaNaOrdemErradaPerdeOValor() {
        val f = RoboMips.fases.first { it.id == "troca" }
        val v = RoboMips.confere(f, listOf("lw \$t0, a", "sw \$t0, b", "lw \$t1, b", "sw \$t1, a"))
        assertFalse(v.passou)
        assertEquals("a = 2, b = 2", v.testes[v.falha!!].obtido)
    }

    @Test fun seComBeqPulaNaHoraErrada() {
        val f = RoboMips.fases.first { it.id == "se" }
        val v = RoboMips.confere(f, listOf("lw \$t0, a", "lw \$t1, b", "beq \$t0, \$t1, fim", "li \$t2, 1", "sw \$t2, x", "fim:"))
        assertFalse(v.passou)
        assertEquals("x = 0", v.testes[v.falha!!].obtido)
    }

    @Test fun lacoQueSobeNaoTermina() {
        val f = RoboMips.fases.first { it.id == "laco" }
        val prog = listOf("lw \$t0, n", "laco:", "beq \$t0, \$zero, fim", "add \$t2, \$t2, \$t0", "addi \$t0, \$t0, 1", "j laco", "fim:", "sw \$t2, x")
        val v = RoboMips.confere(f, prog)
        assertFalse(v.passou)
        val ex = RoboMips.roda(f, prog, f.testes[v.falha!!])
        assertNotNull(ex.erro)
        // Um passo do laço gera até quatro quadros; o palco para logo depois do limite.
        assertTrue(ex.quadros.size <= RoboMips.QUADROS_MAX + 5)
    }

    @Test fun quadrosDoLwLevamOCaixote() {
        val f = RoboMips.fases.first()
        val ex = RoboMips.roda(f, listOf("lw \$t0, a"), mapOf("a" to 7L, "b" to 1L))
        val vai = ex.quadros[1]
        assertEquals(RoboMips.Lugar.MEMORIA, vai.lugar)
        assertEquals(7L, vai.carrega)
        val volta = ex.quadros[2]
        assertEquals(RoboMips.Lugar.REGISTRADOR, volta.lugar)
        assertEquals(7L, volta.regs["\$t0"])
        assertEquals(0, vai.linha)
    }

    @Test fun programaVazioNaoRoda() {
        val v = RoboMips.confere(RoboMips.fases.first(), emptyList())
        assertEquals(-1, v.falha)
        assertFalse(v.passou)
    }

    @Test fun terceiraDicaApontaUmaLinhaSo() {
        val f = RoboMips.fases.first { it.id == "soma" }
        assertEquals(1 to "lw \$t1, b", RoboMips.proximaLinha(f, listOf("lw \$t0, a", "sw \$t2, x")))
        assertNull(RoboMips.proximaLinha(f, f.solucao))
    }

    @Test fun programaMaisLongoPassaMasAvisa() {
        val f = RoboMips.fases.first { it.id == "soma" }
        val v = RoboMips.confere(f, listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t1", "add \$t2, \$t0, \$t1", "sw \$t2, x"))
        assertTrue(v.passou)
        assertTrue("${f.minimo}" in v.frase)
    }

    // ------------------------------------------------------------ Trilho

    private fun resposta(f: Trilho.Fase) = Trilho.montado(f, f.solucao, f.finaisSolucao)

    @Test fun todaFaseDoTrilhoTemResposta() {
        for (f in Trilho.fases) {
            assertNull(DfaCheck.diferenca(resposta(f), f.alvo), "${f.id}: a resposta não aceita a mesma linguagem")
            assertTrue(f.estados >= DfaCheck.pecasMinimas(f.alvo), "${f.id}: estados de menos para a linguagem")
            val v = Trilho.confere(f, resposta(f))
            assertTrue(v.passou, "${f.id}: ${v.frase}")
            assertTrue(f.testes.any { Trilho.deveEntrar(f, it) } && f.testes.any { !Trilho.deveEntrar(f, it) }, "${f.id}: testes sem dos dois lados")
        }
    }

    @Test fun tabelaVaziaMostraAMenorPalavra() {
        val f = Trilho.fases.first { it.id == "termina1" }
        val v = Trilho.confere(f, Trilho.montado(f, emptyMap(), emptySet()))
        assertFalse(v.passou)
        // A menor palavra que devia entrar é "1"; sem trilho, o trem para em q0.
        assertEquals("1", v.testes[v.falha!!].entrada)
        assertTrue("q0" in v.frase, v.frase)
    }

    @Test fun contraexemploForaDaListaEntraNaBancada() {
        val f = Trilho.fases.first { it.id == "termina1" }
        // Depois de um 1, o próximo 1 volta para q0: erra em "11", que não está na lista.
        val d = Trilho.montado(f, mapOf((0 to '0') to 0, (0 to '1') to 1, (1 to '0') to 0, (1 to '1') to 0), setOf(1))
        val v = Trilho.confere(f, d)
        assertFalse(v.passou)
        val t = v.testes[v.falha!!]
        assertEquals("11", t.entrada)
        assertTrue(t.achado)
    }

    @Test fun identificadorComecandoComDigitoDescarrila() {
        val f = Trilho.fases.first { it.id == "ident" }
        val c = Trilho.corre(f, resposta(f), "1x")
        assertTrue(c.semTrilho)
        assertEquals(0, c.letraQuebra)
        assertTrue(Trilho.corre(f, resposta(f), "soma2").aceitou)
    }

    @Test fun terceiraDicaDoTrilhoApontaACelula() {
        val f = Trilho.fases.first { it.id == "termina1" }
        val dica = Trilho.ondeOlhar(f, Trilho.montado(f, emptyMap(), emptySet()))
        assertNotNull(dica)
        assertTrue("q0" in dica, dica)
        assertNull(Trilho.ondeOlhar(f, resposta(f)))
    }

    // ------------------------------------------------------------ na trilha

    @Test fun programarEntraSoNosAssuntosComRobo() {
        for (s in Skill.entries) {
            val tem = Trilha.passos(s).any { it.etapa == Etapa.PROGRAMAR }
            assertEquals(Robo.de(s) != null, tem, "$s")
        }
    }

    @Test fun todasAsFasesFeitasFechamAEtapa() {
        val l = Learner(LearnerState(), Random(1))
        val r = Robo.TRILHO
        val p = Trilha.passos(r.skill).first { it.etapa == Etapa.PROGRAMAR }
        for (i in 0 until r.fases - 1) Robos.marca(l, r, i, 0)
        assertEquals(r.fases - 1, Robos.primeiraAberta(l, r))
        assertEquals(com.compilandia.content.EstadoEtapa.PENDENTE, Trilha.estado(l, r.skill, p))
        Robos.marca(l, r, r.fases - 1, 0)
        assertEquals(com.compilandia.content.EstadoEtapa.FEITA, Trilha.estado(l, r.skill, p))
    }
}
