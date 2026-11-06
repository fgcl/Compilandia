package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.PEGADINHA
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.fileira
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Pegadinhas de prova: semântica e geração de código. */
val pegadinhasFim by lazy {
    listOf(
        episodio(Skill.ATTRIBUTES_TYPES, tx("eppegadinhas3_sintetizado_sobe_herdado_desce"), PEGADINHA) {
            val d = Box("d", "D", 180f, 86f, 70f, 40f, Look.NODE, Tint.ORANGE, size = 16f)
            val t = Box("t", "T: int", 90f, 180f, 100f, 40f, Look.NODE, Tint.ORANGE, size = 15f)
            val l = Box("l", "L", 270f, 180f, 80f, 40f, Look.NODE, Tint.ORANGE, size = 16f)
            val a = Box("a", "a", 220f, 276f, 60f, 40f, Look.NODE, Tint.TEAL, size = 16f)
            val b = Box("b", "b", 320f, 276f, 60f, 40f, Look.NODE, Tint.TEAL, size = 16f)
            cena(tx("eppegadinhas3_declaracao_int_b_tipo_esta")) {
                add(titulo("int a, b", y = 30f))
                add(d, t, l, a, b)
                wire("d", "t", arrow = false); wire("d", "l", arrow = false); wire("l", "a", arrow = false); wire("l", "b", arrow = false)
            }
            cena(tx("eppegadinhas3_em_mais_b_tipo_sobe"), who = PROF) {}
            pergunta(
                tx("eppegadinhas3_aqui_diferente_para_saber_tipo"),
                setOf("t"), setOf("l", "b"),
                tx("eppegadinhas3_int_esta_no_galho_lado"),
                tx("eppegadinhas3_isso_int_passa_pelo_pai"),
            ) { hot() }
            cena(tx("eppegadinhas3_herdado_vem_pai_ou_irmao")) { hot("t", "l", "a", "b") }
            palavra(tx("palavra_herdado"), tx("eppegadinhas3_atribuida_usa_so_sintetizado_l"), who = PROF, mood = EpMood.HAPPY)
        },

        episodio(Skill.SCOPE_SYMBOLS, tx("eppegadinhas3_sombra_variavel"), PEGADINHA) {
            cena(tx("eppegadinhas3_dois_x_fora_dentro_dentro")) {
                add(titulo(tx("eppegadinhas3_dois_x")))
                add(codigo("c", listOf("int x = 1;", "{", "  int x = 2;", "  print(x);", "}", "print(x);"), 76f, x = 90f, passo = 32f, size = 16f))
            }
            pergunta(
                tx("eppegadinhas3_print_dentro_enxerga_qual_x"),
                setOf("c2"), setOf("c0"),
                tx("eppegadinhas3_busca_comeca_no_bloco_mais"),
                tx("eppegadinhas3_isso_ele_acha_x_dentro"),
            ) { hot("c3") }
            pergunta(
                tx("eppegadinhas3_bloco_fechou_print_fora_enxerga"),
                setOf("c0"), setOf("c2"),
                tx("eppegadinhas3_quando_bloco_fecha_nomes_dele"),
                tx("eppegadinhas3_isso_sobrou_so_fora_que"),
            ) { hot("c5") }
            cena(tx("eppegadinhas3_pegadinha_achar_que_x_igual"), who = PROF) { hot("c2", "c0") }
            cena(tx("eppegadinhas3_saida_programa_dois_depois"), mood = EpMood.HAPPY) {
                add(fileira("s", listOf("2" to Tint.GREEN, "1" to Tint.GREEN), 310f, w = 70f, h = 50f, size = 20f))
                hot()
            }
        },

        episodio(Skill.TAC, tx("eppegadinhas3_ordem_dos_temporarios"), PEGADINHA) {
            cena(tx("eppegadinhas3_tres_enderecos_no_maximo_conta")) {
                add(titulo(tx("comum_b_c")))
                add(fileira("e", listOf("a" to Tint.TEAL, "+" to Tint.ORANGE, "b" to Tint.TEAL, "*" to Tint.ORANGE, "c" to Tint.TEAL), 110f, w = 56f, h = 46f, size = 18f))
            }
            pergunta(
                tx("eppegadinhas3_qual_conta_vira_primeira_linha"),
                setOf("e3"), setOf("e1"),
                tx("eppegadinhas3_quem_esta_mais_fundo_na"),
                tx("eppegadinhas3_isso_vezes_vai_primeiro_guarda"),
            ) { hot() }
            cena(tx("eppegadinhas3_primeira_linha_t1_recebe_b")) {
                add(codigo("l", listOf("t1 = b * c"), 190f, x = 110f, passo = 34f, size = 18f))
                hot("l0")
            }
            cena(tx("eppegadinhas3_segunda_linha_t2_recebe_mais")) {
                add(codigo("l", listOf("t1 = b * c", tx("comum_t2_t1")), 190f, x = 110f, passo = 34f, size = 18f))
                hot("l1")
            }
            cena(tx("eppegadinhas3_se_inverter_linhas_t1_lido"), who = PROF, mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.OPTIMIZATION, tx("eppegadinhas3_otimizar_sem_mudar_resultado"), PEGADINHA) {
            cena(tx("eppegadinhas3_otimizar_trocar_codigo_por_outro")) {
                add(titulo(tx("eppegadinhas3_pode_trocar")))
                add(Box("a", tx("eppegadinhas3_2_3_vira_6"), 180f, 110f, 280f, 46f, Look.CELL, Tint.GREEN, size = 16f))
            }
            cena(tx("eppegadinhas3_multiplicar_por_nao_muda_nada")) {
                add(Box("b", tx("eppegadinhas3_x_1_vira_x"), 180f, 166f, 280f, 46f, Look.CELL, Tint.GREEN, size = 16f))
            }
            pergunta(
                tx("eppegadinhas3_destas_trocas_nao_vale_toque"),
                setOf("d"), setOf("c"),
                tx("eppegadinhas3_pense_em_x_valendo_zero"),
                tx("eppegadinhas3_isso_com_x_zero_programa"),
            ) {
                add(Box("c", tx("eppegadinhas3_x_0_vira_x"), 180f, 222f, 280f, 46f, Look.CELL, size = 16f))
                add(Box("d", tx("eppegadinhas3_x_x_vira_1"), 180f, 278f, 280f, 46f, Look.CELL, size = 16f))
            }
            cena(tx("eppegadinhas3_otimizacao_nao_pode_mudar_que"), who = PROF) { tint("d", Tint.RED); hot("d") }
            cena(tx("eppegadinhas3_na_duvida_compilador_deixa_como"), mood = EpMood.HAPPY) { hot() }
        },

        episodio(Skill.RUNTIME, tx("eppegadinhas3_cada_chamada_registro"), PEGADINHA) {
            cena(tx("eppegadinhas3_funcao_recursiva_nao_reaproveita_mesmo")) {
                add(titulo("fat(3)"))
                add(pilha("f", listOf("fat(3)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            cena(tx("eppegadinhas3_fat_tres_chama_fat_dois")) {
                add(pilha("f", listOf("fat(3)", "fat(2)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            cena(tx("eppegadinhas3_fat_dois_chama_fat_tres")) {
                add(pilha("f", listOf("fat(3)", "fat(2)", "fat(1)"), 180f, 320f, w = 170f, h = 46f, tint = Tint.TEAL))
            }
            pergunta(
                tx("comum_quem_retorna_primeiro_toque_no"),
                setOf("f2"), setOf("f0", "f1"),
                tx("eppegadinhas3_pilha_so_tira_topo"),
                tx("comum_isso_ultimo_entrar_primeiro_sair"),
            ) { hot() }
            cena(tx("eppegadinhas3_ao_retornar_registro_some_com"), who = PROF, mood = EpMood.HAPPY) {
                drop("f2")
            }
        },

        episodio(Skill.CODEGEN_MIPS, tx("eppegadinhas3_if_vira_desvio"), PEGADINHA) {
            cena(tx("eppegadinhas3_mips_nao_tem_if_tem")) {
                add(titulo("if (a < b) x = 1;", size = 22f))
                add(codigo("c", listOf("lw  \$t0, a", "lw  \$t1, b", "bge \$t0, \$t1, fim", "li  \$t2, 1", "sw  \$t2, x", "fim:"), 80f, x = 76f, passo = 32f, size = 15f))
            }
            cena(tx("eppegadinhas3_carrega_b_nos_registradores")) { hot("c0", "c1") }
            cena(tx("eppegadinhas3_aqui_esta_truque_if_pede")) { hot("c2") }
            pergunta(
                tx("eppegadinhas3_se_for_maior_ou_igual"),
                setOf("c5"), setOf("c3", "c4"),
                tx("eppegadinhas3_com_condicao_falsa_corpo_if"),
                tx("eppegadinhas3_isso_ele_pula_corpo_cai"),
            ) { hot() }
            cena(tx("eppegadinhas3_teste_invertido_desvio_descreve_quando"), who = PROF, mood = EpMood.HAPPY) { hot("c2", "c5") }
        },

        episodio(Skill.REG_ALLOC, tx("eppegadinhas3_quem_vive_junto_briga"), PEGADINHA) {
            cena(tx("eppegadinhas3_dois_valores_vivos_ao_mesmo")) {
                add(titulo(tx("eppegadinhas3_vivos_juntos")))
                add(codigo("c", listOf(tx("comum_t1_b"), "t2 = c + d", "t3 = t1 * t2"), 80f, x = 100f, passo = 30f, size = 16f))
            }
            cena(tx("eppegadinhas3_t1_nasce_na_primeira_linha")) { hot("c0", "c2") }
            pergunta(
                tx("eppegadinhas3_quem_esta_vivo_junto_com"),
                setOf("n2"), setOf("n3"),
                tx("eppegadinhas3_veja_quem_ja_nasceu_ainda"),
                tx("eppegadinhas3_isso_t1_t2_sao_vizinhos"),
            ) {
                hot()
                add(Box("n1", "t1", 90f, 270f, 56f, 56f, Look.STATE, Tint.TEAL, size = 16f))
                add(Box("n2", "t2", 200f, 270f, 56f, 56f, Look.STATE, size = 16f))
                add(Box("n3", "t3", 300f, 270f, 56f, 56f, Look.STATE, size = 16f))
            }
            cena(tx("eppegadinhas3_t3_nasce_quando_t1_t2")) {
                wire("n1", "n2", arrow = false, hot = true)
                tint("n2", Tint.ORANGE); tint("n3", Tint.TEAL)
            }
            palavra(tx("eppegadinhas3_interferencia"), tx("eppegadinhas3_dois_registradores_bastam_colorir_grafo"), who = PROF, mood = EpMood.HAPPY)
        },
    )
}

@Suppress("unused") private val keepRotulo = ::rotulo
