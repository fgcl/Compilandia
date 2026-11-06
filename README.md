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

O teste `RenderSmokeTest.larguras` fotografa as telas principais em 360, 412, 700 e 1200 dp de largura e no celular deitado (pasta `larguras`).
A tarefa `desktopTestIngles` roda logo depois do `desktopTest`, numa JVM própria, e renderiza o jogo em inglês (pasta `ingles`).

## O que tem dentro

### A oficina do Byte
A tela inicial é uma sala em 3D. Tocar num móvel abre o que ele guarda: o gaveteiro é o mapa, o monitor é o Laboratório, o quadro de ferramentas é o Painel.
Jogar rende engrenagens, e cada engrenagem compra uma peça que entra na sala com sombra e luz. O botão **Continuar** sempre sabe o próximo passo.

### Aulas animadas com voz
Cada um dos 29 assuntos tem um episódio curto: o algoritmo roda no palco e o Byte narra, com a boca sincronizada com a fala.
- Uma ideia por cena, e quem controla o ritmo é você.
- O Byte entra na cena quando precisa. No autômato, ele é o trem.
- A animação para e pede seu toque no próprio desenho.
- Sem som, a legenda aparece sozinha. O botão CC liga a legenda quando quiser.
- **A aula abre com o Bug:** 10 segundos para tentar antes da explicação. Errar aqui ajuda a aula a colar.
- **A fita de cenas** fica no alto, com a miniatura de cada cena. Toque ou arraste para voltar.
- **Outro exemplo:** no fim da aula, o motor gera um exemplo novo do mesmo assunto.
- **Não entendi:** cada fala tem esse botão. Ele guarda qual fala foi, e é o melhor sensor de texto ruim que existe.

Cada região tem até cinco temporadas:
- **Básico:** o episódio de sempre.
- **Pegadinhas:** 26 aulas com o que mais derruba na prova, como o else solto, o conflito sem ambiguidade e o `%left` trocado.
- **Prática:** 11 aulas em que você pilota. Corte a linha, pilote o trem, opere a pilha, rode a esteira, suba os tipos. Cada toque roda o motor de verdade.
- **Tabelas:** 4 aulas em que o assunto vira caixa com compartimentos, como num diagrama de classes. O estado do autômato traz uma linha por letra e o fio sai dela; o autômato vira tabela de transição; os itens LR ficam em caixas como no `bison --graph`; cada chamada de função ganha seu registro de ativação.
- **Em C:** uma série de 5 aulas que escreve o compilador em C. O laço do `getchar`, uma função por regra, o `struct Node`, da árvore ao MIPS, e flex + bison + `main.c`. A série aparece inteira em qualquer região.

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

### Aulas de texto em 6 estilos
Para quem quer ler mais, cada assunto também tem aulas de texto, abertas em "mais" na tela da região:
- 📖 história/analogia
- 🎬 passo a passo visual (widgets interativos do motor)
- ✍️ exemplo resolvido
- 🤔 perguntas guiadas
- 🎓 definição de prova
- 🧠 truque de memória

Nos jogos, **Não entendi** abre a aula animada do assunto e **Trocar jogo** muda para outro que treina a mesma coisa.

### Técnicas de aprendizagem usadas
- **Prática de recuperação**: jogos, "pense e toque para revelar" e flashcards.
- **Repetição espaçada**: curva de esquecimento estilo FSRS; o jogo chama você para revisar quando a memória cai abaixo de 90%.
- **Intercalação**: a "Missão intercalada" mistura assuntos.
- **Metacognição**: antes de cada jogo você diz sua confiança. Depois vê o espelho confiança × resultado × previsão da IA, e acompanha sua calibração (índice de Brier) no Painel do Cérebro.
- **Efeito de geração**: blocos "preveja antes de ver".
- **Técnica Feynman**: explicar com suas palavras, com checagem dos termos-chave.
- **Interrogação elaborativa** ("por quê?"), **codificação dupla** (visual + verbal), **dificuldade desejável** e **mentalidade de crescimento**.
- **Diário de bordo** para reflexão.

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
- **Arquivo em blocos:** declarações e regras viram blocos. A régua de precedência se arrasta pela alça, e tocar no `%left` gira para `%right` e `%nonassoc`.
- **Passo a passo:** pilha de estados, de símbolos e de valores lado a lado. Ao reduzir, a conta aparece com os números: `$$ = 2 + 12 = 14`.
- **Conflitos:** cada célula com duas ações vira um duelo entre o item que empilha e o item que reduz, com o que decidiu a briga.
- **Estados:** os estados do relatório `bison -v`, e o topo da pilha acende durante a execução.
- Exemplos prontos: calculadora, sem precedência, potência com `%right` e menos unário com `%prec`.

## Estrutura

```
composeApp/src/commonMain/kotlin/com/compilandia/
  engine/    motor de compiladores (Kotlin puro, testado)
  learning/  BKT, memória, rede neural, bandits, recomendação
  content/   episódios, clipes, aberturas, brinquedos, álbum, aulas de texto e Guia do TP
  tutor/     o que o Byte faz no Laboratório: lê a entrada, diagnostica e gera as simulações
  ui/        telas, minijogos e widgets (Compose)
    cast/    Byte, Profª Gramática e Bug, desenhados em Compose
    motion/  o palco: anima a diferença entre duas imagens
    voice/   narrador e sincronismo da boca
    room/    a oficina e a loja
    lab/     Laboratório e palco com linha do tempo
    fx/      portão, glitch, overclock, explosão e sala viva
    album/, corrida/, desafio/, feed/
scripts/
  voice/     renderiza e confere as falas (Kokoro + Whisper)
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
