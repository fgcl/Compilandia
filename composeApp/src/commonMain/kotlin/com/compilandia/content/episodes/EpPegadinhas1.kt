package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.alvo
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Pegadinhas de prova: da fase léxica à ponte da transformação. */
val pegadinhasFrente by lazy {
    listOf(
        episodio(Skill.PHASES, tx("eppegadinhas1_quem_pega_este_erro"), PEGADINHA) {
            val caixas = listOf(
                Box("bl", tx("eppegadinhas1_lexico"), 62f, 300f, 104f, 92f, Look.BIN, Tint.TEAL, size = 15f),
                Box("bs", tx("eppegadinhas1_sintatico"), 180f, 300f, 104f, 92f, Look.BIN, Tint.ORANGE, size = 15f),
                Box("bm", tx("eppegadinhas1_semantico"), 298f, 300f, 104f, 92f, Look.BIN, Tint.PINK, size = 15f),
            )
            val fases = setOf("bl", "bs", "bm")
            cena(tx("eppegadinhas1_tres_erros_parecidos_caem_na")) {
                add(titulo(tx("eppegadinhas1_quem_pega")))
                add(caixas)
            }
            pergunta(
                tx("eppegadinhas1_primeiro_int_x_arroba_igual"),
                setOf("bl"), fases - "bl",
                tx("eppegadinhas1_nenhum_token_comeca_com_arroba"),
                tx("eppegadinhas1_isso_scanner_para_antes_qualquer"),
            ) { add(Box("c", "int x@ = 2;", 180f, 140f, 240f, 50f, Look.CELL, size = 18f)); hot("c") }
            pergunta(
                tx("eppegadinhas1_agora_falta_ponto_virgula_tokens"),
                setOf("bs"), fases - "bs",
                tx("eppegadinhas1_toda_palavra_existe_que_falta"),
                tx("eppegadinhas1_sintatica_forma_comando_esta_errada"),
            ) { text("c", "x = 2  y = 3;"); hot("c") }
            pergunta(
                tx("eppegadinhas1_por_ultimo_y_mais_com"),
                setOf("bm"), fases - "bm",
                tx("eppegadinhas1_problema_nao_forma_nome_y"),
                tx("eppegadinhas1_semantica_so_ela_consulta_tabela"),
            ) { text("c", "x = y + 1;"); hot("c") }
            cena(tx("eppegadinhas1_na_prova_pergunte_primeiro"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.TOKENS, tx("eppegadinhas1_por_que_ifa_nao_if"), PEGADINHA) {
            cena(tx("eppegadinhas1_olhe_esta_linha_ifa_igual")) {
                add(titulo("ifa = 1;"))
                add(fita("ifa=1;", 150f, cell = 46f))
                hot("c0")
            }
            cena(tx("eppegadinhas1_depois_i_vem_f_pedaco")) { hot("c0", "c1"); tint("c0", Tint.YELLOW); tint("c1", Tint.YELLOW) }
            pergunta(
                tx("eppegadinhas1_ele_fecha_token_agora_toque"),
                setOf("c2"), setOf("c1", "c3"),
                tx("eppegadinhas1_scanner_estica_enquanto_der_ainda"),
                tx("eppegadinhas1_isso_ainda_cabe_entao_token"),
            ) { hot() }
            cena(tx("eppegadinhas1_maior_casamento_fica_pedaco_mais")) {
                tint("c2", Tint.YELLOW); hot("c0", "c1", "c2")
                add(fileira("t", listOf("ifa" to Tint.TEAL, "=" to Tint.ORANGE, "1" to Tint.YELLOW, ";" to Tint.BLUE), 270f, w = 62f))
            }
            cena(tx("eppegadinhas1_palavra_reservada_so_ganha_no"), who = PROF) { hot() }
            palavra("MAIOR CASAMENTO", tx("eppegadinhas1_ordem_decisao_primeiro_tamanho_depois"), mood = EpMood.HAPPY)
        },

        episodio(Skill.REGEX_DFA, tx("eppegadinhas1_estrela_mais_interrogacao"), PEGADINHA) {
            val linhas = listOf(
                listOf("", "vazia", "a", "aa"),
                listOf("a*", "", "", ""),
                listOf("a+", "", "", ""),
                listOf("a?", "", "", ""),
            )
            cena(tx("eppegadinhas1_tres_simbolos_parecidos_que_prova")) {
                add(titulo(tx("eppegadinhas1_a_a_a")))
                add(tabela("m", linhas, 110f, cw = 76f, ch = 54f, size = 15f))
            }
            cena(tx("eppegadinhas1_estrela_aceita_zero_ou_mais")) {
                hot("ml1c0"); text("ml1c1", "sim"); text("ml1c2", "sim"); text("ml1c3", "sim")
                tint("ml1c1", Tint.GREEN); tint("ml1c2", Tint.GREEN); tint("ml1c3", Tint.GREEN)
            }
            pergunta(
                tx("eppegadinhas1_mais_exige_pelo_menos_toque"),
                setOf("ml2c1"), setOf("ml2c2", "ml2c3"),
                tx("eppegadinhas1_pelo_menos_qual_cadeia_nao"),
                tx("eppegadinhas1_isso_cadeia_vazia_fica_fora"),
            ) { hot("ml2c0") }
            cena(tx("eppegadinhas1_interrogacao_zero_ou_vale_vazio")) {
                text("ml2c1", tx("comum_nao")); text("ml2c2", "sim"); text("ml2c3", "sim")
                tint("ml2c1", Tint.RED); tint("ml2c2", Tint.GREEN); tint("ml2c3", Tint.GREEN)
                text("ml3c1", "sim"); text("ml3c2", "sim"); text("ml3c3", tx("comum_nao"))
                tint("ml3c1", Tint.GREEN); tint("ml3c2", Tint.GREEN); tint("ml3c3", Tint.RED)
                hot("ml3c0")
            }
            cena(tx("eppegadinhas1_estrela_zero_ou_mais_mais"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.NFA_TO_DFA, tx("eppegadinhas1_fecho_esquecido"), PEGADINHA) {
            val pos = listOf(60f to 130f, 180f to 130f, 300f to 130f)
            cena(tx("eppegadinhas1_este_automato_anda_q0_para")) {
                add(titulo(tx("eppegadinhas1_comeco_afd")))
                add(estados(pos, setOf(2)))
                wire("q0", "q1", "ε"); wire("q1", "q2", "a")
            }
            pergunta(
                tx("eppegadinhas1_qual_estado_inicial_afd_toque"),
                setOf("q0", "q1"), setOf("q2"),
                tx("eppegadinhas1_comece_em_q0_ande_pelas"),
                tx("eppegadinhas1_isso_inicial_q0_mais_tudo"),
            ) { hot() }
            cena(tx("eppegadinhas1_erro_comum_escrever_so_q0")) {
                add(Box("s", "{ q0, q1 }", 180f, 260f, 170f, 50f, Look.TOKEN, Tint.YELLOW, size = 18f))
                add(rotulo("r", tx("eppegadinhas1_estado_inicial_afd"), 180f, 302f))
            }
            cena(tx("eppegadinhas1_receita_tem_dois_passos_anda"), who = PROF) {
                add(Box("p", tx("eppegadinhas1_move_depois_fecho"), 180f, 344f, 240f, 40f, Look.CELL, Tint.TEAL, size = 15f))
                hot("p")
            }
            palavra("FECHO", tx("eppegadinhas1_nunca_pule_fecho_ele_vale"), mood = EpMood.HAPPY)
        },

        episodio(Skill.LEX_FLEX, tx("eppegadinhas1_ordem_das_regras_no_flex"), PEGADINHA) {
            cena(tx("eppegadinhas1_neste_arquivo_ponto_l_nome")) {
                add(titulo(tx("comum_arquivo_l")))
                add(codigo("r", listOf("[a-z]+   { return ID; }", "\"if\"     { return IF; }"), 110f, x = 56f, passo = 40f, size = 16f))
            }
            cena(tx("eppegadinhas1_chega_palavra_if_duas_regras")) {
                hot("r0", "r1")
                add(Box("in", "if", 180f, 220f, 90f, 50f, Look.CELL, Tint.YELLOW, size = 22f))
            }
            pergunta(
                tx("eppegadinhas1_no_empate_flex_fica_com"),
                setOf("r0"), setOf("r1"),
                tx("eppegadinhas1_olhe_qual_linha_vem_primeiro"),
                tx("eppegadinhas1_isso_if_vira_id_regra"),
            ) { hot() }
            cena(tx("eppegadinhas1_flex_ate_avisa_que_regra"), who = PROF) {
                add(Box("ok", tx("eppegadinhas1_if_vira_id"), 180f, 300f, 170f, 44f, Look.TOKEN, Tint.RED, size = 17f))
            }
            cena(tx("eppegadinhas1_conserto_subir_palavra_reservada_reservada"), mood = EpMood.HAPPY) {
                move("r1", 56f, 110f); move("r0", 56f, 150f)
                text("ok", tx("eppegadinhas1_if_vira_if")); tint("ok", Tint.GREEN)
            }
        },

        episodio(Skill.AMBIGUITY, tx("eppegadinhas1_else_solto"), PEGADINHA) {
            val i1 = Box("i1", "if 1", 116f, 110f, 74f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val i2 = Box("i2", "if 2", 180f, 200f, 74f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val e = Box("e", "else", 244f, 290f, 74f, 40f, Look.NODE, Tint.TEAL, size = 15f)
            cena(tx("eppegadinhas1_dois_ifs_else_so_qual")) {
                add(titulo(tx("eppegadinhas1_if_if_b_x_else"), size = 22f))
                add(i1, i2, e)
                wire("i1", "i2", arrow = false)
            }
            cena(tx("eppegadinhas1_leitura_liga_else_ao_if")) { wire("i2", "e", arrow = false, hot = true) }
            cena(tx("eppegadinhas1_outra_liga_else_ao_if")) {
                clearWires(); wire("i1", "i2", arrow = false); wire("i1", "e", arrow = false, hot = true)
                move("e", 60f, 200f)
            }
            pergunta(
                tx("eppegadinhas1_regra_das_linguagens_else_fica"),
                setOf("i2"), setOf("i1"),
                tx("eppegadinhas1_ande_para_tras_partir_else"),
                tx("eppegadinhas1_isso_else_fica_com_if"),
            ) { hot() }
            cena(tx("eppegadinhas1_bison_resolve_empilhando_else_assim"), who = PROF) {
                clearWires(); wire("i1", "i2", arrow = false); wire("i2", "e", arrow = false, hot = true)
                move("e", 244f, 290f)
            }
            cena(tx("eppegadinhas1_se_quiser_if_fora_use"), mood = EpMood.HAPPY) {}
        },

        episodio(Skill.CFG_DERIVATION, tx("eppegadinhas1_duas_derivacoes_arvore"), PEGADINHA) {
            cena(tx("eppegadinhas1_olhe_esta_gramatica_pequena_frase")) {
                add(titulo(tx("comum_b_2_2")))
                add(codigo("g", listOf("S -> A B", "A -> a", "B -> b"), 74f, x = 120f, passo = 26f, size = 15f))
            }
            cena(tx("eppegadinhas1_pela_esquerda_vira_b_depois")) {
                add(Box("e", tx("eppegadinhas1_b_b_b"), 180f, 190f, 300f, 42f, Look.CELL, Tint.TEAL, size = 15f))
            }
            cena(tx("eppegadinhas1_pela_direita_vira_b_depois")) {
                add(Box("d", tx("eppegadinhas1_b_b_b_2"), 180f, 240f, 300f, 42f, Look.CELL, Tint.ORANGE, size = 15f))
            }
            pergunta(
                tx("eppegadinhas1_sao_duas_derivacoes_diferentes_isso"),
                setOf("nao"), setOf("sim"),
                tx("eppegadinhas1_ambiguidade_pede_duas_arvores_ou"),
                tx("eppegadinhas1_isso_duas_montam_mesma_arvore"),
            ) {
                add(Box("sim", tx("eppegadinhas1_ambigua"), 106f, 310f, 140f, 50f, Look.TOKEN, Tint.RED, size = 16f))
                add(Box("nao", tx("eppegadinhas1_nao"), 254f, 310f, 140f, 50f, Look.TOKEN, Tint.GREEN, size = 16f))
            }
            cena(tx("eppegadinhas1_para_provar_ambiguidade_compare_duas"), who = PROF, mood = EpMood.HAPPY) { drop("sim", "nao") }
        },

        episodio(Skill.LEFT_RECURSION, tx("eppegadinhas1_recursao_escondida"), PEGADINHA) {
            cena(tx("eppegadinhas1_nenhuma_regra_aqui_comeca_com")) {
                add(titulo(tx("eppegadinhas1_sem_recursao")))
                add(codigo("g", listOf("S -> A a | b", "A -> S c | d"), 80f, x = 104f, passo = 30f, size = 17f))
            }
            cena(tx("eppegadinhas1_siga_primeira_opcao_vira")) {
                add(fileira("f", listOf("A" to Tint.ORANGE, "a" to Tint.TEAL), 210f, w = 56f))
            }
            pergunta(
                tx("eppegadinhas1_agora_troque_por_c_quem"),
                setOf("f0"), setOf("f1", "f2"),
                tx("eppegadinhas1_olhe_primeiro_simbolo_frase"),
                tx("eppegadinhas1_isso_chegou_em_sem_ler"),
            ) {
                drop("f0", "f1")
                add(fileira("f", listOf("S" to Tint.RED, "c" to Tint.TEAL, "a" to Tint.TEAL), 210f, w = 56f))
            }
            cena(tx("eppegadinhas1_conserto_comeca_colocando_opcoes_dentro")) {
                dropAll("f")
                add(Box("n", "S -> S c a | d a | b", 180f, 230f, 280f, 46f, Look.CELL, Tint.YELLOW, size = 17f))
                hot("n")
            }
            cena(tx("eppegadinhas1_agora_recursao_direta_receita_sempre"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.LEFT_FACTORING, tx("eppegadinhas1_quando_fatorar_nao_basta"), PEGADINHA) {
            cena(tx("eppegadinhas1_aqui_opcoes_comecam_diferente_com")) {
                add(titulo(tx("eppegadinhas1_sem_prefixo")))
                add(codigo("g", listOf("A -> a b | B", "B -> a c"), 80f, x = 110f, passo = 30f, size = 17f))
            }
            cena(tx("eppegadinhas1_mas_b_tambem_comeca_com")) { hot("g1") }
            pergunta(
                tx("eppegadinhas1_com_na_entrada_quantas_opcoes"),
                setOf("n2"), setOf("n1"),
                tx("eppegadinhas1_primeira_opcao_comeca_com_b"),
                tx("eppegadinhas1_isso_duas_opcoes_conflito_mesmo"),
            ) {
                hot()
                add(Box("n1", "uma", 110f, 230f, 110f, 56f, Look.TOKEN, Tint.TEAL, size = 18f))
                add(Box("n2", "duas", 250f, 230f, 110f, 56f, Look.TOKEN, Tint.ORANGE, size = 18f))
            }
            cena(tx("eppegadinhas1_primeiro_coloque_b_dentro_depois")) {
                drop("n1", "n2")
                add(codigo("c", listOf("A -> a b | a c", "A -> a X", "X -> b | c"), 220f, x = 110f, passo = 34f, size = 17f))
                hot("c0")
            }
            cena(tx("eppegadinhas1_fatorar_so_enxerga_que_esta"), who = PROF, mood = EpMood.HAPPY) { hot("c1", "c2") }
        },
    )
}

@Suppress("unused") private val keepAlvo = ::alvo
