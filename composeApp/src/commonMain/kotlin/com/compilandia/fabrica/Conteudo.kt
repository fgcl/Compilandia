package com.compilandia.fabrica

/**
 * O conteúdo da Fábrica de Gramáticas, no molde do Mindustry.
 *
 * Lá, `Block` é a definição compartilhada de um tipo de bloco (tamanho, categoria, custo, o que ele faz)
 * e `Building` é a cópia que está numa célula do mapa. Todo bloco do jogo nasce num lugar só, `Blocks.load()`.
 * Aqui é igual: [BlocoTipo] é a definição, `FabGram.Bloco` é a instância na planta e [Blocos] é o registro.
 *
 * Para criar um bloco novo, veja docs/FABRICA.md. Em resumo: uma entrada em [Blocos], o desenho dele em
 * `ui/fabgram/Desenhos.kt`, o nome em `textos/<idioma>/fabgram.xml`, e o comportamento (se for novo) em
 * FabGramCorrida.kt. O teste `ConteudoFabricaTest` reclama do que faltar.
 */

/** A aba do menu de construção (o PlacementFragment do Mindustry). A ordem aqui é a ordem das abas. */
enum class Categoria { DISTRIBUICAO, PRODUCAO, NUCLEO }

/**
 * O que o bloco faz na corrida. Blocos diferentes podem ter o mesmo papel:
 * o Cofre e a Prensa são as duas MAQUINA, só muda quanto cabe na regra.
 */
enum class Papel {
    /** Solta a primeira caixa (a letra inicial). Faz parte do núcleo. */
    FONTE,
    /** Recebe a caixa de uma letra e manda para a máquina da regra escolhida. */
    SEPARADOR,
    /** Aplica uma regra: abre a caixa e solta as peças e as caixas do lado direito. */
    MAQUINA,
    /** Junta as peças na ordem da frase. Faz parte do núcleo. */
    MONTADORA,
    /** Onde cai o que a fábrica não soube montar. Faz parte do núcleo. */
    CESTO,
    /** Recebe tudo e devolve pela esteira que leva ao destino (o sorter do Mindustry). */
    ROTEADOR,
    /** Deixa o item atravessar em linha reta: duas esteiras se cruzam sem se misturar. */
    JUNCAO,
}

/**
 * Um tipo de bloco. Tudo que é igual em todas as cópias fica aqui; a posição e a configuração ficam na instância.
 *
 * @property id nome estável: vai para o save e para a chave do texto (`fabgram_bloco_<id>`). Nunca troque.
 * @property preco engrenagens para destravar na loja; 0 é bloco que já vem destravado.
 * @property requer o bloco que precisa estar destravado antes (a árvore de pesquisa).
 * @property capacidade máquinas: quantos símbolos cabem do lado direito da regra.
 * @property soLetra máquina que só troca a letra (A → B): o lado direito é uma letra só.
 * @property esticavel a instância pode ter outro tamanho (o separador cresce com o número de regras).
 * @property nucleo vem em toda fábrica e não sai: a fonte, a montadora e o cesto.
 */
class BlocoTipo(
    val id: String,
    val papel: Papel,
    val categoria: Categoria,
    val w: Int,
    val h: Int,
    val preco: Int = 0,
    val requer: BlocoTipo? = null,
    val capacidade: Int = 0,
    val soLetra: Boolean = false,
    val esticavel: Boolean = false,
    val nucleo: Boolean = false,
) {
    val maquina: Boolean get() = papel == Papel.MAQUINA

    /** A regra cabe nesta máquina? [letra] diz se um símbolo é letra (não terminal). */
    fun cabe(rhs: List<String>, letra: (String) -> Boolean): Boolean = when {
        !maquina -> false
        soLetra -> rhs.size == 1 && letra(rhs[0])
        else -> rhs.size <= capacidade
    }

    override fun toString() = id
}

/** O registro de todo o conteúdo. A ordem de [todos] é a ordem no menu de construção. */
object Blocos {
    val FONTE = BlocoTipo("fonte", Papel.FONTE, Categoria.NUCLEO, 2, 2, nucleo = true)
    val MONTADORA = BlocoTipo("montadora", Papel.MONTADORA, Categoria.NUCLEO, 2, 2, nucleo = true)
    val CESTO = BlocoTipo("cesto", Papel.CESTO, Categoria.NUCLEO, 2, 2, nucleo = true)

    /** A esteira não é um bloco na planta (fica no mapa de esteiras), mas tem lugar no menu e na loja. */
    val ESTEIRA = BlocoTipo("esteira", Papel.JUNCAO, Categoria.DISTRIBUICAO, 1, 1)
    val SEPARADOR = BlocoTipo("separador", Papel.SEPARADOR, Categoria.DISTRIBUICAO, 1, 1, esticavel = true)
    val ROTEADOR = BlocoTipo("roteador", Papel.ROTEADOR, Categoria.DISTRIBUICAO, 1, 1, esticavel = true)
    val JUNCAO = BlocoTipo("juncao", Papel.JUNCAO, Categoria.DISTRIBUICAO, 1, 1, preco = 20)

    /** ε ou uma peça só. */
    val COFRE = BlocoTipo("cofre", Papel.MAQUINA, Categoria.PRODUCAO, 1, 1, capacidade = 1)
    /** Até três símbolos: a máquina de todo dia. */
    val PRENSA = BlocoTipo("prensa", Papel.MAQUINA, Categoria.PRODUCAO, 2, 2, capacidade = 3)
    /** Só troca a letra: A → B. */
    val BRACO = BlocoTipo(
        "braco", Papel.MAQUINA, Categoria.PRODUCAO, 1, 2, preco = 15, capacidade = 1, soLetra = true,
    )
    /** Regra longa, até seis símbolos. */
    val ARMARIO = BlocoTipo(
        "armario", Papel.MAQUINA, Categoria.PRODUCAO, 2, 2, preco = 30, requer = PRENSA, capacidade = 6,
    )

    val todos: List<BlocoTipo> = listOf(
        ESTEIRA, SEPARADOR, ROTEADOR, JUNCAO, COFRE, PRENSA, BRACO, ARMARIO, FONTE, MONTADORA, CESTO,
    )

    /**
     * Máquinas na ordem em que a planta automática tenta: a primeira em que a regra cabe.
     * O braço vem antes porque só aceita A → B; depois, da menor para a maior.
     */
    val maquinas: List<BlocoTipo> = listOf(BRACO, COFRE, PRENSA, ARMARIO)

    fun porId(id: String): BlocoTipo? = todos.firstOrNull { it.id == id }

    /** O que vai no menu de construção: tudo menos o núcleo, por aba. */
    fun doMenu(c: Categoria): List<BlocoTipo> = todos.filter { it.categoria == c && !it.nucleo }

    /** Os blocos que já vêm destravados. */
    val iniciais: Set<String> = todos.filter { it.preco == 0 }.map { it.id }.toSet()
}
