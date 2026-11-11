# 🤖 Compilândia — aprenda Compiladores jogando

Jogo Android (Kotlin Multiplatform + Compose Multiplatform) para quem acha que "não é bom em compiladores".
O conteúdo segue a disciplina de **Compiladores** da PUC Minas e a base de gramáticas e autômatos de **FTC**.
Detalhes do mapeamento em [docs/CURRICULO.md](docs/CURRICULO.md).

## Como instalar no celular

1. Abra a aba **Actions** do repositório → execução mais recente do workflow **Build APK**.
2. Baixe o artefato **compilandia-apk** (um .zip com o `.apk` dentro).
3. No Android, abra o `.apk` e permita "instalar de fontes desconhecidas".

Ou compile você mesmo (precisa do Android SDK):

```bash
./gradlew :composeApp:assembleDebug
```

### No navegador
O jogo também roda numa página, em Kotlin/Wasm (WebAssembly). A cada push na `main`, o workflow **Página web** publica no GitHub Pages.
Na primeira vez, ative em **Settings → Pages → Source: GitHub Actions**. O endereço fica `https://<usuário>.github.io/<repositório>/`.

Para gerar e abrir na sua máquina:

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
cd composeApp/build/dist/wasmJs/productionExecutable && python3 -m http.server 8080
# abra http://localhost:8080
```

Abrir o `index.html` direto pelo arquivo não funciona: o navegador só carrega WebAssembly por `http://`.
O progresso fica salvo no próprio navegador. No Safari antigo, que não toca OGG, a legenda aparece no lugar da voz.

Versão desktop (útil para testar no PC):

```bash
./gradlew :composeApp:run
```

## Capturas de tela
O teste `RenderSmokeTest` renderiza todas as telas, os 29 minijogos, as aulas, cada passo dos episódios e o Guia do TP. Isso também serve de teste contra erros em tempo de execução.

```bash
./gradlew :composeApp:desktopTest --tests '*RenderSmokeTest*'
```

As imagens ficam em `composeApp/build/screenshots`.
Sem filtro, os episódios saem com uma foto por aula. Para fotografar todos os passos, rode com `EPISODIOS=BASICO` (as 29 aulas animadas) ou com o nome de uma temporada, como `EPISODIOS=TABELAS,EM_C`.
Os testes rodam sem som e gravam o save em `composeApp/build/casa-teste`, nunca no seu.

O teste `RenderSmokeTest.larguras` fotografa as telas principais em 360, 412, 700 e 1200 dp de largura e no celular deitado (pasta `larguras`).
A tarefa `desktopTestIngles` roda logo depois do `desktopTest`, numa JVM própria, e renderiza o jogo em inglês (pasta `ingles`).
Os robôs e as telas de revisão têm capturas próprias, nas pastas `bancada` e `revisao` (`BancadaRenderTest` e `RevisaoRenderTest`).

## O que tem dentro

### A oficina do Byte
A tela inicial é uma sala em 3D. Tocar num móvel abre o que ele guarda: o gaveteiro é o mapa, o monitor é o Laboratório, o quadro de ferramentas é o Painel.
Jogar rende engrenagens, e cada engrenagem compra uma peça que entra na sala com sombra e luz. O botão **Continuar** sempre sabe o próximo passo.

### Aulas animadas com voz
Cada um dos 29 assuntos tem uma aula animada: o algoritmo roda no palco e o Byte narra, com a boca sincronizada com a fala.
Toda aula segue o mesmo caminho. Primeiro vem por que o assunto existe, depois um exemplo, a receita à vista e um segundo exemplo em que você responde. O motor gera os exemplos, e um teste confere cada resposta contra ele.
- Uma ideia por cena, e quem controla o ritmo é você. O play ao lado do Continuar deixa a aula seguir sozinha até a próxima pergunta.
- O Byte entra na cena quando precisa. No autômato, ele é o trem.
- A animação para e pede seu toque no próprio desenho.
- Sem som, a legenda aparece sozinha. O botão CC liga a legenda quando quiser.
- **A aula abre com o Bug:** 10 segundos para tentar antes da explicação. Errar aqui ajuda a aula a colar.
- **A fita de cenas** fica no alto, com a miniatura de cada cena. Toque ou arraste para voltar.
- **Outro exemplo:** no fim da aula, o motor gera um exemplo novo do mesmo assunto.
- **Não entendi:** cada fala tem esse botão. Ele guarda qual fala foi, e é o melhor sensor de texto ruim que existe.
- **Termo sublinhado:** um toque abre um balão com a explicação curta e o caminho para a aula do assunto.

### A trilha de cada assunto
Na região, cada assunto ocupa uma linha. Tocar nela abre a trilha, com as etapas na ordem em que se aprende:
1. Abertura com o Bug.
2. Ver a aula animada. Embaixo dela ficam **Ler com calma**, a aula de texto, e, em quatro assuntos, **Outro jeito de ver**. Nele o autômato vira tabela de transição, os itens LR ficam em caixas como no `bison --graph` e cada chamada de função ganha seu registro de ativação.
3. Fazer com ajuda: quatro problemas gerados pelo motor. No primeiro o Byte resolve tudo, e a cada problema ele faz menos, até você resolver sozinho.
4. Programar o Byte, nos seis assuntos que têm robô (veja a Bancada do Byte, abaixo).
5. Prática: você pilota. Corte a linha, pilote o trem, opere a pilha, rode a esteira, suba os tipos. Cada toque roda o motor de verdade.
6. Pegadinha: o que mais derruba na prova, como o else solto, o conflito sem ambiguidade e o `%left` trocado.
7. Desafio: um minijogo do assunto.

Nem todo assunto tem todas as etapas. Qualquer uma tem **pular**, que não custa nada e deixa voltar depois.

No mapa, **Seu compilador** junta a série em C e o Guia do TP numa trilha só: o laço do `getchar`, uma função por regra, o `struct Node`, da árvore ao MIPS, e flex + bison + `main.c`.

A bolinha pode virar caixa na frente de você: é o mesmo autômato, desenhado de outro jeito.

### 30 segundos
34 aulas curtas numa fila vertical. Deslizou, veio outra, e os assuntos mais fracos aparecem primeiro.

As vozes são geradas com [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M) (`pm_santa` e `pf_dora`) e ficam dentro do jogo, que continua offline.
Veja [scripts/voice/README.md](scripts/voice/README.md).

### Palcos dos jogos
- **Caça ao Bug**, na maioria dos jogos: cada acerto dispara um raio no Bug que rói o compilador.
- **Esteira**, nos jogos de fila e pilha: os tokens chegam pela esteira e a pilha cresce em torre.
- **Máquina que religa**, nos chefões: cada acerto acende lâmpadas do Grande Compilador.

Errar nunca bloqueia o estudo. No fim da partida, a caixa abre e mostra o que você ganhou.

### Sequência e metas do dia
Sequência de dias, com trava comprável para quando faltar um dia, e três metas diárias que pagam engrenagens.

### O mapa: um Palácio da Memória
O gaveteiro tem 9 regiões **na ordem das fases do compilador**. Decorar o caminho é decorar o compilador:
Porto (fases) → Floresta Léxica → Vale das Gramáticas → Ponte da Transformação → Templo Top-Down (LL) →
Fortaleza Bottom-Up (LR) → Oficina Yacc/Bison → Cidade Semântica → Forja do Código.

### Álbum de Bugs
Cada erro comum de prova é um Bug com nome: o do Else Solto, o do FOLLOW Vazio, o da Recursão Única. São 34.
O Bug solta a mentira dele, e você julga três frases, arrastando a carta para um lado ou para o outro. Acertou todas, a figurinha cola.

### Corrida de portas
Nove salas com assuntos misturados. A cada sala você escolhe uma porta: a tranquila, a perigosa ou a oficina, que dá uma peça.
Perdeu a integridade, a corrida acaba e dá para recomeçar na hora. Misturar assuntos é o ponto: faz lembrar mais do que treinar um de cada vez.

### Desafio do dia
Um Wordle de compiladores: a gramática é o dicionário, e cada palpite precisa ser uma frase válida. Um por dia, o mesmo em qualquer celular, sem servidor. Dá para mandar o resultado no grupo.

### Efeitos
- **Overclock:** cinco acertos seguidos esquentam a tela, com multiplicador em chamas e som subindo de tom. A sequência vira engrenagens no fim.
- **Portão:** ao entrar numa região, um portão de oficina fecha com faíscas e abre na tela nova.
- **Glitch:** errou, a tela falha por um instante como código quebrado.
- **Chefão:** o último acerto solta uma onda de choque e o letreiro COMPILOU.
- **Sala viva:** o monitor pisca, a solda solta faísca, a poeira sobe, e um Bug passeia para você esmagar.

### Bancada do Byte
Em seis assuntos você programa o Byte. Monte a solução, toque em Rodar e veja o robô executar. Quando algo quebra, o Byte mostra o primeiro teste que falhou e o que esperava, como "Com a = 2, b = 3, ficou x = 4. Esperava x = 5."

| Robô | O que você monta | Região |
|---|---|---|
| Trilho que você constrói | a tabela de transição e os estados finais | Floresta Léxica |
| Fábrica de frases | as regras da gramática; frase que sai de dois jeitos mostra as duas árvores | Vale das Gramáticas |
| Byte com a pilha | a tabela LL(1); o Byte para na célula vazia | Templo Top-Down |
| Guindaste empilha-reduz | a ordem das regras de redução | Fortaleza Bottom-Up |
| Carteiro do escopo | o bloco de cada declaração; o Byte entrega cada uso subindo de bloco em bloco | Cidade Semântica |
| Robô MIPS | as instruções, que rodam no simulador MIPS | Forja do Código |

O palco tem linha do tempo para pausar e andar passo a passo. A dica tem três degraus, e o próximo só abre depois de rodar.

### 29 minijogos
| Jogo | Mecânica | Assunto |
|---|---|---|
| 🏭 Esteira das Fases | montar o pipeline | fases do compilador |
| 🕵️ Detetive de Erros | contra o relógio, quem pega o erro? | léxico × sintático × semântico |
| 🔪 Fatiador de Tokens | cortar a linha e classificar | tokens, maior casamento |
| 🚂 Trem do Autômato | pilotar o AFD estado a estado | regex/AFD |
| 🚪 Porteiro Regex | deixar entrar quem casa | expressões regulares |
| 🏎️ Derivação Turbo | escolher produções, ver a árvore crescer | derivação mais à esquerda |
| 🌳 Duas Árvores | provar ambiguidade | ambiguidade |
| 🩺 Cirurgia de Gramática | montar a gramática com peças | recursão à esquerda, fatoração |
| ⚗️ Alquimia FIRST/FOLLOW | ingredientes nos caldeirões | FIRST, FOLLOW |
| 🧩 Sudoku LL(1) | preencher a tabela preditiva | tabela LL(1) |
| 🗼 Torre da Pilha | operar o analisador preditivo | LL(1), descida recursiva |
| 📦 Esteira Shift-Reduce | empilhar ou reduzir | shift-reduce, SLR |
| 🏗️ Construtor de Estados | closure e goto | itens LR(0) |
| 🎯 Caça-Conflitos | achar conflitos e resolvê-los como o Bison | SLR, LALR, precedência |
| 🦬 Oficina Bison | **programar** uma especificação `.y` até os testes passarem | Yacc/Bison |
| 🏷️ Árvore Anotada | calcular tipos de baixo para cima | atributos, verificação de tipos |
| 🔦 Esconde-Esconde do Escopo | ligar cada uso à declaração | tabela de símbolos |
| 🔧 Linha de Montagem TAC | montar código de três endereços | código intermediário, otimização |
| 🧺 Fábrica de Subconjuntos | juntar estados do AFN em estados do AFD | AFN → AFD (ε-fecho, move) |
| 🦎 Oficina Flex | reordenar/adicionar regras do `.l` até o scanner acertar | Lex/Flex, maior casamento |
| 🚨 Sala do Pânico | achar onde o erro é detectado e onde o parser se recupera | recuperação de erros |
| 📚 Pilha de Chamadas | empilhar/desempilhar registros de ativação de programas recursivos | ambientes de execução |
| 🔩 Montadora MIPS | traduzir TAC para MIPS e rodar no "MARS de bolso" | geração de código |
| 🧑‍💻 Programador Descendente | montar o método Java do parser (`casaToken`) | descida recursiva |
| 🏭 Fábrica de Autômatos | montar o autômato com o dedo; robôs testam, e a frase que furou aparece | regex/AFD, minimização |

Nove jogos de classificar terminam com **Agora roda**: o motor executa o que você respondeu. No Porteiro Regex, o autômato julga as palavras em que você errou. Nas Duas Árvores, com números no lugar dos ids, cada árvore dá uma conta diferente.

Todos os desafios são **gerados pelo motor de compiladores do próprio jogo**, então a prática é infinita.
Esse motor está em `composeApp/src/commonMain/kotlin/com/compilandia/engine` e inclui:
- FIRST/FOLLOW com rastro passo a passo;
- tabelas LL(1), LR(0), SLR(1), LR(1) e LALR(1);
- Earley;
- Thompson, subconjuntos e minimização de AFD;
- um **mini-Bison** com `%left`, `%right`, `%nonassoc`, `%prec` e ações `$$ = $1 + $3`.

### Chefões, prova e conquistas
- Cada região tem um **chefão**: 3 desafios seguidos, um pouco acima do seu nível, como um simulado de prova. Perdeu, tenta de novo na hora.
- Marque a **data da prova** nos ajustes. O mapa mostra a contagem regressiva, e o jogo passa a priorizar seus pontos fracos nos últimos 10 dias.
- **Conquistas** no Painel do Cérebro.

### 🛠️ Guia do Trabalho Prático
A pesquisa mostrou que o TP da disciplina pede um compilador completo:
- léxico por AFD;
- gramática LL(1) e descida recursiva;
- tabela de símbolos com escopos e verificação de tipos;
- geração de **MIPS** para o MARS.

O guia percorre cada etapa com uma linguagem de exemplo original. Ver [docs/CURRICULO.md](docs/CURRICULO.md).
No mapa, o guia fica em **Seu compilador**.

### Aulas de texto em 6 estilos
Para quem quer ler mais, cada assunto também tem aulas de texto, abertas em **Ler com calma** na trilha. Elas começam com o palco animado das cenas do assunto, e **Não entendi** troca de estilo:
- 📖 história/analogia
- 🎬 passo a passo visual (widgets interativos do motor)
- ✍️ exemplo resolvido
- 🤔 perguntas guiadas
- 🎓 definição de prova
- 🧠 truque de memória

Nos jogos, **Não entendi** abre a aula animada do assunto e **Trocar jogo** muda para outro que treina a mesma coisa.

### Técnicas de aprendizagem usadas
- **Prática de recuperação**: jogos, "pense e toque para revelar" e flashcards.
- **Repetição espaçada**: curva de esquecimento estilo FSRS. Quando a memória de um assunto cai abaixo de 90%, a trilha oferece uma revisão rápida, e o formato muda a cada vez: prever num exemplo novo, julgar as frases do Bug, uma fase do robô, um clipe de 30 segundos, explicar com suas palavras ou a pegadinha.
- **Revisão por ideia**: cada frase dos Bugs do Álbum tem memória própria. A revisão volta só na ideia que você esqueceu.
- **Retomada**: quem volta depois de 2 dias ou mais começa com três frases de verdade ou mentira, tiradas das ideias mais esquecidas. Depois o Continuar segue de onde parou.
- **Lembrete**, só no celular e desligado até você ligar em Ajustes: uma notificação por dia, na hora escolhida, quando há revisão pendente.
- **Intercalação**: a "Missão intercalada" mistura assuntos.
- **Metacognição**: antes de cada jogo você diz sua confiança. Depois vê o espelho confiança × resultado × previsão da IA, e acompanha sua calibração (índice de Brier) no Painel do Cérebro.
- **Efeito de geração**: blocos "preveja antes de ver".
- **Técnica Feynman**: explicar com suas palavras, com checagem dos termos-chave.
- **Interrogação elaborativa** ("por quê?"), **codificação dupla** (visual + verbal), **dificuldade desejável** e **mentalidade de crescimento**.
- **Reflexão**: no fim de cada aula, você marca a cena que ficou confusa.

### A "rede neural" (honestamente)
Tudo roda **no celular, offline**:
- **BKT** (Bayesian Knowledge Tracing) estima o que você sabe;
- uma **MLP 10→12→1** treinada online prevê sua chance de acerto. O jogo usa essa previsão para escolher desafios na faixa de ~75% (a "zona de desafio");
- **Thompson Sampling** aprende quais estilos de explicação e quais jogos funcionam para você.

É uma versão de bolso do *Deep Knowledge Tracing*, não um modelo gigante, mas é real e aprende com os seus dados.

### 🧪 Laboratório
Uma conversa com o Byte. Sem lista de ferramentas: cole o que tiver, e ele descobre o que é.
- **Gramática:** ele diz o que achou, como recursão à esquerda, prefixo comum, ambiguidade, conflito na tabela, regra sem fim ou regra solta.
- **Regex:** ele monta o autômato e roda a cadeia que você mandar.
- **Expressão:** ele vai da árvore até o MIPS.
- **Linha de código:** ele corta em tokens.
- **Bison:** ele lê o `.y`, conta regras e conflitos, diz o que a precedência decidiu e abre o **Laboratório Bison**.
- **Flex e MIPS:** os simuladores de sempre abrem na conversa.
- **Pergunta:** ele responde com o clipe do assunto e chama um teste.

Tudo que ele roda abre no palco com linha do tempo: arraste para voltar, troque a velocidade, ligue o **Prever** para responder antes de cada passo.

As tabelas de sempre seguem lá, em "Tabelas completas", para conferir exercício linha por linha.

**Dica em três degraus:** primeiro ele aponta o lugar, depois diz por quê, e só então mostra resolvendo. A dica direta vem primeiro porque, num ensaio de 2026, a socrática custou mais tempo sem ganho depois. O último degrau só abre depois de uma tentativa, e essa trava está no código.

**Nada sai do aparelho.** O tutor é o motor do jogo, o modelo do aluno e as aulas. Não há API, chave, internet nem permissão de rede. O que ele não sabe, ele diz que ainda não sabe.

### Laboratório Bison
Também abre pela Oficina Yacc/Bison. Cada mudança no `.y` roda o motor na hora.
- **A conta e a árvore:** no alto ficam a entrada, o valor que o `.y` calculou e a árvore da conta, com o valor de cada operador. Quem fica embaixo é calculado primeiro. Se o valor não bate com a matemática, ele fica vermelho e a conta certa aparece.
- **Régua de precedência:** cada `%left` é uma barra que se arrasta pela alça, e a árvore muda junto. Tocar no `%left` gira para `%right` e `%nonassoc`. Sem precedência, **Pôr %left** cria uma linha por operador.
- **Passo a passo:** a pilha numa fila só, com o estado em cima e o valor embaixo de cada símbolo, e a ação do passo: empilhar, ou reduzir com a conta (`$$ = 2 + 12 = 14`).
- **Conflitos:** os que sobraram ficam abertos, como um duelo entre empilhar e reduzir. Os que a precedência resolveu ocupam uma linha cada, e um toque abre o duelo.
- **Estados:** os estados do relatório `bison -v` que estão na pilha, com o topo aceso. Os outros abrem num toque.
- **Arquivo .y:** o texto inteiro, para ler ou editar.
- Exemplos prontos: calculadora, sem precedência (`2*3+4` sai 14), potência com `%right` e menos unário com `%prec`.

## Estrutura

```
composeApp/src/commonMain/textos/
  pt/, en/   todo texto e fala do jogo, no formato do strings.xml
composeApp/src/commonMain/kotlin/com/compilandia/
  engine/    motor de compiladores (Kotlin puro, testado)
  learning/  BKT, memória, rede neural, bandits, recomendação
  content/   episódios, clipes, aberturas, brinquedos, álbum, aulas de texto e Guia do TP
  tutor/     o que o Byte faz no Laboratório: lê a entrada, diagnostica e gera as simulações
  ui/        telas, minijogos e widgets (Compose)
    bancada/ a Bancada do Byte: os robôs que você programa
    cast/    Byte, Profª Gramática e Bug, desenhados em Compose
    motion/  o palco: anima a diferença entre duas imagens
    voice/   narrador e sincronismo da boca
    room/    a oficina e a loja
    lab/     Laboratório e palco com linha do tempo
    fx/      portão, glitch, overclock, explosão e sala viva
    album/, corrida/, desafio/, feed/
scripts/
  voice/     renderiza e confere as falas (Kokoro + Whisper)
  bancada/   painel com as propostas jogáveis e os vereditos
  art/       renderiza a sala no Blender
  text_lint.py  revisa o texto: frase longa, vício de IA, voz passiva, índices do ALT e gramática
verify/      build auxiliar para testar o Kotlin puro sem Android SDK
```

Por que o jogo é assim: [docs/PESQUISA-DIVERSAO.md](docs/PESQUISA-DIVERSAO.md) e [docs/PESQUISA-LAB-IA.md](docs/PESQUISA-LAB-IA.md).

Testes: `./gradlew :composeApp:desktopTest` (ou `cd verify && ../gradlew test`).

## Créditos dos assets
- **Arte e sons:** [Kenney.nl](https://kenney.nl), licença CC0 (domínio público). Inclui os móveis 3D da sala (Furniture Pack), cenários das regiões, medalhas, partículas, emotes, efeitos de interface e jingles. Ver `docs/licenses/Kenney-CC0.txt`.
- **Vozes:** geradas com [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M), licença Apache 2.0.
- **Sons novos:** bipes, impacto, portão, glitch e tique foram sintetizados para o jogo, sem material de terceiros.
- **Desktop:** o áudio OGG usa [vorbisspi](https://github.com/Trilarion/java-vorbis-support), licença LGPL. Não entra no APK.
- **Fontes tipográficas:** Atkinson Hyperlegible, Space Mono e Barlow Condensed, sob a SIL Open Font License (ver `docs/licenses`).

## Conforto visual
Em **Ajustes** você escolhe:
- o tema: claro, sépia, escuro, noite âmbar (quase sem luz azul) ou alto contraste;
- o tamanho do texto;
- se quer **reduzir animações**, o que desliga confete, tremida, raios, portão, glitch, overclock e a sala viva;
- voz do Byte e legenda.
- o idioma: Português ou English. O padrão segue o idioma do aparelho. Save antigo continua em português. Em inglês não há voz: a fala aparece escrita e a boca do Byte mexe no ritmo da legenda. Trocar o idioma reabre o jogo.

## Tela larga e celular deitado
O jogo escolhe o arranjo pela largura da janela, com as três faixas do Material 3 (função `faixaDe` em `ui/Janela.kt`):
- **Compacta** (menos de 600 dp): uma coluna, como no celular em pé.
- **Média** (600 a 840 dp, e todo celular deitado): na região, cada assunto mostra as etapas abertas. Deitado, a aula e a sala ganham um painel lateral.
- **Expandida** (840 dp ou mais): aula com o palco à esquerda e a fala à direita. Região e trilha ficam em duas colunas. Listas e textos ficam numa coluna central de até 760 dp, com o texto corrido em até 640 dp.
