package com.compilandia.content.episodes

import com.compilandia.content.Actor
import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.estados
import com.compilandia.content.fileira
import com.compilandia.content.fita
import com.compilandia.content.rotulo
import com.compilandia.content.tabela
import com.compilandia.content.titulo
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.TokenClass
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

private fun tintOf(c: TokenClass) = when (c) {
    TokenClass.ID -> Tint.TEAL
    TokenClass.NUM -> Tint.YELLOW
    TokenClass.OP -> Tint.ORANGE
    TokenClass.KEYWORD -> Tint.PINK
    TokenClass.DELIM -> Tint.BLUE
    TokenClass.STRING -> Tint.GREEN
}

val lexicos by lazy {
    listOf(
        // ─────────────────────────────── TOKENS ───────────────────────────────
        episodio(Skill.TOKENS, tx("eplexico_cortar_linha")) {
            val src = "conta = 10 + x1;"
            val toks = MiniLexer.tokenize(src)
            cena(tx("eplexico_para_computador_esta_linha_so")) {
                add(titulo(tx("eplexico_scanner")))
                add(fita(src, 92f))
            }
            cena(tx("eplexico_scanner_anda_por_ela_junta")) {
                add(fileira("tk", toks.map { it.lexeme to tintOf(it.cls) }, 190f, w = 50f, h = 36f, size = 14f))
                add(rotulo("e", tx("eplexico_entram_letras"), 90f, 136f), rotulo("s", tx("eplexico_saem_tokens"), 90f, 238f, Tint.YELLOW))
            }
            cena(tx("eplexico_cada_token_cai_na_caixa")) {
                drop("e", "s")
                add(
                    Box("bID", "ID", 70f, 310f, 110f, 120f, Look.BIN),
                    Box("bNUM", "NUM", 180f, 310f, 100f, 120f, Look.BIN),
                    Box("bOP", "OP", 290f, 310f, 110f, 120f, Look.BIN),
                )
                move("tk0", 70f, 262f); move("tk4", 70f, 306f)
                move("tk2", 180f, 262f)
                move("tk1", 262f, 262f); move("tk3", 318f, 262f); move("tk5", 290f, 306f)
                actor(Actor(180f, 200f, 0.8f))
            }
            cena(tx("eplexico_espacos_caem_fora_aqui_nenhuma")) {
                dropAll("c")
                add(fita("conta=10+x1;", 92f))
                actor(null)
            }
            pergunta(
                tx("eplexico_sua_vez_toque_nos_dois"),
                setOf("g1", "g3"), setOf("g2", "g4"),
                tx("eplexico_maior_ou_igual_fica_inteiro"),
                tx("eplexico_isso_tres_tokens_x_maior"),
            ) {
                clear()
                add(titulo(tx("eplexico_onde_corta")))
                add(fita("x>=10", 150f, cell = 52f))
                for (i in 1..4) add(Box("g$i", "", 76f + (i - 0.5f) * 52f, 150f, 10f, 92f, Look.GAP))
            }
            cena(tx("eplexico_essa_regra_maior_casamento_por")) {
                clear()
                add(titulo(tx("comum_maior_casamento")))
                add(fileira("a", listOf(">" to Tint.NONE, "=" to Tint.NONE), 110f, w = 44f))
                add(fileira("b", listOf(">=" to Tint.ORANGE), 180f, w = 100f))
                add(fileira("d", listOf("conta2" to Tint.TEAL), 250f, w = 130f))
            }
            cena(tx("eplexico_texto_exato_lexema_classe_dele")) {
                clear()
                add(titulo(tx("eplexico_lexema_token")))
                add(Box("l", "x1", 110f, 150f, 110f, 56f, Look.CELL, size = 20f))
                add(Box("t", "ID", 260f, 150f, 110f, 56f, Look.TOKEN, Tint.TEAL, size = 20f))
                wire("l", "t")
                add(rotulo("ll", "lexema", 110f, 200f), rotulo("tl", "token", 260f, 200f, Tint.TEAL))
            }
            cena(tx("eplexico_entram_letras_saem_tokens_agora"), mood = EpMood.HAPPY) { hot("t") }
        },

        // ─────────────────────────────── REGEX E AFD ───────────────────────────────
        episodio(Skill.REGEX_DFA, tx("eplexico_trem_automato")) {
            val pos = listOf(80f to 200f, 280f to 200f)
            cena(tx("eplexico_expressao_regular_descreve_formato_esta")) {
                add(titulo(tx("eplexico_b")))
            }
            cena(tx("eplexico_automato_maquina_que_confere_esse")) {
                add(estados(pos, finais = setOf(1)))
                wire("q0", "q1", "a")
                wire("q1", "q1", "b")
            }
            cena(tx("eplexico_trem_comeca_no_primeiro_estado")) {
                add(fita("abb", 320f, cell = 40f))
                actor(Actor(80f, 182f, 0.72f))
                hot("c0")
            }
            cena(tx("eplexico_leu_trem_anda_para_outro")) {
                actor(Actor(280f, 182f, 0.72f))
                hot("c1")
                hotWire("q0", "q1")
            }
            cena(tx("eplexico_leu_b_volta_no_laco")) {
                hot("c2")
                hotWire("q1", "q1")
            }
            cena(tx("eplexico_acabou_cadeia_num_circulo_duplo"), mood = EpMood.HAPPY) {
                hot("q1")
            }
            pergunta(
                tx("eplexico_cadeia_b_trem_sai_primeiro"),
                setOf("nao"), setOf("sim"),
                tx("eplexico_olhe_setas_que_saem_primeiro"),
                tx("eplexico_certo_sem_seta_para_b"),
            ) {
                dropAll("c")
                actor(Actor(80f, 182f, 0.72f))
                add(fita("ba", 320f, cell = 40f))
                add(Box("sim", tx("eplexico_aceita"), 110f, 90f, 120f, 50f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("nao", tx("eplexico_rejeita"), 250f, 90f, 120f, 50f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            cena(tx("eplexico_assim_que_scanner_reconhece_nomes"), mood = EpMood.HAPPY) {
                drop("sim", "nao")
                actor(null)
            }
        },

        // ─────────────────────────────── AFN → AFD ───────────────────────────────
        episodio(Skill.NFA_TO_DFA, tx("eplexico_muitos_caminhos_para")) {
            val pos = listOf(70f to 130f, 180f to 130f, 290f to 130f)
            cena(tx("eplexico_num_afn_mesma_letra_pode")) {
                add(titulo(tx("eplexico_afn_termina_em_ab")))
                add(estados(pos, finais = setOf(2), nomes = listOf("0", "1", "2")))
                wire("q0", "q0", tx("comum_b_2_2"))
                wire("q0", "q1", "a", hot = true)
                wire("q1", "q2", "b")
            }
            cena(tx("eplexico_computador_nao_adivinha_entao_gente"), mood = EpMood.THINK) {
                add(Box("s0", "{0}", 70f, 240f, 86f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            cena(tx("eplexico_conjunto_zero_lendo_posso_estar")) {
                add(Box("s1", "{0,1}", 180f, 240f, 86f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
                wire("s0", "s1", "a")
                hot("s1")
            }
            pergunta(
                tx("eplexico_conjunto_zero_lendo_b_para"),
                setOf("o2"), setOf("o1", "o3"),
                tx("eplexico_veja_cada_estado_conjunto_com"),
                tx("eplexico_isso_juntando_dois_caminhos_zero"),
            ) {
                add(Box("o1", "{0}", 80f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
                add(Box("o2", "{0,2}", 180f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
                add(Box("o3", "{2}", 280f, 340f, 86f, 46f, Look.TOKEN, Tint.NONE, size = 16f))
            }
            cena(tx("eplexico_cada_conjunto_novo_vira_estado")) {
                drop("o1", "o2", "o3")
                add(Box("s2", "{0,2}", 290f, 240f, 86f, 46f, Look.ACCEPT, Tint.GREEN, size = 13f))
                wire("s1", "s2", "b")
                hot("s2")
            }
            cena(tx("eplexico_quando_afn_tem_seta_epsilon"), who = PROF) { hot() }
            cena(tx("eplexico_pronto_nenhum_chute_caminho_so"), mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── MINIMIZAÇÃO ───────────────────────────────

        // ─────────────────────────────── FLEX ───────────────────────────────
        episodio(Skill.LEX_FLEX, tx("eplexico_flex_escreve_scanner")) {
            cena(tx("eplexico_fazer_automato_na_mao_cansa")) {
                add(titulo(tx("comum_arquivo_l")))
                add(codigo("r", listOf("\"if\"      return IF;", "[a-z]+    return ID;", "[0-9]+    return NUM;", "[ \\t\\n]  ; /* ignora */"), 80f, passo = 34f, size = 14f))
            }
            cena(tx("eplexico_cada_linha_tem_padrao_acao")) { hot("r1") }
            pergunta(
                tx("eplexico_texto_if_casa_com_primeira"),
                setOf("r0"), setOf("r1"),
                tx("eplexico_com_tamanhos_iguais_ordem_arquivo"),
                tx("eplexico_isso_no_empate_vence_regra"),
            ) {
                add(fileira("e", listOf("if" to Tint.YELLOW), 260f, w = 90f))
                hot()
            }
            cena(tx("eplexico_texto_ifa_agora_segunda_regra"), mood = EpMood.THINK) {
                drop("e0")
                add(fileira("e", listOf("ifa" to Tint.TEAL), 260f, w = 90f))
                hot("r1")
            }
            cena(tx("eplexico_guarde_duas_regras_primeiro_maior")) {
                add(rotulo("a", tx("eplexico_1_maior_casamento"), 180f, 320f, Tint.YELLOW, 17f), rotulo("b", tx("eplexico_2_empate_cima"), 180f, 350f, Tint.YELLOW, 17f))
                hot()
            }
            cena(tx("eplexico_por_isso_palavras_reservadas_vem"), mood = EpMood.HAPPY) { hot("r0") }
        },
    )
}
