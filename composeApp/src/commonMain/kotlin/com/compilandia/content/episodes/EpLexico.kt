package com.compilandia.content.episodes

import com.compilandia.content.fecho
import com.compilandia.content.Fecho
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
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.MiniFlex
import com.compilandia.engine.MiniLexer
import com.compilandia.engine.SmallNFA
import com.compilandia.engine.Token
import com.compilandia.engine.TokenClass
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/*
 * Aulas básicas do léxico. TOKENS, REGEX_DFA, NFA_TO_DFA e LEX_FLEX são aulas longas (A9), no molde do A2:
 * por que existe, o primeiro exemplo (as cenas da aula curta), a receita à vista e um segundo exemplo em que a pessoa calcula.
 * O que o segundo exemplo afirma vem do motor e é conferido em A9LexicoFactsTest.
 */

/** Linha do problema da aula de tokens: poucas palavras, muitos caracteres. */
val TOKENS_LINHA_PROBLEMA = "if (x1 >= 10)"

/** Linha do segundo exemplo da aula de tokens. */
val TOKENS_LINHA_2 = "while (i<=n) i++;"

/** Pedaço da linha do segundo exemplo em que a pessoa aponta os cortes. */
val TOKENS_PEDACO_2 = "i++;"

/** Nomes do problema da aula de expressões regulares. Só um deles não é nome de variável. */
val REGEX_NOMES = listOf("x1", "conta", "1x", "total2")

/** Expressão do segundo exemplo da aula de expressões regulares. */
val REGEX_2 = "ab*c"

/** AFN do primeiro exemplo da aula de subconjuntos: cadeias que terminam em ab. */
val AFN_1: SmallNFA by lazy { SmallNFA.bank.first { it.id == "ab" } }

/** AFN do segundo exemplo da aula de subconjuntos: ab* ou c, com setas épsilon. */
val AFN_2 = SmallNFA(
    "a9", "", 5, 0, setOf(2, 4),
    listOf(Triple(0, null, 1), Triple(0, null, 3), Triple(1, 'a', 2), Triple(2, 'b', 2), Triple(3, 'c', 4)),
)

/** Arquivo .l do segundo exemplo da aula do Flex, com a regra do while no lugar errado. */
val FLEX_2 = listOf(
    "[a-z]+    return ID;",
    "\"while\"   return WHILE;",
    "\"=\"       return ATRIB;",
    "\"==\"      return IGUAL;",
    "[0-9]+    return NUM;",
    "\" \"       ;",
)

/** O mesmo arquivo, com a regra do while no topo. */
val FLEX_2_CERTO = listOf(FLEX_2[1], FLEX_2[0]) + FLEX_2.drop(2)

/** Entrada do segundo exemplo da aula do Flex. */
val FLEX_2_ENTRADA = "while x==10"

private fun tintOf(c: TokenClass) = when (c) {
    TokenClass.ID -> Tint.TEAL
    TokenClass.NUM -> Tint.YELLOW
    TokenClass.OP -> Tint.ORANGE
    TokenClass.KEYWORD -> Tint.PINK
    TokenClass.DELIM -> Tint.BLUE
    TokenClass.STRING -> Tint.GREEN
}

/** Tokens lado a lado, cada um da largura do seu lexema. */
private fun fichas(prefix: String, toks: List<Token>, y: Float, size: Float = 13f): List<Box> {
    val ws = toks.map { maxOf(26f, it.lexeme.length * 10f + 12f) }
    val gap = 5f
    var x = 180f - (ws.sum() + gap * (ws.size - 1)) / 2
    return toks.mapIndexed { i, t ->
        val b = Box("$prefix$i", t.lexeme, x + ws[i] / 2, y, ws[i], 34f, Look.TOKEN, tintOf(t.cls), size = size)
        x += ws[i] + gap
        b
    }
}

/** Conjunto de estados como na prova: {0,1,3}. */
private fun conj(s: Set<Int>) = s.sorted().joinToString(",", "{", "}")

val lexicos by lazy {
    listOf(
        // ─────────────────────────────── TOKENS ───────────────────────────────
        episodio(Skill.TOKENS, tx("eplexico_cortar_linha")) {
            val src = "conta = 10 + x1;"
            val toks = MiniLexer.tokenize(src)
            val toks0 = MiniLexer.tokenize(TOKENS_LINHA_PROBLEMA)
            val toks2 = MiniLexer.tokenize(TOKENS_LINHA_2)
            // 1. Por que existe
            cena(tx("tok_l_01")) {
                add(titulo(tx("tok_l_titulo_porque")))
                add(fita(TOKENS_LINHA_PROBLEMA, 150f, cell = 24f))
            }
            pergunta(tx("tok_l_02"), setOf("n1"), setOf("n0", "n2"), tx("tok_l_02_dica"), tx("tok_l_02_ok")) {
                add(opcao("n0", "${toks0.size - 1}", 110f, 280f), opcao("n1", "${toks0.size}", 180f, 280f), opcao("n2", "${TOKENS_LINHA_PROBLEMA.length}", 250f, 280f))
            }
            cena(tx("tok_l_03")) {
                drop("n0", "n1", "n2")
                add(fichas("w", toks0, 280f, size = 14f))
            }
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("eplexico_para_computador_esta_linha_so")) {
                clear()
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
                add(rotulo("ll", tx("comum_kt_lexema"), 110f, 200f), rotulo("tl", "token", 260f, 200f, Tint.TEAL))
            }
            // 3. A receita
            cena(tx("tok_l_04")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("tok_l_r1"), 110f), regra("r2", tx("tok_l_r2"), 166f), regra("r3", tx("tok_l_r3"), 222f))
            }
            pergunta(tx("tok_l_05"), setOf("r2"), setOf("r1", "r3"), tx("tok_l_05_dica"), tx("tok_l_05_ok")) {
                hot()
                add(fileira("q", listOf("conta2" to Tint.TEAL), 310f, w = 130f))
            }
            cena(tx("tok_l_06")) {
                drop("q0")
                hot("r3")
                add(fichas("k", MiniLexer.tokenize("if ifx"), 310f, size = 16f))
            }
            // 4. Segundo exemplo, com perguntas
            cena(tx("eplexico_entram_letras_saem_tokens_agora")) {
                clear()
                add(titulo(tx("tok_l_titulo_outra")))
                add(fita(TOKENS_LINHA_2, 100f, cell = 20f))
            }
            pergunta(tx("tok_l_07"), setOf("kw"), setOf("id"), tx("tok_l_07_dica"), tx("tok_l_07_ok")) {
                for (i in 0 until 5) tint("c$i", Tint.PINK)
                add(Box("id", "ID", 100f, 200f, 120f, 44f, Look.TOKEN, Tint.TEAL, size = 16f))
                add(Box("kw", tx("comum_kt_palavra_chave"), 262f, 200f, 140f, 44f, Look.TOKEN, Tint.PINK, size = 16f))
            }
            pergunta(tx("tok_l_08"), setOf("h1", "h3"), setOf("h2"), tx("tok_l_08_dica"), tx("tok_l_08_ok")) {
                drop("id", "kw")
                add(fita(TOKENS_PEDACO_2, 260f, cell = 52f, prefix = "d"))
                val x0 = 180f - (TOKENS_PEDACO_2.length - 1) * 52f / 2
                for (i in 1 until TOKENS_PEDACO_2.length) add(Box("h$i", "", x0 + (i - 0.5f) * 52f, 260f, 10f, 92f, Look.GAP))
            }
            pergunta(tx("tok_l_09"), setOf("m1"), setOf("m0", "m2"), tx("tok_l_09_dica"), tx("tok_l_09_ok")) {
                dropAll("d"); dropAll("h")
                add(opcao("m0", "${toks2.size - 2}", 110f, 260f), opcao("m1", "${toks2.size}", 180f, 260f), opcao("m2", "${TOKENS_LINHA_2.length}", 250f, 260f))
            }
            cena(tx("tok_l_10"), mood = EpMood.HAPPY) {
                drop("m0", "m1", "m2")
                add(fichas("z", toks2, 220f))
                hot()
            }
            fecho(Fecho(
                tx("tok_f_af"), false, tx("tok_f_q"), tx("tok_f_dica"), tx("tok_f_ok"), tx("tok_f_certo"),
                tx("tok_f_erro2"), tx("tok_f_certo2"), tx("tok_f_fala2"),
                listOf(tx("tok_f_z1"), tx("tok_f_z2"), tx("tok_f_z3")), tx("tok_f_resumo"),
            ))
        },

        // ─────────────────────────────── REGEX E AFD ───────────────────────────────
        episodio(Skill.REGEX_DFA, tx("eplexico_trem_automato")) {
            val pos = listOf(80f to 200f, 280f to 200f)
            val d2 = DFA.fromRegex(REGEX_2)
            // 1. Por que existe
            cena(tx("regex_l_01")) {
                add(titulo(tx("regex_l_titulo_porque")))
                REGEX_NOMES.forEachIndexed { i, n ->
                    add(Box("n$i", n, if (i % 2 == 0) 110f else 250f, 150f + (i / 2) * 80f, 120f, 50f, Look.TOKEN, Tint.TEAL, size = 18f))
                }
            }
            val ruim = "n${REGEX_NOMES.indexOf("1x")}"
            pergunta(tx("regex_l_02"), setOf(ruim), REGEX_NOMES.indices.map { "n$it" }.toSet() - ruim, tx("regex_l_02_dica"), tx("regex_l_02_ok")) {}
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("eplexico_expressao_regular_descreve_formato_esta")) {
                clear()
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
            pergunta(tx("regex_l_03"), setOf("q1"), setOf("q0"), tx("regex_l_03_dica"), tx("regex_l_03_ok")) {
                hot("c2")
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
            // 3. A receita
            cena(tx("regex_l_04")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(
                    regra("r1", tx("regex_l_r1"), 100f), regra("r2", tx("regex_l_r2"), 152f),
                    regra("r3", tx("regex_l_r3"), 204f), regra("r4", tx("regex_l_r4"), 256f),
                )
            }
            pergunta(tx("regex_l_05"), setOf("r3"), setOf("r1", "r2", "r4"), tx("regex_l_05_dica"), tx("regex_l_05_ok")) {
                add(fita("aba", 330f, cell = 40f))
                tint("c2", Tint.RED)
            }
            cena(tx("regex_l_06")) {
                dropAll("c")
                add(rotulo("est", "b*  →  ε, b, bb, bbb...", 180f, 330f, Tint.YELLOW, 17f))
            }
            // 4. Segundo exemplo, com perguntas
            val pos2 = listOf(70f to 170f, 180f to 170f, 290f to 170f)
            cena(tx("regex_l_07")) {
                clear()
                add(titulo("a b* c"))
                add(estados(pos2.take(d2.stateCount)))
                for ((par, alvo) in d2.delta) wire("q${par.first}", "q$alvo", par.second.toString())
            }
            val finais = d2.accepting.map { "q$it" }.toSet()
            pergunta(tx("regex_l_08"), finais, (0 until d2.stateCount).map { "q$it" }.toSet() - finais, tx("regex_l_08_dica"), tx("regex_l_08_ok")) {}
            pergunta(tx("regex_l_09"), setOf("sim"), setOf("nao"), tx("regex_l_09_dica"), tx("regex_l_09_ok")) {
                for (f in finais) change(f) { it.copy(look = Look.ACCEPT) }
                add(fita("ac", 280f, cell = 40f))
                add(Box("sim", tx("eplexico_aceita"), 110f, 370f, 120f, 44f, Look.TOKEN, Tint.TEAL, size = 17f))
                add(Box("nao", tx("eplexico_rejeita"), 250f, 370f, 120f, 44f, Look.TOKEN, Tint.ORANGE, size = 17f))
            }
            pergunta(tx("regex_l_10"), setOf("nao"), setOf("sim"), tx("regex_l_10_dica"), tx("regex_l_10_ok")) {
                dropAll("c")
                add(fita("abcb", 280f, cell = 40f))
                tint("c3", Tint.RED)
            }
            cena(tx("regex_l_11"), mood = EpMood.HAPPY) {
                drop("sim", "nao")
                hot(*finais.toTypedArray())
            }
            fecho(Fecho(
                tx("regex_f_af"), false, tx("regex_f_q"), tx("regex_f_dica"), tx("regex_f_ok"), tx("regex_f_certo"),
                tx("regex_f_erro2"), tx("regex_f_certo2"), tx("regex_f_fala2"),
                listOf(tx("regex_f_z1"), tx("regex_f_z2"), tx("regex_f_z3")), tx("regex_f_resumo"),
            ))
        },

        // ─────────────────────────────── AFN → AFD ───────────────────────────────
        episodio(Skill.NFA_TO_DFA, tx("eplexico_muitos_caminhos_para")) {
            val pos = listOf(70f to 130f, 180f to 130f, 290f to 130f)
            val r1 = AFN_1.subsetConstruction()
            val r2 = AFN_2.subsetConstruction()
            // 1. Por que existe
            cena(tx("eplexico_num_afn_mesma_letra_pode")) {
                add(titulo(tx("eplexico_afn_termina_em_ab")))
                add(estados(pos, finais = setOf(2), nomes = listOf("0", "1", "2")))
                wire("q0", "q0", tx("comum_b_2_2"))
                wire("q0", "q1", "a", hot = true)
                wire("q1", "q2", "b")
            }
            pergunta(tx("afn_l_01"), setOf("duas"), setOf("fica", "vai"), tx("afn_l_01_dica"), tx("afn_l_01_ok")) {
                add(Box("fica", tx("afn_l_fica"), 70f, 250f, 100f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("vai", tx("afn_l_vai"), 180f, 250f, 100f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f))
                add(Box("duas", tx("afn_l_as_duas"), 290f, 250f, 100f, 40f, Look.TOKEN, Tint.YELLOW, size = 14f))
            }
            cena(tx("eplexico_computador_nao_adivinha_entao_gente"), mood = EpMood.THINK) {
                drop("fica", "vai", "duas")
                add(Box("s0", "{0}", 70f, 240f, 86f, 46f, Look.TOKEN, Tint.YELLOW, size = 16f))
            }
            // 2. Primeiro exemplo
            comecaExemplo()
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
                add(Box("s2", "{0,2}", 298f, 240f, 62f, 62f, Look.ACCEPT, Tint.GREEN, size = 13f))
                wire("s1", "s2", "b")
                hot("s2")
            }
            pergunta(tx("afn_l_02"), setOf("s1"), setOf("s0", "s2"), tx("afn_l_02_dica"), tx("afn_l_02_ok")) {}
            cena(tx("afn_l_03")) {
                // As setas que faltam, tiradas da construção do motor.
                val feitas = setOf(Triple(0, 'a', 1), Triple(1, 'b', 2))
                for (p in r1.steps) {
                    val de = r1.states.indexOf(p.from)
                    val para = r1.states.indexOf(p.to)
                    if (para < 0 || Triple(de, p.symbol, para) in feitas) continue
                    val curva = when { de == para -> 0f; para == de - 1 -> 26f; para < de -> -56f; else -> 0f }
                    wire("s$de", "s$para", p.symbol.toString(), bend = curva)
                }
                hot()
            }
            cena(tx("eplexico_pronto_nenhum_chute_caminho_so"), mood = EpMood.HAPPY) {}
            // 3. A receita
            cena(tx("afn_l_04")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(
                    regra("r1", tx("afn_l_r1"), 100f), regra("r2", tx("afn_l_r2"), 152f),
                    regra("r3", tx("afn_l_r3"), 204f), regra("r4", tx("afn_l_r4"), 256f),
                )
            }
            cena(tx("eplexico_quando_afn_tem_seta_epsilon"), who = PROF) { hot("r1", "r3") }
            pergunta(tx("afn_l_05"), setOf("r4"), setOf("r1", "r2", "r3"), tx("afn_l_05_dica"), tx("afn_l_05_ok")) { hot() }
            // 4. Segundo exemplo, com perguntas
            val pos2 = listOf(46f to 165f, 160f to 110f, 290f to 110f, 160f to 220f, 290f to 220f)
            cena(tx("afn_l_06")) {
                clear()
                add(titulo("AFN: ab* | c"))
                add(estados(pos2, finais = AFN_2.accepting, nomes = (0 until AFN_2.stateCount).map { "$it" }))
                for ((de, c, para) in AFN_2.edges) wire("q$de", "q$para", c?.toString() ?: "ε")
            }
            val fecho = r2.states[0]
            pergunta(
                tx("afn_l_07"), fecho.map { "q$it" }.toSet(), (0 until AFN_2.stateCount).filter { it !in fecho }.map { "q$it" }.toSet(),
                tx("afn_l_07_dica"), tx("afn_l_07_ok"),
            ) {}
            val comA = AFN_2.epsClosure(AFN_2.move(fecho, 'a'))
            val comC = AFN_2.epsClosure(AFN_2.move(fecho, 'c'))
            pergunta(tx("afn_l_08"), setOf("o1"), setOf("o2", "o3"), tx("afn_l_08_dica"), tx("afn_l_08_ok")) {
                hot()
                add(Box("d0", conj(fecho), 70f, 320f, 96f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
                add(opcao("o2", "{2,4}", 112f, 370f), opcao("o1", conj(comA), 190f, 370f), opcao("o3", "{1,2}", 268f, 370f))
                for (o in listOf("o1", "o2", "o3")) change(o) { it.copy(w = 62f) }
            }
            pergunta(tx("afn_l_09"), setOf("p1"), setOf("p2", "p3"), tx("afn_l_09_dica"), tx("afn_l_09_ok")) {
                drop("o1", "o2", "o3")
                add(Box("d1", conj(comA), 200f, 290f, 70f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
                wire("d0", "d1", "a")
                add(opcao("p2", "{3,4}", 112f, 370f), opcao("p3", "{2,4}", 190f, 370f), opcao("p1", conj(comC), 268f, 370f))
                for (o in listOf("p1", "p2", "p3")) change(o) { it.copy(w = 62f) }
            }
            val finaisD = r2.accepting.map { "d${listOf(fecho, comA, comC).indexOf(r2.states[it])}" }.toSet()
            pergunta(tx("afn_l_10"), finaisD, setOf("d0", "d1", "d2") - finaisD, tx("afn_l_10_dica"), tx("afn_l_10_ok")) {
                drop("p1", "p2", "p3")
                add(Box("d2", conj(comC), 200f, 370f, 70f, 44f, Look.TOKEN, Tint.YELLOW, size = 15f))
                wire("d0", "d2", "c")
            }
            cena(tx("afn_l_11"), mood = EpMood.HAPPY) {
                for (d in finaisD) { change(d) { it.copy(look = Look.ACCEPT) }; tint(d, Tint.GREEN) }
                wire("d1", "d1", "b")
                hot(*finaisD.toTypedArray())
            }
            fecho(Fecho(
                tx("afn_f_af"), false, tx("afn_f_q"), tx("afn_f_dica"), tx("afn_f_ok"), tx("afn_f_certo"),
                tx("afn_f_erro2"), tx("afn_f_certo2"), tx("afn_f_fala2"),
                listOf(tx("afn_f_z1"), tx("afn_f_z2"), tx("afn_f_z3")), tx("afn_f_resumo"),
            ))
        },

        // ─────────────────────────────── MINIMIZAÇÃO ───────────────────────────────

        // ─────────────────────────────── FLEX ───────────────────────────────
        episodio(Skill.LEX_FLEX, tx("eplexico_flex_escreve_scanner")) {
            val errado = MiniFlex.parseSpec(FLEX_2.joinToString("\n")).scan(FLEX_2_ENTRADA).tokens
            val certo = MiniFlex.parseSpec(FLEX_2_CERTO.joinToString("\n")).scan(FLEX_2_ENTRADA).tokens
            // 1. Por que existe
            cena(tx("flex_l_01")) {
                add(titulo(tx("flex_l_titulo_porque")))
                add(codigo("m", listOf("if (isalpha(c)) {", "  while (isalnum(c)) ...", "} else if (isdigit(c)) {", "  while (isdigit(c)) ...", "} else if (c == '=') ..."), 90f, x = 40f, passo = 36f, size = 14f))
            }
            pergunta(tx("flex_l_02"), setOf("m2"), setOf("m0", "m4"), tx("flex_l_02_dica"), tx("flex_l_02_ok")) {}
            // 2. Primeiro exemplo
            comecaExemplo()
            cena(tx("eplexico_fazer_automato_na_mao_cansa")) {
                clear()
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
            // 3. A receita
            cena(tx("flex_l_03")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(
                    regra("r1", tx("flex_l_r1"), 100f), regra("r2", tx("flex_l_r2"), 152f),
                    regra("r3", tx("flex_l_r3"), 204f), regra("r4", tx("flex_l_r4"), 256f),
                )
            }
            pergunta(tx("flex_l_04"), setOf("r4"), setOf("r1", "r2", "r3"), tx("flex_l_04_dica"), tx("flex_l_04_ok")) {
                add(codigo("q", listOf("[ \\t\\n]  ; /* ignora */"), 320f, x = 70f, size = 15f))
            }
            // 4. Segundo exemplo, com perguntas
            cena(tx("flex_l_05")) {
                clear()
                add(titulo(tx("comum_arquivo_l")))
                add(codigo("r", FLEX_2, 66f, x = 60f, passo = 32f, size = 14f))
                add(fita(FLEX_2_ENTRADA, 290f, cell = 26f))
            }
            pergunta(tx("flex_l_06"), setOf("oID"), setOf("oWH"), tx("flex_l_06_dica"), tx("flex_l_06_ok")) {
                for (i in 0 until 5) tint("c$i", Tint.YELLOW)
                add(Box("oID", errado[0].token, 110f, 366f, 110f, 42f, Look.TOKEN, Tint.TEAL, size = 16f))
                add(Box("oWH", "WHILE", 252f, 366f, 110f, 42f, Look.TOKEN, Tint.PINK, size = 16f))
            }
            val iguais = errado.first { it.text == "==" }.rule
            pergunta(tx("flex_l_07"), setOf("r$iguais"), (0 until FLEX_2.size).map { "r$it" }.toSet() - "r$iguais", tx("flex_l_07_dica"), tx("flex_l_07_ok")) {
                drop("oID", "oWH")
                for (i in 0 until 5) tint("c$i", Tint.NONE)
                tint("c7", Tint.YELLOW); tint("c8", Tint.YELLOW)
            }
            pergunta(tx("flex_l_08"), setOf("r1"), setOf("r0", "r2", "r3", "r4", "r5"), tx("flex_l_08_dica"), tx("flex_l_08_ok")) {
                tint("c7", Tint.NONE); tint("c8", Tint.NONE)
                hot()
            }
            cena(tx("flex_l_09"), mood = EpMood.HAPPY) {
                move("r0", 60f, 98f); move("r1", 60f, 66f)
                dropAll("c")
                add(fileira("tk", certo.map { it.token to if (it.token == "WHILE") Tint.PINK else Tint.TEAL }, 300f, w = 76f, size = 14f))
                hot("r1")
            }
            fecho(Fecho(
                tx("flex_f_af"), false, tx("flex_f_q"), tx("flex_f_dica"), tx("flex_f_ok"), tx("flex_f_certo"),
                tx("flex_f_erro2"), tx("flex_f_certo2"), tx("flex_f_fala2"),
                listOf(tx("flex_f_z1"), tx("flex_f_z2"), tx("flex_f_z3")), tx("flex_f_resumo"),
            ))
        },
    )
}
