package com.compilandia.content

import com.compilandia.content.Beat.*
import com.compilandia.content.CalloutKind.*
import com.compilandia.textos.tx

/*
 * Capítulo 10: a recuperação de erro implementada (professor do bloco 6). Modo pânico no FOLLOW de comando,
 * com um teste de três erros. PedidosProfessoresTest monta o Sintatico do capítulo 5 com esta rede,
 * compila com o javac e confere as mensagens.
 */
internal const val TP_REC_REDE = """    private final List<String> erros = new ArrayList<>();   // import java.util.*;
    public List<String> erros() { return erros; }

    private void bloco() {                     // agora repete até o '}', e não só enquanto vem comando
        casaToken(TipoToken.ABRE_CHAVE);
        tabela.abreEscopo();
        while (!eh(TipoToken.FECHA_CHAVE, TipoToken.EOF)) comando();
        tabela.fechaEscopo();
        casaToken(TipoToken.FECHA_CHAVE);
    }

    private void comando() {                   // o switch do cap. 5 virou comandoSemRede()
        try {
            comandoSemRede();
        } catch (ErroCompilacao e) {
            if (eh(TipoToken.EOF) || e.getMessage().contains("erro léxico"))
                throw e;                       // não há onde se segurar: o compila() registra e para
            erros.add(e.getMessage());
            proxTemp = 0;                      // a expressão quebrada não liberou os registradores
            sincroniza();
        }
    }

    private void sincroniza() {                // modo pânico: descarta até o FOLLOW de comando
        while (!comecaComando() && !eh(TipoToken.FECHA_CHAVE, TipoToken.EOF)) {
            if (eh(TipoToken.PONTO_VIRGULA)) { avanca(); return; }   // o ';' fecha o comando quebrado
            avanca();
        }
    }"""

/** O pedaço do compila() que muda: o resto continua igual ao do capítulo 8. */
internal const val TP_REC_COMPILA = """    public String compila() {
        try {
            programa();
        } catch (ErroCompilacao e) {           // erro léxico, fim de arquivo, ou o '{' e o '}' do programa
            erros.add(e.getMessage());
        }
        // ... daqui para baixo, igual ao capítulo 8
    }

// Main.java, dentro do try: com erro, nenhum .asm é gravado
            String asm = s.compila();
            if (!s.erros().isEmpty()) {
                s.erros().forEach(System.out::println);
                System.exit(1);
            }
            Files.writeString(Path.of(saida), asm);"""

internal const val TP_REC_PROGRAMA = """programa erros {
    inteiro x;
    x <- 3 escreva(x);
    se (x > ) { escreva(x); }
    x <- x + ;
    escreva("fim");
}"""

internal const val TP_REC_ESPERADO = """linha 3: erro sintático: esperava ';', encontrou 'escreva'
linha 4: erro sintático: esperava uma expressão, encontrou ')'
linha 5: erro sintático: esperava uma expressão, encontrou ';'"""

internal fun tpCap10Recuperacao(): List<Beat> = listOf(
    Text("**" + tx("tprec_titulo") + ".** " + tx("tprec_intro")),
    Code(TP_REC_REDE, tx("tprec_codigo_legenda")),
    Text(tx("tprec_follow")),
    Code(TP_REC_COMPILA, tx("tprec_compila_legenda")),
    Code(TP_REC_PROGRAMA + "\n\n" + TP_REC_ESPERADO, tx("tprec_teste_legenda")),
    Predict(tx("tprec_preveja"), listOf(tx("tprec_preveja_1"), tx("tprec_preveja_2"), tx("tprec_preveja_3")), 1, tx("tprec_preveja_exp")),
    Callout(WATCH_OUT, tx("tprec_cuidado")),
)
