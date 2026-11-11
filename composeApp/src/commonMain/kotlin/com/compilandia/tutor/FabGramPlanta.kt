package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.tutor.FabGram.Bloco
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.FabGram.TipoBloco

/**
 * A planta automática: uma fábrica que já funciona, para o jogador mexer a partir dela.
 *
 * Cada letra tem uma linha. O separador fica na coluna 2, colado no elevador (coluna 1, sobe).
 * A esteira de distribuição passa por cima das máquinas da letra; cada máquina solta as peças
 * numa descida curta até a linha de baixo, que corre para a direita até o coletor.
 * O coletor desce, a esteira do fundo passa pela montadora e volta ao elevador:
 * a caixa que sai de uma máquina dá a volta e entra no separador da letra dela.
 */
private const val PASSO = 6
private const val Y0 = 3
private const val X0 = 3

fun FabGram.planta(g: Grammar): Planta {
    val blocos = mutableListOf<Bloco>()
    val est = LinkedHashMap<Cel, Dir>()
    var id = 0
    val nts = letras(g)
    blocos += Bloco(id++, TipoBloco.FONTE, 0, 0, 2, 2, nt = g.start)
    est[Cel(1, 2)] = Dir.BAIXO
    var xC = X0 + 2
    nts.forEachIndexed { r, nt ->
        val yA = Y0 + r * PASSO
        blocos += Bloco(id++, TipoBloco.SEPARADOR, 2, yA, 1, 1, nt = nt)
        var x = X0
        var ultimo = X0
        for (p in g.productionsOf(nt)) {
            val m = modelo(p, g)
            blocos += Bloco(id++, TipoBloco.MAQUINA, x, yA + 1, m.w, m.h, nt = nt, prod = p, modelo = m)
            if (p.rhs.isNotEmpty()) for (y in yA + 1 + m.h..yA + 3) est[Cel(x, y)] = Dir.BAIXO
            ultimo = x
            x += m.w + 1
        }
        xC = maxOf(xC, x)
        for (cx in X0..ultimo) est[Cel(cx, yA)] = Dir.DIREITA
    }
    val yB = Y0 + nts.size * PASSO
    nts.indices.forEach { r -> for (cx in X0 until xC) est[Cel(cx, Y0 + r * PASSO + 4)] = Dir.DIREITA }
    for (y in Y0 + 4 until yB) est[Cel(xC, y)] = Dir.BAIXO
    for (cx in 2..xC) est[Cel(cx, yB)] = Dir.ESQUERDA
    for (y in Y0..yB) est[Cel(1, y)] = Dir.CIMA
    val xM = maxOf(2, xC - 4)
    blocos += Bloco(id++, TipoBloco.MONTADORA, xM, yB + 1, 2, 2)
    blocos += Bloco(id, TipoBloco.CESTO, xM + 3, yB + 1, 2, 2)
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
