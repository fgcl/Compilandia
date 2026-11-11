package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.FabGram.TipoBloco

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
    blocos += Bloco(id++, TipoBloco.FONTE, 2, 0, 2, 2, nt = g.start)
    est[Cel(2, 2)] = Dir.BAIXO
    val faixas = mutableListOf<Faixa>()
    var yA = Y0
    for (nt in nts) {
        val regras = g.productionsOf(nt)
        val k = regras.size
        blocos += Bloco(id++, TipoBloco.SEPARADOR, 2, yA, 1, k, nt = nt)
        val yM = yA + k
        // a última regra fica colada no separador; cada regra anterior vai mais para a direita
        val xs = IntArray(k)
        xs[k - 1] = X0
        for (j in k - 2 downTo 0) xs[j] = xs[j + 1] + modelo(regras[j + 1], g).w + 1
        val ms = regras.mapIndexed { j, p ->
            val m = modelo(p, g)
            Bloco(id++, TipoBloco.MAQUINA, xs[j], yM, m.w, m.h, nt = nt, prod = p, modelo = m)
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
    blocos += Bloco(id++, TipoBloco.ROTEADOR, 2 - n, yR, n + 1, 1)
    faixas.forEachIndexed { r, f ->
        val xr = 2 - n + r
        for (y in f.yA + 1 until yR) est[Cel(xr, y)] = Dir.CIMA
        for (x in xr..1) est[Cel(x, f.yA)] = Dir.DIREITA
    }
    est[Cel(2, yR + 1)] = Dir.BAIXO
    blocos += Bloco(id++, TipoBloco.MONTADORA, 1, yR + 2, 2, 2)
    blocos += Bloco(id, TipoBloco.CESTO, 4, yR + 2, 2, 2)
    return Planta(g, blocos, est)
}

/** A assinatura da gramática: a planta guardada só vale para as mesmas regras. */
private fun assinatura(g: Grammar): String {
    var h = 7L
    for (ch in g.productions.joinToString("/") { it.lhs + ">" + it.rhs.joinToString(" ") }) h = (h * 31 + ch.code) % 1_000_000_007L
    return h.toString(36)
}

/** A planta em texto, para guardar no save: versão, assinatura, "id,x,y;..." e "x,y,d;...", separados por "|". */
fun FabGram.salva(pl: Planta): String =
    "2|" + assinatura(pl.g) + "|" + pl.blocos.joinToString(";") { "${it.id},${it.x},${it.y}" } + "|" +
        pl.esteiras.entries.joinToString(";") { (c, d) -> "${c.x},${c.y},${d.ordinal}" }

/**
 * Lê a planta guardada por [salva]. Os blocos vêm da gramática; do texto só saem as posições.
 * Nulo se o texto é de outra gramática ou tem bloco em cima de bloco.
 */
fun FabGram.carrega(g: Grammar, s: String): Planta? = runCatching {
    val partes = s.split("|")
    if (partes.size != 4 || partes[0] != "2" || partes[1] != assinatura(g)) return null
    val base = planta(g)
    val pos = partes[2].split(";").filter { it.isNotBlank() }.associate { t ->
        val (i, x, y) = t.split(",").map { it.toInt() }
        i to (x to y)
    }
    if (pos.keys != base.blocos.map { it.id }.toSet()) return null
    val blocos = base.blocos.map { b -> pos.getValue(b.id).let { (x, y) -> b.copy(x = x, y = y) } }
    if (blocos.any { a -> blocos.any { b -> b.id != a.id && b.celulas().any(a::contem) } }) return null
    val est = partes[3].split(";").filter { it.isNotBlank() }.associate { t ->
        val (x, y, d) = t.split(",").map { it.toInt() }
        Cel(x, y) to Dir.entries[d]
    }.filterKeys { c -> blocos.none { it.contem(c) } }
    Planta(g, blocos, est)
}.getOrNull()
