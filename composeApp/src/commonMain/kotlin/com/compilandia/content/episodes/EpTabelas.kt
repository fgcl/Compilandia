package com.compilandia.content.episodes

import com.compilandia.content.EpMood
import com.compilandia.content.Speaker.PROF
import com.compilandia.content.Temporada.TABELAS
import com.compilandia.content.Tint
import com.compilandia.content.ToyClosure
import com.compilandia.content.ToyTabelaViva
import com.compilandia.content.alturaRegistro
import com.compilandia.content.bolinhasDoAfd
import com.compilandia.content.caixasDeItens
import com.compilandia.content.caixasDoAfd
import com.compilandia.content.celulaAfd
import com.compilandia.content.codigo
import com.compilandia.content.emOrdem
import com.compilandia.content.episodio
import com.compilandia.content.fita
import com.compilandia.content.linhaQueComeca
import com.compilandia.content.linhasDoEstado
import com.compilandia.content.registro
import com.compilandia.content.rotulo
import com.compilandia.content.tabelaDoAfd
import com.compilandia.content.titulo
import com.compilandia.engine.DFA
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRAction
import com.compilandia.engine.LRAutomaton
import com.compilandia.engine.MiniBison
import com.compilandia.engine.RtEvent
import com.compilandia.engine.RuntimeSim
import com.compilandia.engine.isComplete
import com.compilandia.engine.render
import com.compilandia.learning.Skill
import com.compilandia.textos.tx

/** Spec da calculadora usada nas aulas de Bison desta temporada e da série em C. */
internal val SPEC_CALC = listOf(
    "%token NUM",
    "%left '+'",
    "%left '*'",
    "%%",
    "e: e '+' e { \$\$ = \$1 + \$3; }",
    " | e '*' e { \$\$ = \$1 * \$3; }",
    " | NUM ;",
)

/** Temporada Tabelas: o mesmo assunto desenhado como caixas com compartimentos, igual à prova e às ferramentas. */
val tabelas by lazy {
    listOf(
        // ─────────────────────────────── AFD em caixas e em tabela ───────────────────────────────
        episodio(Skill.REGEX_DFA, tx("eptabelas_automato_vira_tabela"), TABELAS) {
            val d = DFA.fromRegex("(a|b)*abb").minimize().emOrdem()
            val pos = listOf(70f to 120f, 290f to 120f, 290f to 300f, 70f to 300f)
            val (bol, fiosBol) = bolinhasDoAfd(d, pos)
            val (cx, fiosCx) = caixasDoAfd(d, pos)
            cena(tx("eptabelas_este_automato_aceita_palavras_que")) {
                add(titulo("(a|b)*abb"))
                add(bol); wires(fiosBol)
            }
            cena(tx("eptabelas_agora_cada_estado_vira_caixa")) {
                clearWires()
                add(cx); wires(fiosCx)
                hot("q0")
            }
            cena(tx("eptabelas_fio_sai_linha_certa_linha")) {
                hot("q1"); lit("q1", 1); hotRow("q1", 1)
            }
            pergunta(
                tx("eptabelas_estou_no_q2_chegou_b"),
                setOf("q3"), setOf("q0", "q1", "q2"),
                tx("eptabelas_procure_linha_b_dentro_caixa"),
                tx("eptabelas_isso_b_q2_leva_ao"),
            ) { lit("q1", -1); lit("q2", 1); hot("q2"); hotRow("q2", 1) }
            cena(tx("eptabelas_junte_caixas_cada_vira_linha"), mood = EpMood.SURPRISED) {
                clear()
                add(titulo(tx("eptabelas_tabela_transicao")))
                add(tabelaDoAfd(d, 90f))
                hot("dl1c0", "dl4c0")
            }
            cena(tx("eptabelas_vamos_ler_abb_comeca_na")) {
                add(fita("abb", 320f, 34f))
                tint("c0", Tint.YELLOW)
                hot(celulaAfd(0, 0)); tint(celulaAfd(0, 0), Tint.YELLOW)
            }
            cena(tx("eptabelas_celula_diz_q1_pula_para")) {
                tint(celulaAfd(0, 0), Tint.NONE)
                tint("c1", Tint.YELLOW)
                hot(celulaAfd(1, 1)); tint(celulaAfd(1, 1), Tint.YELLOW)
            }
            cena(tx("eptabelas_deu_q2_le_ultimo_b")) {
                tint(celulaAfd(1, 1), Tint.NONE)
                tint("c2", Tint.YELLOW)
                hot(celulaAfd(2, 1)); tint(celulaAfd(2, 1), Tint.YELLOW)
            }
            cena(tx("eptabelas_q3_tem_asterisco_palavra_foi"), who = PROF, mood = EpMood.HAPPY) {
                tint(celulaAfd(2, 1), Tint.NONE)
                hot("dl4c0"); tint("dl4c0", Tint.GREEN)
            }
            brinquedo(
                tx("eptabelas_agora_voce_leia_quatro_letras"),
                tx("eptabelas_olhe_celula_linha_acesa_para"),
                tx("eptabelas_chegou_ao_final_usando_so"),
            ) { ToyTabelaViva(d, 4, tx("eptabelas_quatro_letras_terminando_na_linha")) }
        },

        // ─────────────────────────────── Itens LR(0) em caixas ───────────────────────────────
        episodio(Skill.LR0_ITEMS, tx("eptabelas_caixa_estado"), TABELAS) {
            val g = Grammar.parse("E -> E + T | T\nT -> id")
            val aut = LRAutomaton.lr0(g)
            val gg = aut.grammar
            val i0 = aut.states[0].items.toList()
            val linhas0 = i0.map { it.render(gg) }
            val alvoE = aut.transitions.getValue(0 to "E")
            val alvoT = aut.transitions.getValue(0 to "T")
            val alvoId = aut.transitions.getValue(0 to "id")
            val pos = listOf(80f to 210f, 270f to 95f, 270f to 225f, 270f to 335f)
            val (caixas, fios) = caixasDeItens(aut, pos, listOf(0, alvoE, alvoT, alvoId))
            val reduzem = listOf(alvoE, alvoT, alvoId).filter { s -> aut.states[s].items.any { it.isComplete(gg) && it.prodId != 0 } }.map { "I$it" }.toSet()
            fun i0com(n: Int) = registro("I0", "I0", linhas0.take(n), 180f, 150f, 200f, size = 14f, lit = n - 1)
            cena(tx("eptabelas_no_parser_lr_cada_estado")) {
                add(titulo(tx("eptabelas_estado_i0")))
                add(codigo("g", listOf("E → E + T | T", "T → id"), 320f, x = 110f, passo = 26f, size = 14f))
                add(i0com(1))
            }
            cena(tx("eptabelas_comeca_com_item_so_ponto")) { hot("I0") }
            cena(tx("eptabelas_se_falta_entram_regras_com")) {
                add(i0com(3)); hot("g0")
            }
            cena(tx("eptabelas_desses_itens_tem_ponto_antes")) {
                add(i0com(linhas0.size)); hot("g1")
            }
            cena(tx("eptabelas_nao_entra_mais_nada_essa"), mood = EpMood.HAPPY) { hot("I0") }
            cena(tx("eptabelas_agora_goto_pulando_cada_simbolo")) {
                drop("g0", "g1")
                add(caixas)
                wires(fios.filter { it.from == "I0" })
                hot("I$alvoE", "I$alvoT", "I$alvoId")
            }
            pergunta(
                tx("eptabelas_algumas_caixas_tem_item_com"),
                reduzem, setOf("I0", "I$alvoE", "I$alvoT", "I$alvoId") - reduzem,
                tx("eptabelas_procure_ponto_na_ultima_posicao"),
                tx("eptabelas_isso_com_ponto_no_fim"),
            ) { hot() }
            cena(tx("eptabelas_bison_desenha_estados_assim_com"), who = PROF) { hot(*reduzem.toTypedArray()) }
            brinquedo(
                tx("eptabelas_agora_monte_fecho_outra_gramatica"),
                tx("eptabelas_so_entra_item_com_ponto"),
                tx("eptabelas_fecho_completo_exatamente_i0_que"),
            ) { ToyClosure(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> ( E ) | id"), tx("eptabelas_complete_fecho_i0")) }
        },

        // ─────────────────────────────── O relatório do bison -v ───────────────────────────────
        episodio(Skill.YACC_BISON, tx("eptabelas_lendo_bison_v"), TABELAS) {
            val bison = MiniBison.compile(SPEC_CALC.joinToString("\n"))
            val t = bison.table
            val g = t.grammar
            val s0 = linhasDoEstado(t, 0)
            val num = (t.action[0 to "NUM"] as LRAction.Shift).state
            val sNum = linhasDoEstado(t, num)
            val ge = t.gotoTable.getValue(0 to "e")
            val sGe = linhasDoEstado(t, ge)
            // O estado em que '+' já leu os dois lados e chega um '*': o conflito que a precedência resolve.
            val conflito = t.rawConflictCells.first { (s, a) -> a == "'*'" && t.automaton.states[s].kernel.any { it.isComplete(g) && g.productions[it.prodId].rhs.contains("'+'") } }.first
            val sCf = linhasDoEstado(t, conflito)
            fun caixa(id: String, sid: Int, linhas: List<String>, x: Float, y: Float, w: Float = 200f, lit: Int = -1, tint: Tint = Tint.NONE) =
                registro(id, tx("comum_estado_1", sid), linhas, x, y, w, tint, 13f, lit)
            cena(tx("eptabelas_com_opcao_v_bison_escreve")) {
                add(titulo("calc.output"))
                add(codigo("s", SPEC_CALC, 70f, passo = 26f, size = 13f))
            }
            cena(tx("eptabelas_cada_estado_relatorio_vira_caixa")) {
                dropAll("s")
                add(caixa("A", 0, s0, 95f, 70f + alturaRegistro(s0.size) / 2, 170f, tint = Tint.TEAL))
            }
            cena(tx("eptabelas_em_cima_ficam_itens_embaixo")) {
                lit("A", linhaQueComeca(s0, "NUM"))
            }
            cena(tx("eptabelas_chegou_num_linha_manda_empilhar")) {
                add(caixa("B", num, sNum, 270f, 290f, 170f))
                wire(com.compilandia.content.Wire("A", "B", fromRow = linhaQueComeca(s0, "NUM"), hot = true))
            }
            cena(tx("eptabelas_nesse_estado_item_ja_terminou")) {
                lit("A", -1); lit("B", linhaQueComeca(sNum, "\$default")); hot("B")
            }
            pergunta(
                tx("eptabelas_depois_reduzir_pilha_volta_ao"),
                setOf("C"), setOf("A", "B"),
                tx("eptabelas_procure_linha_dentro_estado_zero"),
                tx("eptabelas_isso_linha_no_estado_zero"),
            ) {
                add(caixa("C", ge, sGe, 270f, 120f, 170f))
                lit("B", -1); lit("A", linhaQueComeca(s0, tx("eptabelas_vai")))
                change("B") { it.copy(y = 340f) }
            }
            cena(tx("eptabelas_agora_estado_conflito_mais_ja"), mood = EpMood.THINK) {
                clear()
                add(titulo("calc.output"))
                add(caixa("D", conflito, sCf, 180f, 90f + alturaRegistro(sCf.size) / 2, 300f, linhaQueComeca(sCf, "'*'"), Tint.ORANGE))
                add(rotulo("res", tx("eptabelas_conflito_resolvido_vezes_empilha"), 180f, 360f, Tint.YELLOW, 15f))
            }
            cena(tx("eptabelas_vezes_esta_mais_abaixo_no"), who = PROF) { hot("D") }
            cena(tx("eptabelas_sem_conflito_sobrando_relatorio_fica"), mood = EpMood.HAPPY) {
                drop("res")
                add(rotulo("ok", tx("eptabelas_0_conflitos_1_estados", t.automaton.states.size), 180f, 360f, Tint.GREEN, 16f))
            }
        },

        // ─────────────────────────────── Registro de ativação ───────────────────────────────
        episodio(Skill.RUNTIME, tx("eptabelas_caixa_por_chamada"), TABELAS) {
            val prog = RuntimeSim.fatorial(3)
            val linhas = prog.code.lines()
            // Uma imagem da pilha a cada evento do motor: quem está nela, de baixo para cima.
            fun pilhaApos(k: Int) = prog.events[k].stack
            fun caixas(k: Int, valor: Pair<String, Int>? = null): List<com.compilandia.content.Box> =
                pilhaApos(k).mapIndexed { i, f ->
                    val campos = f.args.map { (n, v) -> "$n = $v" } + tx("eptabelas_volta_1", f.returnTo)
                    registro("f$i", f.fn, campos, 268f, 370f - i * 74f, 164f, if (i == pilhaApos(k).lastIndex) Tint.YELLOW else Tint.NONE, 13f, lit = if (valor != null && i == pilhaApos(k).lastIndex) 0 else -1)
                }
            fun mostra(k: Int) { dropAll("f"); add(caixas(k)) }
            val chamadas = prog.events.indices.filter { prog.events[it] is RtEvent.Call }
            val primeiraVolta = prog.events.indexOfFirst { it is RtEvent.Return }
            cena(tx("eptabelas_cada_chamada_funcao_ganha_caixa")) {
                add(titulo("fat(3)"))
                add(codigo("s", linhas, 70f, x = 8f, passo = 22f, size = 11f).map { it.copy(w = 172f) })
                mostra(chamadas[0])
            }
            cena(tx("eptabelas_main_chama_fat_tres_nasce")) {
                mostra(chamadas[1]); hot("s5")
            }
            cena(tx("eptabelas_dentro_dela_fica_tambem_endereco")) { lit("f1", 1) }
            cena(tx("eptabelas_fat_tres_chama_fat_dois")) {
                mostra(chamadas[2]); hot("s2")
            }
            cena(tx("eptabelas_fat_dois_chama_fat_pilha")) { mostra(chamadas[3]) }
            pergunta(
                tx("eptabelas_quando_fat_terminar_qual_caixa"),
                setOf("f3"), setOf("f0", "f1", "f2"),
                tx("eptabelas_pilha_so_tira_pelo_topo"),
                tx("eptabelas_isso_sai_sempre_cima_ultima"),
            ) { hot() }
            cena(tx("eptabelas_fat_devolve_caixa_dele_sai")) {
                mostra(primeiraVolta)
                add(rotulo("v", tx("eptabelas_devolveu_1", (prog.events[primeiraVolta] as RtEvent.Return).value), 100f, 370f, Tint.YELLOW, 16f))
            }
            cena(tx("eptabelas_cada_caixa_que_sai_leva", prog.result), who = PROF, mood = EpMood.HAPPY) {
                mostra(prog.events.lastIndex - 1)
                text("v", "fat(3) = ${prog.result}")
            }
        },
    )
}
