package com.compilandia.engine

import com.compilandia.textos.tx

/**
 * J1, a Bancada do Byte: montar, rodar, ver onde quebra, consertar.
 *
 * Todo jogo de montar devolve o mesmo formato: um resultado por teste e, para o primeiro que falha,
 * uma frase com a entrada exata e o que deu errado. Quem decide o certo é o motor
 * (o simulador MIPS, o AFD da expressão regular), nunca o desenho.
 */
data class ResultadoTeste(
    /** A entrada já escrita para a tela: "a = 7, b = 1" ou "0110". */
    val entrada: String,
    val passou: Boolean,
    /** O que aconteceu: "x = 14", "entrou". */
    val obtido: String,
    /** O que devia acontecer. */
    val esperado: String,
    /** O teste não estava na lista: o motor achou essa entrada procurando onde o programa falha. */
    val achado: Boolean = false,
)

/** O veredito da bancada. [falha] é o índice do teste que o palco mostra; null quando tudo passou; -1 quando nem deu para rodar. */
data class Veredito(val testes: List<ResultadoTeste>, val falha: Int?, val frase: String) {
    val passou: Boolean get() = falha == null
}

// ============================================================ J2 Robô MIPS

/**
 * J2: o Byte carrega caixas entre a prateleira da memória e os registradores.
 * O programa é montado com cartas de instrução; quem executa é o [MipsVM].
 */
object RoboMips {
    val REGISTRADORES = listOf("\$t0", "\$t1", "\$t2")

    /** Mais que isso e o programa não termina: laço sem saída. */
    const val PASSOS_MAX = 200

    /** O palco mostra até esse número de quadros. Um laço infinito para aqui. */
    const val QUADROS_MAX = 160

    const val LINHAS_MAX = 12

    class Fase(
        val id: String,
        val nome: String,
        val pedido: String,
        /** As caixas da prateleira, na ordem em que aparecem. */
        val variaveis: List<String>,
        /** As caixas que o teste confere no fim. */
        val saidas: List<String>,
        val cartas: List<String>,
        /** Uma resposta certa. Os testes garantem que ela passa. */
        val solucao: List<String>,
        /** Cada teste dá o valor inicial das caixas; as que faltam começam em 0. */
        val testes: List<Map<String, Long>>,
        val dicas: List<String>,
        val alvo: (Map<String, Long>) -> Map<String, Long>,
    ) {
        /** Instruções da resposta mais curta (rótulo não conta). */
        val minimo: Int get() = solucao.count { !eRotulo(it) }
    }

    fun eRotulo(linha: String) = linha.trim().endsWith(":")

    val fases: List<Fase> by lazy {
        listOf(
            Fase(
                "soma", tx("robomips_fase_soma"), tx("robomips_pedido_soma"),
                listOf("a", "b", "x"), listOf("x"),
                listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t1", "sw \$t2, x", "add \$t2, \$t0, \$t0", "sw \$t0, x"),
                listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t1", "sw \$t2, x"),
                listOf(mapOf("a" to 2L, "b" to 3L), mapOf("a" to 7L, "b" to 1L), mapOf("a" to 0L, "b" to 5L)),
                listOf(tx("robomips_dica_soma_1"), tx("robomips_dica_soma_2")),
            ) { m -> mapOf("x" to m.v("a") + m.v("b")) },
            Fase(
                "dobro", tx("robomips_fase_dobro"), tx("robomips_pedido_dobro"),
                listOf("a", "b", "x"), listOf("x"),
                listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t1", "add \$t2, \$t2, \$t2", "add \$t2, \$t0, \$t0", "sw \$t2, x", "sw \$t1, x"),
                listOf("lw \$t0, a", "lw \$t1, b", "add \$t2, \$t0, \$t1", "add \$t2, \$t2, \$t2", "sw \$t2, x"),
                listOf(mapOf("a" to 1L, "b" to 2L), mapOf("a" to 4L, "b" to 0L), mapOf("a" to 3L, "b" to 3L)),
                listOf(tx("robomips_dica_dobro_1"), tx("robomips_dica_dobro_2")),
            ) { m -> mapOf("x" to 2 * (m.v("a") + m.v("b"))) },
            Fase(
                "troca", tx("robomips_fase_troca"), tx("robomips_pedido_troca"),
                listOf("a", "b"), listOf("a", "b"),
                listOf("lw \$t0, a", "lw \$t1, b", "sw \$t0, b", "sw \$t1, a", "sw \$t0, a", "lw \$t0, b"),
                listOf("lw \$t0, a", "lw \$t1, b", "sw \$t0, b", "sw \$t1, a"),
                listOf(mapOf("a" to 2L, "b" to 3L), mapOf("a" to 9L, "b" to 1L), mapOf("a" to 5L, "b" to 5L)),
                listOf(tx("robomips_dica_troca_1"), tx("robomips_dica_troca_2")),
            ) { m -> mapOf("a" to m.v("b"), "b" to m.v("a")) },
            Fase(
                "se", tx("robomips_fase_se"), tx("robomips_pedido_se"),
                listOf("a", "b", "x"), listOf("x"),
                listOf("lw \$t0, a", "lw \$t1, b", "bne \$t0, \$t1, fim", "beq \$t0, \$t1, fim", "li \$t2, 1", "sw \$t2, x", "fim:"),
                listOf("lw \$t0, a", "lw \$t1, b", "bne \$t0, \$t1, fim", "li \$t2, 1", "sw \$t2, x", "fim:"),
                listOf(mapOf("a" to 3L, "b" to 3L), mapOf("a" to 2L, "b" to 5L), mapOf("a" to 4L, "b" to 4L), mapOf("a" to 0L, "b" to 1L)),
                listOf(tx("robomips_dica_se_1"), tx("robomips_dica_se_2")),
            ) { m -> mapOf("x" to if (m.v("a") == m.v("b")) 1L else 0L) },
            Fase(
                "laco", tx("robomips_fase_laco"), tx("robomips_pedido_laco"),
                listOf("n", "x"), listOf("x"),
                listOf(
                    "lw \$t0, n", "laco:", "beq \$t0, \$zero, fim", "add \$t2, \$t2, \$t0", "addi \$t0, \$t0, -1",
                    "addi \$t0, \$t0, 1", "j laco", "fim:", "sw \$t2, x",
                ),
                listOf("lw \$t0, n", "laco:", "beq \$t0, \$zero, fim", "add \$t2, \$t2, \$t0", "addi \$t0, \$t0, -1", "j laco", "fim:", "sw \$t2, x"),
                listOf(mapOf("n" to 3L), mapOf("n" to 1L), mapOf("n" to 0L), mapOf("n" to 4L)),
                listOf(tx("robomips_dica_laco_1"), tx("robomips_dica_laco_2")),
            ) { m -> mapOf("x" to (1..m.v("n")).sum()) },
        )
    }

    private fun Map<String, Long>.v(k: String) = this[k] ?: 0L

    /** Onde o Byte está no palco. [nome] é a caixa: "a", "\$t0". Vazio no somador e na porta. */
    enum class Lugar { PORTA, MEMORIA, REGISTRADOR, SOMADOR }

    /** Um quadro do palco: o Byte num lugar, talvez com um caixote, e as caixas com seus valores. */
    data class Quadro(
        /** A carta acesa no programa; -1 antes de começar e no fim. */
        val linha: Int,
        val lugar: Lugar,
        val nome: String,
        val carrega: Long?,
        val mem: Map<String, Long>,
        val regs: Map<String, Long>,
        /** A conta no somador: "3 + 4 = 7". */
        val somador: String?,
        /** A caixa que pisca neste quadro. */
        val acende: String?,
        /** A legenda do passo. */
        val nota: String,
    )

    data class Execucao(val quadros: List<Quadro>, val mem: Map<String, Long>, val erro: String?)

    private fun reg(r: String) = if (r == "\$zero" || r == "\$0") "\$zero" else r

    /** Roda o programa com os valores iniciais de [entrada] e devolve os quadros do palco. */
    fun roda(f: Fase, programa: List<String>, entrada: Map<String, Long>): Execucao {
        val mem0 = f.variaveis.associateWith { entrada[it] ?: 0L }
        val fonte = buildString {
            appendLine(".data")
            for (v in f.variaveis) appendLine("$v: .word ${mem0[v]}")
            appendLine(".text")
            appendLine("main:")
            for (l in programa) appendLine(l)
        }
        // As cartas começam depois de .data, das variáveis, de .text e de main:.
        val desloc = f.variaveis.size + 3
        val vm = try { MipsVM(fonte, PASSOS_MAX) } catch (e: MipsVM.AsmError) { return Execucao(emptyList(), mem0, e.message) }
        fun mem() = f.variaveis.associateWith { vm.word(it) ?: 0L }
        fun regs() = REGISTRADORES.associateWith { vm.regs[it] ?: 0L }
        fun valor(r: String, rs: Map<String, Long>) = if (reg(r) == "\$zero") 0L else rs[r] ?: r.toLongOrNull() ?: 0L
        val q = mutableListOf(Quadro(-1, Lugar.PORTA, "", null, mem(), regs(), null, null, tx("robomips_nota_inicio")))
        var erro: String? = null
        try {
            while (q.size < QUADROS_MAX) {
                val pc = vm.pc
                if (vm.halted || pc >= vm.program.size) break
                val ins = vm.program[pc]
                val linha = ins.line - desloc
                val m0 = mem()
                val r0 = regs()
                if (!vm.step()) break
                val a = ins.args.map { it.trim() }
                when (ins.op) {
                    "lw" -> {
                        q += Quadro(linha, Lugar.MEMORIA, a[1], m0[a[1]], m0, r0, null, a[1], tx("robomips_nota_lw", a[1], a[0]))
                        q += Quadro(linha, Lugar.REGISTRADOR, a[0], null, mem(), regs(), null, a[0], tx("robomips_nota_lw", a[1], a[0]))
                    }
                    "sw" -> {
                        q += Quadro(linha, Lugar.REGISTRADOR, a[0], r0[a[0]], m0, r0, null, a[0], tx("robomips_nota_sw", a[0], a[1]))
                        q += Quadro(linha, Lugar.MEMORIA, a[1], null, mem(), regs(), null, a[1], tx("robomips_nota_sw", a[0], a[1]))
                    }
                    "add", "addi" -> {
                        val v1 = valor(a[1], r0)
                        val v2 = if (ins.op == "addi") a[2].toLong() else valor(a[2], r0)
                        val conta = if (v2 < 0) "$v1 - ${-v2} = ${v1 + v2}" else "$v1 + $v2 = ${v1 + v2}"
                        val nota = if (ins.op == "addi") tx("robomips_nota_addi", a[1], a[2], a[0]) else tx("robomips_nota_add", a[1], a[2], a[0])
                        q += Quadro(linha, Lugar.REGISTRADOR, a[1], null, m0, r0, null, a[1], nota)
                        if (ins.op == "add" && a[2] != a[1]) q += Quadro(linha, Lugar.REGISTRADOR, a[2], null, m0, r0, null, a[2], nota)
                        q += Quadro(linha, Lugar.SOMADOR, "", v1 + v2, m0, r0, conta, null, nota)
                        q += Quadro(linha, Lugar.REGISTRADOR, a[0], null, mem(), regs(), null, a[0], nota)
                    }
                    "li" -> q += Quadro(linha, Lugar.REGISTRADOR, a[0], null, mem(), regs(), null, a[0], tx("robomips_nota_li", a[1], a[0]))
                    "beq", "bne" -> {
                        val v1 = valor(a[0], r0)
                        val v2 = valor(a[1], r0)
                        val pulou = vm.pc != pc + 1
                        val conta = if (v1 == v2) "$v1 = $v2" else "$v1 ≠ $v2"
                        val nota = if (pulou) tx("robomips_nota_desvio_pula", conta, a[2]) else tx("robomips_nota_desvio_segue", conta)
                        q += Quadro(linha, Lugar.SOMADOR, "", null, mem(), regs(), conta, null, nota)
                    }
                    "j" -> q += Quadro(linha, q.last().lugar, q.last().nome, null, mem(), regs(), null, null, tx("robomips_nota_j", a[0]))
                    else -> q += Quadro(linha, q.last().lugar, q.last().nome, null, mem(), regs(), null, null, ins.text)
                }
            }
            if (q.size >= QUADROS_MAX) {
                // O palco para aqui. O motor termina a conta para saber se o laço tem fim.
                while (vm.step()) { }
            }
        } catch (e: MipsVM.AsmError) {
            erro = e.message
        }
        val fim = mem()
        q += Quadro(-1, Lugar.PORTA, "", null, fim, regs(), null, null, if (erro != null) tx("robomips_nota_parou", erro) else tx("robomips_nota_fim"))
        return Execucao(q, fim, erro)
    }

    private fun mostra(m: Map<String, Long>, chaves: List<String>) = chaves.joinToString(", ") { "$it = ${m[it] ?: 0}" }

    /** A entrada de um teste como a tela mostra: só as caixas que o teste preenche. */
    fun entrada(t: Map<String, Long>) = t.entries.joinToString(", ") { "${it.key} = ${it.value}" }

    /** Roda todos os testes. O primeiro que falha vira a frase do Byte. */
    fun confere(f: Fase, programa: List<String>): Veredito {
        if (programa.none { !eRotulo(it) }) return Veredito(emptyList(), -1, tx("bancada_programa_vazio"))
        val res = f.testes.map { t ->
            val ex = roda(f, programa, t)
            val quer = f.alvo(f.variaveis.associateWith { t[it] ?: 0L })
            val ok = ex.erro == null && f.saidas.all { ex.mem[it] == quer[it] }
            ResultadoTeste(entrada(t), ok, ex.erro?.let { tx("robomips_nao_terminou") } ?: mostra(ex.mem, f.saidas), mostra(quer, f.saidas))
        }
        val i = res.indexOfFirst { !it.passou }
        if (i < 0) {
            val n = programa.count { !eRotulo(it) }
            val frase = if (n > f.minimo) tx("robomips_passou_curto", res.size, f.minimo) else tx("bancada_passou", res.size)
            return Veredito(res, null, frase)
        }
        val r = res[i]
        val ex = roda(f, programa, f.testes[i])
        val frase = if (ex.erro != null) tx("robomips_falha_erro", r.entrada, ex.erro) else tx("robomips_falha", r.entrada, r.obtido, r.esperado)
        return Veredito(res, i, frase)
    }

    /**
     * A terceira dica: aponta a primeira linha que difere da resposta, sem dar o resto.
     * Null quando o começo do programa já bate com a resposta inteira.
     */
    fun proximaLinha(f: Fase, programa: List<String>): Pair<Int, String>? {
        val i = f.solucao.indices.firstOrNull { it >= programa.size || programa[it].trim() != f.solucao[it] } ?: return null
        return i to f.solucao[i]
    }
}

// ============================================================ J3 Trilho do autômato

/**
 * J3: a pessoa liga os trilhos (a tabela de transição) e marca os estados finais.
 * O Byte, como trem, lê a palavra letra por letra. O certo vem do AFD da expressão regular,
 * e o contraexemplo é a menor palavra em que os dois discordam ([DfaCheck.diferenca]).
 */
object Trilho {
    class Fase(
        val id: String,
        val nome: String,
        val pedido: String,
        val estados: Int,
        /** As colunas da tabela. No identificador, L é qualquer letra e D qualquer dígito. */
        val alfabeto: List<Char>,
        val regex: String,
        /** Palavras como a pessoa lê. [classe] leva cada letra para a coluna da tabela. */
        val testes: List<String>,
        val dicas: List<String>,
        /** Uma resposta certa, linha a linha da tabela: estado → (símbolo → destino). */
        val solucao: Map<Pair<Int, Char>, Int>,
        val finaisSolucao: Set<Int>,
        /** Por que a palavra devia entrar ou ficar de fora: "termina em 0", "tem 3 uns". */
        val motivo: (String) -> String,
    ) {
        val alvo: DFA by lazy { DFA.fromRegex(regex) }
        val identificador: Boolean get() = 'L' in alfabeto

        fun classe(c: Char): Char = if (!identificador) c else if (c.isDigit()) 'D' else 'L'

        /** O rótulo da coluna e da seta: "0", "1", "a-z", "0-9". */
        fun rotulo(c: Char): String = when (c) { 'L' -> "a-z"; 'D' -> "0-9"; else -> c.toString() }

        /** Uma palavra de colunas (LDL) virada em palavra que se lê (x1x). */
        fun legivel(w: String): String = if (!identificador) w else w.map { if (it == 'D') '1' else 'x' }.joinToString("")
    }

    val fases: List<Fase> by lazy {
        fun uns(w: String) = w.count { it == '1' }
        listOf(
            Fase(
                "termina1", tx("trilho_fase_termina1"), tx("trilho_pedido_termina1"), 2, listOf('0', '1'), "(0|1)*1",
                listOf("1", "10", "101", "0", "0011", ""),
                listOf(tx("trilho_dica_termina1_1"), tx("trilho_dica_termina1_2")),
                mapOf((0 to '0') to 0, (0 to '1') to 1, (1 to '0') to 0, (1 to '1') to 1), setOf(1),
            ) { w -> if (w.isEmpty()) tx("trilho_motivo_vazia") else if (w.endsWith("1")) tx("trilho_motivo_termina", "1") else tx("trilho_motivo_termina", "0") },
            Fase(
                "par", tx("trilho_fase_par"), tx("trilho_pedido_par"), 2, listOf('0', '1'), "0*(10*10*)*",
                listOf("", "1", "11", "101", "0110", "111"),
                listOf(tx("trilho_dica_par_1"), tx("trilho_dica_par_2")),
                mapOf((0 to '0') to 0, (0 to '1') to 1, (1 to '0') to 1, (1 to '1') to 0), setOf(0),
            ) { w -> when (uns(w)) { 0 -> tx("trilho_motivo_nenhum_um"); 1 -> tx("trilho_motivo_um_so"); else -> tx("trilho_motivo_uns", uns(w)) } },
            Fase(
                "contem00", tx("trilho_fase_contem00"), tx("trilho_pedido_contem00"), 3, listOf('0', '1'), "(0|1)*00(0|1)*",
                listOf("00", "0", "1001", "0101", "100", "11"),
                listOf(tx("trilho_dica_contem00_1"), tx("trilho_dica_contem00_2")),
                mapOf((0 to '0') to 1, (0 to '1') to 0, (1 to '0') to 2, (1 to '1') to 0, (2 to '0') to 2, (2 to '1') to 2), setOf(2),
            ) { w -> if ("00" in w) tx("trilho_motivo_tem00") else tx("trilho_motivo_sem00") },
            Fase(
                "ident", tx("trilho_fase_ident"), tx("trilho_pedido_ident"), 2, listOf('L', 'D'), "L(L|D)*",
                listOf("x", "x1", "1x", "soma2", "9", ""),
                listOf(tx("trilho_dica_ident_1"), tx("trilho_dica_ident_2")),
                mapOf((0 to 'L') to 1, (1 to 'L') to 1, (1 to 'D') to 1), setOf(1),
            ) { w -> if (w.isEmpty()) tx("trilho_motivo_vazia") else if (w.first().isDigit()) tx("trilho_motivo_comeca_digito") else tx("trilho_motivo_comeca_letra") },
        )
    }

    /** O autômato que a pessoa montou. Célula sem trilho fica fora do mapa. */
    fun montado(f: Fase, trilhos: Map<Pair<Int, Char>, Int>, finais: Set<Int>) = DFA(f.estados, 0, finais, f.alfabeto, trilhos)

    /** Um quadro: quantas letras o trem já leu, onde está e por qual trilho veio. */
    data class Quadro(val lidas: Int, val estado: Int, val veioDe: Int?, val semTrilho: Boolean, val nota: String)

    data class Corrida(val palavra: String, val quadros: List<Quadro>, val aceitou: Boolean, val semTrilho: Boolean, val parouEm: Int, val letraQuebra: Int?)

    /** O trem lê [palavra] (como a pessoa escreve) no autômato [d]. */
    fun corre(f: Fase, d: DFA, palavra: String): Corrida {
        var q = d.start
        val qs = mutableListOf(Quadro(0, q, null, false, tx("trilho_nota_inicio", q)))
        for ((i, ch) in palavra.withIndex()) {
            val c = f.classe(ch)
            val p = d.step(q, c)
            if (p < 0) {
                qs += Quadro(i, q, null, true, tx("trilho_nota_sem_trilho", q, ch.toString()))
                return Corrida(palavra, qs, false, true, q, i)
            }
            qs += Quadro(i + 1, p, q, false, tx("trilho_nota_anda", ch.toString(), q, p))
            q = p
        }
        val aceitou = q in d.accepting
        qs += Quadro(palavra.length, q, null, false, if (aceitou) tx("trilho_nota_final", q) else tx("trilho_nota_nao_final", q))
        return Corrida(palavra, qs, aceitou, false, q, null)
    }

    private fun mostra(w: String) = w.ifEmpty { "ε" }

    private fun obtido(c: Corrida) = when {
        c.semTrilho -> tx("trilho_resultado_sem_trilho")
        c.aceitou -> tx("trilho_resultado_entrou")
        else -> tx("trilho_resultado_fora")
    }

    /** As palavras de teste e, se escapou alguma, a menor delas no fim da lista. */
    fun palavras(f: Fase, d: DFA): List<Pair<String, Boolean>> {
        val ce = DfaCheck.diferenca(d, f.alvo)?.let { f.legivel(it) }
        return f.testes.map { it to false } + if (ce != null && ce !in f.testes) listOf(ce to true) else emptyList()
    }

    fun deveEntrar(f: Fase, palavra: String) = f.alvo.accepts(palavra.map { f.classe(it) }.joinToString(""))

    fun confere(f: Fase, d: DFA): Veredito {
        val lista = palavras(f, d)
        val res = lista.map { (w, achado) ->
            val c = corre(f, d, w)
            val quer = deveEntrar(f, w)
            ResultadoTeste(mostra(w), c.aceitou == quer, obtido(c), if (quer) tx("trilho_resultado_entrou") else tx("trilho_resultado_fora"), achado)
        }
        // A menor palavra que escapou é a que mais ensina; sem ela, o primeiro teste que falhou.
        val ce = DfaCheck.diferenca(d, f.alvo)?.let { f.legivel(it) }
        if (ce == null) return Veredito(res, null, tx("trilho_passou", res.size))
        val i = lista.indexOfFirst { it.first == ce }
        val c = corre(f, d, ce)
        val quer = deveEntrar(f, ce)
        val mas = when {
            c.semTrilho -> tx("trilho_mas_sem_trilho", c.parouEm, ce[c.letraQuebra!!].toString())
            c.aceitou -> tx("trilho_mas_final", c.parouEm)
            else -> tx("trilho_mas_nao_final", c.parouEm)
        }
        val frase = tx("trilho_falha", mostra(ce), f.motivo(ce), if (quer) tx("trilho_devia_entrar") else tx("trilho_devia_ficar_fora"), mas)
        return Veredito(res, i, frase)
    }

    /**
     * A terceira dica: aponta a célula ou o estado em que a menor palavra que escapou termina.
     * Não diz o destino certo; diz onde olhar.
     */
    fun ondeOlhar(f: Fase, d: DFA): String? {
        val ce = DfaCheck.diferenca(d, f.alvo)?.let { f.legivel(it) } ?: return null
        val c = corre(f, d, ce)
        return if (c.semTrilho) tx("trilho_olhe_celula", c.parouEm, f.rotulo(f.classe(ce[c.letraQuebra!!])), mostra(ce))
        else tx("trilho_olhe_estado", mostra(ce), c.parouEm)
    }
}
