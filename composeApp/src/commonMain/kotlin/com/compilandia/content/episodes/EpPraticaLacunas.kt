package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PRATICA
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.DagDoBloco
import com.compilandia.engine.MiniLang
import com.compilandia.engine.TiposComandos
import com.compilandia.engine.TiposComandos.Atribuicao
import com.compilandia.engine.TiposComandos.Chamada
import com.compilandia.engine.TiposComandos.Condicao
import com.compilandia.engine.TiposComandos.Retorno
import com.compilandia.engine.Type
import com.compilandia.engine.VivacidadeComLaco
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Lacunas que o revisor professor do bloco 5 apontou: tipos nos comandos, o DAG do bloco básico e a vivacidade com laço.
 * Cada aula de prática pergunta o que o motor responde: TiposComandos, DagDoBloco e VivacidadeComLaco.
 * PraticaLacunasTest confere os vereditos, os reusos e os conjuntos de vivas.
 */

/** A tabela de símbolos da aula de tipos. a e b são os parâmetros de media. */
val TIPOS_ENV = mapOf("n" to Type.INT, "x" to Type.FLOAT, "ok" to Type.BOOL, "a" to Type.INT, "b" to Type.INT)

/** Os três grupos da aula: o texto na tela e o comando que o motor confere. */
val TIPOS_ATRIB = listOf("n = x * 2;" to Atribuicao("n", "x * 2"), "x = n + 1;" to Atribuicao("x", "n + 1"), "ok = n < x;" to Atribuicao("ok", "n < x"))
val TIPOS_COND = listOf("if (n < 10) ..." to Condicao("n < 10"), "while (ok) ..." to Condicao("ok"), "if (n) ..." to Condicao("n"))
val TIPOS_FUNC = listOf("return a + b;" to Retorno(Type.FLOAT, "a + b"), "media(x, 2);" to Chamada(listOf(Type.INT, Type.INT), listOf("x", "2")), "media(1, 2);" to Chamada(listOf(Type.INT, Type.INT), listOf("1", "2")))

/** O bloco do Dragão (8.5.1): a linha 4 repete a conta da 2, e a 3 não repete a da 1. */
val DAG_BLOCO = listOf("a = b + c", "b = a - d", "c = b + c", "d = a - d")

/** O laço da soma, com o print no fim para o s ter quem o leia. */
const val VIVAS_LACO = "i = 0; s = 0; while (i < n) { s = s + i; i = i + 1; } print(s);"

/** A linha do corpo em que a aula para: a última atribuição ao i. */
fun linhaDoFimDoCorpo(code: List<String>) = code.indexOfLast { it.startsWith("i = ") }

/** Seguindo o caminho da execução a partir de [i], a primeira linha que lê [v]. */
fun primeiraQueLe(code: List<String>, i: Int, v: String): Int {
    var k = VivacidadeComLaco.sucessores(code, i).first()
    while (v !in VivacidadeComLaco.use(code[k])) k = VivacidadeComLaco.sucessores(code, k).first()
    return k
}

fun conjunto(s: Set<String>) = s.sorted().joinToString(", ", "{ ", " }")

private fun tecla(id: String, t: String, x: Float, y: Float, w: Float) = Box(id, t, x, y, w, 40f, Look.TOKEN, Tint.YELLOW, size = 14f)

/** As linhas do grupo na tela e a que o verificador recusa. */
private fun EpisodeBuilder.grupo(p: String, g: List<Pair<String, TiposComandos.Comando>>, y0: Float): Pair<String, Set<String>> {
    add(codigo(p, g.map { it.first }, y0, x = 70f, passo = 34f, size = 17f))
    val ids = g.indices.map { "$p$it" }.toSet()
    val erro = "$p${g.indexOfFirst { TiposComandos.verifica(it.second, TIPOS_ENV) == TiposComandos.Veredito.ERRO }}"
    return erro to ids - erro
}

val praticasLacunas by lazy {
    listOf(
        // ─────────────────────────────── TIPOS NOS COMANDOS ───────────────────────────────
        episodio(Skill.ATTRIBUTES_TYPES, tx("tcm_titulo"), PRATICA) {
            val tabela = Box("ts", "n: int    x: float    ok: bool", 180f, 76f, 300f, 36f, Look.TOKEN, Tint.TEAL, size = 15f)
            cena(tx("tcm_01"), mood = EpMood.THINK) {
                add(titulo(tx("tcm_titulo"), size = 22f))
                add(tabela)
            }
            var resp: Pair<String, Set<String>> = "" to emptySet()
            cena(tx("tcm_02"), who = PROF) { resp = grupo("a", TIPOS_ATRIB, 150f) }
            pergunta(tx("tcm_03"), setOf(resp.first), resp.second, tx("tcm_03_dica"), tx("tcm_03_ok")) { hot() }
            cena(tx("tcm_04")) { dropAll("a"); resp = grupo("c", TIPOS_COND, 150f) }
            pergunta(tx("tcm_05"), setOf(resp.first), resp.second, tx("tcm_05_dica"), tx("tcm_05_ok")) { hot() }
            cena(tx("tcm_06"), who = PROF) {
                dropAll("c")
                add(codigo("h", listOf("float media(int a, int b)"), 130f, x = 40f, size = 16f))
                resp = grupo("f", TIPOS_FUNC, 180f)
            }
            pergunta(tx("tcm_07"), setOf(resp.first), resp.second, tx("tcm_07_dica"), tx("tcm_07_ok")) { hot() }
            cena(tx("tcm_08"), mood = EpMood.HAPPY) { hot("ts") }
        },

        // ─────────────────────────────── O DAG DO BLOCO ───────────────────────────────
        episodio(Skill.OPTIMIZATION, tx("dag_titulo"), PRATICA) {
            val dag = DagDoBloco.monta(DAG_BLOCO)
            // Posições à mão; os nós, os nomes e os filhos saem do motor.
            val pos = mapOf(0 to (50f to 382f), 1 to (310f to 382f), 3 to (196f to 382f), 2 to (90f to 316f), 4 to (150f to 256f), 5 to (260f to 206f))
            /** Redesenha o DAG das [linhas] primeiras linhas, com os nós [ks]. */
            fun mostra(linhas: Int, vararg ks: Int) {
                dropAll("n")
                val parcial = DagDoBloco.monta(DAG_BLOCO.take(linhas))
                for (k in ks) {
                    val n = parcial.nos[k]
                    val t = if (n.interno) "${n.op}  ${n.nomes.joinToString(", ")}" else n.folha!!
                    val (x, y) = pos.getValue(k)
                    add(Box("n$k", t, x, y, maxOf(44f, t.length * 11f + 16f), 32f, Look.NODE, if (n.interno) Tint.ORANGE else Tint.TEAL, size = 15f))
                }
                for (k in ks) for (f in parcial.nos[k].filhos) wire("n$k", "n$f")
            }
            val reusada = dag.reusos.keys.single()
            val criadora = dag.reusos.getValue(reusada)
            cena(tx("dag_01"), mood = EpMood.THINK) {
                add(titulo(tx("dag_titulo"), size = 22f))
                add(codigo("d", DAG_BLOCO, 70f, x = 110f, passo = 30f, size = 17f))
            }
            cena(tx("dag_02"), who = PROF) { mostra(1, 0, 1, 2); hot("d0", "n2") }
            cena(tx("dag_03")) { mostra(2, 0, 1, 2, 3, 4); hot("d1", "n4") }
            pergunta(tx("dag_04"), setOf("d$criadora"), DAG_BLOCO.indices.map { "d$it" }.toSet() - "d$criadora" - "d$reusada", tx("dag_04_dica"), tx("dag_04_ok")) { hot("d$reusada") }
            cena(tx("dag_05")) { mostra(DAG_BLOCO.size, 0, 1, 2, 3, 4); hot("n4") }
            val certa = if (2 in dag.reusos) "o0" else "o1"
            pergunta(tx("dag_06"), setOf(certa), setOf("o0", "o1", "o2") - certa, tx("dag_06_dica"), tx("dag_06_ok")) {
                dropAll("n")
                hot("d2")
                add(tecla("o0", tx("dag_op_sim"), 180f, 240f, 300f), tecla("o1", tx("dag_op_b_mudou"), 180f, 290f, 300f), tecla("o2", tx("dag_op_esquerda"), 180f, 340f, 300f))
            }
            cena(tx("dag_07"), mood = EpMood.HAPPY) {
                drop("o0", "o1", "o2")
                mostra(DAG_BLOCO.size, *dag.nos.indices.toList().toIntArray())
                hot("n5")
            }
            cena(tx("dag_08"), who = PROF) { hot("n4") }
        },

        // ─────────────────────────────── VIVAS NO LAÇO ───────────────────────────────
        episodio(Skill.REG_ALLOC, tx("vlc_titulo"), PRATICA) {
            val code = MiniLang.tac(VIVAS_LACO)
            val r = VivacidadeComLaco.analisa(code, emptySet())
            val k = linhaDoFimDoCorpo(code)
            val le = primeiraQueLe(code, k, "i")
            val goto = code.indexOfFirst { it.startsWith("goto ") }
            val topo = code.indexOf(code[goto].substringAfter("goto ") + ":")
            val opcoes = listOf(r.liveOut[k], VivacidadeComLaco.semVoltar(code, emptySet())[k], VivacidadeComLaco.def(code[k]))
            cena(tx("vlc_01"), mood = EpMood.THINK) {
                add(titulo(tx("vlc_titulo"), size = 22f))
                add(codigo("v", code, 64f, x = 90f, passo = 26f, size = 14f))
            }
            cena(tx("vlc_02"), who = PROF) { hot("v$goto", "v$topo") }
            pergunta(tx("vlc_03"), setOf("v$le"), (k + 1 until code.size).map { "v$it" }.toSet(), tx("vlc_03_dica"), tx("vlc_03_ok")) { hot("v$k") }
            // A certa fica no meio: as outras duas são o engano de ler o goto como texto e o de olhar só a linha.
            val ordem = listOf(1, 0, 2)
            pergunta(tx("vlc_04"), setOf("p0"), setOf("p1", "p2"), tx("vlc_04_dica"), tx("vlc_04_ok")) {
                hot("v$k")
                ordem.forEachIndexed { pos, o -> add(tecla("p$o", conjunto(opcoes[o]), 70f + pos * 110f, 390f, 100f)) }
            }
            cena(tx("vlc_05")) {
                drop("p0", "p1", "p2")
                add(rotulo("viva", conjunto(r.liveOut[k]), 300f, 64f + k * 26f, Tint.GREEN, 16f))
                hot("v$goto", "v$topo")
            }
            cena(tx("vlc_06"), who = PROF, mood = EpMood.HAPPY) {
                add(rotulo("viva2", conjunto(r.liveIn[topo]), 300f, 64f + topo * 26f, Tint.GREEN, 16f))
                hot("viva", "viva2")
            }
        },
    )
}
