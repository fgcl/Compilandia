package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.engine.Production
import com.compilandia.fabrica.BlocoTipo
import com.compilandia.fabrica.Blocos
import com.compilandia.fabrica.Papel

/**
 * Fábrica de Gramáticas: cada regra é uma máquina numa grade vista de cima.
 *
 * A fábrica É a gramática. O jogador compra máquinas na loja, põe no chão e escolhe a regra de cada uma;
 * a gramática que o Laboratório analisa é a soma das regras das máquinas ([Planta.g]).
 * Uma caixa (não terminal) ou peça (terminal) segue a esteira em que está e só entra
 * no bloco para onde a esteira aponta, como no Mindustry. O separador tem uma saída por regra
 * e carimba na caixa o número da máquina; o roteador recebe o que as máquinas soltam e manda
 * cada peça para a montadora e cada caixa para o separador da letra dela.
 *
 * Os tipos de bloco estão em fabrica/Conteudo.kt; [planta] monta uma fábrica que já funciona;
 * a corrida está em FabGramCorrida.kt.
 */
object FabGram {

    enum class Dir(val dx: Int, val dy: Int) {
        DIREITA(1, 0), BAIXO(0, 1), ESQUERDA(-1, 0), CIMA(0, -1);

        /** Um quarto de volta no sentido do relógio. */
        fun gira(): Dir = entries[(ordinal + 1) % entries.size]
        fun oposta(): Dir = gira().gira()

        companion object {
            fun de(dx: Int, dy: Int): Dir? = entries.firstOrNull { it.dx == dx && it.dy == dy }
        }
    }

    data class Cel(val x: Int, val y: Int) {
        operator fun plus(d: Dir) = Cel(x + d.dx, y + d.dy)
        operator fun minus(d: Dir) = Cel(x - d.dx, y - d.dy)
    }

    /**
     * Um bloco na planta (o Building do Mindustry): o tipo, onde está e como foi configurado.
     * [nt] é a letra do separador, a letra inicial da fonte ou a letra da regra da máquina.
     * [prod] é a regra da máquina; nulo é máquina ainda sem regra (não entra na gramática).
     */
    data class Bloco(
        val id: Int,
        val def: BlocoTipo,
        val x: Int, val y: Int,
        val w: Int = def.w, val h: Int = def.h,
        val nt: String? = null,
        val prod: Production? = null,
    ) {
        val tipo: Papel get() = def.papel
        val cx: Float get() = x + w / 2f
        val cy: Float get() = y + h / 2f
        fun contem(c: Cel) = c.x in x until x + w && c.y in y until y + h

        /** A célula encosta num lado do bloco (sem contar a diagonal). */
        fun encosta(c: Cel): Boolean = !contem(c) && Dir.entries.any { contem(c + it) }

        fun celulas(): List<Cel> = (x until x + w).flatMap { cx -> (y until y + h).map { Cel(cx, it) } }
        fun sobrepoe(o: Bloco): Boolean = x < o.x + o.w && o.x < x + w && y < o.y + o.h && o.y < y + h
    }

    /** Um retângulo de células: [x0], [y0] inclusivos; [x1], [y1] exclusivos. */
    data class Area(val x0: Int, val y0: Int, val x1: Int, val y1: Int) {
        fun cabe(b: Bloco): Boolean = b.x >= x0 && b.y >= y0 && b.x + b.w <= x1 && b.y + b.h <= y1
    }

    /**
     * Uma fábrica: os blocos e, em cada célula de esteira, para onde ela empurra.
     * A gramática [g] sai das máquinas: a letra inicial é a da fonte, e cada máquina com regra é uma produção,
     * na ordem dos blocos. As máquinas guardam a produção já com o número dela em [g].
     */
    class Planta(blocos0: List<Bloco>, val esteiras: Map<Cel, Dir>) {
        val g: Grammar
        val blocos: List<Bloco>

        init {
            val inicio = blocos0.firstOrNull { it.tipo == Papel.FONTE }?.nt ?: "S"
            var n = 0
            blocos = blocos0.map { b -> b.prod?.let { p -> b.copy(prod = p.copy(id = n++)) } ?: b }
            g = Grammar(inicio, blocos.mapNotNull { it.prod })
        }

        /** O retângulo que a fábrica ocupa: [x0], [y0] inclusivos; [x1], [y1] exclusivos. A grade não tem borda. */
        val x0: Int = minOf(blocos.minOf { it.x }, esteiras.keys.minOfOrNull { it.x } ?: 0)
        val y0: Int = minOf(blocos.minOf { it.y }, esteiras.keys.minOfOrNull { it.y } ?: 0)
        val x1: Int = maxOf(blocos.maxOf { it.x + it.w }, (esteiras.keys.maxOfOrNull { it.x } ?: 0) + 1)
        val y1: Int = maxOf(blocos.maxOf { it.y + it.h }, (esteiras.keys.maxOfOrNull { it.y } ?: 0) + 1)
        val largura: Int get() = x1 - x0
        val altura: Int get() = y1 - y0

        fun blocoEm(c: Cel): Bloco? = blocos.firstOrNull { it.contem(c) }
        fun maquinaDe(p: Production): Bloco = blocos.first { it.prod?.id == p.id }
        fun separadorDe(nt: String): Bloco? = blocos.firstOrNull { it.tipo == Papel.SEPARADOR && it.nt == nt }
        val fonte: Bloco get() = blocos.first { it.tipo == Papel.FONTE }
        val montadora: Bloco get() = blocos.first { it.tipo == Papel.MONTADORA }
        val cesto: Bloco get() = blocos.first { it.tipo == Papel.CESTO }
        val proximoId: Int get() = (blocos.maxOfOrNull { it.id } ?: -1) + 1

        /** A primeira esteira que sai de [b] (colada nele e empurrando para longe), ou nulo: bloco sem saída. */
        fun saidaDe(b: Bloco): Cel? = esteiras.entries
            .filter { (c, d) -> b.encosta(c) && b.contem(c - d) }
            .minWithOrNull(compareBy({ it.key.y }, { it.key.x }))?.key

        private fun livre(n: Bloco, area: Area?): Boolean =
            (area == null || area.cabe(n)) && blocos.none { it.id != n.id && it.sobrepoe(n) }

        /** Move o bloco [id] para ([x], [y]). Esteira embaixo dele some. Nulo se sair de [area] ou cair em cima de outro bloco. */
        fun move(id: Int, x: Int, y: Int, area: Area? = null): Planta? {
            val b = blocos.firstOrNull { it.id == id } ?: return null
            val n = b.copy(x = x, y = y)
            if (!livre(n, area)) return null
            return Planta(blocos.map { if (it.id == id) n else it }, esteiras.filterKeys { !n.contem(it) })
        }

        /** Constrói um bloco novo de [tipo] com o canto em [c]. Nulo se não couber. */
        fun constroi(tipo: BlocoTipo, c: Cel, area: Area? = null): Planta? {
            val n = Bloco(proximoId, tipo, c.x, c.y)
            if (!livre(n, area)) return null
            return Planta(blocos + n, esteiras.filterKeys { !n.contem(it) })
        }

        /** Tira o bloco [id]. O núcleo não sai. */
        fun desmonta(id: Int): Planta? {
            val b = blocos.firstOrNull { it.id == id } ?: return null
            if (b.def.nucleo) return null
            return Planta(blocos - b, esteiras)
        }

        /** Troca a configuração do bloco [id]: a letra e, se for máquina, a regra. */
        fun configura(id: Int, nt: String?, rhs: List<String>?): Planta {
            val novos = blocos.map { b ->
                if (b.id != id) b
                else b.copy(nt = nt, prod = regra(b, nt, rhs))
            }
            return Planta(novos, esteiras)
        }

        private fun regra(b: Bloco, nt: String?, rhs: List<String>?): Production? =
            if (b.def.maquina && nt != null && rhs != null) Production(0, nt, rhs) else null

        /** O separador cresce para ter uma saída por regra da letra dele. */
        fun esticaSeparadores(): Planta {
            val esticados = blocos.map { b -> if (b.tipo == Papel.SEPARADOR) b.copy(h = saidasDoSeparador(b)) else b }
            val p = Planta(esticados, esteiras)
            val choca = p.blocos.any { a -> p.blocos.any { it.id != a.id && it.sobrepoe(a) } }
            return if (choca) this else p
        }

        private fun saidasDoSeparador(b: Bloco) = maxOf(1, g.productionsOf(b.nt.orEmpty()).size)

        /** Põe esteira em [c]. Nulo se a célula é de um bloco. */
        fun comEsteira(c: Cel, d: Dir): Planta? {
            if (blocoEm(c) != null) return null
            return Planta(blocos, esteiras + (c to d))
        }

        /**
         * O dedo arrastando esteira passa por [c] indo para [d]. Como no Mindustry, cruzar de lado
         * uma esteira que já existe põe uma junção ali, e as duas linhas seguem sem se misturar.
         * Uma junção que já está no caminho fica. Nulo se a célula é de outro bloco.
         */
        fun estende(c: Cel, d: Dir): Planta? {
            val existente = esteiras[c]
            val cruza = existente != null && (existente.dx == 0) != (d.dx == 0)
            return when {
                blocoEm(c)?.def == Blocos.JUNCAO -> this
                cruza -> semEsteira(c).constroi(Blocos.JUNCAO, c)
                else -> comEsteira(c, d)
            }
        }

        fun semEsteira(c: Cel): Planta = if (c in esteiras) Planta(blocos, esteiras - c) else this

        fun gira(c: Cel): Planta = esteiras[c]?.let { Planta(blocos, esteiras + (c to it.gira())) } ?: this
    }

    /** A máquina mais barata em que a regra cabe. */
    fun modelo(p: Production, g: Grammar): BlocoTipo = Blocos.maquinas.first { it.cabe(p.rhs, g::isNonTerminal) }

    /** O número da máquina entre as regras da mesma letra (1, 2, ...); nulo quando a letra tem uma regra só. */
    fun numero(g: Grammar, p: Production): Int? =
        g.productionsOf(p.lhs).let { rs -> if (rs.size < 2) null else rs.indexOfFirst { it.id == p.id } + 1 }

    /** As letras na ordem em que a fábrica mostra: a inicial primeiro. */
    fun letras(g: Grammar): List<String> {
        val ordem = mutableListOf(g.start)
        for (p in g.productions) if (p.lhs !in ordem) ordem += p.lhs
        return ordem
    }

    /** Exemplos do menu. As gramáticas não se traduzem. */
    val exemplos: List<Pair<String, String>> = listOf(
        "S -> a S b | ε" to "a a b b",
        "S -> ( S ) S | ε" to "( ) ( )",
        "E -> E + T | T\nT -> id" to "id + id",
        "E -> E + E | id" to "id + id + id",
        "S -> if E then S else S | x\nE -> id" to "if id then x else x",
    )
}
