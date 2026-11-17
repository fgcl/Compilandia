package com.compilandia.ui.fabgram

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.compilandia.engine.tokensOf
import com.compilandia.fabrica.BlocoTipo
import com.compilandia.fabrica.Blocos
import com.compilandia.fabrica.Categoria
import com.compilandia.fabrica.Papel
import com.compilandia.tutor.Corrida
import com.compilandia.tutor.FabGram
import com.compilandia.tutor.FabGram.Planta
import com.compilandia.tutor.Missao
import com.compilandia.tutor.MissaoPronta
import com.compilandia.tutor.corrida
import com.compilandia.tutor.planta
import com.compilandia.tutor.prepara
import com.compilandia.tutor.salva
import com.compilandia.ui.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DESFAZER_MAX = 60

/** Estado da tela: a gramática escrita, a planta montada, a missão do tutorial e a corrida que está passando. */
internal class EstadoFabrica(texto0: String, frase0: String) {
    var texto by mutableStateOf(texto0)
    var frase by mutableStateOf(frase0)
    var planta by mutableStateOf<Planta?>(null)
    var corrida by mutableStateOf<Corrida?>(null)
    var t by mutableFloatStateOf(0f)
    var sel by mutableStateOf<Int?>(null)
    var ferramenta by mutableStateOf(Ferramenta.MOVER)
    var painel by mutableStateOf<Boolean?>(null)
    /** A missão do tutorial em jogo; nulo é o modo livre. */
    var missao by mutableStateOf<MissaoPronta?>(null)
    var zoom by mutableFloatStateOf(1f)
    var pan by mutableStateOf(Offset.Zero)
    val desfazer = mutableStateListOf<Planta>()
    val refazer = mutableStateListOf<Planta>()
    /** Dicas pedidas no desafio: cada uma tira uma estrela. */
    var dicas by mutableStateOf(0)
    var dicaGuia by mutableStateOf<Map<FabGram.Cel, FabGram.Dir>>(emptyMap())
    /** Engrenagens que a missão cumprida rendeu agora. */
    var ganhou by mutableStateOf(0)
    /** O robô testou as frases extras do desafio: nulo enquanto testa. */
    var extrasOk by mutableStateOf<Boolean?>(null)
    /** A primeira frase extra que não saiu. */
    var extraFalhou by mutableStateOf<String?>(null)

    /** A aba aberta no menu de construção. */
    var aba by mutableStateOf(Categoria.PRODUCAO)
    /** O bloco que o dedo põe no chão (ferramenta CONSTRUIR). */
    var construindo by mutableStateOf<BlocoTipo?>(null)
    /** O bloco trancado que a loja está oferecendo. */
    var oferta by mutableStateOf<BlocoTipo?>(null)
    /** O bloco cujo painel de configurar está aberto. */
    var config by mutableStateOf<Int?>(null)
    /** Onde a grade está na tela agora. O teste que joga a fábrica usa para achar a célula. */
    var grade: Grade? = null
    /** O texto que saiu da planta: quando o painel mostra este texto, a planta não precisa ser refeita. */
    var textoDaPlanta: String? = null

    val terminou: Boolean get() = corrida?.let { t >= it.duracao } == true
    private val extrasPassaram: Boolean get() = missao?.missao?.extras.isNullOrEmpty() || extrasOk == true
    val cumpriu: Boolean get() = missao != null && terminou && corrida?.saiu == true && extrasPassaram

    /** O tutorial mostra o gesto até o jogador mexer na fábrica. */
    val mostrarMao: Boolean get() {
        val m = missao ?: return false
        val temGesto = m.guiaEsteiras.isNotEmpty() || m.guiaBloco != null
        return !m.missao.desafio && desfazer.isEmpty() && corrida == null && temGesto
    }

    /** O Byte aparece no canto enquanto há um gesto para ensinar ou a primeira missão espera o Ligar. */
    val mostrarByte: Boolean get() = mostrarMao || (missao?.missao == Missao.LIGAR && corrida == null)

    /** Desafio: estrelas pelo número de esteiras usadas. */
    val estrelas: Int get() {
        val pelaObra = missao?.estrelas(planta?.esteiras?.size ?: 0) ?: 0
        return if (pelaObra == 0) 0 else maxOf(1, pelaObra - dicas)
    }

    /** Troca a planta guardando a anterior para o Desfazer. */
    fun troca(pl: Planta) {
        planta?.let { desfazer += it; if (desfazer.size > DESFAZER_MAX) desfazer.removeAt(0) }
        refazer.clear()
        planta = pl; corrida = null
    }

    fun volta() {
        val a = desfazer.removeLastOrNull() ?: return
        planta?.let { refazer += it }; planta = a; corrida = null
    }

    fun avanca() {
        val a = refazer.removeLastOrNull() ?: return
        planta?.let { desfazer += it }; planta = a; corrida = null
    }

    fun recomeca(pl: Planta?) {
        desfazer.clear(); refazer.clear()
        planta = pl; corrida = null; sel = null; config = null; zoom = 1f; pan = Offset.Zero
    }

    fun abre(m: Missao) {
        val p = FabGram.prepara(m)
        missao = p; texto = m.gramatica; frase = m.frase; dicas = 0; dicaGuia = emptyMap(); ganhou = 0
        construindo = null; oferta = null
        ferramenta = when (m) {
            Missao.MOVER, Missao.LIGAR, Missao.REGRA -> Ferramenta.MOVER
            Missao.CONSTRUIR -> {
                construindo = p.guiaConstruir?.first; aba = Categoria.PRODUCAO
                Ferramenta.CONSTRUIR
            }
            else -> Ferramenta.ESTEIRA
        }
        recomeca(p.planta)
    }

    fun livre() { missao = null; ferramenta = Ferramenta.MOVER; construindo = null }

    /** O botão do menu está aceso? */
    fun escolhido(t: BlocoTipo): Boolean =
        if (t == Blocos.ESTEIRA) ferramenta == Ferramenta.ESTEIRA
        else ferramenta == Ferramenta.CONSTRUIR && construindo == t

    /** Tocar num bloco do menu: o que já tem vira a ferramenta; o trancado abre a loja. */
    fun escolhe(t: BlocoTipo, tem: Boolean) {
        when {
            !tem -> oferta = t
            t == Blocos.ESTEIRA -> { ferramenta = Ferramenta.ESTEIRA; construindo = null }
            else -> { ferramenta = Ferramenta.CONSTRUIR; construindo = t }
        }
    }
}

/** O texto da gramática que a planta faz, no formato do painel: a letra inicial primeiro. */
internal fun textoDa(pl: Planta): String {
    val g = pl.g
    return FabGram.letras(g).filter { g.productionsOf(it).isNotEmpty() }.joinToString("\n") { nt ->
        "$nt -> " + g.productionsOf(nt).joinToString(" | ") { it.rhsText() }
    }
}

/** Blocos que abrem o painel de configurar quando tocados. */
internal fun configuravel(b: FabGram.Bloco): Boolean {
    return b.tipo in setOf(Papel.MAQUINA, Papel.SEPARADOR, Papel.FONTE)
}

/**
 * O que a tela faz com o save e com o tempo: guardar a planta, ligar a fábrica, construir, comprar.
 * Fica fora do composable para cada ação ser curta e testável.
 */
internal class ControleFabrica(
    private val app: AppState,
    private val e: EstadoFabrica,
    private val escopo: CoroutineScope,
) {

    /** Troca a planta. No modo livre, a gramática do painel acompanha a fábrica e tudo vai para o save. */
    fun guarda(pl: Planta) {
        e.troca(pl)
        if (e.missao != null) return
        val texto = textoDa(pl)
        if (texto.isNotEmpty()) { e.textoDaPlanta = texto; e.texto = texto }
        app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl)); app.commit()
    }

    /** Arrumar: a missão volta ao começo; no modo livre, a planta automática da gramática do painel. */
    fun arruma(g: com.compilandia.engine.Grammar?) {
        val m = e.missao
        if (m != null) { e.abre(m.missao); return }
        g?.let { guarda(FabGram.planta(it)); e.zoom = 1f; e.pan = Offset.Zero }
    }

    /** Constrói o bloco escolhido em [c]. Máquina e separador já abrem o painel de configurar. */
    fun constroi(c: FabGram.Cel, area: FabGram.Area) {
        val t = e.construindo ?: return
        val pl = e.planta ?: return
        val nova = pl.constroi(t, c, area) ?: return
        guarda(nova)
        val b = nova.blocos.last()
        if (configuravel(b)) e.config = b.id
    }

    fun toca(b: FabGram.Bloco?) {
        e.sel = b?.takeIf { it.tipo == Papel.MAQUINA }?.id
        e.config = b?.takeIf { configuravel(it) }?.id
    }

    fun compra(t: BlocoTipo) {
        if (app.learner.compraBlocoFabrica(t.id, t.preco)) { app.commit(); e.oferta = null; e.escolhe(t, true) }
    }

    fun liga() {
        val pl = e.planta ?: return
        val toks = tokensOf(e.frase)
        if (toks.isEmpty()) return
        if (e.missao == null) {
            app.learner.setFabGram(e.texto, e.frase)
            app.learner.setFabGramPlanta(e.texto, FabGram.salva(pl))
            app.commit()
        }
        val m = e.missao
        e.extrasOk = null; e.extraFalhou = null
        escopo.launch {
            e.corrida = withContext(Dispatchers.Default) { FabGram.corrida(pl, toks) }
            if (m != null && m.missao.extras.isNotEmpty()) {
                val falha = withContext(Dispatchers.Default) {
                    m.missao.extras.firstOrNull { !FabGram.corrida(pl, tokensOf(it)).saiu }
                }
                e.extraFalhou = falha; e.extrasOk = falha == null
            }
        }
    }
}
