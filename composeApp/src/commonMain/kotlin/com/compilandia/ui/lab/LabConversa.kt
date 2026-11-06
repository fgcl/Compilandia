package com.compilandia.ui.lab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.compilandia.content.Episode
import com.compilandia.content.VisualKind
import com.compilandia.engine.DFA
import com.compilandia.engine.Grammar
import com.compilandia.engine.LRKind
import com.compilandia.engine.MiniBison
import com.compilandia.engine.SetStep
import com.compilandia.engine.tokensOf
import com.compilandia.learning.GameId
import com.compilandia.learning.Skill
import com.compilandia.platform.Sfx
import com.compilandia.platform.Sound
import com.compilandia.platform.nowMs
import com.compilandia.tutor.Degraus
import com.compilandia.tutor.Diagnostico
import com.compilandia.tutor.Entrada
import com.compilandia.tutor.Falha
import com.compilandia.tutor.Laudo
import com.compilandia.tutor.Problema
import com.compilandia.tutor.Respostas
import com.compilandia.tutor.Sim
import com.compilandia.tutor.Sims
import com.compilandia.tutor.Tipo
import com.compilandia.ui.AppState
import com.compilandia.ui.Screen
import com.compilandia.ui.cast.Mood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.compilandia.textos.tx

/** Uma mensagem da conversa do Laboratório. */
sealed class Msg {
    data class Eu(val texto: String, val mono: Boolean) : Msg()
    data class Fala(val texto: String, val humor: Mood = Mood.NEUTRAL) : Msg()
    data class Palco(val sim: Sim) : Msg()
    data class Clipe(val ep: Episode) : Msg()
    data class Acoes(val acoes: List<Acao>) : Msg()
    data class Ferramenta(val kind: VisualKind, val grammar: String, val input: String) : Msg()
    data object Pensando : Msg()
}

data class Acao(val rotulo: String, val destaque: Boolean = false, val travada: Boolean = false, val faz: () -> Unit)

/**
 * A cabeça do Laboratório: lê o que chegou, chama o motor e decide a próxima fala.
 * Uma entrada só, sem escolher ferramenta. A trava das dicas fica em [Degraus].
 */
class LabConversa(private val app: AppState, private val scope: CoroutineScope, private val abre: (Sim) -> Unit) {
    val msgs = mutableStateListOf<Msg>()
    /** Texto que volta para a caixa, quando o Byte pede para a pessoa editar. */
    var rascunho by mutableStateOf("")

    /** A fala mais recente é a única que aparece digitando. Fica guardada para a tela não varrer a lista. */
    var ultimaFala by mutableStateOf<Msg.Fala?>(null)
        private set

    private var texto = ""
    private var laudo: Laudo? = null
    private var degraus: Degraus? = null
    private var regex: String? = null

    init { abertura() }

    private fun abertura() {
        byte(tx("labconversa_cole_aqui_exercicio_gramatica_regex"), Mood.HAPPY)
        acoes(
            Acao("E -> E + T | T") { entra("E -> E + T | T\nT -> id") },
            Acao("(a|b)*abb") { entra("(a|b)*abb") },
            Acao(tx("comum_b_c")) { entra(tx("comum_b_c")) },
            Acao("if (x >= 10) y = 1;") { entra("if (x >= 10) y = 1;") },
            Acao(tx("labconversa_que_handle")) { entra(tx("labconversa_que_handle")) },
            Acao(tx("comum_laboratorio_bison")) { app.go(Screen.LabBison()) },
        )
    }

    /** O Byte lê o .y: quantas regras, quantos conflitos, o que a precedência decidiu e quanto dá a conta. */
    private fun bison(t: String) {
        val b = try { MiniBison.compile(t) } catch (e: Exception) {
            byte(tx("labconversa_isso_nao_compilou_1", e.message), Mood.SAD)
            rascunho = t
            return
        }
        val regras = b.grammar.productions.size
        val conta = "2+3*4"
        val r = b.run(conta)
        val partes = buildList {
            add(tx("labconversa_li_1_regra_2", regras, if (regras > 1) "s" else ""))
            add(if (b.conflicts.isEmpty()) tx("labconversa_nenhum_conflito_sobrou") else tx("labconversa_sobraram_1_conflitos", b.conflicts.size))
            if (b.resolvedByPrecedence > 0) add(tx("labconversa_precedencia_decidiu_1", b.resolvedByPrecedence))
            if (r.accepted) add(tx("labconversa_veja_1_2", conta, r.value))
        }
        // O palco com a conta vem primeiro; a conclusão do Byte fica embaixo, perto dos botões.
        msgs += Msg.Ferramenta(VisualKind.BISON, t, conta)
        byte(partes.joinToString(" "), if (b.conflicts.isEmpty()) Mood.HAPPY else Mood.THINK)
        acoes(
            Acao(tx("labconversa_abrir_no_laboratorio_bison"), destaque = true) { app.go(Screen.LabBison(t)) },
            Acao(tx("labconversa_ver_conflitos")) { app.go(Screen.LabBison(t)) },
        )
    }

    private fun byte(t: String, humor: Mood = Mood.NEUTRAL) {
        val f = Msg.Fala(t, humor)
        ultimaFala = f
        msgs += f
    }
    private fun acoes(vararg a: Acao) { limpaAcoes(); msgs += Msg.Acoes(a.toList()) }
    /** Botão antigo da conversa não vale mais: tocar nele voltaria no tempo. */
    private fun limpaAcoes() { msgs.removeAll { it is Msg.Acoes } }

    fun entra(bruto: String) {
        val t = bruto.trim()
        if (t.isEmpty()) return
        limpaAcoes()
        val tipo = Entrada.tipo(t)
        msgs += Msg.Eu(t, tipo != Tipo.PERGUNTA)
        when {
            // Com gramática ou regex na bancada, uma linha feita só dos símbolos dela é entrada para rodar.
            tipo != Tipo.GRAMATICA && !t.endsWith("?") && fraseDaGramatica(t) -> rodaFrase(tokensOf(t))
            cadeiaDaRegex(t) -> rodaCadeia(t)
            else -> when (tipo) {
                Tipo.VAZIO -> {}
                Tipo.GRAMATICA -> gramatica(t)
                Tipo.REGEX -> regex(t)
                Tipo.EXPRESSAO -> expressao(t)
                Tipo.CODIGO -> codigo(t)
                Tipo.BISON -> bison(t)
                Tipo.FLEX -> ferramenta(VisualKind.FLEX, t, "if x1 >= 42") {}
                Tipo.MIPS -> ferramenta(VisualKind.MIPS_RUN, t, "") { com.compilandia.engine.MipsVM(t) }
                Tipo.PERGUNTA -> pergunta(t)
            }
        }
    }

    private fun cadeiaDaRegex(t: String): Boolean {
        val r = regex ?: return false
        val d = try { DFA.fromRegex(r) } catch (e: Exception) { return false }
        return t.all { it in d.alphabet }
    }

    private fun fraseDaGramatica(t: String): Boolean {
        val g = laudo?.g ?: return false
        return tokensOf(t).all { it in g.terminals }
    }

    // ------------------------------------------------------------ Gramática

    private fun gramatica(t: String) {
        val antes = degraus
        msgs += Msg.Pensando
        scope.launch {
            val l = withContext(Dispatchers.Default) { Diagnostico.analisa(t) }
            msgs.remove(Msg.Pensando)
            laudo = l
            texto = t
            regex = null
            app.learner.setLabUltimo(t)
            app.commit()
            if (l.g == null) {
                byte(tx("labconversa_nao_consegui_ler_1_escreva", l.erro), Mood.SAD)
                rascunho = t
                return@launch
            }
            val p = l.problemas.firstOrNull()
            if (antes != null && p != null && p.falha == antes.problema.falha && p.nt == antes.problema.nt) {
                // Mandou de novo e o problema continua: conta como tentativa e abre o degrau seguinte.
                antes.tentou()
                byte(tx("labconversa_ainda_nao_1", p.aponta), Mood.THINK)
                acoesDoDegrau(antes, l)
                return@launch
            }
            if (antes != null && (p == null || p.falha != antes.problema.falha)) {
                byte(tx("labconversa_isso_sumiu_problema_1", nomeFalha(antes.problema.falha)), Mood.HAPPY)
                Sound.play(Sfx.CORRECT)
            }
            if (p == null) { degraus = null; semProblema(l) } else mostra(p, l)
        }
    }

    private fun nomeFalha(f: Falha) = when (f) {
        Falha.RECURSAO, Falha.RECURSAO_INDIRETA -> tx("labconversa_recursao")
        Falha.PREFIXO -> tx("comum_fatoracao")
        Falha.AMBIGUA -> "ambiguidade"
        Falha.CONFLITO_LL, Falha.CONFLITO_SLR -> "conflito"
        Falha.IMPRODUTIVO -> tx("labconversa_regra_sem_fim")
        Falha.INALCANCAVEL -> tx("labconversa_regra_solta")
    }

    private fun mostra(p: Problema, l: Laudo) {
        val d = Degraus(p)
        degraus = d
        val extra = if (l.problemas.size > 1) tx("labconversa_tem_mais_1_depois_desse", l.problemas.size - 1) else ""
        byte(p.aponta + extra, Mood.THINK)
        simDoProblema(p, l)?.let { msgs += Msg.Palco(it) }
        acoesDoDegrau(d, l)
    }

    private fun simDoProblema(p: Problema, l: Laudo): Sim? {
        val g = l.g ?: return null
        return when (p.falha) {
            Falha.RECURSAO -> laco(g, p.nt ?: g.start)
            Falha.AMBIGUA -> p.frase?.let { Sims.ambigua(g, it) }
            Falha.CONFLITO_LL -> Sims.tabelaLL(g)
            else -> null
        }
    }

    /** O desenho que mostra o parser preso: a regra se abre e nunca chega num terminal. */
    private fun laco(g: Grammar, nt: String): Sim? = Sims.laco(g, nt)

    private fun acoesDoDegrau(d: Degraus, l: Laudo) {
        val g = l.g
        val conserto = g?.let { Diagnostico.conserto(it) }
        val mostra = g?.let { resolveu(it, d.problema) }
        val lista = mutableListOf<Acao>()
        if (d.degrau == 1) lista += Acao(tx("labconversa_me_dica"), destaque = true) {
            limpaAcoes()
            d.sobe()
            byte(d.texto, Mood.THINK)
            rascunho = texto
            byte(tx("labconversa_tente_edite_gramatica_aqui_embaixo"))
            acoesDoDegrau(d, l)
        }
        if (d.degrau >= 2 && mostra != null) lista += Acao(tx("labconversa_mostrar_resolvendo"), destaque = !d.travado, travada = d.travado) {
            if (!d.sobe() && d.travado) {
                byte(tx("labconversa_antes_tente_vez_mesmo_errada"), Mood.THINK)
                rascunho = texto
            } else {
                limpaAcoes()
                abre(mostra)
                val nova = conserto?.toText()
                if (nova != null) {
                    byte(tx("labconversa_agora_feche_olhos_refaca_cabeca"))
                    acoes(Acao(tx("labconversa_usar_consertada"), destaque = true) { entra(nova) }, Acao(tx("labconversa_refazer_sozinho")) { rascunho = texto })
                }
            }
        }
        if (g != null && l.frases.isNotEmpty()) lista += Acao(tx("labconversa_rodar_assim_mesmo")) { limpaAcoes(); rodaFrase(l.frases.first()) }
        // Recursão à esquerda só é problema para LL. Quem estuda LR precisa das tabelas assim mesmo.
        if (g != null) lista += Acao(tx("labconversa_tabelas_completas")) { limpaAcoes(); maisFerramentas(g) }
        acoes(*lista.toTypedArray())
    }

    private fun resolveu(g: Grammar, p: Problema): Sim? = when (p.falha) {
        Falha.RECURSAO -> Sims.recursao(g)
        Falha.PREFIXO -> Sims.fatorar(g)
        Falha.CONFLITO_LL -> Sims.tabelaLL(g)
        Falha.AMBIGUA -> p.frase?.let { Sims.ambigua(g, it) }
        else -> null
    }

    private fun semProblema(l: Laudo) {
        val g = l.g ?: return
        val como = when {
            l.ll1 -> tx("labconversa_sem_problema_nenhum_ll_1")
            l.slr -> tx("labconversa_nao_ll_1_mas_slr")
            l.lalr -> tx("labconversa_so_lalr_1_resolve_essa")
            else -> tx("labconversa_essa_precisa_lr_1_completo")
        }
        byte(como, Mood.HAPPY)
        Sound.play(Sfx.CORRECT)
        val frases = l.frases.take(2).map { f -> Acao(tx("labconversa_rodar_1", f.joinToString(" ")), destaque = true) { limpaAcoes(); rodaFrase(f) } }
        acoes(
            *frases.toTypedArray(),
            Acao("FIRST") { abre(Sims.conjuntos(g, SetStep.Kind.FIRST)) },
            Acao("FOLLOW") { abre(Sims.conjuntos(g, SetStep.Kind.FOLLOW)) },
            Acao(tx("comum_tabela_ll_1_2")) { abre(Sims.tabelaLL(g)) },
            Acao(tx("labconversa_estados_lr_0")) { abre(Sims.lr0(g)) },
            Acao(tx("labconversa_tabelas_completas")) { limpaAcoes(); maisFerramentas(g) },
        )
    }

    /** As tabelas de sempre continuam aqui, para conferir exercício linha por linha. */
    private fun maisFerramentas(g: Grammar) {
        val t = g.toText()
        fun tool(r: String, k: VisualKind, i: String = "") = Acao(r) { limpaAcoes(); msgs += Msg.Ferramenta(k, t, i); maisFerramentas(g) }
        val f = laudo?.frases?.firstOrNull()?.joinToString(" ").orEmpty()
        val ll1 = laudo?.ll1 == true
        val lista = buildList {
            add(tool(tx("labconversa_first_follow"), VisualKind.FIRST_FOLLOW))
            add(tool(tx("comum_tabela_ll_1_2"), VisualKind.LL1_TABLE))
            add(tool(tx("labconversa_tabela_slr"), VisualKind.SLR_TABLE))
            add(tool(tx("labconversa_tabela_lalr_1"), VisualKind.LALR_TABLE))
            add(tool(tx("labconversa_estados_lr_0"), VisualKind.LR0_AUTOMATON))
            if (f.isNotEmpty()) add(tool(tx("labconversa_derivacao_direita"), VisualKind.RIGHTMOST, f))
            // Essa ferramenta só existe para gramática LL(1): fora disso ela nem aparece.
            if (ll1 && f.isNotEmpty()) add(tool(tx("labconversa_codigo_descida"), VisualKind.DESCENT_CODE, f))
        }
        acoes(*lista.toTypedArray())
    }

    private fun rodaFrase(tokens: List<String>) {
        val l = laudo
        val g = l?.g ?: return
        val aceita = com.compilandia.engine.Earley(g).accepts(tokens)
        val sim = when {
            l.ll1 -> Sims.parseLL(g, tokens)
            l.slr -> Sims.parseLR(g, tokens)
            l.lalr -> Sims.parseLR(g, tokens, LRKind.LALR1)
            else -> Sims.derivacao(g, tokens) ?: Sims.parseLR(g, tokens, LRKind.LR1)
        }
        byte(if (aceita) tx("labconversa_essa_frase_vale_veja_maquina") else tx("labconversa_essa_frase_nao_vale_veja"), if (aceita) Mood.HAPPY else Mood.THINK)
        abre(sim)
        val frases = l.frases.filter { it != tokens }.take(2)
        acoes(
            *frases.map { f -> Acao(tx("labconversa_rodar_1", f.joinToString(" "))) { limpaAcoes(); rodaFrase(f) } }.toTypedArray(),
            Acao(tx("labconversa_ver_arvore")) { Sims.derivacao(g, tokens)?.let(abre) ?: byte(tx("labconversa_sem_arvore_frase_nao_vale"), Mood.SAD) },
        )
    }

    // ------------------------------------------------------------ Regex

    private fun regex(t: String) {
        val d = try { DFA.fromRegex(t) } catch (e: Exception) {
            byte(tx("labconversa_essa_regex_nao_fechou_1", e.message), Mood.SAD)
            rascunho = t
            return
        }
        regex = t
        laudo = null
        val amostra = d.samples(kotlin.random.Random(3), 6).filter { it.first.isNotEmpty() }
        byte(tx("labconversa_montei_automato_1_estados_digite", d.stateCount), Mood.HAPPY)
        msgs += Msg.Palco(Sims.afd(t, amostra.firstOrNull { it.second }?.first.orEmpty()))
        acoes(*amostra.take(4).map { (c, _) -> Acao(c) { entra(c) } }.toTypedArray())
    }

    private fun rodaCadeia(c: String) {
        val r = regex ?: return
        abre(Sims.afd(r, c))
        val aceita = try { DFA.fromRegex(r).accepts(c) } catch (e: Exception) { false }
        byte(if (aceita) tx("labconversa_1_casa_com_2", c, r) else tx("labconversa_1_nao_casa_com_2", c, r), if (aceita) Mood.HAPPY else Mood.THINK)
    }

    // ------------------------------------------------------------ Expressão e código

    private fun expressao(t: String) {
        val sim = try { Sims.expressao(t) } catch (e: Exception) {
            byte(tx("labconversa_nao_consegui_ler_essa_expressao", e.message), Mood.SAD)
            rascunho = t
            return
        }
        byte(tx("labconversa_vou_arvore_ate_mips"), Mood.HAPPY)
        msgs += Msg.Palco(sim)
        acoes(
            Acao(tx("labconversa_ver_em_tela_cheia"), destaque = true) { abre(sim) },
            Acao(tx("labconversa_tipos")) { msgs += Msg.Ferramenta(VisualKind.ATTR_TREE, "", t) },
            Acao(tx("labconversa_ast_derivacao")) { msgs += Msg.Ferramenta(VisualKind.AST, "", t) },
            Acao(tx("comum_registradores")) { msgs += Msg.Ferramenta(VisualKind.REGALLOC, "", t) },
        )
    }

    private fun codigo(t: String) {
        val sim = Sims.tokens(t)
        byte(tx("labconversa_cortei_linha_em_tokens"), Mood.HAPPY)
        msgs += Msg.Palco(sim)
        acoes(Acao(tx("labconversa_ver_em_tela_cheia"), destaque = true) { abre(sim) })
    }

    private fun ferramenta(k: VisualKind, g: String, i: String, valida: () -> Unit) {
        try {
            valida()
            msgs += Msg.Ferramenta(k, g, i)
        } catch (e: Exception) {
            byte(tx("labconversa_isso_nao_compilou_1", e.message), Mood.SAD)
            rascunho = g
        }
    }

    // ------------------------------------------------------------ Perguntas

    private fun pergunta(q: String) {
        val r = Respostas.busca(q)
        if (r != null) {
            byte(r.texto, Mood.NEUTRAL)
            r.clipe?.let { msgs += Msg.Clipe(it) }
            r.skill?.let { skill -> acoes(*acoesDeTeste(skill)) }
            return
        }
        byte(tx("labconversa_essa_eu_ainda_nao_sei"), Mood.THINK)
        acoes(
            Acao(tx("labconversa_que_follow")) { entra(tx("labconversa_que_follow")) },
            Acao(tx("labconversa_que_conflito")) { entra(tx("labconversa_que_conflito_shift_reduce")) },
            Acao(tx("labconversa_para_que_serve")) { entra(tx("labconversa_para_que_serve_epsilon")) },
        )
    }

    private fun acoesDeTeste(skill: Skill): Array<Acao> {
        val l = app.learner
        return arrayOf(
            Acao(tx("labconversa_me_testa_agora"), destaque = true) {
                val g: GameId = l.chooseGame(skill)
                app.go(Screen.Play(skill, g, l.chooseLevel(skill, nowMs()), seed = nowMs()))
            },
            Acao(tx("labconversa_ver_aula")) { app.go(Screen.Episode(skill)) },
        )
    }
}
