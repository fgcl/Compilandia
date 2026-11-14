package com.compilandia

import com.compilandia.content.CUIDADO_ID
import com.compilandia.content.Temporada
import com.compilandia.content.episodes.Episodes
import com.compilandia.ui.Screen
import com.compilandia.ui.screens.EpisodeScreen
import kotlin.test.Test

/** Fotos do "diga por quê" e do julgamento do colega com o motivo, em 360 dp, para conferir de olho que as razões cabem. */
class PorQueRenderTest : RenderBase() {
    @Test fun dizerPorQueEJulgarComMotivo() {
        for (e in Episodes.all.filter { it.temporada == Temporada.BASICO }) {
            val nome = e.skill.name.lowercase()
            val pq = e.passos.indexOfFirst { p -> p.shot.boxes.any { it.id == com.compilandia.content.PORQUE_ID } }
            if (pq >= 0) shot("porque_${nome}", w = 360, h = 760) {
                com.compilandia.ui.CompilandiaTheme { EpisodeScreen(stateWith(), Screen.Episode(e.skill, id = e.id), onStep = pq, abertura = false) }
            }
            val fc = e.passos.indexOfFirst { p -> p.ask != null && p.shot.boxes.any { it.id == CUIDADO_ID } }
            shot("fecho_${nome}", w = 360, h = 760) {
                com.compilandia.ui.CompilandiaTheme { EpisodeScreen(stateWith(), Screen.Episode(e.skill, id = e.id), onStep = fc, abertura = false) }
            }
        }
    }

    /** As onze perguntas de sim ou não (e aceita ou rejeita, juntar ou separar) que ganharam a terceira opção. */
    @Test fun terceiraOpcao() = fotos("terceira", listOf("first_l_15", "lr_l_11", "min_l_10", "min_l_20", "eplexico_cadeia_b_trem_sai_primeiro", "regex_l_09", "regex_l_10", "cfg_l_12", "amb_l_05", "rec_l_assoc_3", "rec_l_11"))

    /** As cenas que o revisor professor do bloco 5 fez mudar: a - b - c na AST, o retorno do fat e as cores da alocação. */
    @Test fun bloco5() = fotos("bloco5", listOf("ast_l_13", "ast_l_14", "epbackend_fat_dois_chama_fat_outro", "epbackend_pinto_grafo_vizinhos_nunca_tem"))

    /** As cenas da série Em C que o revisor professor do bloco 6 fez mudar. */
    @Test fun bloco6() = fotos("bloco6", listOf("epemc_termo_igual_com_vezes_fator", "epemc_esq_dir_sao_ponteiros_eles", "epemc_mais_usa_resultado_vezes_que", "epemc_main_chama_yyparse_parser_que", "epemc_calc_l_inteiro", "epemc_calc_y_falta", "epemc_novo_cria_no", "epemc_qual_linha_cria_no", "epemc_acao_monta_no"))

    private fun fotos(prefixo: String, chaves: List<String>) {
        for (c in chaves) {
            val alvo = com.compilandia.textos.tx(c)
            val e = Episodes.all.first { ep -> ep.passos.any { it.fala.text == alvo } }
            val k = e.passos.indexOfFirst { it.fala.text == alvo }
            shot("${prefixo}_$c", w = 360, h = 760) {
                com.compilandia.ui.CompilandiaTheme { EpisodeScreen(stateWith(), Screen.Episode(e.skill, id = e.id), onStep = k, abertura = false) }
            }
        }
    }
}
