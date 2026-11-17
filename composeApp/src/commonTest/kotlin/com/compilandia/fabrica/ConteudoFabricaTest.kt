package com.compilandia.fabrica

import com.compilandia.engine.Grammar
import com.compilandia.tutor.Diagnostico
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.carrega
import com.compilandia.tutor.corrida
import com.compilandia.tutor.planta
import com.compilandia.tutor.salva
import com.compilandia.ui.fabgram.Desenhos
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * As regras do conteúdo da fábrica (docs/FABRICA.md). Quem cria um bloco novo e esquece uma parte cai aqui:
 * id estável, desenho, nome e descrição nos dois idiomas, árvore da loja sem ciclo e núcleo completo.
 */
class ConteudoFabricaTest {
    private val textos = listOf("pt", "en").associateWith { lingua ->
        listOf("src/commonMain/textos", "composeApp/src/commonMain/textos").map { File(it, "$lingua/fabgram.xml") }
            .first { it.exists() }.readText()
    }

    @Test fun idsSaoUnicosESimples() {
        val ids = Blocos.todos.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "id repetido em Blocos.todos")
        for (id in ids) assertTrue(Regex("[a-z]+").matches(id), "id '$id': só letras minúsculas, vai para o save")
    }

    @Test fun todoBlocoTemDesenho() {
        for (t in Blocos.todos) assertTrue(t.id in Desenhos.porTipo, "${t.id} sem desenho em Desenhos.porTipo")
        for (id in Desenhos.porTipo.keys) assertNotNull(Blocos.porId(id), "desenho de '$id', que não está em Blocos")
    }

    @Test fun todoBlocoTemNomeEDescricaoNosDoisIdiomas() {
        for ((lingua, xml) in textos) for (t in Blocos.todos) {
            for (chave in listOf("fabgram_bloco_${t.id}", "fabgram_bloco_${t.id}_desc")) {
                assertTrue("name=\"$chave\"" in xml, "falta $chave em textos/$lingua/fabgram.xml")
            }
        }
        for ((lingua, xml) in textos) for (c in Categoria.entries) {
            assertTrue("name=\"fabgram_cat_${c.name.lowercase()}\"" in xml, "falta a aba ${c.name} em $lingua")
        }
    }

    @Test fun arvoreDaLojaNaoTemCicloEComecaNoQueJaVem() {
        for (t in Blocos.todos) {
            val vistos = mutableSetOf<BlocoTipo>()
            var r: BlocoTipo? = t
            while (r != null) { assertTrue(vistos.add(r), "ciclo na loja a partir de ${t.id}"); r = r.requer }
            t.requer?.let { assertTrue(it in Blocos.todos, "${t.id} requer ${it.id}, que não está registrado") }
        }
        assertTrue(Blocos.todos.filter { it.nucleo }.all { it.preco == 0 }, "o núcleo não se compra")
    }

    @Test fun nucleoTemUmBlocoPorPapel() {
        val nucleo = Blocos.todos.filter { it.nucleo }
        assertEquals(nucleo.size, nucleo.map { it.papel }.toSet().size)
        assertEquals(setOf(Papel.FONTE, Papel.MONTADORA, Papel.CESTO), nucleo.map { it.papel }.toSet())
    }

    @Test fun maquinasVemDaMenorParaAMaior() {
        assertTrue(Blocos.maquinas.all { it.maquina && it.capacidade > 0 })
        val gerais = Blocos.maquinas.filter { !it.soLetra }
        // a planta automática pega a primeira em que a regra cabe
        assertEquals(gerais.sortedBy { it.capacidade }, gerais, "Blocos.maquinas fora de ordem")
    }

    @Test fun lojaCobraESegueAArvore() {
        assertEquals(Loja.Situacao.TEM, Loja.situacao(Blocos.PRENSA, emptyList(), 0))
        assertEquals(Loja.Situacao.CARO, Loja.situacao(Blocos.BRACO, emptyList(), 5))
        assertEquals(Loja.Situacao.COMPRA, Loja.situacao(Blocos.BRACO, emptyList(), 50))
        assertEquals(Loja.Situacao.TEM, Loja.situacao(Blocos.BRACO, listOf("braco"), 0))
    }
}

/** A fábrica é a gramática: montar à mão, configurar, atravessar a junção e guardar. */
class FabricaMontadaTest {
    private fun g(t: String) = Grammar.parse(Diagnostico.normaliza(t))

    @Test fun aGramaticaSaiDasMaquinas() {
        val pl = FabGram.planta(g("S -> a S b | ε"))
        assertEquals(2, pl.g.productions.size)
        val prensa = pl.blocos.first { it.def == Blocos.PRENSA }
        val semRegra = pl.configura(prensa.id, null, null)
        assertEquals(listOf("S → ε"), semRegra.g.productions.map { it.toString() })
    }

    @Test fun trocarAMaquinaPorUmaNovaEConfigurarFazAFraseDeNovo() {
        val pl = FabGram.planta(g("S -> a b"))
        val velha = pl.blocos.first { it.def == Blocos.PRENSA }
        val vazia = assertNotNull(pl.desmonta(velha.id))
        assertFalse(FabGram.corrida(vazia, listOf("a", "b")).saiu)
        val nova = assertNotNull(vazia.constroi(Blocos.PRENSA, Cel(velha.x, velha.y)))
        val id = nova.blocos.last().id
        val pronta = nova.configura(id, "S", listOf("a", "b"))
        assertTrue(FabGram.corrida(pronta, listOf("a", "b")).saiu)
    }

    @Test fun oNucleoNaoSaiENaoSeEmpilha() {
        val pl = FabGram.planta(g("S -> a b"))
        assertNull(pl.desmonta(pl.fonte.id))
        assertNull(pl.constroi(Blocos.PRENSA, Cel(pl.fonte.x, pl.fonte.y)))
    }

    @Test fun letraSemSeparadorTravaComMotivo() {
        val pl = FabGram.planta(g("S -> a b"))
        val sep = assertNotNull(pl.separadorDe("S"))
        val c = FabGram.corrida(assertNotNull(pl.desmonta(sep.id)), listOf("a", "b"))
        assertEquals(com.compilandia.tutor.Motivo.SEM_SEPARADOR, c.quebra?.motivo)
    }

    @Test fun aJuncaoDeixaOItemAtravessar() {
        val pl = FabGram.planta(g("S -> a b"))
        // uma célula de esteira reta, com esteira antes e depois na mesma direção, vira junção
        val reta = pl.esteiras.entries.first { (c, d) -> pl.esteiras[c - d] == d && pl.esteiras[c + d] == d }.key
        val comJuncao = assertNotNull(pl.semEsteira(reta).constroi(Blocos.JUNCAO, reta))
        assertTrue(FabGram.corrida(comJuncao, listOf("a", "b")).saiu, "a junção devia deixar passar")
    }

    @Test fun salvaComRegrasESimbolosEstranhos() {
        val pl = FabGram.planta(g("L -> id , L | id ;\nX -> % | >"))
        val volta = assertNotNull(FabGram.carrega(FabGram.salva(pl)))
        assertEquals(pl.g.toText(), volta.g.toText())
    }
}
