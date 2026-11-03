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
        episodio(Skill.REGEX_DFA, "O autômato vira tabela", TABELAS) {
            val d = DFA.fromRegex("(a|b)*abb").minimize().emOrdem()
            val pos = listOf(70f to 120f, 290f to 120f, 290f to 300f, 70f to 300f)
            val (bol, fiosBol) = bolinhasDoAfd(d, pos)
            val (cx, fiosCx) = caixasDoAfd(d, pos)
            cena("Este autômato aceita as palavras que terminam em abb.") {
                add(titulo("(a|b)*abb"))
                add(bol); wires(fiosBol)
            }
            cena("Agora cada estado vira caixa: o nome em cima, uma linha por letra.") {
                clearWires()
                add(cx); wires(fiosCx)
                hot("q0")
            }
            cena("O fio sai da linha certa. A linha b do q1 leva ao q2.") {
                hot("q1"); lit("q1", 1); hotRow("q1", 1)
            }
            pergunta(
                "Estou no q2 e chegou um b. Toque no estado de destino.",
                setOf("q3"), setOf("q0", "q1", "q2"),
                "Procure a linha do b dentro da caixa do q2.",
                "Isso. O b do q2 leva ao q3, que é o estado final.",
            ) { lit("q1", -1); lit("q2", 1); hot("q2"); hotRow("q2", 1) }
            cena("Junte as caixas e cada uma vira uma linha. A seta marca o início, e o asterisco, o final.", mood = EpMood.SURPRISED) {
                clear()
                add(titulo("Tabela de transição"))
                add(tabelaDoAfd(d, 90f))
                hot("dl1c0", "dl4c0")
            }
            cena("Vamos ler abb. Começa na linha do q0 e lê o a.") {
                add(fita("abb", 320f, 34f))
                tint("c0", Tint.YELLOW)
                hot(celulaAfd(0, 0)); tint(celulaAfd(0, 0), Tint.YELLOW)
            }
            cena("A célula diz q1. Pula para a linha do q1 e lê o b.") {
                tint(celulaAfd(0, 0), Tint.NONE)
                tint("c1", Tint.YELLOW)
                hot(celulaAfd(1, 1)); tint(celulaAfd(1, 1), Tint.YELLOW)
            }
            cena("Deu q2. Lê o último b, e a célula manda para o q3.") {
                tint(celulaAfd(1, 1), Tint.NONE)
                tint("c2", Tint.YELLOW)
                hot(celulaAfd(2, 1)); tint(celulaAfd(2, 1), Tint.YELLOW)
            }
            cena("O q3 tem asterisco. A palavra foi aceita só olhando a tabela.", who = PROF, mood = EpMood.HAPPY) {
                tint(celulaAfd(2, 1), Tint.NONE)
                hot("dl4c0"); tint("dl4c0", Tint.GREEN)
            }
            brinquedo(
                "Agora você. Leia quatro letras e termine na linha com asterisco.",
                "Olhe a célula da linha acesa. Para terminar no q3, as três últimas precisam ser abb.",
                "Chegou ao final usando só a tabela.",
            ) { ToyTabelaViva(d, 4, "Quatro letras, terminando na linha com asterisco.") }
        },

        // ─────────────────────────────── Itens LR(0) em caixas ───────────────────────────────
        episodio(Skill.LR0_ITEMS, "A caixa do estado", TABELAS) {
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
            cena("No parser LR, cada estado é uma caixa de itens. Vamos montar a primeira.") {
                add(titulo("Estado I0"))
                add(codigo("g", listOf("E → E + T | T", "T → id"), 320f, x = 110f, passo = 26f, size = 14f))
                add(i0com(1))
            }
            cena("Começa com um item só. O ponto antes do E quer dizer: falta ler um E.") { hot("I0") }
            cena("Se falta um E, entram as regras do E, com o ponto no começo.") {
                add(i0com(3)); hot("g0")
            }
            cena("Um desses itens tem o ponto antes do T. Então entram as regras do T.") {
                add(i0com(linhas0.size)); hot("g1")
            }
            cena("Não entra mais nada. Essa caixa completa se chama fecho, ou closure.", mood = EpMood.HAPPY) { hot("I0") }
            cena("Agora o goto. Pulando cada símbolo, nasce outra caixa. O fio sai da linha dele.") {
                drop("g0", "g1")
                add(caixas)
                wires(fios.filter { it.from == "I0" })
                hot("I$alvoE", "I$alvoT", "I$alvoId")
            }
            pergunta(
                "Algumas caixas têm item com o ponto no fim. Essas mandam reduzir. Toque nelas.",
                reduzem, setOf("I0", "I$alvoE", "I$alvoT", "I$alvoId") - reduzem,
                "Procure o ponto na última posição, sem nada depois dele.",
                "Isso. Com o ponto no fim, a regra inteira já está na pilha.",
            ) { hot() }
            cena("O bison desenha os estados assim, com a opção graph.", who = PROF) { hot(*reduzem.toTypedArray()) }
            brinquedo(
                "Agora monte o fecho de outra gramática. Toque nos itens que entram.",
                "Só entra item com o ponto no começo, de quem aparece logo depois de um ponto.",
                "Fecho completo. É exatamente o I0 que o motor calcula.",
            ) { ToyClosure(Grammar.parse("E -> E + T | T\nT -> T * F | F\nF -> ( E ) | id"), "Complete o fecho do I0.") }
        },

        // ─────────────────────────────── O relatório do bison -v ───────────────────────────────
        episodio(Skill.YACC_BISON, "Lendo o bison -v", TABELAS) {
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
                registro(id, "Estado $sid", linhas, x, y, w, tint, 13f, lit)
            cena("Com a opção -v, o Bison escreve um relatório: o arquivo output.") {
                add(titulo("calc.output"))
                add(codigo("s", SPEC_CALC, 70f, passo = 26f, size = 13f))
            }
            cena("Cada estado do relatório vira uma caixa. Este é o estado zero.") {
                dropAll("s")
                add(caixa("A", 0, s0, 95f, 70f + alturaRegistro(s0.size) / 2, 170f, tint = Tint.TEAL))
            }
            cena("Em cima ficam os itens. Embaixo, o que fazer com cada token.") {
                lit("A", linhaQueComeca(s0, "NUM"))
            }
            cena("Chegou NUM. A linha manda empilhar e ir para outro estado.") {
                add(caixa("B", num, sNum, 270f, 290f, 170f))
                wire(com.compilandia.content.Wire("A", "B", fromRow = linhaQueComeca(s0, "NUM"), hot = true))
            }
            cena("Nesse estado o item já terminou. Com qualquer token, ele reduz.") {
                lit("A", -1); lit("B", linhaQueComeca(sNum, "\$default")); hot("B")
            }
            pergunta(
                "Depois de reduzir, a pilha volta ao estado zero com um e. Toque no destino.",
                setOf("C"), setOf("A", "B"),
                "Procure a linha do e dentro do estado zero.",
                "Isso. A linha do e no estado zero aponta para essa caixa.",
            ) {
                add(caixa("C", ge, sGe, 270f, 120f, 170f))
                lit("B", -1); lit("A", linhaQueComeca(s0, "e vai"))
                change("B") { it.copy(y = 340f) }
            }
            cena("Agora o estado do conflito. O mais já leu os dois lados e chegou um vezes.", mood = EpMood.THINK) {
                clear()
                add(titulo("calc.output"))
                add(caixa("D", conflito, sCf, 180f, 90f + alturaRegistro(sCf.size) / 2, 300f, linhaQueComeca(sCf, "'*'"), Tint.ORANGE))
                add(rotulo("res", "conflito resolvido: vezes empilha", 180f, 360f, Tint.YELLOW, 15f))
            }
            cena("O vezes está mais abaixo no arquivo. Por isso a linha dele empilha.", who = PROF) { hot("D") }
            cena("Sem conflito sobrando, o relatório fica limpo. Agora é rodar.", mood = EpMood.HAPPY) {
                drop("res")
                add(rotulo("ok", "0 conflitos · ${t.automaton.states.size} estados", 180f, 360f, Tint.GREEN, 16f))
            }
        },

        // ─────────────────────────────── Registro de ativação ───────────────────────────────
        episodio(Skill.RUNTIME, "Uma caixa por chamada", TABELAS) {
            val prog = RuntimeSim.fatorial(3)
            val linhas = prog.code.lines()
            // Uma imagem da pilha a cada evento do motor: quem está nela, de baixo para cima.
            fun pilhaApos(k: Int) = prog.events[k].stack
            fun caixas(k: Int, valor: Pair<String, Int>? = null): List<com.compilandia.content.Box> =
                pilhaApos(k).mapIndexed { i, f ->
                    val campos = f.args.map { (n, v) -> "$n = $v" } + "volta: ${f.returnTo}"
                    registro("f$i", f.fn, campos, 268f, 370f - i * 74f, 164f, if (i == pilhaApos(k).lastIndex) Tint.YELLOW else Tint.NONE, 13f, lit = if (valor != null && i == pilhaApos(k).lastIndex) 0 else -1)
                }
            fun mostra(k: Int) { dropAll("f"); add(caixas(k)) }
            val chamadas = prog.events.indices.filter { prog.events[it] is RtEvent.Call }
            val primeiraVolta = prog.events.indexOfFirst { it is RtEvent.Return }
            cena("Cada chamada de função ganha uma caixa na pilha. É o registro de ativação.") {
                add(titulo("fat(3)"))
                add(codigo("s", linhas, 70f, x = 8f, passo = 22f, size = 11f).map { it.copy(w = 172f) })
                mostra(chamadas[0])
            }
            cena("O main chama fat de três. Nasce uma caixa com n igual a três.") {
                mostra(chamadas[1]); hot("s5")
            }
            cena("Dentro dela fica também o endereço de volta: para onde o resultado vai.") { lit("f1", 1) }
            cena("fat de três chama fat de dois. Outra caixa entra por cima.") {
                mostra(chamadas[2]); hot("s2")
            }
            cena("E fat de dois chama fat de um. A pilha cresce para cima.") { mostra(chamadas[3]) }
            pergunta(
                "Quando fat de um terminar, qual caixa sai primeiro? Toque nela.",
                setOf("f3"), setOf("f0", "f1", "f2"),
                "A pilha só tira pelo topo.",
                "Isso. Sai sempre a de cima, a última que entrou.",
            ) { hot() }
            cena("fat de um devolve um. A caixa dele sai, e o valor desce.") {
                mostra(primeiraVolta)
                add(rotulo("v", "devolveu ${(prog.events[primeiraVolta] as RtEvent.Return).value}", 100f, 370f, Tint.YELLOW, 16f))
            }
            cena("Cada caixa que sai leva o valor para a de baixo. No fim, sobra o main com ${prog.result}.", who = PROF, mood = EpMood.HAPPY) {
                mostra(prog.events.lastIndex - 1)
                text("v", "fat(3) = ${prog.result}")
            }
        },
    )
}
