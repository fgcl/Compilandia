package com.compilandia.learning

import com.compilandia.textos.tx

data class Achievement(val emoji: String, val title: String, val description: String, val unlocked: Boolean)

/** Conquistas calculadas a partir do estado (nada a mais para salvar). */
object Achievements {
    fun all(l: Learner): List<Achievement> {
        val s = l.state
        val cal = l.calibration()
        fun m(sk: Skill) = l.mastery(sk) >= 0.8
        return listOf(
            Achievement("👣", tx("achievements_primeiro_passo"), tx("achievements_jogar_primeira_partida"), s.history.isNotEmpty()),
            Achievement("📖", tx("achievements_leitor_curioso"), tx("achievements_ver_aulas_em_3_estilos"), s.styleArms.size >= 3),
            Achievement("✍️", tx("achievements_explicador"), tx("achievements_escrever_explicacao_com_suas_palavras"), s.journal.isNotEmpty()),
            Achievement("🔥", tx("achievements_constancia"), tx("achievements_3_dias_seguidos_estudando"), s.streakDays >= 3),
            Achievement("🔥🔥", tx("achievements_habito_ferro"), tx("achievements_7_dias_seguidos"), s.streakDays >= 7),
            Achievement("🎯", tx("achievements_calibrado"), tx("achievements_10_respostas_confianca_com_brier"), cal.samples >= 10 && cal.brier < 0.2),
            Achievement("😕➡️💡", tx("achievements_nao_desisti"), tx("achievements_apertar_nao_entendi_depois_acertar"), s.feedback.any { f -> f.kind == "nao_entendi" && s.history.any { it.skill == f.skill && it.ts > f.ts && it.score >= 0.8 } }),
            Achievement("🔪", tx("achievements_ninja_lexico"), tx("achievements_dominar_tokens_automatos"), m(Skill.TOKENS) && m(Skill.REGEX_DFA)),
            Achievement("⚗️", tx("achievements_alquimista"), tx("achievements_dominar_first_follow"), m(Skill.FIRST) && m(Skill.FOLLOW)),
            Achievement("🛕", tx("achievements_sacerdote_ll_1"), tx("achievements_dominar_tabela_analisador_ll_1"), m(Skill.LL1_TABLE) && m(Skill.LL1_PARSING)),
            Achievement("🏰", tx("achievements_senhor_lr"), tx("achievements_dominar_shift_reduce_itens_slr"), m(Skill.SHIFT_REDUCE) && m(Skill.LR0_ITEMS) && m(Skill.SLR_TABLE)),
            Achievement("🦬", tx("achievements_domador_bison"), tx("achievements_dominar_yacc_bison"), m(Skill.YACC_BISON)),
            Achievement("🔩", tx("achievements_montador"), tx("achievements_dominar_geracao_codigo_mips"), m(Skill.CODEGEN_MIPS)),
            Achievement("⚔️", tx("achievements_cacador_chefoes"), tx("achievements_derrotar_3_chefoes"), s.bossesDefeated.size >= 3),
            Achievement("👑", tx("achievements_compilador_humano"), tx("achievements_derrotar_todos_chefoes"), s.bossesDefeated.size >= Region.entries.size),
            Achievement("🛠️", tx("achievements_engenheiro_tp"), tx("achievements_ler_todo_guia_trabalho_pratico"), s.guideRead.size >= 9),
        )
    }
}

/** Chefões de cada região (personagens originais). */
enum class Boss(val region: Region, val emoji: String, val bossName: String, val taunt: String) {
    B_PORTO(Region.PORTO, "🐙", tx("achievements_polvo_desordenado"), tx("achievements_eu_embaralho_fases_compilador_quero")),
    B_FLORESTA(Region.FLORESTA_LEXICA, "👻", tx("achievements_token_fantasma"), tx("achievements_sou_lexema_que_ninguem_consegue")),
    B_VALE(Region.VALE_GRAMATICAS, "🐉", tx("achievements_dragao_ambiguo"), tx("achievements_toda_frase_minha_tem_duas")),
    B_PONTE(Region.PONTE_TRANSFORMACAO, "🌀", tx("achievements_recursao_infinita"), "E → E + T → E + T + T → E + T + T + T → …"),
    B_TEMPLO(Region.TEMPLO_TOPDOWN, "🗿", tx("achievements_guardiao_tabela"), tx("achievements_celula_com_duas_producoes_templo")),
    B_FORTALEZA(Region.FORTALEZA_BOTTOMUP, "⚔️", tx("achievements_cavaleiro_shift_reduce"), tx("achievements_empilho_ou_reduzo_nem_eu")),
    B_OFICINA(Region.OFICINA_BISON, "🦬", tx("achievements_bisao_conflituoso"), tx("achievements_bison_4_shift_reduce_conflicts")),
    B_CIDADE(Region.CIDADE_SEMANTICA, "🦹", tx("achievements_tipo_errado"), tx("achievements_somo_inteiro_com_bool_escondo")),
    B_FORJA(Region.FORJA_CODIGO, "💀", tx("achievements_segmentation_fault"), tx("achievements_esqueceu_sw_sua_pilha_estourou")),
    ;
    companion object { fun of(r: Region) = entries.first { it.region == r } }
}
