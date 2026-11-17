package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpisodeBuilder
import com.compilandia.content.Look
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ErrosDaSerieC
import com.compilandia.engine.LexerC
import com.compilandia.engine.MiniBison
import com.compilandia.textos.tx

/*
 * Os erros comuns da série Em C (pedido do revisor professor do bloco 6), cada um com uma segunda entrada:
 * Em C 1 sem o ungetc (7*13), Em C 3 sem o e = (1 + 2 + 3) e Em C 5 sem o %left (2*3+4).
 * Quem responde é o motor: LexerC, ErrosDaSerieC e MiniBison. PraticaLacunas2Test e SerieEmCErrosTest conferem.
 */

/** A segunda entrada da Em C 1, no erro comum de esquecer o ungetc. */
internal const val EMC1_SEGUNDA = "7*13"

/** As opções da pergunta do ungetc: com ele, o engano de achar que os números grudam e sem ele (a certa, no fim). */
internal fun emC1Opcoes(): List<List<String>> =
    listOf(LexerC.tokens(EMC1_SEGUNDA), listOf("NUM 713"), LexerC.tokens(EMC1_SEGUNDA, comUngetc = false))

/** Erro comum (professor do bloco 6): o mesmo lexer sem o ungetc, rodando numa entrada nova. */
internal fun EpisodeBuilder.emC1SemUngetc(c: List<String>) {
    val opcoes = emC1Opcoes()
    val certa = opcoes.indexOf(LexerC.tokens(EMC1_SEGUNDA, comUngetc = false))
    cena(tx("emc1_erro")) {
        clear()
        add(titulo(tx("emc1_erro_titulo"), size = 22f))
        add(codigo("e", c.filterNot { "ungetc" in it }, 58f, passo = 21f, size = 12f))
        add(fita(EMC1_SEGUNDA, 300f, 30f))
    }
    val ids = opcoes.indices.map { "k$it" }
    val erradas = ids.toSet() - "k$certa"
    pergunta(tx("emc1_erro_q"), setOf("k$certa"), erradas, tx("emc1_erro_dica"), tx("emc1_erro_ok")) {
        dropAll("e")
        opcoes.forEachIndexed { k, o ->
            val y = 110f + k * 52f
            add(Box(ids[k], o.joinToString("   "), 180f, y, 300f, 40f, Look.TOKEN, Tint.YELLOW, size = 15f))
        }
    }
}

/** A segunda conta da Em C 3, no erro comum de chamar novo sem guardar o nó em e. */
internal val EMC3_SEGUNDA = listOf("1", "+", "2", "+", "3")

/** As opções: a do erro (a resposta, primeiro), a árvore certa e a da soma pela direita. */
internal fun emC3Opcoes(): List<String> = listOf(
    ErrosDaSerieC.exprMontaArvore(EMC3_SEGUNDA, guardaNo = false).texto(),
    ErrosDaSerieC.exprMontaArvore(EMC3_SEGUNDA).texto(),
    "1 + (2 + 3)",
)

/** Erro comum (professor do bloco 6): o novo(...) sem o e =, rodando 1 + 2 + 3. */
internal fun EpisodeBuilder.emC3SemGuardar() {
    val semE = CODIGO_EXPR_ARVORE.map { it.replace("e = novo(", "novo(") }
    val opcoes = emC3Opcoes()
    val doErro = ErrosDaSerieC.exprMontaArvore(EMC3_SEGUNDA, guardaNo = false).texto()
    val certa = "k${opcoes.indexOf(doErro)}"
    cena(tx("emc3_erro")) {
        dropAll("s")
        add(titulo(tx("emc3_erro_titulo"), size = 22f))
        add(codigo("s", semE, 96f, x = 14f, passo = 26f, size = 12f))
        hot("s3")
    }
    val erradas = opcoes.indices.map { "k$it" }.toSet() - certa
    pergunta(tx("emc3_erro_q"), setOf(certa), erradas, tx("emc3_erro_dica"), tx("emc3_erro_ok")) {
        hot()
        opcoes.forEachIndexed { k, t ->
            add(Box("k$k", t, 180f, 300f + k * 40f, 260f, 34f, Look.TOKEN, Tint.YELLOW, size = 15f))
        }
    }
}

/** A segunda conta da Em C 5: com o vezes antes do mais. */
internal const val EMC5_SEGUNDA = "2*3+4"

/** O calc.y da tela sem as duas linhas do %left: o erro comum da Em C 5. */
internal val SPEC_SEM_LEFT = SPEC_CALC.filterNot { it.startsWith("%left") }

/** As três opções: com o %left, sem ele (a resposta) e o engano de achar que conflito impede de compilar. */
internal fun emC5Opcoes(): List<String> {
    val sem = MiniBison.compile(SPEC_SEM_LEFT.joinToString("\n")).run(EMC5_SEGUNDA).value
    val com = MiniBison.compile(SPEC_CALC.joinToString("\n")).run(EMC5_SEGUNDA).value
    return listOf("= $com", "= $sem", tx("emc5_op_nao_compila"))
}

/** Erro comum (professor do bloco 6): esquecer o %left. O Bison avisa os conflitos e escolhe empilhar. */
internal fun EpisodeBuilder.emC5SemLeft() {
    val conflitos = MiniBison.compile(SPEC_SEM_LEFT.joinToString("\n")).conflicts.size
    val opcoes = emC5Opcoes()
    cena(tx("emc5_erro")) {
        clear(); clearWires()
        add(titulo(tx("emc5_erro_titulo"), size = 22f))
        add(codigo("y", SPEC_SEM_LEFT, 80f, x = 40f, passo = 28f, size = 13f))
        add(rotulo("av", tx("emc5_aviso", conflitos), 180f, 250f, Tint.RED, 14f))
    }
    val ids = opcoes.indices.map { "k$it" }
    pergunta(tx("emc5_erro_q"), setOf("k1"), ids.toSet() - "k1", tx("emc5_erro_dica"), tx("emc5_erro_ok")) {
        opcoes.forEachIndexed { k, t -> add(Box(ids[k], t, 180f, 300f + k * 40f, 220f, 34f, Look.TOKEN, Tint.YELLOW)) }
    }
}
