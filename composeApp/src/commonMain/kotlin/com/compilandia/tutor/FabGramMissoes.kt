package com.compilandia.tutor

import com.compilandia.engine.Grammar
import com.compilandia.tutor.FabGram.Cel
import com.compilandia.tutor.FabGram.Dir
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.FabGram.TipoBloco

/**
 * O tutorial da fábrica, no jeito do Mindustry: um objetivo curto por vez, e o chão mostra onde mexer.
 * Cada missão parte da planta automática da gramática e estraga uma coisa só:
 * apaga um trecho de esteira ou tira uma máquina do lugar. O que falta vira o fantasma no chão.
 * Conta como feita quando a frase sai inteira da montadora, por qualquer caminho.
 */
enum class Missao(val gramatica: String, val frase: String, val extras: List<String> = emptyList(), val desafio: Boolean = false) {
    /** Só ligar e olhar: a caixa vira peças. */
    LIGAR("S -> a b", "a b"),
    /** Desenhar esteira: o caminho das peças está cortado. */
    CONSERTAR("S -> a b", "a b"),
    /** Mover: a máquina saiu do lugar. */
    MOVER("S -> a b", "a b"),
    /** Duas regras: o separador carimba o número da máquina na caixa. */
    ESCOLHA("S -> a | b", "b"),
    /** Recursão: a caixa nova volta ao separador. */
    VOLTA("S -> a S | b", "a b"),
    /** Duas letras: cada uma tem o seu separador. */
    LETRAS("S -> A b\nA -> a", "a b"),

    // Desafios: os blocos no lugar e nenhuma esteira. As estrelas contam quantas você gastou,
    // e o robô também testa as frases extras.
    /** a^n b^n, a gramática da prova. */
    D_ANBN("S -> a S b | ε", "a a b b", listOf("a b", "a a a b b b"), desafio = true),
    /** Parênteses balanceados. */
    D_PARENTESES("S -> ( S ) S | ε", "( ) ( )", listOf("( ( ) )"), desafio = true),
    /** Expressão com duas letras. */
    D_EXPRESSAO("E -> E + T | T\nT -> id", "id + id", listOf("id", "id + id + id"), desafio = true),
}

/** Quantas missões do tutorial vêm antes dos desafios. */
val TUTORIAL: Int get() = Missao.entries.count { !it.desafio }

/** A missão está cumprida: a frase e as extras saem inteiras da montadora. */
fun FabGram.cumpre(p: MissaoPronta, pl: Planta): Boolean =
    (listOf(p.missao.frase) + p.missao.extras).all { corrida(pl, it.split(" ")).saiu }

/** A missão pronta para jogar: a planta estragada e o que mostrar no chão. */
class MissaoPronta(
    val missao: Missao,
    val planta: Planta,
    /** As esteiras que faltam, com a direção certa. */
    val guiaEsteiras: Map<Cel, Dir>,
    /** O bloco fora do lugar e onde ele deve ficar. */
    val guiaBloco: Pair<Int, Cel>?,
    /** Desafio: as esteiras da planta automática, a medida das três estrelas. */
    val orcamento: Int? = null,
) {
    /** 3 estrelas até o orçamento, 2 até 30% a mais, 1 com qualquer número. */
    fun estrelas(esteiras: Int): Int = when {
        orcamento == null -> 0
        esteiras <= orcamento -> 3
        esteiras <= orcamento + (orcamento * 3 + 9) / 10 -> 2
        else -> 1
    }
}

private fun gramatica(m: Missao) = Grammar.parse(Diagnostico.normaliza(m.gramatica))

/** As células da rota de [de] até [para] na planta automática. */
private fun Planta.celulasDaRota(de: FabGram.Bloco, para: FabGram.Bloco): List<Cel> = when (val r = rota(de, para)) {
    is Rota.Chega -> r.celulas
    is Rota.Trava -> r.celulas
}

fun FabGram.prepara(m: Missao): MissaoPronta {
    val pl = planta(gramatica(m))
    val maquinas = pl.blocos.filter { it.tipo == TipoBloco.MAQUINA }
    fun tira(cs: List<Cel>): MissaoPronta {
        val guia = cs.filter { it in pl.esteiras }.associateWith { pl.esteiras.getValue(it) }
        return MissaoPronta(m, Planta(pl.g, pl.blocos, pl.esteiras - guia.keys), guia, null)
    }
    if (m.desafio) return MissaoPronta(m, Planta(pl.g, pl.blocos, emptyMap()), emptyMap(), null, orcamento = pl.esteiras.size)
    return when (m) {
        Missao.LIGAR -> MissaoPronta(m, pl, emptyMap(), null)
        // o trecho logo depois da descida da prensa, com a curva para o coletor
        Missao.CONSERTAR -> pl.celulasDaRota(maquinas[0], pl.montadora).let { tira(it.subList(2, minOf(it.size, 6))) }
        Missao.MOVER -> {
            val pr = maquinas[0]
            val longe = pl.move(pr.id, pl.x1 + 1, pr.y)!!
            MissaoPronta(m, longe, emptyMap(), pr.id to Cel(pr.x, pr.y))
        }
        // a esteira de distribuição para antes da máquina 2
        Missao.ESCOLHA -> pl.celulasDaRota(pl.separadorDe("S"), maquinas[1]).let { tira(it.takeLast(2)) }
        // um pedaço do elevador: a caixa S nova não volta
        Missao.VOLTA -> pl.celulasDaRota(maquinas[0], pl.separadorDe("S")).let { tira(it.subList(it.size - 6, it.size - 2)) }
        // o fim do elevador, colado no separador A
        Missao.LETRAS -> pl.celulasDaRota(pl.maquinaDe(pl.g.productionsOf("S")[0]), pl.separadorDe("A")).let { tira(it.takeLast(3)) }
        else -> error("desafio tratado acima")
    }
}

/** A planta resolvida: a missão com o fantasma preenchido. Serve para testar e para o botão de ajuda. */
fun FabGram.resolve(p: MissaoPronta): Planta {
    if (p.missao.desafio) return planta(p.planta.g)
    var pl = Planta(p.planta.g, p.planta.blocos, p.planta.esteiras + p.guiaEsteiras)
    p.guiaBloco?.let { (id, c) -> pl = pl.move(id, c.x, c.y) ?: pl }
    return pl
}
