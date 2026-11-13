package com.compilandia.content.episodes

import com.compilandia.content.fecho
import com.compilandia.content.Fecho
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
import com.compilandia.engine.Expr
import com.compilandia.engine.ExprGenerator
import com.compilandia.engine.ExprParser
import com.compilandia.engine.Frame
import com.compilandia.engine.ParseNode
import com.compilandia.engine.RegAlloc
import com.compilandia.engine.RtEvent
import com.compilandia.engine.RuntimeSim
import com.compilandia.engine.ScopeGen
import com.compilandia.engine.TacGen
import com.compilandia.engine.Type
import com.compilandia.engine.TypeChecker
import com.compilandia.learning.Skill
import com.compilandia.textos.tx
import kotlin.random.Random

/*
 * Aulas de semântica e back-end. Desde o A9, as seis aulas básicas daqui são longas, com o roteiro do A2:
 * por que existe, primeiro exemplo (as cenas da aula curta), a receita à vista e um segundo exemplo em que
 * a pessoa calcula. Tudo o que o segundo exemplo mostra sai do motor; A9BackendFactsTest confere as falas.
 */

private fun no(s: String, vararg f: ParseNode) = ParseNode(s, f.toList())

/** A AST do motor desenhada como árvore no palco. */
private fun Expr.paraNo(): ParseNode = ParseNode(text, children.map { it.paraNo() })

/** Cada nó com o id que [arvore] dá a ele: t, t0, t01... */
private fun Expr.caminhos(p: String = "t"): List<Pair<String, Expr>> =
    listOf(p to this) + children.flatMapIndexed { i, c -> c.caminhos("$p$i") }

/** Etiqueta de tipo logo abaixo de um nó. */
private fun tipoDe(id: String, t: String, alvo: Box, tint: Tint = Tint.YELLOW) =
    Box(id, t, alvo.x, alvo.y + 26f, 56f, 20f, Look.LABEL, tint, size = 14f)

/** Linhas de um programa do motor sem o número que vem na frente. */
private fun semNumero(code: String) = code.lines().map { it.replace(Regex("^\\d+ ?"), "") }

// ─────────────── entradas dos segundos exemplos (A9), conferidas em A9BackendFactsTest ───────────────

/** Atributos: a conta do segundo exemplo, com c int e y float em [ExprGenerator.env]. */
const val ATTR_EXEMPLO_2 = "c * 2 + y"
/** A mesma conta com uma variável bool no lugar do y. */
const val ATTR_EXEMPLO_2_ERRO = "c * 2 + ok"
/** Escopo: semente do programa que [ScopeGen.random] gera para o segundo exemplo. */
const val ESCOPO_SEMENTE_2 = 4881
/** TAC: a conta e a variável que recebe o resultado. */
const val TAC_EXEMPLO_2 = "( a + b ) * ( c - d )"
const val TAC_DESTINO_2 = "x"
/** Otimização: a conta em que uma dobra puxa a outra. */
const val OTIM_EXEMPLO_2 = "( 10 - 2 * 3 ) * x"
/** Registros de ativação: o argumento que o main passa para f em [RuntimeSim.chain]. */
const val RT_ARGUMENTO_2 = 1
/** Alocação: a conta cujos temporários a pessoa coloca no grafo. */
const val REG_EXEMPLO_2 = "a * b + ( c - d ) * e"

/** As linhas que a aula de escopo acende no segundo exemplo, tiradas do programa do motor. */
class EscopoExemplo2(val prog: ScopeGen.Program) {
    private val l = prog.lines
    private fun declaradaEm(i: Int, nivel: Int) = l[i].resolvesTo?.let { l[it].indent == nivel } == true
    /** Uso no bloco de dentro que acha o nome na tabela de dentro. */
    val usoDentro = l.indices.first { l[it].indent == 2 && declaradaEm(it, 2) }
    /** Uso no bloco de dentro que só acha o nome na tabela de fora. */
    val usoFora = l.indices.first { l[it].indent == 2 && declaradaEm(it, 1) }
    /** A chave que fecha o bloco de dentro. */
    val fecha = l.indices.first { l[it].indent == 1 && l[it].text == "}" }
    /** O último uso, já fora do bloco de dentro. */
    val depois = l.indices.last { l[it].indent == 1 && l[it].use != null }
    /** "x float" para cada declaração do nível. */
    fun tabela(nivel: Int) = l.filter { it.indent == nivel && it.declId != null }
        .joinToString(", ") { d -> d.text.removeSuffix(";").split(" ").let { "${it[1]} ${it[0]}" } }
}

fun escopoExemplo2() = EscopoExemplo2(ScopeGen.random(Random(ESCOPO_SEMENTE_2)))

val semantica by lazy {
    listOf(
        // ─────────────────────────────── ATRIBUTOS ───────────────────────────────
        episodio(Skill.ATTRIBUTES_TYPES, tx("epbackend_tipos_sobem_pela_arvore")) {
            // 1. Por que existe
            cena(tx("attr_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("epbackend_tipos_sobem_pela_arvore")))
                add(Box("src", "a + x", 180f, 110f, 120f, 46f, Look.CELL, size = 18f))
                add(Box("ii", "add", 100f, 220f, 120f, 46f, Look.TOKEN, Tint.TEAL, size = 17f), Box("ff", "add.s", 260f, 220f, 120f, 46f, Look.TOKEN, Tint.ORANGE, size = 17f))
                add(rotulo("li", "int + int", 100f, 262f), rotulo("lf", "float + float", 260f, 262f))
            }
            cena(tx("attr_l_02")) { hot("src", "ii", "ff") }
            palavra(tx("attr_l_palavra"), tx("attr_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            val raiz = no("+", no("a"), no("*", no("x"), no("2")))
            val (b, w) = arvore(raiz, 96f, passoY = 80f, largura = 300f)
            fun tipo(id: String, t: String, alvo: Box) = Box(id, t, alvo.x, alvo.y + 26f, 56f, 20f, Look.LABEL, Tint.YELLOW, size = 14f)
            val node = b.associateBy { it.text }
            cena(tx("epbackend_arvore_ja_existe_agora_semantica")) {
                clear()
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
            // 3. A receita
            cena(tx("attr_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("attr_l_r1"), 110f), regra("r2", tx("attr_l_r2"), 166f), regra("r3", tx("attr_l_r3"), 222f), regra("r4", tx("attr_l_r4"), 278f))
            }
            pergunta(tx("attr_l_06"), setOf("r2"), setOf("r1", "r3", "r4"), tx("attr_l_06_dica"), tx("attr_l_06_ok")) { hot() }
            cena(tx("attr_l_07")) { hot("r3") }
            // 4. Segundo exemplo: os tipos saem do TypeChecker do motor
            val e2 = ExprParser.parse(ATTR_EXEMPLO_2)
            val at2 = TypeChecker.evaluate(e2, ExprGenerator.env)
            val nos2 = e2.caminhos()
            val (b2, w2) = arvore(e2.paraNo(), 90f, passoY = 80f, largura = 280f)
            val box2 = b2.associateBy { it.id }
            fun opcaoTipo(t: Type) = "o" + t.name.lowercase()
            fun opcoes(vararg ts: Type) = ts.mapIndexed { i, t -> Box(opcaoTipo(t), t.label, 100f + i * 80f, 370f, 72f, 42f, Look.TOKEN, Tint.YELLOW, size = 16f) }
            val vezes = nos2.first { it.second.text == "*" }
            val folhaVar = nos2.last { it.second.isLeaf }
            cena(tx("attr_l_08")) {
                clear()
                add(titulo(ATTR_EXEMPLO_2))
                add(b2); for (x in w2) wire(x)
                for ((p, e) in nos2) if (e.isLeaf) add(tipoDe("k$p", at2.getValue(e.id).type.label, box2.getValue(p)))
            }
            val tiposConta = listOf(Type.INT, Type.FLOAT, Type.BOOL)
            pergunta(
                tx("attr_l_09"),
                setOf(opcaoTipo(at2.getValue(vezes.second.id).type)), tiposConta.map(::opcaoTipo).toSet() - opcaoTipo(at2.getValue(vezes.second.id).type),
                tx("attr_l_09_dica"), tx("attr_l_09_ok"),
            ) {
                add(opcoes(*tiposConta.toTypedArray()))
                hot(vezes.first)
            }
            pergunta(
                tx("attr_l_10"),
                setOf(opcaoTipo(at2.getValue(e2.id).type)), tiposConta.map(::opcaoTipo).toSet() - opcaoTipo(at2.getValue(e2.id).type),
                tx("attr_l_10_dica"), tx("attr_l_10_ok"),
            ) {
                add(tipoDe("k${vezes.first}", at2.getValue(vezes.second.id).type.label, box2.getValue(vezes.first)))
                hot("t")
            }
            val e3 = ExprParser.parse(ATTR_EXEMPLO_2_ERRO)
            val at3 = TypeChecker.evaluate(e3, ExprGenerator.env)
            val folhaNova = e3.caminhos().first { it.first == folhaVar.first }.second
            val tiposErro = listOf(Type.INT, Type.FLOAT, Type.ERROR)
            pergunta(
                tx("attr_l_11"),
                setOf(opcaoTipo(at3.getValue(e3.id).type)), tiposErro.map(::opcaoTipo).toSet() - opcaoTipo(at3.getValue(e3.id).type),
                tx("attr_l_11_dica"), tx("attr_l_11_ok"),
            ) {
                dropAll("o")
                add(opcoes(*tiposErro.toTypedArray()))
                text(folhaVar.first, folhaNova.text)
                text("k${folhaVar.first}", at3.getValue(folhaNova.id).type.label); tint("k${folhaVar.first}", Tint.RED)
                hot(folhaVar.first)
            }
            cena(tx("attr_l_12"), mood = EpMood.HAPPY) {
                dropAll("o")
                add(tipoDe("kt", at3.getValue(e3.id).type.label, box2.getValue("t"), Tint.RED))
                hot("t")
            }
            fecho(Fecho(
                tx("attr_f_af"), false, tx("attr_f_q"), tx("attr_f_dica"), tx("attr_f_ok"), tx("attr_f_certo"),
                tx("attr_f_erro2"), tx("attr_f_certo2"), tx("attr_f_fala2"),
                listOf(tx("attr_f_z1"), tx("attr_f_z2"), tx("attr_f_z3")), tx("attr_f_resumo"),
            ))
        },

        // ─────────────────────────────── ESCOPO ───────────────────────────────
        episodio(Skill.SCOPE_SYMBOLS, tx("epbackend_quem_esse_x")) {
            // 1. Por que existe
            cena(tx("esc_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("epbackend_escopo")))
                add(codigo("c", listOf("void f() {", "  int i;", "}", "void g() {", "  float i;", "}"), 80f, x = 90f, passo = 30f, size = 16f))
            }
            cena(tx("esc_l_02")) { hot("c1", "c4") }
            palavra(tx("epbackend_escopo").uppercase(), tx("esc_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            val src = listOf("int x;", "void f() {", "  float x;", "  x = 1.5;", "}", "int main() { x = 2; }")
            cena(tx("epbackend_mesmo_nome_pode_existir_em")) {
                clear()
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
            // Uso fora de f, mas dentro de uma função: em C, comando solto no arquivo não compila.
            cena(tx("epbackend_por_isso_x_igual_dois"), mood = EpMood.HAPPY) {
                add(Box("p2", tx("epbackend_main_vazia"), 180f, 328f, 220f, 38f, Look.TOKEN, Tint.ORANGE))
                hot("c5", "p2", "p0")
            }
            // 3. A receita
            cena(tx("esc_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("esc_l_r1"), 110f), regra("r2", tx("esc_l_r2"), 166f), regra("r3", tx("esc_l_r3"), 222f), regra("r4", tx("esc_l_r4"), 278f))
            }
            pergunta(tx("esc_l_06"), setOf("r3"), setOf("r1", "r2", "r4"), tx("esc_l_06_dica"), tx("esc_l_06_ok")) { hot() }
            cena(tx("esc_l_07")) { hot("r4") }
            // 4. Segundo exemplo: o programa e as respostas saem do ScopeGen do motor
            val ex = escopoExemplo2()
            fun tab(id: String, t: String, y: Float, c: Tint) = Box(id, t, 180f, y, 250f, 36f, Look.TOKEN, c, size = 14f)
            cena(tx("esc_l_08")) {
                clear()
                add(titulo(tx("epbackend_escopo")))
                add(codigo("c", ex.prog.lines.map { "  ".repeat(it.indent) + it.text }, 52f, x = 70f, passo = 21f, size = 14f))
                add(tab("pf", tx("esc_l_fora", ex.tabela(1)), 374f, Tint.TEAL))
                hot("c0")
            }
            pergunta(tx("esc_l_09"), setOf("pd"), setOf("pf"), tx("esc_l_09_dica"), tx("esc_l_09_ok")) {
                add(tab("pd", tx("esc_l_dentro", ex.tabela(2)), 332f, Tint.ORANGE))
                hot("c${ex.usoDentro}")
            }
            pergunta(tx("esc_l_10"), setOf("pf"), setOf("pd"), tx("esc_l_10_dica"), tx("esc_l_10_ok")) { hot("c${ex.usoFora}") }
            pergunta(tx("esc_l_11"), setOf("pd"), setOf("pf"), tx("esc_l_11_dica"), tx("esc_l_11_ok")) { hot("c${ex.fecha}") }
            cena(tx("esc_l_12"), mood = EpMood.HAPPY) {
                drop("pd")
                hot("c${ex.depois}", "pf")
            }
            fecho(Fecho(
                tx("esc_f_af"), true, tx("esc_f_q"), tx("esc_f_dica"), tx("esc_f_ok"), tx("esc_f_af"),
                tx("esc_f_erro2"), tx("esc_f_certo2"), tx("esc_f_fala2"),
                listOf(tx("esc_f_z1"), tx("esc_f_z2"), tx("esc_f_z3")), tx("esc_f_resumo"),
            ))
        },

        // ─────────────────────────────── AST ───────────────────────────────
    )
}

val backend by lazy {
    listOf(
        // ─────────────────────────────── TAC ───────────────────────────────
        episodio(Skill.TAC, tx("epbackend_conta_por_linha")) {
            val code = TacGen.generate(ExprParser.parse(tx("comum_b_c")))
            // 1. Por que existe
            cena(tx("tac_l_01"), mood = EpMood.THINK) {
                add(titulo("TAC"))
                add(Box("src", tx("comum_b_c"), 90f, 170f, 140f, 52f, Look.CELL, size = 17f))
                add(Box("cpu", tx("tac_l_processador"), 270f, 170f, 150f, 52f, Look.CELL, Tint.DARK, size = 15f))
                add(rotulo("lc", tx("tac_l_uma_por_vez"), 270f, 216f))
            }
            pergunta(tx("tac_l_02"), setOf("n2"), setOf("n1", "n3"), tx("tac_l_02_dica"), tx("tac_l_02_ok")) {
                add(opcao("n1", "1", 120f, 300f), opcao("n2", "2", 180f, 300f), opcao("n3", "3", 240f, 300f))
                hot("src")
            }
            palavra("TAC", tx("epbackend_maquina_nao_entende_conta_comprida"))
            // 2. Primeiro exemplo
            comecaExemplo()
            val (b, w) = arvore(no("+", no("a"), no("*", no("b"), no("c"))), 86f, passoY = 62f, largura = 240f)
            cena(tx("tac_l_04")) {
                clear()
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
            // 3. A receita
            cena(tx("tac_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("tac_l_r1"), 110f), regra("r2", tx("tac_l_r2"), 166f), regra("r3", tx("tac_l_r3"), 222f), regra("r4", tx("tac_l_r4"), 278f))
            }
            pergunta(tx("tac_l_06"), setOf("r1"), setOf("r2", "r3", "r4"), tx("tac_l_06_dica"), tx("tac_l_06_ok")) { hot() }
            cena(tx("tac_l_07")) { hot("r3") }
            // 4. Segundo exemplo: as linhas saem do TacGen do motor, na ordem em que ele gera
            val e2 = ExprParser.parse(TAC_EXEMPLO_2)
            val code2 = TacGen.generate(e2, assignTo = TAC_DESTINO_2)
            val caminho = e2.caminhos().associate { it.second.id to it.first }
            val ops = e2.postorder().filter { !it.isLeaf }.map { caminho.getValue(it.id) }
            val (b2, w2) = arvore(e2.paraNo(), 74f, passoY = 54f, largura = 260f)
            fun linha(i: Int) = codigo("l$i", listOf(code2[i].toString()), 230f + i * 30f, x = 110f, size = 17f)
            fun vira(i: Int) {
                val p = ops[i]
                drop("${p}0", "${p}1")
                text(p, code2[i].target); tint(p, Tint.GREEN)
                add(linha(i))
            }
            cena(tx("tac_l_08")) {
                clear()
                add(titulo("$TAC_DESTINO_2 = $TAC_EXEMPLO_2", size = 20f))
                add(b2); for (x in w2) wire(x)
            }
            pergunta(tx("tac_l_09"), setOf(ops[0]), ops.drop(1).toSet(), tx("tac_l_09_dica"), tx("tac_l_09_ok")) {}
            pergunta(tx("tac_l_10"), setOf(ops[1]), setOf(ops[2]), tx("tac_l_10_dica"), tx("tac_l_10_ok")) { vira(0) }
            val raizCerta = code2[2].toString()
            pergunta(tx("tac_l_11"), setOf("w0"), setOf("w1", "w2"), tx("tac_l_11_dica"), tx("tac_l_11_ok")) {
                vira(1)
                // As duas erradas: operandos que não são os temporários dos filhos, ou conta demais numa linha.
                add(
                    Box("w1", "${code2[2].target} = a * d", 180f, 304f, 210f, 36f, Look.CELL, Tint.YELLOW, size = 15f),
                    Box("w0", raizCerta, 180f, 346f, 210f, 36f, Look.CELL, Tint.YELLOW, size = 15f),
                    Box("w2", "${code2[2].target} = ${code2[0].target} * c - d", 180f, 388f, 210f, 36f, Look.CELL, Tint.YELLOW, size = 15f),
                )
                hot(ops[2])
            }
            val temps = code2.dropLast(1).map { it.target }.reversed()
            pergunta(tx("tac_l_12"), setOf("v${code2.last().a}"), temps.map { "v$it" }.toSet() - "v${code2.last().a}", tx("tac_l_12_dica"), tx("tac_l_12_ok")) {
                drop("w0", "w1", "w2")
                vira(2)
                temps.forEachIndexed { i, t -> add(opcao("v$t", t, 120f + i * 60f, 370f)) }
                hot()
            }
            cena(tx("tac_l_13"), mood = EpMood.HAPPY) {
                dropAll("v")
                add(linha(3))
                hot("l00", "l10", "l20", "l30")
            }
            fecho(Fecho(
                tx("tac_f_af"), true, tx("tac_f_q"), tx("tac_f_dica"), tx("tac_f_ok"), tx("tac_f_af"),
                tx("tac_f_erro2"), tx("tac_f_certo2"), tx("tac_f_fala2"),
                listOf(tx("tac_f_z1"), tx("tac_f_z2"), tx("tac_f_z3")), tx("tac_f_resumo"),
            ))
        },

        // ─────────────────────────────── OTIMIZAÇÃO ───────────────────────────────
        episodio(Skill.OPTIMIZATION, tx("epbackend_conta_feita_antes")) {
            // 1. Por que existe
            cena(tx("otim_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("comum_otimizacao_2")))
                add(codigo("c", listOf("for (i = 0; i < 1000; i++)", "  x = 2 * 3 + y;"), 150f, x = 50f, passo = 32f, size = 16f))
                hot("c1")
            }
            cena(tx("otim_l_02")) {
                add(rotulo("mil", "× 1000", 180f, 250f, Tint.ORANGE, 22f))
                hot("c1", "mil")
            }
            palavra(tx("otim_l_palavra"), tx("otim_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            val parts = listOf("x", "=", "2", "*", "3", "+", "y")
            fun item(i: Int, t: String, x: Float) = Box("o$i", t, x, 140f, 40f, 48f, Look.TOKEN, Tint.TEAL, size = 18f)
            cena(tx("epbackend_se_para_fazer_conta_agora")) {
                clear()
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
            // 3. A receita
            cena(tx("otim_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("otim_l_r1"), 110f), regra("r2", tx("otim_l_r2"), 166f), regra("r3", tx("otim_l_r3"), 222f))
            }
            pergunta(tx("otim_l_06"), setOf("r3"), setOf("r1", "r2"), tx("otim_l_06_dica"), tx("otim_l_06_ok")) { hot() }
            cena(tx("otim_l_07")) { hot("r2") }
            // 4. Segundo exemplo: a dobra e as linhas saem do motor (constantFold e TacGen)
            val e2 = ExprParser.parse(OTIM_EXEMPLO_2)
            val nos = e2.caminhos()
            val antes = TacGen.generate(e2).map { it.toString() }
            val depois = TacGen.generate(TacGen.constantFold(e2)).map { it.toString() }
            val primeira = nos.first { (_, e) -> !e.isLeaf && e.children.all { it.kind == Expr.Kind.NUM } }
            val segunda = nos.first { it.first == primeira.first.dropLast(1) }
            val operadores = nos.filter { !it.second.isLeaf }.map { it.first }.toSet()
            val (b2, w2) = arvore(e2.paraNo(), 70f, passoY = 52f, largura = 260f)
            fun dobra(p: Pair<String, Expr>) {
                drop("${p.first}0", "${p.first}1")
                text(p.first, TacGen.constantFold(p.second).text); tint(p.first, Tint.GREEN)
            }
            cena(tx("otim_l_08")) {
                clear()
                add(titulo(OTIM_EXEMPLO_2, size = 22f))
                add(b2); for (x in w2) wire(x)
                add(codigo("b", antes, 286f, x = 110f, passo = 28f, size = 16f))
            }
            pergunta(tx("otim_l_09"), setOf(primeira.first), operadores - primeira.first, tx("otim_l_09_dica"), tx("otim_l_09_ok")) {}
            pergunta(tx("otim_l_10"), setOf(segunda.first), operadores - primeira.first - segunda.first, tx("otim_l_10_dica"), tx("otim_l_10_ok")) {
                dobra(primeira)
            }
            pergunta(tx("otim_l_11"), setOf("d0"), setOf("a0", "a1"), tx("otim_l_11_dica"), tx("otim_l_11_ok")) {
                dobra(segunda)
                dropAll("b")
                add(
                    Box("a0", antes[0], 180f, 292f, 200f, 38f, Look.CELL, Tint.YELLOW, size = 15f),
                    Box("d0", depois[0], 180f, 336f, 200f, 38f, Look.CELL, Tint.YELLOW, size = 15f),
                    Box("a1", antes[1], 180f, 380f, 200f, 38f, Look.CELL, Tint.YELLOW, size = 15f),
                )
                hot("t")
            }
            cena(tx("otim_l_12"), mood = EpMood.HAPPY) {
                drop("a0", "a1")
                move("d0", 180f, 310f); tint("d0", Tint.GREEN)
                hot("d0")
            }
            fecho(Fecho(
                tx("otim_f_af"), false, tx("otim_f_q"), tx("otim_f_dica"), tx("otim_f_ok"), tx("otim_f_certo"),
                tx("otim_f_erro2"), tx("otim_f_certo2"), tx("otim_f_fala2"),
                listOf(tx("otim_f_z1"), tx("otim_f_z2"), tx("otim_f_z3")), tx("otim_f_resumo"),
            ))
        },

        // ─────────────────────────────── REGISTROS DE ATIVAÇÃO ───────────────────────────────
        episodio(Skill.RUNTIME, tx("epbackend_pilha_chamadas")) {
            // 1. Por que existe
            val fat = RuntimeSim.fatorial(2)
            cena(tx("rt_l_01"), mood = EpMood.THINK) {
                add(titulo("fat(2)"))
                add(codigo("c", semNumero(fat.code).take(4), 76f, x = 50f, passo = 28f, size = 16f))
                add(Box("n", "n = 2", 180f, 240f, 110f, 44f, Look.TOKEN, Tint.TEAL, size = 17f))
                hot("c2")
            }
            pergunta(tx("rt_l_02"), setOf("v2"), setOf("v1"), tx("rt_l_02_dica"), tx("rt_l_02_ok")) {
                drop("n")
                add(Box("v2", "n = 2", 120f, 300f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f), Box("v1", "n = 1", 240f, 300f, 110f, 44f, Look.TOKEN, Tint.YELLOW, size = 17f))
                hot()
            }
            palavra(tx("rt_l_palavra"), tx("rt_l_03"))
            // 2. Primeiro exemplo: o fat do motor para em n = 1 (if n <= 1)
            comecaExemplo()
            fun reg(id: String, t: String, i: Int, c: Tint) = Box(id, t, 180f, 330f - i * 62f, 250f, 54f, Look.TOKEN, c, size = 15f)
            cena(tx("epbackend_toda_chamada_funcao_ganha_registro")) {
                clear()
                add(titulo("fat(2)"))
                add(reg("r0", tx("epbackend_fat_n_2_volta_main"), 0, Tint.TEAL))
            }
            cena(tx("epbackend_ali_moram_parametros_variaveis_locais")) { hot("r0") }
            cena(tx("epbackend_fat_dois_chama_fat_outro")) {
                add(reg("r1", tx("epbackend_fat_n_1_volta_fat"), 1, Tint.ORANGE))
                hot("r1")
            }
            cena(tx("rt_l_04")) { hot("r1") }
            pergunta(
                tx("comum_quem_retorna_primeiro_toque_no"),
                setOf("r1"), setOf("r0"),
                tx("epbackend_so_topo_pilha_esta_rodando"),
                tx("comum_isso_ultimo_entrar_primeiro_sair"),
            ) { hot() }
            cena(tx("epbackend_retornou_registro_sai_controle_volta")) {
                drop("r1")
                hot("r0")
            }
            cena(tx("epbackend_por_isso_recursao_funciona_cada"), mood = EpMood.HAPPY) { hot() }
            // 3. A receita
            cena(tx("rt_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("rt_l_r1"), 110f), regra("r2", tx("rt_l_r2"), 166f), regra("r3", tx("rt_l_r3"), 222f))
            }
            pergunta(tx("rt_l_06"), setOf("r3"), setOf("r1", "r2"), tx("rt_l_06_dica"), tx("rt_l_06_ok")) { hot() }
            cena(tx("rt_l_07")) { hot("r1") }
            // 4. Segundo exemplo: as chamadas e os valores saem do RuntimeSim do motor
            val prog = RuntimeSim.chain(RT_ARGUMENTO_2)
            val chamadas = prog.events.filterIsInstance<RtEvent.Call>()
            val linhas = semNumero(prog.code).take(9)
            fun quadro(f: Frame) = if (f.args.isEmpty()) f.fn else f.fn + "  " + f.args.entries.joinToString { "${it.key} = ${it.value}" }
            fun pos(nivel: Int, f: Frame) = Box("k$nivel", quadro(f), 110f, 386f - nivel * 42f, 180f, 38f, Look.TOKEN, if (nivel % 2 == 0) Tint.TEAL else Tint.ORANGE, size = 15f)
            fun empilha(c: RtEvent.Call) = add(pos(c.stack.size - 1, c.frame))
            fun linhaCom(s: String) = "c" + linhas.indexOfFirst { s in it }
            val g = chamadas[2]
            val h = chamadas[3]
            val depoisDoG = prog.events.filterIsInstance<RtEvent.Return>().first { it.fn == g.frame.fn }.stack
            cena(tx("rt_l_08")) {
                clear()
                add(titulo("f($RT_ARGUMENTO_2)"))
                add(codigo("c", linhas, 56f, x = 40f, passo = 20f, size = 13f))
                empilha(chamadas[0]); empilha(chamadas[1])
                hot("k1")
            }
            pergunta(tx("rt_l_09"), setOf("f${g.frame.fn}"), setOf("f${h.frame.fn}") - "f${g.frame.fn}", tx("rt_l_09_dica"), tx("rt_l_09_ok")) {
                add(Box("fg", "g", 290f, 290f, 64f, 42f, Look.TOKEN, Tint.YELLOW, size = 18f), Box("fh", "h", 290f, 340f, 64f, 42f, Look.TOKEN, Tint.YELLOW, size = 18f))
                hot(linhaCom("g(c)"))
            }
            val aDoH = h.frame.args.getValue("a")
            pergunta(tx("rt_l_10"), setOf("a$aDoH"), (1..3).map { "a$it" }.toSet() - "a$aDoH", tx("rt_l_10_dica"), tx("rt_l_10_ok")) {
                drop("fg", "fh")
                empilha(g)
                add((1..3).map { Box("a$it", "a = $it", 300f, 230f + it * 46f, 90f, 40f, Look.TOKEN, Tint.YELLOW, size = 16f) })
                hot(linhaCom("h(b"))
            }
            pergunta(tx("rt_l_11"), setOf("n${h.stack.size}"), (3..5).map { "n$it" }.toSet() - "n${h.stack.size}", tx("rt_l_11_dica"), tx("rt_l_11_ok")) {
                dropAll("a")
                empilha(h)
                add((3..5).map { Box("n$it", "$it", 300f, 140f + it * 46f, 60f, 40f, Look.TOKEN, Tint.YELLOW, size = 17f) })
                hot()
            }
            val topo = "k${depoisDoG.size - 1}"
            pergunta(tx("rt_l_12"), setOf(topo), (0 until depoisDoG.size - 1).map { "k$it" }.toSet(), tx("rt_l_12_dica"), tx("rt_l_12_ok")) {
                dropAll("n")
                for (nivel in depoisDoG.size until h.stack.size) drop("k$nivel")
            }
            cena(tx("rt_l_13")) {
                empilha(chamadas[4])
                hot("k${chamadas[4].stack.size - 1}", linhaCom("h(c)"))
            }
            cena(tx("rt_l_14"), mood = EpMood.HAPPY) {
                for (nivel in 1 until chamadas[4].stack.size) drop("k$nivel")
                add(Box("res", "f($RT_ARGUMENTO_2) = ${prog.result}", 290f, 386f, 120f, 38f, Look.TOKEN, Tint.GREEN, size = 15f))
                hot("k0", "res")
            }
            fecho(Fecho(
                tx("rt_f_af"), true, tx("rt_f_q"), tx("rt_f_dica"), tx("rt_f_ok"), tx("rt_f_af"),
                tx("rt_f_erro2"), tx("rt_f_certo2"), tx("rt_f_fala2"),
                listOf(tx("rt_f_z1"), tx("rt_f_z2"), tx("rt_f_z3")), tx("rt_f_resumo"),
            ))
        },

        // ─────────────────────────────── MIPS ───────────────────────────────

        // ─────────────────────────────── REGISTRADORES ───────────────────────────────
        episodio(Skill.REG_ALLOC, tx("epbackend_colorir_grafo")) {
            // 1. Por que existe
            cena(tx("reg_l_01"), mood = EpMood.THINK) {
                add(titulo(tx("epbackend_alocacao")))
                add(Box("mem", tx("comum_memoria"), 90f, 180f, 130f, 52f, Look.TOKEN, Tint.BLUE, size = 15f), Box("reg", tx("palco_registradores"), 270f, 180f, 150f, 52f, Look.TOKEN, Tint.ORANGE, size = 15f))
            }
            pergunta(tx("reg_l_02"), setOf("reg"), setOf("mem"), tx("reg_l_02_dica"), tx("reg_l_02_ok")) { hot() }
            palavra(tx("epbackend_alocacao").uppercase(), tx("reg_l_03"))
            // 2. Primeiro exemplo
            comecaExemplo()
            fun v(id: String, x: Float, y: Float) = Box(id, id, x, y, 56f, 56f, Look.STATE, Tint.NONE, size = 18f)
            cena(tx("epbackend_registrador_pouco_variavel_muita_vamos")) {
                clear()
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
                add(rotulo("ra", "\$s0", 80f, 186f, Tint.TEAL, 17f), rotulo("rb", "\$s1", 180f, 286f, Tint.ORANGE, 17f), rotulo("rc", "\$s0", 280f, 186f, Tint.TEAL, 17f))
            }
            cena(tx("epbackend_faltou_cor_variavel_vai_para")) {}
            // 3. A receita
            cena(tx("reg_l_05")) {
                clear()
                add(titulo(tx("a2_receita")))
                add(regra("r1", tx("reg_l_r1"), 110f), regra("r2", tx("reg_l_r2"), 166f), regra("r3", tx("reg_l_r3"), 222f))
            }
            pergunta(tx("reg_l_06"), setOf("r2"), setOf("r1", "r3"), tx("reg_l_06_dica"), tx("reg_l_06_ok")) { hot() }
            cena(tx("reg_l_07")) { hot("r1") }
            // 4. Segundo exemplo: vivacidade, grafo e cores saem do RegAlloc do motor
            val code = TacGen.generate(ExprParser.parse(REG_EXEMPLO_2))
            val vivo = RegAlloc.liveness(code).liveOut
            val grafo = RegAlloc.interference(code)
            val k = RegAlloc.chromatic(grafo)
            val cores = RegAlloc.color(grafo, k).colors
            val temps = RegAlloc.temps(code)
            val lugar = listOf(180f to 236f, 80f to 326f, 280f to 326f, 70f to 216f)
            fun t(id: String, i: Int) = Box(id, id, lugar[i].first, lugar[i].second, 50f, 50f, Look.STATE, Tint.NONE, size = 16f)
            fun liga(vivos: Set<String>) {
                val l = vivos.sorted()
                for (i in l.indices) for (j in i + 1 until l.size) wire(l[i], l[j], arrow = false)
            }
            val linhaT2 = code.indexOfFirst { it.target == temps[1] }
            val linhaT3 = code.indexOfFirst { it.target == temps[2] }
            cena(tx("reg_l_08")) {
                clear()
                add(titulo(REG_EXEMPLO_2, size = 20f))
                add(codigo("c", code.map { it.toString() }, 64f, x = 110f, passo = 26f, size = 16f))
                temps.forEachIndexed { i, id -> add(t(id, i)) }
            }
            // A vivacidade anda de baixo para cima, como a cena da receita diz: primeiro a linha do t3, depois a do t2.
            pergunta(tx("reg_l_09"), vivo[linhaT3], temps.toSet() - vivo[linhaT3], tx("reg_l_09_dica"), tx("reg_l_09_ok")) {
                hot("c$linhaT3")
            }
            pergunta(tx("reg_l_10"), vivo[linhaT2], temps.toSet() - vivo[linhaT2], tx("reg_l_10_dica"), tx("reg_l_10_ok")) {
                liga(vivo[linhaT3])
                hot("c$linhaT2")
            }
            pergunta(tx("reg_l_11"), setOf("q$k"), (1..3).map { "q$it" }.toSet() - "q$k", tx("reg_l_11_dica"), tx("reg_l_11_ok")) {
                liga(vivo[linhaT2])
                add((1..3).map { opcao("q$it", "$it", 120f + (it - 1) * 60f, 386f) })
                hot()
            }
            cena(tx("reg_l_12"), mood = EpMood.HAPPY) {
                dropAll("q")
                for ((id, c) in cores) {
                    val tint = if (c == 0) Tint.TEAL else Tint.ORANGE
                    tint(id, tint)
                    val b = t(id, temps.indexOf(id))
                    add(rotulo("s$id", "\$s$c", b.x, b.y + 40f, tint, 16f))
                }
            }
            fecho(Fecho(
                tx("reg_f_af"), true, tx("reg_f_q"), tx("reg_f_dica"), tx("reg_f_ok"), tx("reg_f_af"),
                tx("reg_f_erro2"), tx("reg_f_certo2"), tx("reg_f_fala2"),
                listOf(tx("reg_f_z1"), tx("reg_f_z2"), tx("reg_f_z3")), tx("reg_f_resumo"),
            ))
        },
    )
}
