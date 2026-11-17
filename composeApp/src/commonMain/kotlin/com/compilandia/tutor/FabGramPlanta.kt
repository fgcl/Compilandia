package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.engine.Production
import com.compilandia.fabrica.Blocos
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta

/**
 * A planta automática: uma fábrica que já funciona, para o jogador mexer a partir dela.
 * Nenhuma esteira cruza outra, e toda esteira entrega no bloco para onde aponta.
 *
 * Cada letra tem uma faixa. O separador (1 de largura, 1 de altura por regra) fica na coluna 2;
 * de cada saída dele parte uma esteira que vai para a direita e desce na máquina da regra.
 * As máquinas ficam da direita para a esquerda (regra 1 na ponta), assim as esteiras não se cruzam.
 * Cada máquina desce numa linha que corre para a direita até o coletor, que desce e entra no roteador.
 * Do roteador sai uma esteira para a montadora e uma coluna de volta para o separador de cada letra:
 * a da primeira letra fica mais por fora e sobe mais alto, e nenhuma corta a outra.
 */
private const val Y0 = 3
private const val X0 = 4

private class Faixa(val nt: String, val yA: Int, val maquinas: List<Bloco>)

fun FabGram.planta(g: Grammar): Planta {
    val blocos = mutableListOf<Bloco>()
    val est = LinkedHashMap<Cel, Dir>()
    var id = 0
    val nts = letras(g)
    val n = nts.size
    blocos += Bloco(id++, Blocos.FONTE, 2, 0, nt = g.start)
    est[Cel(2, 2)] = Dir.BAIXO
    val faixas = mutableListOf<Faixa>()
    var yA = Y0
    for (nt in nts) {
        val regras = g.productionsOf(nt)
        val k = regras.size
        blocos += Bloco(id++, Blocos.SEPARADOR, 2, yA, 1, k, nt = nt)
        val yM = yA + k
        // a última regra fica colada no separador; cada regra anterior vai mais para a direita
        val xs = IntArray(k)
        xs[k - 1] = X0
        for (j in k - 2 downTo 0) xs[j] = xs[j + 1] + modelo(regras[j + 1], g).w + 1
        val ms = regras.mapIndexed { j, p ->
            val m = modelo(p, g)
            Bloco(id++, m, xs[j], yM, nt = nt, prod = p)
        }
        blocos += ms
        for ((j, m) in ms.withIndex()) {
            for (x in 3 until m.x) est[Cel(x, yA + j)] = Dir.DIREITA
            for (y in yA + j until yM) est[Cel(m.x, y)] = Dir.BAIXO
            if (m.prod!!.rhs.isNotEmpty()) for (y in yM + m.h..yM + 2) est[Cel(m.x, y)] = Dir.BAIXO
        }
        faixas += Faixa(nt, yA, ms)
        yA = yM + 5
    }
    val xC = faixas.maxOf { f -> f.maquinas.maxOf { it.x + it.w } } + 1
    for (f in faixas) {
        val yL = f.maquinas.first().y + 3
        for (x in X0 until xC) est[Cel(x, yL)] = Dir.DIREITA
    }
    val yR = faixas.last().maquinas.first().y + 5
    for (y in faixas.first().maquinas.first().y + 3 until yR) est[Cel(xC, y)] = Dir.BAIXO
    for (x in 3..xC) est[Cel(x, yR)] = Dir.ESQUERDA
    blocos += Bloco(id++, Blocos.ROTEADOR, 2 - n, yR, n + 1, 1)
    faixas.forEachIndexed { r, f ->
        val xr = 2 - n + r
        for (y in f.yA + 1 until yR) est[Cel(xr, y)] = Dir.CIMA
        for (x in xr..1) est[Cel(x, f.yA)] = Dir.DIREITA
    }
    est[Cel(2, yR + 1)] = Dir.BAIXO
    blocos += Bloco(id++, Blocos.MONTADORA, 1, yR + 2)
    blocos += Bloco(id, Blocos.CESTO, 4, yR + 2)
    return Planta(blocos, est)
}

/** A assinatura da gramática: diz se o texto do painel e a planta falam das mesmas regras. */
fun FabGram.assinatura(g: Grammar): String {
    var h = 7L
    val texto = g.start + "#" + g.productions.joinToString("/") { it.lhs + ">" + it.rhs.joinToString(" ") }
    for (ch in texto) h = (h * HASH_BASE + ch.code) % HASH_MOD
    return h.toString(HASH_RADIX)
}

private const val HASH_BASE = 31
private const val HASH_MOD = 1_000_000_007L
private const val HASH_RADIX = 36
private const val VERSAO = "3"

/** Cada campo do save passa por aqui: vírgula, ponto e vírgula, barra e espaço viram %XX. */
private fun esc(s: String): String = buildString {
    for (ch in s) if (ch in ",;|% >~") append('%').append(ch.code.toString(HEX).padStart(2, '0')) else append(ch)
}

private val ESCAPE = Regex("%([0-9a-f]{2})")
private const val HEX = 16

private fun desesc(s: String): String = ESCAPE.replace(s) { it.groupValues[1].toInt(HEX).toChar().toString() }

private fun salvaBloco(b: Bloco): String = listOf(
    b.id.toString(), b.def.id, b.x.toString(), b.y.toString(), b.w.toString(), b.h.toString(),
    esc(b.nt.orEmpty()), b.prod?.rhs?.joinToString("~") { esc(it) }?.let { ">$it" }.orEmpty(),
).joinToString(",")

private fun carregaBloco(t: String): Bloco? {
    val f = t.split(",")
    val def = Blocos.porId(f[1]) ?: return null
    val nt = desesc(f[6]).ifEmpty { null }
    val rhs = f.getOrNull(7)?.takeIf { it.startsWith(">") }?.drop(1)
    val simbolos = rhs?.split("~")?.filter { it.isNotEmpty() }?.map(::desesc)
    val prod = if (nt != null && simbolos != null) Production(0, nt, simbolos) else null
    return Bloco(f[0].toInt(), def, f[2].toInt(), f[3].toInt(), f[4].toInt(), f[5].toInt(), nt, prod)
}

/** A planta em texto, para guardar no save: versão, os blocos com tipo e regra, e as esteiras, separados por "|". */
fun FabGram.salva(pl: Planta): String =
    VERSAO + "|" + pl.blocos.joinToString(";") { salvaBloco(it) } + "|" +
        pl.esteiras.entries.joinToString(";") { (c, d) -> "${c.x},${c.y},${d.ordinal}" }

/**
 * Lê a planta guardada por [salva]: os blocos com o tipo e a regra de cada um, e as esteiras.
 * Nulo se o texto é de uma versão antiga, falta o núcleo ou tem bloco em cima de bloco.
 */
fun FabGram.carrega(s: String): Planta? = runCatching {
    val partes = s.split("|")
    if (partes.size != 3 || partes[0] != VERSAO) return null
    val blocos = partes[1].split(";").filter { it.isNotBlank() }.map { carregaBloco(it) ?: return null }
    val faltaNucleo = Blocos.todos.filter { it.nucleo }.any { n -> blocos.none { it.def == n } }
    if (faltaNucleo) return null
    if (blocos.any { a -> blocos.any { b -> b.id != a.id && b.sobrepoe(a) } }) return null
    val est = partes[2].split(";").filter { it.isNotBlank() }.associate { t ->
        val (x, y, d) = t.split(",").map { it.toInt() }
        Cel(x, y) to Dir.entries[d]
    }.filterKeys { c -> blocos.none { it.contem(c) } }
    Planta(blocos, est)
}.getOrNull()
