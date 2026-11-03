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
        byte("Cole aqui o exercício: gramática, regex, expressão ou linha de código. Também pode perguntar.", Mood.HAPPY)
        acoes(
            Acao("E -> E + T | T") { entra("E -> E + T | T\nT -> id") },
            Acao("(a|b)*abb") { entra("(a|b)*abb") },
            Acao("a + b * c") { entra("a + b * c") },
            Acao("if (x >= 10) y = 1;") { entra("if (x >= 10) y = 1;") },
            Acao("o que é handle?") { entra("o que é handle?") },
            Acao("Laboratório Bison") { app.go(Screen.LabBison()) },
        )
    }

    /** O Byte lê o .y: quantas regras, quantos conflitos, o que a precedência decidiu e quanto dá a conta. */
    private fun bison(t: String) {
        val b = try { MiniBison.compile(t) } catch (e: Exception) {
            byte("Isso não compilou: ${e.message}.", Mood.SAD)
            rascunho = t
            return
        }
        val regras = b.grammar.productions.size
        val conta = "2+3*4"
        val r = b.run(conta)
        val partes = buildList {
            add("Li $regras regra${if (regras > 1) "s" else ""}.")
            add(if (b.conflicts.isEmpty()) "Nenhum conflito sobrou." else "Sobraram ${b.conflicts.size} conflitos.")
            if (b.resolvedByPrecedence > 0) add("A precedência decidiu ${b.resolvedByPrecedence}.")
            if (r.accepted) add("Veja: $conta dá ${r.value}.")
        }
        // O palco com a conta vem primeiro; a conclusão do Byte fica embaixo, perto dos botões.
        msgs += Msg.Ferramenta(VisualKind.BISON, t, conta)
        byte(partes.joinToString(" "), if (b.conflicts.isEmpty()) Mood.HAPPY else Mood.THINK)
        acoes(
            Acao("Abrir no Laboratório Bison", destaque = true) { app.go(Screen.LabBison(t)) },
            Acao("Ver os conflitos") { app.go(Screen.LabBison(t)) },
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
                byte("Não consegui ler: ${l.erro}. Escreva uma regra por linha: E -> E + T | T", Mood.SAD)
                rascunho = t
                return@launch
            }
            val p = l.problemas.firstOrNull()
            if (antes != null && p != null && p.falha == antes.problema.falha && p.nt == antes.problema.nt) {
                // Mandou de novo e o problema continua: conta como tentativa e abre o degrau seguinte.
                antes.tentou()
                byte("Ainda não: ${p.aponta}", Mood.THINK)
                acoesDoDegrau(antes, l)
                return@launch
            }
            if (antes != null && (p == null || p.falha != antes.problema.falha)) {
                byte("Isso! Sumiu o problema da ${nomeFalha(antes.problema.falha)}.", Mood.HAPPY)
                Sound.play(Sfx.CORRECT)
            }
            if (p == null) { degraus = null; semProblema(l) } else mostra(p, l)
        }
    }

    private fun nomeFalha(f: Falha) = when (f) {
        Falha.RECURSAO, Falha.RECURSAO_INDIRETA -> "recursão"
        Falha.PREFIXO -> "fatoração"
        Falha.AMBIGUA -> "ambiguidade"
        Falha.CONFLITO_LL, Falha.CONFLITO_SLR -> "conflito"
        Falha.IMPRODUTIVO -> "regra sem fim"
        Falha.INALCANCAVEL -> "regra solta"
    }

    private fun mostra(p: Problema, l: Laudo) {
        val d = Degraus(p)
        degraus = d
        val extra = if (l.problemas.size > 1) " Tem mais ${l.problemas.size - 1} depois desse." else ""
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
        if (d.degrau == 1) lista += Acao("Me dá uma dica", destaque = true) {
            limpaAcoes()
            d.sobe()
            byte(d.texto, Mood.THINK)
            rascunho = texto
            byte("Tente: edite a gramática aqui embaixo e mande de novo.")
            acoesDoDegrau(d, l)
        }
        if (d.degrau >= 2 && mostra != null) lista += Acao("Mostrar resolvendo", destaque = !d.travado, travada = d.travado) {
            if (!d.sobe() && d.travado) {
                byte("Antes, tente uma vez. Mesmo errada, a tentativa faz a resposta colar.", Mood.THINK)
                rascunho = texto
            } else {
                limpaAcoes()
                abre(mostra)
                val nova = conserto?.toText()
                if (nova != null) {
                    byte("Agora feche os olhos e refaça de cabeça. Quando quiser, use a versão consertada.")
                    acoes(Acao("Usar a consertada", destaque = true) { entra(nova) }, Acao("Refazer sozinho") { rascunho = texto })
                }
            }
        }
        if (g != null && l.frases.isNotEmpty()) lista += Acao("Rodar assim mesmo") { limpaAcoes(); rodaFrase(l.frases.first()) }
        // Recursão à esquerda só é problema para LL. Quem estuda LR precisa das tabelas assim mesmo.
        if (g != null) lista += Acao("Tabelas completas") { limpaAcoes(); maisFerramentas(g) }
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
            l.ll1 -> "Sem problema nenhum: é LL(1)."
            l.slr -> "Não é LL(1), mas o SLR dá conta."
            l.lalr -> "Só o LALR(1) resolve essa."
            else -> "Essa precisa de LR(1) completo."
        }
        byte(como, Mood.HAPPY)
        Sound.play(Sfx.CORRECT)
        val frases = l.frases.take(2).map { f -> Acao("Rodar: ${f.joinToString(" ")}", destaque = true) { limpaAcoes(); rodaFrase(f) } }
        acoes(
            *frases.toTypedArray(),
            Acao("FIRST") { abre(Sims.conjuntos(g, SetStep.Kind.FIRST)) },
            Acao("FOLLOW") { abre(Sims.conjuntos(g, SetStep.Kind.FOLLOW)) },
            Acao("Tabela LL(1)") { abre(Sims.tabelaLL(g)) },
            Acao("Estados LR(0)") { abre(Sims.lr0(g)) },
            Acao("Tabelas completas") { limpaAcoes(); maisFerramentas(g) },
        )
    }

    /** As tabelas de sempre continuam aqui, para conferir exercício linha por linha. */
    private fun maisFerramentas(g: Grammar) {
        val t = g.toText()
        fun tool(r: String, k: VisualKind, i: String = "") = Acao(r) { limpaAcoes(); msgs += Msg.Ferramenta(k, t, i); maisFerramentas(g) }
        val f = laudo?.frases?.firstOrNull()?.joinToString(" ").orEmpty()
        val ll1 = laudo?.ll1 == true
        val lista = buildList {
            add(tool("FIRST e FOLLOW", VisualKind.FIRST_FOLLOW))
            add(tool("Tabela LL(1)", VisualKind.LL1_TABLE))
            add(tool("Tabela SLR", VisualKind.SLR_TABLE))
            add(tool("Tabela LALR(1)", VisualKind.LALR_TABLE))
            add(tool("Estados LR(0)", VisualKind.LR0_AUTOMATON))
            if (f.isNotEmpty()) add(tool("Derivação à direita", VisualKind.RIGHTMOST, f))
            // Essa ferramenta só existe para gramática LL(1): fora disso ela nem aparece.
            if (ll1 && f.isNotEmpty()) add(tool("Código da descida", VisualKind.DESCENT_CODE, f))
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
        byte(if (aceita) "Essa frase vale. Veja a máquina andando." else "Essa frase não vale. Veja onde a máquina trava.", if (aceita) Mood.HAPPY else Mood.THINK)
        abre(sim)
        val frases = l.frases.filter { it != tokens }.take(2)
        acoes(
            *frases.map { f -> Acao("Rodar: ${f.joinToString(" ")}") { limpaAcoes(); rodaFrase(f) } }.toTypedArray(),
            Acao("Ver a árvore") { Sims.derivacao(g, tokens)?.let(abre) ?: byte("Sem árvore: a frase não vale.", Mood.SAD) },
        )
    }

    // ------------------------------------------------------------ Regex

    private fun regex(t: String) {
        val d = try { DFA.fromRegex(t) } catch (e: Exception) {
            byte("Essa regex não fechou: ${e.message}.", Mood.SAD)
            rascunho = t
            return
        }
        regex = t
        laudo = null
        val amostra = d.samples(kotlin.random.Random(3), 6).filter { it.first.isNotEmpty() }
        byte("Montei o autômato: ${d.stateCount} estados. Digite uma cadeia, ou toque numa destas.", Mood.HAPPY)
        msgs += Msg.Palco(Sims.afd(t, amostra.firstOrNull { it.second }?.first.orEmpty()))
        acoes(*amostra.take(4).map { (c, _) -> Acao(c) { entra(c) } }.toTypedArray())
    }

    private fun rodaCadeia(c: String) {
        val r = regex ?: return
        abre(Sims.afd(r, c))
        val aceita = try { DFA.fromRegex(r).accepts(c) } catch (e: Exception) { false }
        byte(if (aceita) "$c casa com $r." else "$c não casa com $r.", if (aceita) Mood.HAPPY else Mood.THINK)
    }

    // ------------------------------------------------------------ Expressão e código

    private fun expressao(t: String) {
        val sim = try { Sims.expressao(t) } catch (e: Exception) {
            byte("Não consegui ler essa expressão: ${e.message}.", Mood.SAD)
            rascunho = t
            return
        }
        byte("Vou da árvore até o MIPS.", Mood.HAPPY)
        msgs += Msg.Palco(sim)
        acoes(
            Acao("Ver em tela cheia", destaque = true) { abre(sim) },
            Acao("Tipos") { msgs += Msg.Ferramenta(VisualKind.ATTR_TREE, "", t) },
            Acao("AST e derivação") { msgs += Msg.Ferramenta(VisualKind.AST, "", t) },
            Acao("Registradores") { msgs += Msg.Ferramenta(VisualKind.REGALLOC, "", t) },
        )
    }

    private fun codigo(t: String) {
        val sim = Sims.tokens(t)
        byte("Cortei a linha em tokens.", Mood.HAPPY)
        msgs += Msg.Palco(sim)
        acoes(Acao("Ver em tela cheia", destaque = true) { abre(sim) })
    }

    private fun ferramenta(k: VisualKind, g: String, i: String, valida: () -> Unit) {
        try {
            valida()
            msgs += Msg.Ferramenta(k, g, i)
        } catch (e: Exception) {
            byte("Isso não compilou: ${e.message}.", Mood.SAD)
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
        byte("Essa eu ainda não sei. Tente com outras palavras, ou cole o exercício.", Mood.THINK)
        acoes(
            Acao("o que é FOLLOW?") { entra("o que é FOLLOW?") },
            Acao("o que é conflito?") { entra("o que é conflito shift-reduce?") },
            Acao("para que serve o ε?") { entra("para que serve o épsilon?") },
        )
    }

    private fun acoesDeTeste(skill: Skill): Array<Acao> {
        val l = app.learner
        return arrayOf(
            Acao("Me testa agora", destaque = true) {
                val g: GameId = l.chooseGame(skill)
                app.go(Screen.Play(skill, g, l.chooseLevel(skill, nowMs()), seed = nowMs()))
            },
            Acao("Ver a aula") { app.go(Screen.Episode(skill)) },
        )
    }
}
