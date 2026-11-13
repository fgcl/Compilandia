# Relatório do professor 4: regiões 6 (Bottom-Up) e 7 (Yacc/Bison)

## Veredito

O conteúdo técnico está quase todo correto: refiz todas as coleções de itens, tabelas, FOLLOWs e traços e só achei erros localizados. As aulas de texto (WORKED e FORMAL) têm nível de prova e seguem o Dragão. O problema maior é de desenho: **a trilha animada nunca constrói um autômato LR(0) inteiro nem tira uma tabela SLR dele**. O aluno reconhece item, fecho, casa de redução e conflito em pedaços já prontos, mas não produz. Respondendo à pergunta da tarefa: **só com a trilha, o aluno não faria sozinho "construa o autômato LR(0), monte a tabela SLR e diga se há conflito"**. Quem ler também os textos WORKED de LR0_ITEMS e SLR_TABLE terá visto cada peça, mas nunca as três em sequência, e sempre lendo ou revelando a resposta. Segundo problema: o handle é apresentado primeiro como "lado direito no topo", o glossário fixa essa versão, e a correção vem com um argumento de precedência que não vale para a gramática mostrada.

## Erros técnicos

| Assunto | Onde | Trecho | Problema | Conserto | Grav. |
|---|---|---|---|---|---|
| SHIFT_REDUCE | Glossário | "handle: O pedaço do topo da pilha que casa com o lado direito de uma regra." | É o erro que a aula e o Bug combatem ("Handle é qualquer pedaço igual a um lado direito" = falso). | "handle: o lado direito no topo da pilha que é certo reduzir agora. A redução desfaz um passo da derivação mais à direita. Nem todo lado direito no topo é handle." | alta |
| SHIFT_REDUCE | Pegadinha c2 e c5; aula c21 | "Se reduzir agora, a soma roda antes do vezes." / "faz a conta na ordem errada" / "Empilha, porque o vezes liga mais forte." | Em E → E + T, T → T * F a redução precoce não troca a ordem da conta: deixa E na pilha com * na entrada, e nenhuma forma sentencial tem "E *". O parser rejeita uma frase válida. "Ordem errada" só ocorre na gramática ambígua. Os textos STORY e VISUAL dizem o certo. | c2: "Se reduzir agora, fica E na pilha e vem um vezes. Nenhuma regra continua E com vezes: o parser trava numa frase certa." Acerto: "Isso. Empilha: esse T ainda vai virar T vezes F." c5: "Reduzir antes da hora leva a um beco sem saída." | média |
| LR0_ITEMS | Tabelas "A caixa do estado", c7 | "Algumas caixas têm item com o ponto no fim... Toque nelas." certo = I2, I3; errado = I1 | I1 contém E' → E •, ponto no fim. Pelo critério da pergunta, I1 está certo e é marcado como erro. | Perguntar "Quais caixas mandam reduzir por uma regra da gramática?" e, ao tocar I1: "I1 tem ponto no fim, mas em E' → E •. Esse item aceita, e só com $." | média |
| LR0_ITEMS | Aula, c17 | "Os estados estão prontos." | Só apareceram 3 dos 6 estados de S → ( S ) \| id. Faltam {S' → S •}, {S → ( S • )}, {S → ( S ) •} e o laço goto(I2, "(") = I2. | Mostrar os 6 estados com as setas, ou dizer "Faltam três estados: você monta na prática". | média |
| LR0_ITEMS × LALR | Ficha, FORMAL e WORKED de LR0; aula LALR c5 | "Item de núcleo (kernel)" e "O núcleo é o item sem o lookahead" | A mesma palavra para dois conceitos (kernel e core). | LR0: "itens de base (kernel)". LALR: "núcleo (core)". Avisar uma vez: "não é o mesmo que os itens de base". | média |
| LALR | Aula c5–6 e c12–14 | "Toque no item com o mesmo núcleo" / "Dois estados LR(1)" com três itens soltos | O LALR funde **estados** cujo conjunto inteiro de núcleos coincide, não itens. A tela não separa os dois estados, e "T → ( • E ) , $" aparece sem fecho. Erro de prova: fundir estados que têm só um item em comum. | Desenhar duas caixas de estado completas (as da pegadinha servem) e dizer: "Só junta se todos os itens, sem o lookahead, forem iguais nas duas caixas." | média |
| YACC_BISON | Tabelas "Lendo o bison -v", c2 e c5 | "Estado 0{e' → • e; ...}" e "Com qualquer token, ele reduz." | O estado 0 aparece só com o item de base, uma região depois de ensinar que I0 tem o fecho. E "$default" contradiz "redução só nos lookaheads" sem aviso. | c2: "O relatório mostra só os itens de base. O fecho fica subentendido." c5: "$default é um atalho do Bison para encolher a tabela. Se o token for errado, o erro aparece antes de empilhar o próximo." | baixa |
| YACC_BISON | Texto VISUAL, widget do "^" | "{ $$ = $1 ^ $3; }" | Em C, ^ é XOR: copiado para um Bison real, 2^3^2 dá 3. | Legenda: "No jogo, ^ é potência. Em C, ^ é XOR: use pow()." | baixa |
| SHIFT_REDUCE | Aula c11 × ficha | "escolhe entre três ações" × "As 4 ações?" | Contagem diferente (falta erro). | "Quatro ações: empilhar, reduzir, aceitar e, se nenhuma serve, erro." | baixa |
| LALR | MNEMONIC | "tamanho: LR(0) = SLR(1) = LALR(1) < LR(1)" | Pode empatar. | Trocar por ≤. | baixa |
| LR0_ITEMS | Glossário | "closure: junta ao estado as regras do símbolo..." | Falta "não-terminal" e "com o ponto no começo". | "junta as regras do não-terminal que vem logo depois do ponto, com o ponto no começo." | baixa |

**Conferi refazendo e está certo:** coleção LR(0) de S → C C (7 estados, todos os gotos); I0–I3 de E → E + T \| T, T → id; fecho de I0 da gramática de expressões e os estados 1, 2, 9 e 10 (s6, s7, r1, r2, r3, colunas do FOLLOW); tabela SLR de E → E + n \| n (5 estados) e o traço de "n + n" (0 → 0 2 → 0 1 → 0 1 3 4 → 0 1 → aceita); FOLLOW(R) = {=, $} e o conflito s6/r5 no estado 2; LR(1) da gramática dos ponteiros (I0, I2, goto(I0, *), 14 estados, 4 pares fundidos, 10 no LALR); o exemplo LR(1)-não-LALR e o "syntax error" em "a c e"; traços de "id * id", "id + id * id", "b a a" e "a b b c d e"; definição formal de handle e de prefixo viável; algoritmos CLOSURE, GOTO, SLR e LALR; hierarquia LR(0) ⊂ SLR ⊂ LALR ⊂ LR(1) e LL(1) incomparável com SLR(1); argumento de que a fusão não cria shift-reduce; no Bison, 14 e 7 estados, 1 e 4 conflitos, 5-2-1 = 4 ou 2, 2*3+4 = 14 sem precedência, 2^3^2 = 512, 20 × 14 na pegadinha, regra de desempate completa, %prec UMINUS, $$ = $2, calc.y e calc.l.

## Problemas de didática e estrutura

**SHIFT_REDUCE**
- (alta) A aula ensina o erro antes da correção. Em c6 handle é "esse trecho pronto para reduzir", e a dica de c8 é "E mais n é o lado direito de uma regra?", ou seja, "casou, reduz". A correção (c21) usa "id mais id vezes id", gramática que a aula não mostrou. O contraexemplo está na própria pilha de c8: com E + n, o n do topo também casa com E → n. Cena nova: "O n do topo é lado direito de E → n. Se eu reduzir, fica E + E, e nenhuma regra desfaz isso. O handle é E + n."
- (média) A esteira tem só as teclas [Empilhar, Reduzir]: o aluno nunca escolhe **qual regra** reduzir. O formato de prova Pilha | Entrada | Ação com a produção só existe no texto WORKED. Sugestão: tecla de redução por regra.
- O segundo exemplo (parênteses) é bom: traz o caso "não-terminal no topo, mas empilha".

**LR0_ITEMS**
- (alta) Nenhum autômato completo na trilha, sem "Fazer com ajuda" e sem robô. A receita na tela tem três linhas e omite os passos que mais custam ponto: aumentar a gramática, I0 = fecho de S' → • S, repetir o goto para cada símbolo de cada estado, **conferir se o conjunto já existe**, numerar. Reaproveitar estado e laço só aparecem no texto. Receita proposta: "0. Crie S' → S (para ter um único item de aceitação). 1. I0 = fecho de S' → • S. 2. Para cada estado e cada símbolo depois de um ponto: ande o ponto e feche. 3. Conjunto repetido não é estado novo: é seta para o que já existe. 4. Pare quando não surgir estado novo."
- (baixa) Notação mista: "•" na aula, "." na abertura e na pegadinha. "O bison desenha os estados assim" cita uma ferramenta que só chega na região 7.

**SLR_TABLE**
- (alta) A tabela aparece pronta em c4, sem o autômato ao lado e sem as regras numeradas na tela (r2 é qual regra?). O "sua vez" preenche duas casas de uma linha. Falta o exemplo de ponta a ponta: gramática → itens → FOLLOW → tabela → conflito. E → E + n \| n serve (5 estados, a mesma tabela da prática).
- (média) Pilha de símbolos em SHIFT_REDUCE, pilha de estados na prática SLR, e nenhuma cena liga as duas. Acrescentar: "Cada estado da pilha representa um símbolo já lido: 0 E 1 + 3 n 4. Por isso reduzir tira um estado por símbolo."
- (média) Só há conflito shift-reduce, sempre com o estado e o FOLLOW entregues. Falta um reduce-reduce e uma busca em que o aluno acha o conflito sozinho.
- (baixa) "ok" na aula e "acc" nos textos; três regras na aula e quatro nos textos. Padronizar em "acc" e quatro regras.
- Funcionam bem: a pegadinha "Conflito não é ambiguidade", o erro "r em todas as colunas é LR(0)" e a dica FORMAL de como justificar conflito (estado, token, dois itens, FOLLOW).

**LALR**
- (média) O porquê não tem exemplo concreto. A aula não volta ao estado 2 dos ponteiros, deixado em aberto na pegadinha do SLR. Cena: "No SLR, R → L • reduz em = e $ (o FOLLOW). No LR(1), o item é R → L •, $: reduz só em $. A casa do = fica só com o empilhar."
- (média) O passo 1 da receita ("Monte os estados LR(1)") não é ensinado na trilha: FIRST(β a) só aparece em texto e ficha, e a ficha lista como erro comum algo que a aula não mostrou.

**YACC_BISON**
- (média) A aula animada só ensina "mais embaixo, mais forte". O mecanismo (precedência da regra = a do último terminal, comparada com a do token; empate decide pela associatividade), %right, %nonassoc e %prec ficam só no texto, e o resumo (c23) cita %right sem tê-lo mostrado.
- (baixa) c2 é pergunta de tabuada (14 × 8), não de compiladores. Os pré-requisitos declarados omitem SLR_TABLE e LALR, mas a aula abre com "tabela LALR" e conflitos.
- Funcionam bem: o experimento 5 - 2 - 1 (4 sem %left, 2 com) e a pegadinha 20 × 14.

**Geral**
- (média) Em todas as cinco aulas a pergunta de "Erros comuns" tem resposta "errado": o aluno aprende a posição. Alternar com afirmações verdadeiras.
- (média) Perguntas "qual passo da receita?" se resolvem casando o texto da tela (SHIFT_REDUCE c12, LR0 c11, SLR c11, LALR c10, Bison c12). Trocar por perguntas que aplicam o passo.
- (baixa) Nomes demais para a mesma coisa: shift-reduce, empilha-reduz, empilhar/reduzir, E/R; closure, fecho, fechamento; goto, desvio. Fixar um par e apresentar o sinônimo uma vez.
- A trilha animada não pede autoexplicação; os REVELAR dos textos ("defina handle com suas palavras") fazem isso bem.

## Lacunas

1. Questão clássica completa (itens → tabela SLR → conflito), com apoio decrescente: um exemplo resolvido, um para completar e um sozinho, sendo um deles com conflito.
2. Produção ε no LR: A → • já nasce completo, a redução desempilha zero estados, tabela de S → a S b | ε. Hoje só há um flashcard e uma legenda.
3. Conflito reduce-reduce no SLR com exemplo, e o teste "é LR(0)?" dentro da trilha.
4. Traço com estados e símbolos lado a lado, no formato de prova.
5. Closure LR(1) com FIRST(β a) na trilha, com ao menos um problema para fazer.
6. Bison: %right, %nonassoc e %prec na aula animada; recuperação de erro (token error); %union/%type para quem fizer o TP com Bison.
7. Recuperação de erro em parser LR e um quadro LL × LR.

## Nota por assunto

| Assunto | Correção | Didática | Prova | Frase |
|---|---|---|---|---|
| SHIFT_REDUCE | 3 | 3 | 3 | Bons traços nos textos; handle mal definido no glossário e pegadinha explicada por precedência. |
| LR0_ITEMS | 4 | 3 | 2 | Item, fecho e goto claros; nenhum autômato completo nem prática de produção na trilha. |
| SLR_TABLE | 5 | 3 | 3 | Tudo confere; a tabela chega pronta e o aluno só roda ou preenche casas. |
| LALR | 4 | 3 | 3 | Textos excelentes; a aula animada funde itens soltos e não mostra o ganho sobre o FOLLOW. |
| YACC_BISON | 4 | 4 | 3 | Experimentos bons; mecanismo de precedência, %right, %nonassoc e %prec só no texto. |

## As 5 melhorias que eu faria primeiro

1. Criar a etapa "Questão de prova" em SLR_TABLE: E → E + n | n do zero até a tabela e o traço; depois S → ( S ) | id para completar; depois S → L = R | R para o aluno achar o conflito. Dar "Fazer com ajuda" a LR0_ITEMS.
2. Consertar o handle: novo texto do glossário, contraexemplo com a pilha E + n dentro da aula e pegadinha reescrita com "beco sem saída" no lugar de "conta na ordem errada".
3. Completar o autômato de S → ( S ) | id (6 estados, com o laço) e pôr na tela a receita de cinco passos, incluindo "conjunto repetido não é estado novo".
4. Corrigir a pergunta de I1 (E' → E •), separar "itens de base (kernel)" de "núcleo (core)" e redesenhar a fusão LALR com duas caixas de estado completas.
5. No Bison, acrescentar três cenas: precedência da regra × do token, %right com 2^3^2 e %prec no menos unário. Nas cinco aulas, alternar a resposta da pergunta de "Erros comuns".
