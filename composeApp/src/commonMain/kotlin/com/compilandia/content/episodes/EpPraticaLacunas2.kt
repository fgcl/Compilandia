package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Episode
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PRATICA
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.registro
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.AcessoVetor
import com.compilandia.engine.HerdadoNaDescida
import com.compilandia.engine.HerdadoNaDescida.Lugar
import com.compilandia.engine.TabelaComFuncoes
import com.compilandia.engine.TabelaComFuncoes.Categoria
import com.compilandia.engine.TabelaComFuncoes.Funcao
import com.compilandia.engine.TiposComandos
import com.compilandia.engine.Type
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * As lacunas que sobravam do revisor professor do bloco 5: o herdado como parâmetro da descida, com a ação no meio
 * da regra; a tabela de símbolos com funções; e o acesso a vetor no TAC e no MIPS.
 * Quem responde é o motor: HerdadoNaDescida, TabelaComFuncoes e AcessoVetor. PraticaLacunasTest confere.
 */

/** A descida da declaração: o T devolve o tipo, e o L e o R recebem o herdado h como parâmetro. */
val HERDADO_CODIGO = listOf(
    "void D() {",
    "  Tipo t = T();",
    "  L(t);",
    "  casa(';');",
    "}",
    "void L(Tipo h) {",
    "  addtipo(id(), h);",
    "  R(h);",
    "}",
    "void R(Tipo h) {",
    "  if (tok.tipo == ',') {",
    "    casa(','); addtipo(id(), h);",
    "    R(h);",
    "  }",
    "}",
)
const val HERDADO_DECL = "float a, b;"

/** A linha do código em que cada função chama o addtipo. */
fun linhaDoAddtipo(funcao: String): Int = HERDADO_CODIGO.indices.first { i ->
    val dona = HERDADO_CODIGO.take(i).lastOrNull { it.startsWith("void ") }.orEmpty()
    "addtipo" in HERDADO_CODIGO[i] && dona.startsWith("void $funcao(")
}

/** As três regras do D com a ação em cada lugar, na ordem da tela. A certa não fica no meio. */
val HERDADO_LUGARES = listOf(
    Lugar.ANTES_DO_T to "D → { L.h = T.tipo } T L ;",
    Lugar.DEPOIS_DO_L to "D → T L { L.h = T.tipo } ;",
    Lugar.ENTRE_T_E_L to "D → T { L.h = T.tipo } L ;",
)

/** O programa da aula de funções: a soma e o main com três chamadas. */
val FUNCOES_CODIGO = listOf(
    "int soma(int a, int b) {",
    "  int s = a + b;",
    "  return s;",
    "}",
    "void main() {",
    "  int x = soma(1, 2);",
    "  int y = soma(x);",
    "  int z = soma(x, y);",
    "}",
)
val FUNCOES = listOf(
    Funcao("soma", Type.INT, listOf("a" to Type.INT, "b" to Type.INT), listOf("s" to Type.INT)),
    Funcao("main", null, emptyList(), listOf("x" to Type.INT, "y" to Type.INT, "z" to Type.INT)),
)

/** As chamadas do main, como o motor as confere: os argumentos de cada soma(...). */
fun argumentosDasChamadas(): List<List<String>> =
    FUNCOES_CODIGO.filter { "= soma(" in it }.map { it.substringAfter("soma(").substringBefore(")").split(", ") }

/** O vetor da aula de vetores, o índice e o endereço onde ele começa. */
val VETOR = listOf(10L, 20L, 30L, 40L)
const val VETOR_I = 2L
const val VETOR_BASE = 1000L

private fun tecla(id: String, t: String, y: Float) = Box(id, t, 180f, y, 300f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f)

private fun nomeTipo(t: Type?) = t?.label ?: "void"

/** As linhas de uma tabela, tiradas do motor. */
private fun linhasDaTabela(e: TabelaComFuncoes.Escopo): List<String> = e.entradas.map { en ->
    when (en.categoria) {
        Categoria.FUNCAO -> "${en.nome}: ${nomeTipo(en.tipo)} (${en.parametros.joinToString(", ") { it.label }})"
        Categoria.PARAMETRO -> tx("fts_linha_param", en.nome, nomeTipo(en.tipo))
        Categoria.VARIAVEL -> "${en.nome}: ${nomeTipo(en.tipo)}"
    }
}

// ─────────────────────────────── O HERDADO NA DESCIDA ───────────────────────────────
private fun herdadoNaDescida(): Episode = episodio(Skill.ATTRIBUTES_TYPES, tx("hd_titulo"), PRATICA) {
    val r = HerdadoNaDescida.roda(HERDADO_DECL)
    val doB = linhaDoAddtipo(r.rastro.single { it.id == "b" }.funcao)
    val outras = setOf(linhaDoAddtipo("L"), HERDADO_CODIGO.indexOf("  L(t);"), HERDADO_CODIGO.indexOf("  R(h);"))
    cena(tx("hd_01"), mood = EpMood.THINK) {
        add(titulo(tx("hd_titulo"), size = 22f))
        add(codigo("h", HERDADO_CODIGO, 58f, x = 40f, passo = 22f, size = 13f))
    }
    cena(tx("hd_02"), who = PROF) { hot("h1", "h5") }
    pergunta(tx("hd_03"), setOf("h$doB"), outras.map { "h$it" }.toSet(), tx("hd_03_dica"), tx("hd_03_ok")) { hot() }
    cena(tx("hd_04")) {
        dropAll("h")
        add(codigo("r", r.rastro.map(::passoNaTela), 70f, x = 70f, passo = 30f, size = 16f))
    }
    cena(tx("hd_05"), who = PROF) {
        dropAll("r")
        add(codigo("g", listOf("D → T L ;", "L → id R", "R → , id R  |  ε"), 70f, x = 90f, passo = 30f, size = 16f))
    }
    val certa = HERDADO_LUGARES.indexOfFirst { (l, _) ->
        HerdadoNaDescida.roda(HERDADO_DECL, l).tabela.values.all { it != null }
    }
    val ids = HERDADO_LUGARES.indices.map { "p$it" }.toSet()
    pergunta(tx("hd_06"), setOf("p$certa"), ids - "p$certa", tx("hd_06_dica"), tx("hd_06_ok")) {
        HERDADO_LUGARES.forEachIndexed { k, (_, t) -> add(tecla("p$k", t, 230f + k * 52f)) }
    }
    cena(tx("hd_07"), who = PROF, mood = EpMood.HAPPY) {
        clear()
        add(titulo(tx("hd_titulo"), size = 22f))
        add(codigo("h", HERDADO_CODIGO.take(5), 80f, x = 60f, passo = 28f, size = 16f))
        add(tecla("acao", HERDADO_LUGARES[certa].second, 260f).copy(tint = Tint.GREEN))
        hot("h1", "h2", "acao")
    }
}

private fun passoNaTela(p: HerdadoNaDescida.Passo): String = when {
    p.id != null -> "  addtipo(${p.id}, ${p.herdado})"
    p.funcao == "T" -> tx("hd_t_devolve", HerdadoNaDescida.tokens(HERDADO_DECL).first())
    else -> "${p.funcao}(${p.herdado})"
}

// ─────────────────────────────── FUNÇÕES NA TABELA ───────────────────────────────
private fun funcoesNaTabela(): Episode = episodio(Skill.SCOPE_SYMBOLS, tx("fts_titulo"), PRATICA) {
    val t = TabelaComFuncoes.monta(FUNCOES)
    // Lado a lado, com a certa (soma) na ponta direita.
    val mesas = listOf("global" to 70f, "main" to 180f, "soma" to 290f)
    cena(tx("fts_01"), mood = EpMood.THINK) {
        add(titulo(tx("fts_titulo"), size = 22f))
        add(codigo("c", FUNCOES_CODIGO, 60f, x = 50f, passo = 24f, size = 14f))
    }
    val dosParam = "t" + TabelaComFuncoes.busca(t, "soma", "a")?.first
    val outras = mesas.map { "t${it.first}" }.toSet() - dosParam
    pergunta(tx("fts_02"), setOf(dosParam), outras, tx("fts_02_dica"), tx("fts_02_ok")) {
        hot("c0")
        for (m in mesas) add(Box("t${m.first}", m.first, m.second, 320f, 100f, 40f, Look.TOKEN, Tint.TEAL))
    }
    cena(tx("fts_03"), who = PROF) { clear(); mostraTabelas(t) }
    funcoesBuscaEChamada(t)
}

private fun EpisodeBuilder.mostraTabelas(t: List<TabelaComFuncoes.Escopo>) {
    add(titulo(tx("fts_titulo"), size = 22f))
    val pos = mapOf(TabelaComFuncoes.GLOBAL to (180f to 90f), "soma" to (95f to 200f), "main" to (265f to 200f))
    for (e in t) {
        val (x, y) = pos.getValue(e.nome)
        val cor = if (e.pai == null) Tint.ORANGE else Tint.TEAL
        add(registro("r${e.nome}", e.nome, linhasDaTabela(e), x, y, tint = cor, size = 13f))
    }
    for (e in t) e.pai?.let { wire("r${e.nome}", "r$it") }
}

private fun EpisodeBuilder.funcoesBuscaEChamada(t: List<TabelaComFuncoes.Escopo>) {
    val achou = TabelaComFuncoes.busca(t, "main", "a")
    val certaBusca = if (achou == null) "o2" else "o0"
    val opcoes = listOf("o0", "o1", "o2")
    pergunta(tx("fts_04"), setOf(certaBusca), opcoes.toSet() - certaBusca, tx("fts_04_dica"), tx("fts_04_ok")) {
        hot("rmain", "rglobal")
        val textos = listOf(tx("fts_op_a_soma"), tx("fts_op_a_global"), tx("fts_op_nada"))
        opcoes.forEachIndexed { k, id -> add(tecla(id, textos[k], 290f + k * 46f)) }
    }
    val vereditos = argumentosDasChamadas().map { TabelaComFuncoes.confereChamada(t, "main", "soma", it) }
    val recusada = vereditos.indexOf(TiposComandos.Veredito.ERRO)
    cena(tx("fts_05")) {
        drop("o0", "o1", "o2", "rsoma", "rmain")
        val chamadas = FUNCOES_CODIGO.filter { "= soma(" in it }.map { it.trim() }
        add(codigo("m", chamadas, 260f, x = 90f, passo = 40f, size = 17f))
        hot("rglobal")
    }
    val aceitas = vereditos.indices.map { "m$it" }.toSet() - "m$recusada"
    pergunta(tx("fts_06"), setOf("m$recusada"), aceitas, tx("fts_06_dica"), tx("fts_06_ok")) { hot() }
    cena(tx("fts_07"), who = PROF, mood = EpMood.HAPPY) { hot("rglobal", "m$recusada") }
}

// ─────────────────────────────── ACESSO A VETOR ───────────────────────────────
private fun acessoAVetor(): Episode = episodio(Skill.CODEGEN_MIPS, tx("vet_titulo"), PRATICA) {
    val alvo = ((AcessoVetor.endereco(VETOR_BASE, VETOR_I) - VETOR_BASE) / AcessoVetor.BYTES).toInt()
    cena(tx("vet_01"), mood = EpMood.THINK) {
        add(titulo(tx("vet_titulo"), size = 22f))
        add(codigo("x", listOf("x = v[i];"), 76f, x = 130f, size = 18f))
        VETOR.forEachIndexed { k, v ->
            add(Box("m$k", "$v", 75f + k * 70f, 190f, 64f, 44f, Look.CELL, Tint.TEAL, size = 17f))
            add(rotulo("a$k", "${AcessoVetor.endereco(VETOR_BASE, k.toLong())}", 75f + k * 70f, 230f, size = 14f))
        }
    }
    val vizinhas = VETOR.indices.map { "m$it" }.toSet() - "m$alvo"
    pergunta(tx("vet_02"), setOf("m$alvo"), vizinhas, tx("vet_02_dica"), tx("vet_02_ok")) { hot() }
    cena(tx("vet_03")) {
        add(codigo("t", AcessoVetor.tac("x", "v", "i"), 280f, x = 110f, passo = 34f, size = 17f))
        hot("t0")
    }
    pergunta(tx("vet_04"), setOf("q1"), setOf("q0", "q2"), tx("vet_04_dica"), tx("vet_04_ok")) {
        dropAll("m"); dropAll("a")
        val textos = listOf(tx("vet_op_tamanho"), tx("vet_op_bytes"), tx("vet_op_conta"))
        textos.forEachIndexed { k, t -> add(tecla("q$k", t, 125f + k * 47f)) }
    }
    vetorNoMips()
}

private fun EpisodeBuilder.vetorNoMips() {
    val mips = AcessoVetor.mips("x", "v", "i")
    val sll = mips.indexOfFirst { it.startsWith("sll ") }
    val outras = mips.indices.filter { mips[it].startsWith("la ") || mips[it].startsWith("add ") || "(" in mips[it] }
    cena(tx("vet_05")) {
        clear()
        add(titulo(tx("vet_titulo"), size = 22f))
        add(codigo("s", mips, 70f, x = 80f, passo = 36f, size = 17f))
        hot("s0", "s1")
    }
    pergunta(tx("vet_06"), setOf("s$sll"), outras.map { "s$it" }.toSet(), tx("vet_06_dica"), tx("vet_06_ok")) { hot() }
    val x = AcessoVetor.roda("x", "v", "i", VETOR, VETOR_I)
    cena(tx("vet_07")) {
        hot("s3", "s4")
        add(rotulo("res", "x = $x", 180f, 300f, Tint.GREEN, 20f))
    }
    cena(tx("vet_08"), who = PROF, mood = EpMood.HAPPY) { hot("s5") }
}

val praticasLacunas2 by lazy { listOf(funcoesNaTabela(), herdadoNaDescida(), acessoAVetor()) }
