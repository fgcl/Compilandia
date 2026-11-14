package com.compilandia.content

import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/**
 * O "diga por quê" do fim da Sua vez (seção 3 da revisão). Sem rede e sem IA, a resposta aberta não tem quem corrija;
 * por isso a pessoa escolhe a razão num menu, como em Johnson e Mayer (2010), em que escolher a razão num jogo
 * rendeu mais que escrever. A certa explica o passo; as erradas são os enganos de prova.
 */
class PorQue(val fala: String, val fato: String, val certa: String, val erradas: List<String>, val dica: String, val ok: String)

/** O motivo no julgamento do colega: o certo, um errado com o mesmo veredito e um com o veredito trocado. */
class RazaoDoFecho(val certa: String, val mesmoVeredito: String, val outroVeredito: String)

object PorQues {
    /** Ambiguidade fica de fora: a aula já pergunta "por que o vezes fica mais fundo?" dentro da Sua vez. */
    val suaVez: Map<Skill, PorQue> get() = mapOf(
        Skill.PHASES to PorQue(tx("pq_phases_q"), tx("pq_phases_fato"), tx("pq_phases_c"), listOf(tx("pq_phases_e1"), tx("pq_phases_e2")), tx("pq_phases_dica"), tx("pq_phases_ok")),
        Skill.FIRST to PorQue(tx("pq_first_q"), tx("pq_first_fato"), tx("pq_first_c"), listOf(tx("pq_first_e1"), tx("pq_first_e2")), tx("pq_first_dica"), tx("pq_first_ok")),
        Skill.FOLLOW to PorQue(tx("pq_follow_q"), tx("pq_follow_fato"), tx("pq_follow_c"), listOf(tx("pq_follow_e1"), tx("pq_follow_e2")), tx("pq_follow_dica"), tx("pq_follow_ok")),
        Skill.LL1_TABLE to PorQue(tx("pq_ll1_q"), tx("pq_ll1_fato"), tx("pq_ll1_c"), listOf(tx("pq_ll1_e1"), tx("pq_ll1_e2")), tx("pq_ll1_dica"), tx("pq_ll1_ok")),
        Skill.LR0_ITEMS to PorQue(tx("pq_lr0_q"), tx("pq_lr0_fato"), tx("pq_lr0_c"), listOf(tx("pq_lr0_e1"), tx("pq_lr0_e2")), tx("pq_lr0_dica"), tx("pq_lr0_ok")),
        Skill.SLR_TABLE to PorQue(tx("pq_slr_q"), tx("pq_slr_fato"), tx("pq_slr_c"), listOf(tx("pq_slr_e1"), tx("pq_slr_e2")), tx("pq_slr_dica"), tx("pq_slr_ok")),
        Skill.LALR to PorQue(tx("pq_lalr_q"), tx("pq_lalr_fato"), tx("pq_lalr_c"), listOf(tx("pq_lalr_e1"), tx("pq_lalr_e2")), tx("pq_lalr_dica"), tx("pq_lalr_ok")),
        Skill.DFA_MINIMIZATION to PorQue(tx("pq_min_q"), tx("pq_min_fato"), tx("pq_min_c"), listOf(tx("pq_min_e1"), tx("pq_min_e2")), tx("pq_min_dica"), tx("pq_min_ok")),
        Skill.AST_BUILD to PorQue(tx("pq_ast_q"), tx("pq_ast_fato"), tx("pq_ast_c"), listOf(tx("pq_ast_e1"), tx("pq_ast_e2")), tx("pq_ast_dica"), tx("pq_ast_ok")),
        Skill.CODEGEN_MIPS to PorQue(tx("pq_mips_q"), tx("pq_mips_fato"), tx("pq_mips_c"), listOf(tx("pq_mips_e1"), tx("pq_mips_e2")), tx("pq_mips_dica"), tx("pq_mips_ok")),
        Skill.TOKENS to PorQue(tx("pq_tokens_q"), tx("pq_tokens_fato"), tx("pq_tokens_c"), listOf(tx("pq_tokens_e1"), tx("pq_tokens_e2")), tx("pq_tokens_dica"), tx("pq_tokens_ok")),
        Skill.REGEX_DFA to PorQue(tx("pq_regex_q"), tx("pq_regex_fato"), tx("pq_regex_c"), listOf(tx("pq_regex_e1"), tx("pq_regex_e2")), tx("pq_regex_dica"), tx("pq_regex_ok")),
        Skill.NFA_TO_DFA to PorQue(tx("pq_afn_q"), tx("pq_afn_fato"), tx("pq_afn_c"), listOf(tx("pq_afn_e1"), tx("pq_afn_e2")), tx("pq_afn_dica"), tx("pq_afn_ok")),
        Skill.LEX_FLEX to PorQue(tx("pq_flex_q"), tx("pq_flex_fato"), tx("pq_flex_c"), listOf(tx("pq_flex_e1"), tx("pq_flex_e2")), tx("pq_flex_dica"), tx("pq_flex_ok")),
        Skill.CFG_DERIVATION to PorQue(tx("pq_cfg_q"), tx("pq_cfg_fato"), tx("pq_cfg_c"), listOf(tx("pq_cfg_e1"), tx("pq_cfg_e2")), tx("pq_cfg_dica"), tx("pq_cfg_ok")),
        Skill.LEFT_RECURSION to PorQue(tx("pq_rec_q"), tx("pq_rec_fato"), tx("pq_rec_c"), listOf(tx("pq_rec_e1"), tx("pq_rec_e2")), tx("pq_rec_dica"), tx("pq_rec_ok")),
        Skill.LEFT_FACTORING to PorQue(tx("pq_fat_q"), tx("pq_fat_fato"), tx("pq_fat_c"), listOf(tx("pq_fat_e1"), tx("pq_fat_e2")), tx("pq_fat_dica"), tx("pq_fat_ok")),
        Skill.LL1_PARSING to PorQue(tx("pq_ll1p_q"), tx("pq_ll1p_fato"), tx("pq_ll1p_c"), listOf(tx("pq_ll1p_e1"), tx("pq_ll1p_e2")), tx("pq_ll1p_dica"), tx("pq_ll1p_ok")),
        Skill.RECURSIVE_DESCENT to PorQue(tx("pq_desc_q"), tx("pq_desc_fato"), tx("pq_desc_c"), listOf(tx("pq_desc_e1"), tx("pq_desc_e2")), tx("pq_desc_dica"), tx("pq_desc_ok")),
        Skill.ERROR_RECOVERY to PorQue(tx("pq_erro_q"), tx("pq_erro_fato"), tx("pq_erro_c"), listOf(tx("pq_erro_e1"), tx("pq_erro_e2")), tx("pq_erro_dica"), tx("pq_erro_ok")),
        Skill.SHIFT_REDUCE to PorQue(tx("pq_sr_q"), tx("pq_sr_fato"), tx("pq_sr_c"), listOf(tx("pq_sr_e1"), tx("pq_sr_e2")), tx("pq_sr_dica"), tx("pq_sr_ok")),
        Skill.YACC_BISON to PorQue(tx("pq_bison_q"), tx("pq_bison_fato"), tx("pq_bison_c"), listOf(tx("pq_bison_e1"), tx("pq_bison_e2")), tx("pq_bison_dica"), tx("pq_bison_ok")),
        Skill.ATTRIBUTES_TYPES to PorQue(tx("pq_tipos_q"), tx("pq_tipos_fato"), tx("pq_tipos_c"), listOf(tx("pq_tipos_e1"), tx("pq_tipos_e2")), tx("pq_tipos_dica"), tx("pq_tipos_ok")),
        Skill.SCOPE_SYMBOLS to PorQue(tx("pq_escopo_q"), tx("pq_escopo_fato"), tx("pq_escopo_c"), listOf(tx("pq_escopo_e1"), tx("pq_escopo_e2")), tx("pq_escopo_dica"), tx("pq_escopo_ok")),
        Skill.TAC to PorQue(tx("pq_tac_q"), tx("pq_tac_fato"), tx("pq_tac_c"), listOf(tx("pq_tac_e1"), tx("pq_tac_e2")), tx("pq_tac_dica"), tx("pq_tac_ok")),
        Skill.OPTIMIZATION to PorQue(tx("pq_otim_q"), tx("pq_otim_fato"), tx("pq_otim_c"), listOf(tx("pq_otim_e1"), tx("pq_otim_e2")), tx("pq_otim_dica"), tx("pq_otim_ok")),
        Skill.RUNTIME to PorQue(tx("pq_rt_q"), tx("pq_rt_fato"), tx("pq_rt_c"), listOf(tx("pq_rt_e1"), tx("pq_rt_e2")), tx("pq_rt_dica"), tx("pq_rt_ok")),
        Skill.REG_ALLOC to PorQue(tx("pq_reg_q"), tx("pq_reg_fato"), tx("pq_reg_c"), listOf(tx("pq_reg_e1"), tx("pq_reg_e2")), tx("pq_reg_dica"), tx("pq_reg_ok")),
    )

    val fecho: Map<Skill, RazaoDoFecho> get() = mapOf(
        Skill.PHASES to RazaoDoFecho(tx("fc_phases_c"), tx("fc_phases_a"), tx("fc_phases_b")),
        Skill.FIRST to RazaoDoFecho(tx("fc_first_c"), tx("fc_first_a"), tx("fc_first_b")),
        Skill.FOLLOW to RazaoDoFecho(tx("fc_follow_c"), tx("fc_follow_a"), tx("fc_follow_b")),
        Skill.LL1_TABLE to RazaoDoFecho(tx("fc_ll1_c"), tx("fc_ll1_a"), tx("fc_ll1_b")),
        Skill.LR0_ITEMS to RazaoDoFecho(tx("fc_lr0_c"), tx("fc_lr0_a"), tx("fc_lr0_b")),
        Skill.SLR_TABLE to RazaoDoFecho(tx("fc_slr_c"), tx("fc_slr_a"), tx("fc_slr_b")),
        Skill.LALR to RazaoDoFecho(tx("fc_lalr_c"), tx("fc_lalr_a"), tx("fc_lalr_b")),
        Skill.DFA_MINIMIZATION to RazaoDoFecho(tx("fc_min_c"), tx("fc_min_a"), tx("fc_min_b")),
        Skill.AST_BUILD to RazaoDoFecho(tx("fc_ast_c"), tx("fc_ast_a"), tx("fc_ast_b")),
        Skill.CODEGEN_MIPS to RazaoDoFecho(tx("fc_mips_c"), tx("fc_mips_a"), tx("fc_mips_b")),
        Skill.TOKENS to RazaoDoFecho(tx("fc_tokens_c"), tx("fc_tokens_a"), tx("fc_tokens_b")),
        Skill.REGEX_DFA to RazaoDoFecho(tx("fc_regex_c"), tx("fc_regex_a"), tx("fc_regex_b")),
        Skill.NFA_TO_DFA to RazaoDoFecho(tx("fc_afn_c"), tx("fc_afn_a"), tx("fc_afn_b")),
        Skill.LEX_FLEX to RazaoDoFecho(tx("fc_flex_c"), tx("fc_flex_a"), tx("fc_flex_b")),
        Skill.CFG_DERIVATION to RazaoDoFecho(tx("fc_cfg_c"), tx("fc_cfg_a"), tx("fc_cfg_b")),
        Skill.AMBIGUITY to RazaoDoFecho(tx("fc_amb_c"), tx("fc_amb_a"), tx("fc_amb_b")),
        Skill.LEFT_RECURSION to RazaoDoFecho(tx("fc_rec_c"), tx("fc_rec_a"), tx("fc_rec_b")),
        Skill.LEFT_FACTORING to RazaoDoFecho(tx("fc_fat_c"), tx("fc_fat_a"), tx("fc_fat_b")),
        Skill.LL1_PARSING to RazaoDoFecho(tx("fc_ll1p_c"), tx("fc_ll1p_a"), tx("fc_ll1p_b")),
        Skill.RECURSIVE_DESCENT to RazaoDoFecho(tx("fc_desc_c"), tx("fc_desc_a"), tx("fc_desc_b")),
        Skill.ERROR_RECOVERY to RazaoDoFecho(tx("fc_erro_c"), tx("fc_erro_a"), tx("fc_erro_b")),
        Skill.SHIFT_REDUCE to RazaoDoFecho(tx("fc_sr_c"), tx("fc_sr_a"), tx("fc_sr_b")),
        Skill.YACC_BISON to RazaoDoFecho(tx("fc_bison_c"), tx("fc_bison_a"), tx("fc_bison_b")),
        Skill.ATTRIBUTES_TYPES to RazaoDoFecho(tx("fc_tipos_c"), tx("fc_tipos_a"), tx("fc_tipos_b")),
        Skill.SCOPE_SYMBOLS to RazaoDoFecho(tx("fc_escopo_c"), tx("fc_escopo_a"), tx("fc_escopo_b")),
        Skill.TAC to RazaoDoFecho(tx("fc_tac_c"), tx("fc_tac_a"), tx("fc_tac_b")),
        Skill.OPTIMIZATION to RazaoDoFecho(tx("fc_otim_c"), tx("fc_otim_a"), tx("fc_otim_b")),
        Skill.RUNTIME to RazaoDoFecho(tx("fc_rt_c"), tx("fc_rt_a"), tx("fc_rt_b")),
        Skill.REG_ALLOC to RazaoDoFecho(tx("fc_reg_c"), tx("fc_reg_a"), tx("fc_reg_b")),
    )
}

/** A peça do fato no alto da cena do "diga por quê". É por ela que a cola e os testes acham a cena. */
const val PORQUE_ID = "pqF"

/** A cena do "diga por quê": o fato no alto e três razões. A certa gira de lugar conforme o assunto. */
fun EpisodeBuilder.porQue(p: PorQue) {
    pergunta(p.fala, setOf("pq0"), setOf("pq1", "pq2"), p.dica, p.ok) {
        clear()
        add(titulo(tx("aula_titulo_porque")))
        add(cartao(PORQUE_ID, p.fato, 100f, h = 64f))
        val textos = listOf(p.certa) + p.erradas
        val giro = skill.ordinal % 3
        for (pos in 0 until 3) {
            val k = (pos + giro) % 3
            add(Box("pq$k", textos[k], 180f, 186f + pos * 58f, 336f, 50f, Look.CARD, Tint.YELLOW, size = 15.5f))
        }
    }
}
