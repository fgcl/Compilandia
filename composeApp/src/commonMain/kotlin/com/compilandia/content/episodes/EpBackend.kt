package com.compilandia.content.episodes

import com.compilandia.content.Box
import com.compilandia.content.EpMood
import com.compilandia.content.Look
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Tint
import com.compilandia.content.arvore
import com.compilandia.content.codigo
import com.compilandia.content.episodio
import com.compilandia.content.pilha
import com.compilandia.content.rotulo
import com.compilandia.content.titulo
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Mips
import com.compilandia.engine.ParseNode
import com.compilandia.engine.TacGen
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

val semantica by lazy {
    listOf(
        // ─────────────────────────────── ATRIBUTOS ───────────────────────────────
        episodio(Skill.ATTRIBUTES_TYPES, tx("epbackend_tipos_sobem_pela_arvore")) {
            val raiz = no("+", no("a"), no("*", no("x"), no("2")))
            val (b, w) = arvore(raiz, 96f, passoY = 80f, largura = 300f)
            fun tipo(id: String, t: String, alvo: Box) = Box(id, t, alvo.x, alvo.y + 26f, 56f, 20f, Look.LABEL, Tint.YELLOW, size = 14f)
            val node = b.associateBy { it.text }
            cena(tx("epbackend_arvore_ja_existe_agora_semantica")) {
                add(titulo(tx("comum_x_2")))
                add(b); for (x in w) wire(x)
                add(rotulo("info", tx("epbackend_int_x_float"), 180f, 330f, size = 17f))
            }
            cena(tx("epbackend_comeco_pelas_folhas_inteiro_x")) {
                add(tipo("ta", "int", node.getValue("a")), tipo("tx", "float", node.getValue("x")), tipo("t2", "int", node.getValue("2")))
            }
            pergunta(
                tx("epbackend_real_vezes_inteiro_que_toque"),
                setOf("of"), setOf("oi"),
                tx("epbackend_tipo_maior_ganha_para_nao"),
                tx("epbackend_isso_real_com_inteiro_real"),
            ) {
                add(Box("oi", "int", 120f, 380f, 100f, 42f, Look.TOKEN, Tint.TEAL, size = 17f), Box("of", "float", 240f, 380f, 100f, 42f, Look.TOKEN, Tint.ORANGE, size = 17f))
                hot("t1")
            }
            cena(tx("epbackend_dois_convertido_para_real_resultado")) {
                drop("oi", "of")
                add(tipo("tm", "float", node.getValue("*")))
                hot()
            }
            cena(tx("epbackend_na_raiz_inteiro_mais_real")) {
                add(tipo("tr", "float", node.getValue("+")))
                hot("t")
            }
            cena(tx("epbackend_se_tipos_nao_combinam_compilador"), who = PROF) { hot() }
        },

        // ─────────────────────────────── ESCOPO ───────────────────────────────
        episodio(Skill.SCOPE_SYMBOLS, tx("epbackend_quem_esse_x")) {
            val src = listOf("int x;", "void f() {", "  float x;", "  x = 1.5;", "}", "x = 2;")
            cena(tx("epbackend_mesmo_nome_pode_existir_em")) {
                add(titulo(tx("epbackend_escopo")))
                add(codigo("c", src, 62f, passo = 27f, size = 15f))
                add(pilha("p", listOf("global: x int"), 180f, 370f, w = 220f, h = 38f, tint = Tint.TEAL))
            }
            cena(tx("epbackend_entrou_num_bloco_empilha_tabela")) {
                hot("c1")
                add(Box("p1", "f: x float", 180f, 328f, 220f, 38f, Look.TOKEN, Tint.ORANGE))
            }
            pergunta(
                tx("epbackend_na_linha_x_igual_meio"),
                setOf("p1"), setOf("p0"),
                tx("epbackend_busca_comeca_na_tabela_topo"),
                tx("epbackend_isso_x_dentro_esconde_fora"),
            ) { hot("c3") }
            cena(tx("epbackend_busca_comeca_no_topo_desce")) { hot("p1", "p0") }
            cena(tx("epbackend_quando_bloco_acaba_tabela_sai")) {
                drop("p1")
                hot("c4")
            }
            cena(tx("epbackend_por_isso_x_igual_dois"), mood = EpMood.HAPPY) { hot("c5", "p0") }
        },

        // ─────────────────────────────── AST ───────────────────────────────
    )
}

val backend by lazy {
    listOf(
        // ─────────────────────────────── TAC ───────────────────────────────
        episodio(Skill.TAC, tx("epbackend_conta_por_linha")) {
            val code = TacGen.generate(ExprParser.parse(tx("comum_b_c")))
            val (b, w) = arvore(no("+", no("a"), no("*", no("b"), no("c"))), 86f, passoY = 62f, largura = 240f)
            cena(tx("epbackend_maquina_nao_entende_conta_comprida")) {
                add(titulo(tx("comum_b_c")))
                add(b); for (x in w) wire(x)
            }
            pergunta(
                tx("epbackend_qual_conta_tem_que_sair"),
                setOf("t1"), setOf("t"),
                tx("epbackend_arvore_manda_quem_esta_mais"),
                tx("epbackend_isso_vezes_esta_mais_fundo"),
            ) {}
            cena(tx("epbackend_guardo_resultado_num_temporario_t1")) {
                add(codigo("c", listOf(code[0].toString()), 300f, x = 110f, size = 18f))
                hot("c0", "t1")
            }
            cena(tx("epbackend_na_linha_seguinte_arvore_ja")) {
                add(codigo("d", listOf(code[1].toString()), 332f, x = 110f, size = 18f))
                hot("d0", "t")
            }
            cena(tx("epbackend_cada_linha_tem_no_maximo"), who = PROF) { hot() }
            cena(tx("epbackend_ordem_saiu_arvore_parser_ja"), mood = EpMood.HAPPY) {}
        },

        // ─────────────────────────────── OTIMIZAÇÃO ───────────────────────────────
        episodio(Skill.OPTIMIZATION, tx("epbackend_conta_feita_antes")) {
            val parts = listOf("x", "=", "2", "*", "3", "+", "y")
            fun item(i: Int, t: String, x: Float) = Box("o$i", t, x, 140f, 40f, 48f, Look.TOKEN, Tint.TEAL, size = 18f)
            cena(tx("epbackend_se_para_fazer_conta_agora")) {
                add(titulo(tx("comum_dobra_constantes")))
                parts.forEachIndexed { i, t -> add(item(i, t, 48f + i * 44f)) }
            }
            pergunta(
                tx("epbackend_toque_na_parte_que_compilador"),
                setOf("o3"), setOf("o5", "o0"),
                tx("epbackend_procure_conta_so_com_numeros"),
                tx("epbackend_isso_dois_vezes_tres_nao"),
            ) {}
            cena(tx("epbackend_dois_vezes_tres_vira_seis"), mood = EpMood.HAPPY) {
                drop("o3", "o4")
                text("o2", "6"); tint("o2", Tint.GREEN); move("o2", 180f, 140f)
                move("o5", 224f, 140f); move("o6", 268f, 140f)
                hot("o2")
            }
            cena(tx("epbackend_y_fica_valor_dele_so")) { hot("o6") }
            cena(tx("epbackend_otimizar_nunca_pode_mudar_resultado"), who = PROF) { hot() }
        },

        // ─────────────────────────────── REGISTROS DE ATIVAÇÃO ───────────────────────────────
        episodio(Skill.RUNTIME, tx("epbackend_pilha_chamadas")) {
            fun reg(id: String, t: String, i: Int, c: Tint) = Box(id, t, 180f, 330f - i * 62f, 250f, 54f, Look.TOKEN, c, size = 15f)
            cena(tx("epbackend_toda_chamada_funcao_ganha_registro")) {
                add(titulo("fat(2)"))
                add(reg("r0", tx("epbackend_fat_n_2_volta_main"), 0, Tint.TEAL))
            }
            cena(tx("epbackend_ali_moram_parametros_variaveis_locais")) { hot("r0") }
            cena(tx("epbackend_fat_dois_chama_fat_outro")) {
                add(reg("r1", tx("epbackend_fat_n_1_volta_fat"), 1, Tint.ORANGE))
                hot("r1")
            }
            cena(tx("epbackend_mais_chamada_agora_com_n")) {
                add(reg("r2", tx("epbackend_fat_n_0_volta_fat"), 2, Tint.YELLOW))
                hot("r2")
            }
            pergunta(
                tx("comum_quem_retorna_primeiro_toque_no"),
                setOf("r2"), setOf("r0", "r1"),
                tx("epbackend_so_topo_pilha_esta_rodando"),
                tx("comum_isso_ultimo_entrar_primeiro_sair"),
            ) { hot() }
            cena(tx("epbackend_retornou_registro_sai_controle_volta")) {
                drop("r2")
                hot("r1")
            }
            cena(tx("epbackend_por_isso_recursao_funciona_cada"), mood = EpMood.HAPPY) { hot() }
        },

        // ─────────────────────────────── MIPS ───────────────────────────────

        // ─────────────────────────────── REGISTRADORES ───────────────────────────────
        episodio(Skill.REG_ALLOC, tx("epbackend_colorir_grafo")) {
            fun v(id: String, x: Float, y: Float) = Box(id, id, x, y, 56f, 56f, Look.STATE, Tint.NONE, size = 18f)
            cena(tx("epbackend_registrador_pouco_variavel_muita_vamos")) {
                add(titulo(tx("epbackend_alocacao")))
                add(v("a", 80f, 140f), v("b", 180f, 240f), v("c", 280f, 140f))
            }
            cena(tx("epbackend_duas_variaveis_vivas_ao_mesmo")) {
                wire("a", "b", arrow = false)
                wire("b", "c", arrow = false)
            }
            cena(tx("epbackend_liguei_quem_convive_esse_grafo"), who = PROF) {}
            pergunta(
                tx("epbackend_quais_duas_podem_dividir_registrador"),
                setOf("a", "c"), setOf("b"),
                tx("epbackend_procure_duas_que_nao_tem"),
                tx("epbackend_isso_sem_fio_entre_elas"),
            ) {}
            cena(tx("epbackend_pinto_grafo_vizinhos_nunca_tem"), mood = EpMood.HAPPY) {
                tint("a", Tint.TEAL); tint("c", Tint.TEAL); tint("b", Tint.ORANGE)
                add(rotulo("ra", "\$t0", 80f, 186f, Tint.TEAL, 17f), rotulo("rb", "\$t1", 180f, 286f, Tint.ORANGE, 17f), rotulo("rc", "\$t0", 280f, 186f, Tint.TEAL, 17f))
            }
            cena(tx("epbackend_faltou_cor_variavel_vai_para")) {}
        },
    )
}
