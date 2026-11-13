# Relatório do aluno 6: "Seu compilador" (série em C + Guia do TP)

## Como foi estudar isto

O Guia do TP é a parte do jogo que eu mais usaria: ele me dá arquivos Java inteiros, mensagens de erro prontas, um `.asm` completo e um checklist de véspera. Com ele eu saio do pânico. Mas ele ensina a compilar a **Byteiro**, e o meu trabalho é outra linguagem: em nenhum capítulo há um "agora faça isto na sua". O cronograma é de 8 semanas e eu tenho 3. A primeira coisa rodando só aparece no capítulo 3, e MIPS no MARS só no capítulo 8. A série em C eu não usaria para o TP: são cinco aulas de 40 segundos, bonitas (abri três telas, todas legíveis), que mostram outra arquitetura (árvore com `struct`, tudo na memória) com outros nomes, e ninguém me diz por que existem duas linguagens. O que mais me travou: a prova de LL(1) da minha gramática (cap. 4) e a troca para registradores em pilha (cap. 8).

## Onde eu travei

**Série em C e guia juntos**

- **Alta. Ninguém explica por que são duas linguagens.** A série nunca diz "seu TP é em Java, veja o guia", e o guia só cita C de passagem (cap. 1: "Em alguns campi o léxico é feito com lex/flex"). As duas ainda se contradizem: a série monta árvore ("Em C 3", cena 8: "A árvore é isso: structs ligados por ponteiros") e o guia diz "Sem árvore gigante na memória, sem segunda passada" (cap. 1). Eu fico sem saber qual das duas seguir. Mudaria: uma cena de abertura na série, "Esta série mostra o caminho com árvore, em C. O Guia do TP faz o mesmo sem árvore, em Java. A ideia é a mesma: uma função por regra", e uma caixa no cap. 1 com a tabela de equivalência (`casa` = `casaToken`, `tok == '+'` = `eh(MAIS)`, `termo` = `exprMul`, `fator` = `primario`).

**Série em C**

- **Média. "Em C 2", cenas 1 a 3:** `casa('+')` e `tok` aparecem e nunca são definidos. Quem avança o token? E `fator(); while (tok == '*') ...` termina em reticências. Mudaria: uma cena com `void casa(int t) { if (tok == t) tok = proximo(); else erro(); }`.
- **Média. Salto entre "Em C 2" e "Em C 3":** na aula 2 as funções são `void`; na 3, "o parser cria os nós de baixo para cima" (cena 4) sem uma única linha que crie nó. Não sei onde entra o `malloc` nem o que `expr()` devolve. Falta a cena `Node *expr() { Node *e = termo(); ... e = novo('+', e, termo()); }`.
- **Média. "Em C 4", cenas 3 e 5:** `sw $t3, t1` ao lado de `lw $t1, b`. `$t1` é registrador e `t1` é uma variável na memória, com o mesmo nome. Eu li como erro de digitação. Trocaria o temporário por `tmp1`.
- **Média. "Em C 4", cena 6:** `emite(n)` é a parte difícil e não aparece. `if (n->op)` depende de a folha ter `op` zero, o que nunca foi dito, e o nó ganhou um campo `nome` que não existe no `struct` da aula 3.
- **Baixa. "Em C 1", cena 9:** o código só reconhece número, mas o brinquedo pede para cortar `x1 = 42 + y ;`, com identificador e `=`.
- **Baixa. "Em C 2", cena 7:** a pergunta tem duas opções (`expr()` ou `main()`); acerto sem entender. As cenas 6 e 7 têm a mesma tela.
- **Baixa. "Em C 5":** termina numa calculadora que imprime 14, depois de a aula 4 ter gerado MIPS. A série não fecha em compilador, e nenhuma aula em C toca tabela de símbolos ou tipos, que o TP exige.

**Guia do TP**

- **Alta. Não existe a ponte "na SUA linguagem".** O cap. 10 diz "os exemplos são da Byteiro, a sua linguagem é outra, e o código precisa ser seu", mas nenhum capítulo diz o que muda. Mudaria: fechar cada capítulo com uma caixa "Na sua linguagem" de três perguntas (cap. 3: "Quais tokens seus começam com o mesmo caractere? Cada grupo vira um estado como o 3").
- **Alta. Cap. 4, a prova de LL(1).** O relatório precisa de "FIRST/FOLLOW como prova", mas a conta só acontece dentro de widgets, e sobre uma gramática encolhida (`E -> id | num`, `escreva ( E ) ;`, sem declaração nem `leia`). Não há no texto nenhuma tabela FIRST/FOLLOW da Byteiro, só dois REVELAR. E eu aprendi FIRST/FOLLOW em BNF; o guia entrega EBNF e não diz como calcular com `{ }` e `[ ]`. A frase "FOLLOW de `{ comando }` dentro do bloco" eu não sei o que significa. Mudaria: um bloco com a tabela pronta da Byteiro e a regra "em `{ α }` e `[ α ]`, confira que FIRST(α) não cruza com o que pode vir depois".
- **Alta. Cap. 4, três notações.** Cap. 2 usa `=` e aspas (`bloco = "{" { comando } "}"`); cap. 4 usa `->` com maiúsculas e depois `->` sem aspas. Em `exprAdd -> exprMul { ( + | - ) exprMul }` os parênteses agrupam; duas linhas abaixo, em `primario -> ( expr )`, são tokens. Manteria as aspas do cap. 2 até o fim.
- **Alta. Cap. 1, cronograma.** "Cronograma de 8 semanas" sem versão curta. Proporia: semana 1, linguagem + léxico + gramática; semana 2, parser + tabela + tipos; semana 3, MIPS + testes + relatório, e otimização fora.
- **Média. Demora para rodar.** O `Main.java` do cap. 1 não compila até o cap. 5 (não existe `Sintatico`). O comando `javac -d bin src/*.java` só aparece no cap. 9; no cap. 3 o guia manda "rode ANTES de escrever uma linha do parser" sem dizer como. O MARS só é instalado no cap. 8. Mudaria: no cap. 1, digitar à mão um `.asm` de seis linhas que escreve 42 e rodar no MARS; no cap. 3, a linha `javac -d bin src/*.java && java -cp bin TesteLexico`.
- **Média. Cap. 8, registradores.** O jogo e a série ensinaram "carrega, opera, guarda na memória"; o guia troca para "`$t0…$t9` funcionam como uma pilha", avisando só na legenda de um widget. Em `condicao()`, `liberaTemp(); // o valor continua no registrador até o beq` libera antes de usar e eu não sei por que isso é seguro. Acrescentaria: "liberar só diz que o próximo cálculo pode reusar o registrador; como o `beq` vem logo em seguida, ninguém escreveu por cima".
- **Média. Cap. 8, a cola.** "só isto já compila a Byteiro inteira", mas `argumento()` emite `move $a0, ...` e `move` não está na cola.
- **Média. Cap. 8, `record`.** `private record Res(TipoDado tipo, String reg) {}` aparece sem explicação. O Byte "mal faz um for em Java"; eu também. Uma linha resolve: "record é uma classe pequena só com campos; `e.reg()` lê o campo".
- **Média. Quando dá erro no MEU compilador.** O guia cuida dos erros do programa compilado, não dos meus. `TesteLexico` não tem `try/catch`, então testar um erro léxico mostra um stack trace. Não há dica para parser em laço infinito ou que rejeita programa certo. Sugestão: um `trace("exprAdd")` no começo de cada método, ligado por uma flag. O assert de registrador do cap. 10 é esse tipo de ajuda e deveria estar no cap. 8.
- **Média. Cap. 9.** As "três trocas mecânicas" vêm em um parágrafo de prosa que mexe em todo o gerador; eu erraria. O peephole só tem antes e depois, sem nenhuma linha de Java. O script de testes só chega aqui; a versão das pastas `erro/` deveria nascer no cap. 3.
- **Média. Cap. 10, "Variável em laço não reinicia".** É uma decisão de geração de código que só aparece na véspera. Lugar dela: cap. 8, junto do `declaraUm`.
- **Baixa. Cap. 3:** o truque `t.ordinal() <= TipoToken.FALSO.ordinal()` quebra em silêncio se eu puser uma palavra reservada depois de `FALSO`. Na tabela, `M_IGU`, `MA_IGU` e `DIFER` são abreviações sem legenda.
- **Baixa. Cap. 6, passo a passo:** "Linhas 5 e 7: topo = {x→x_1, y→y_2}" vem antes de "Linha 6: busca(x)"; na linha 6 o `y` ainda não existe.
- **Baixa. Caps. 2 e 7:** os widgets falam C (`&&`, `x<-1` como `x < -1`) no meio de um guia de Byteiro.

## Palavras que apareceram sem explicação

| Palavra | Onde apareceu | Onde foi explicada |
|---|---|---|
| `casa`, `tok` | Em C 2, cena 1 | nunca |
| `(Token){NUM, v}` | Em C 1, cena 1 | só "é um struct", cena 8; a definição nunca aparece |
| `emite(n)` | Em C 4, cena 6 | nunca |
| `t1` sem cifrão | Em C 4, cena 3 | nunca (cena 5 diz "guardado na memória") |
| `struct`, `typedef` | Em C 3, cena 1 | não aparecem nas regiões; a aula explica campo a campo |
| EBNF | Guia cap. 2 | na própria legenda; nunca nas regiões |
| "maior lexema" | Guia cap. 2 | o jogo sempre disse "maior casamento" (mudou de nome) |
| "dobramento de constantes" | Guia cap. 9 | o jogo disse "dobra de constantes" (mudou de nome) |
| `oprel` | Guia cap. 4, primeira gramática | dois blocos depois |
| `record` | Guia cap. 8 | nunca |
| `move` | Guia cap. 8, `argumento()` | nunca no guia; fora da cola |
| pseudo-instrução | Guia cap. 8 | na própria caixa |
| peephole | Guia cap. 1 (resumo do 9) e cap. 9 | uma linha no cap. 9 |
| numeração de Sethi-Ullman | Guia cap. 8, REVELAR | nunca (é só curiosidade) |
| "tradução dirigida pela sintaxe" | Guia cap. 1 | região 8, texto; ok |

## TP de bolso

| Aula ou capítulo | O que tentei para a MINHA linguagem | Consigo? | O que faltou |
|---|---|---|---|
| Em C 1 | Lexer que lê número e identificador | em parte | Só mostra número; o `struct Token` não aparece |
| Em C 2 | Parser de expressão com `-` e parênteses | em parte | `casa` e `tok` indefinidos; `termo` com reticências |
| Em C 3 | Montar a árvore dentro do parser | não | Nenhuma linha cria nó |
| Em C 4 | Função que gera MIPS da árvore | não | `emite` não existe; `t1` e `$t1` confundem |
| Em C 5 | Calculadora flex + bison | em parte | Mostra uma regra só; os comandos do terminal ajudam |
| Cap. 1 | Montar as pastas e o plano | em parte | Nada roda; plano de 8 semanas |
| Cap. 2 | Especificar tokens, precedência e gramática | consigo | Um aviso de quais recursos quebram LL(1) |
| Cap. 3 | Léxico com `==`, `!=` e número real | em parte | `==` sai pelo modelo do `<`; real e escape em string não |
| Cap. 4 | Provar que a minha gramática é LL(1) | não | Tabela escrita e FIRST/FOLLOW em EBNF |
| Cap. 5 | Parser descendente | consigo | A receita é mecânica; falta como depurar |
| Cap. 6 | Tabela de símbolos com escopos | consigo | Nada grave; a classe inteira serve |
| Cap. 7 | Regras de tipo | consigo | Se houver real, a conversão não é mostrada |
| Cap. 8 | Gerar MIPS de `se`, laço e expressão | em parte | Copio os moldes; não saberia criar um comando novo (um `para`) nem explicar o `liberaTemp` |
| Cap. 9 | Dobra de constantes e testes | em parte | Testes sim; dobra só copiando; peephole não |
| Cap. 10 | Fechar a entrega e a arguição | consigo | Nada: é o melhor capítulo |

## Diversão e ritmo

Funcionou: a abertura do cap. 1 ("Você lê duas vezes. Depois lê de novo.") e a do cap. 10 ("23h47. A geladeira faz um barulho estranho. O cursor pisca.") são exatamente a minha vida. A Pilha falando devagar ("Devagar… e sempre… uma fase… por vez.") e a Lexi pedindo a katana têm personalidade. Os PREVEJA são justos e me fazem pensar: o do `x<y` no estado 3, o da linha do `;` esquecido e o do `java.util.Stack` percorrendo de baixo para cima valem ouro. "Bug campeão" e "Aceitar lixo" dão vontade de testar na hora.

Cansou: no celular, o guia mostra um balão por vez. O cap. 5 tem quatro blocos de código seguidos (uns 150 linhas) e o cap. 3 um `switch` de 70 linhas sem nenhuma pergunta no meio; o cap. 8 repete o padrão. Não cabem em sessão de 15 minutos. Quebraria cada bloco grande com um PREVEJA curto ("que linha deste `case` você mudaria para aceitar `==`?"). Na série em C acontece o contrário: 40 segundos por aula, o Byte só narra a linha destacada, e acaba antes de eu ter feito algo; o único momento ativo é o brinquedo final. A "Em C 5" não tem brinquedo.

## Nota por assunto

| Aula ou capítulo | Clareza | Diversão | Pronto para o TP | Uma frase |
|---|---|---|---|---|
| Em C 1 | 4 | 3 | 2 | Boa animação do `ungetc`, mas só lê número |
| Em C 2 | 3 | 3 | 2 | A pilha de chamadas ajuda; `casa` cai do céu |
| Em C 3 | 3 | 2 | 1 | Mostra a árvore pronta, não como montar |
| Em C 4 | 2 | 3 | 1 | `t1` e `$t1` me derrubaram |
| Em C 5 | 3 | 2 | 1 | Visão geral que não liga com o resto |
| Cap. 1 | 4 | 4 | 3 | Acalma, mas não roda nada |
| Cap. 2 | 5 | 4 | 4 | Modelo de especificação que eu copiaria |
| Cap. 3 | 4 | 3 | 4 | Primeira coisa rodando, com saída esperada |
| Cap. 4 | 2 | 3 | 2 | A prova que vale nota ficou dentro dos widgets |
| Cap. 5 | 5 | 3 | 5 | Receita mecânica; código longo demais de uma vez |
| Cap. 6 | 5 | 4 | 5 | A classe cabe numa tela e a armadilha do `Stack` salva |
| Cap. 7 | 4 | 3 | 4 | Tabelas de regra claras, testes com resposta |
| Cap. 8 | 3 | 4 | 3 | O `.asm` completo é ótimo; a pilha de registradores assusta |
| Cap. 9 | 3 | 3 | 3 | A lista de testes é melhor que a otimização |
| Cap. 10 | 5 | 5 | 5 | Checklist e arguição: leria de novo na véspera |

## Desconfio que está errado

- "Em C 5", cena 1: `e: e '+' e` sem `%left` me parece a gramática ambígua que a Oficina Bison mandou consertar; e a tela dá `2+3*4 = 14` mostrando só a regra do `+`.
- Cap. 8, caixa das pseudo-instruções: `lw $t0, n_0` com rótulo, `li`, `la`, `move` e `mul` não entram na lista; se o enunciado proibir pseudo-instruções, a lista parece incompleta.
- Cap. 8: a cola afirma que "só isto" basta, mas o gerador usa `move`.
- Cap. 3: o `.asciiz` recebe o lexema cru; uma string com `\` ou acento se comporta bem no MARS?
- Cap. 6: o comentário "y -> y_2" e o contador batem, mas o texto do cap. 10 diz que o `.word` de um bloco de laço "guarda o valor da volta anterior"; conferir se isso está coerente com "variáveis valem 0".

## As 5 coisas que eu mudaria primeiro

1. Dizer, na série em C e no cap. 1, por que há duas linguagens e pôr a tabela de equivalência de nomes; avisar que a série usa árvore e o guia não.
2. Fechar cada capítulo do guia com a caixa "Na sua linguagem" (três perguntas) e um "Teste desta etapa" com o comando exato e a saída esperada.
3. Escrever no cap. 4 a tabela FIRST/FOLLOW da Byteiro inteira, com a regra para `{ }` e `[ ]`, em uma notação só.
4. Fazer algo rodar no cap. 1: um `.asm` à mão no MARS e um `Main` que compila sozinho; e dar o cronograma de 3 semanas ao lado do de 8.
5. No cap. 8, explicar `record`, pôr `move` na cola, justificar o `liberaTemp` antes do `beq` e trazer para cá o assert de registrador e a decisão da variável em laço; na série em C, mostrar `casa`, a criação do nó e o `emite`.
