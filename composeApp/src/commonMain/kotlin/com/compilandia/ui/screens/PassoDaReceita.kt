package com.compilandia.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.compilandia.content.Episode
import com.compilandia.content.Temporada
import com.compilandia.learning.Skill
import com.compilandia.ui.Palette

/**
 * Qual passo da receita cada cena da Sua vez usa (mural, rodada 16).
 * Um dígito por cena da Sua vez, na ordem; 0 é cena de abrir ou fechar, sem passo aceso.
 * O `AulaExtrasTest` confere que cada linha tem uma cena por dígito e só passos que existem.
 */
object PassoDaReceita {
    val porAssunto: Map<Skill, String> = mapOf(
        Skill.PHASES to "01240",
        Skill.FIRST to "013100334",
        Skill.FOLLOW to "02330022",
        Skill.LL1_TABLE to "01123023013",
        Skill.LR0_ITEMS to "012232233",
        Skill.SLR_TABLE to "02200123",
        Skill.LALR to "1223",
        Skill.DFA_MINIMIZATION to "01300122330012",
        Skill.AST_BUILD to "0113",
        Skill.CODEGEN_MIPS to "0120",
        Skill.TOKENS to "03200",
        Skill.REGEX_DFA to "04430",
        Skill.NFA_TO_DFA to "0123400323",
        Skill.LEX_FLEX to "03230",
        Skill.CFG_DERIVATION to "12223000000",
        Skill.AMBIGUITY to "12223000000",
        Skill.LEFT_RECURSION to "01230000130",
        Skill.LEFT_FACTORING to "0124400013300",
        Skill.LL1_PARSING to "0212123",
        Skill.RECURSIVE_DESCENT to "024024",
        Skill.ERROR_RECOVERY to "012230",
        Skill.SHIFT_REDUCE to "0122123",
        Skill.YACC_BISON to "0331100",
        Skill.ATTRIBUTES_TYPES to "03344",
        Skill.SCOPE_SYMBOLS to "03343",
        Skill.TAC to "012340",
        Skill.OPTIMIZATION to "01230",
        Skill.RUNTIME to "1111213",
        Skill.REG_ALLOC to "01130",
    )

    /** A cena [index] está na Sua vez? Devolve o passo aceso (1, 2...) ou 0; fora da Sua vez, nulo. */
    fun na(ep: Episode, index: Int): Int? {
        if (ep.temporada != Temporada.BASICO) return null
        val digitos = porAssunto[ep.skill] ?: return null
        val suaVez = partesDaAula(ep.passos, ep.inicioDoExemplo)?.getOrNull(3) ?: return null
        if (index !in suaVez) return null
        return digitos.getOrNull(index - suaVez.first)?.digitToIntOrNull()
    }
}

/**
 * A tira da receita na Sua vez: um círculo por passo, o da cena aceso em amarelo
 * e, ao lado, o texto desse passo numa linha. Liga o que a pessoa faz à regra que ela usa.
 */
@Composable
fun TiraDaReceita(linhas: List<String>, aceso: Int, onClick: () -> Unit) {
    Row(
        // Mural, rodada 17: um toque na tira abre a cola com a receita inteira.
        Modifier.fillMaxWidth().background(Palette.paper).clickable { onClick() }.padding(horizontal = 8.dp).padding(bottom = 4.dp).height(26.dp).testTag("tira-da-receita"),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        linhas.indices.forEach { i ->
            val n = i + 1
            val fundo by animateColorAsState(if (n == aceso) Palette.hazard else Palette.paperDark)
            Box(Modifier.size(22.dp).clip(CircleShape).background(fundo), contentAlignment = Alignment.Center) {
                Text("$n", color = if (n == aceso) Palette.hazardDark else Palette.inkSoft, style = MaterialTheme.typography.labelMedium)
            }
        }
        val texto = linhas.getOrNull(aceso - 1)?.removePrefix("$aceso. ")?.removePrefix("$aceso.")
        if (texto != null) Text(
            texto, Modifier.weight(1f).padding(start = 4.dp), color = Palette.ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/** O fim da aula repete a receita, numerada, logo antes do botão de praticar (mural, rodada 17). */
@Composable
fun ReceitaDoFim(s: Skill) {
    val linhas = androidx.compose.runtime.remember(s) { Cola.receita(s) }
    if (linhas.size < 2) return
    Column(Modifier.fillMaxWidth().testTag("receita-do-fim"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        linhas.forEachIndexed { i, l -> LinhaDaCola(i + 1, l, Palette.paperDark) }
    }
}
