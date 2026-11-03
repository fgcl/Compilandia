# Pesquisa: laboratório, tutor, aulas interativas e mecânicas

Feita em 2 de novembro de 2026. Continua [PESQUISA-DIVERSAO.md](PESQUISA-DIVERSAO.md) e não repete o que está lá.

Nem toda fonte pesa igual. Ensaio randomizado publicado vale mais que pôster. Pôster vale mais que blog. Números de empresa servem de pista. Onde a fonte é fraca, a linha diz.

## O que estava errado no Laboratório

Olhando a captura da tela:

- São 26 botões antes de qualquer coisa. Quem não sabe o nome da técnica não sabe por onde começar.
- O resultado fica abaixo da dobra, depois do formulário.
- O resultado é uma tabela parada. O algoritmo não anda.
- Ninguém ajuda. Se a gramática der erro, sobra uma linha vermelha.

## Tutor com IA: o que os estudos mostram

| Estudo | Desenho | Resultado | O que muda aqui |
|---|---|---|---|
| [Kestin et al., Scientific Reports, 2025](https://pmc.ncbi.nlm.nih.gov/articles/PMC12179260/) | Ensaio cruzado, 194 alunos de física em Harvard | Ganho mediano mais que o dobro da aula ativa, em cerca de 49 contra 75 minutos | O tutor [soltava um passo por vez](https://hechingerreport.org/?p=103689) e nunca a solução inteira |
| [Bastani et al., PNAS, 2025](https://ideas.repec.org/a/nas/journl/v122y2025pe2422633122.html) | Quase mil alunos de matemática | ChatGPT comum: +48% na prática e −17% na prova sem IA. Tutor com pistas: +127% na prática e dano quase todo evitado | O tutor recebia a solução e os erros comuns, e só podia dar dicas |
| [Liding et al., pôster SIGCSE 2026](https://sigcse2026.sigcse.org/details/sigcse-ts-2026-posters/161/To-Tell-or-to-Ask-Comparing-the-Effects-of-Targeted-vs-Socratic-AI-Hints) | Ensaio com 178 alunos de CS1, dois semestres | Dica socrática: mais tempo, mais tentativas e mais erros repetidos, sem ganho depois | **Começar por dica direta**, e não por pergunta. O pôster não traz tamanho de efeito |
| [Eedi e Google, preprint](https://arxiv.org/pdf/2512.23633) | 165 alunos, exploratório | Com supervisão humana, a IA foi ao menos tão boa quanto tutores: [66%, 60% e 56% com dica estática](https://eedi.substack.com/p/ai-in-education-7-five-things-i-learned) | Dica estática é o piso, não o teto |
| [Learn Your Way, Google, 2025](https://research.google/blog/learn-your-way-reimagining-textbooks-with-generative-ai/) | [60 alunos](https://www.folio3.ai/ai-pulse/google-launches-ai-textbook-tool-showing-improved-student-retention), um capítulo só | 78% contra 67% de retenção em 3 a 5 dias | Várias formas do mesmo conteúdo ajudam. Não revisado por pares, e [não se sabe qual parte causou o ganho](https://www.folio3.ai/ai-pulse/google-launches-ai-textbook-tool-showing-improved-student-retention) |
| [CS50 Duck, Harvard, 2026](https://news.harvard.edu/gazette/story/2026/09/taming-the-duck-for-starters/) | Relato do professor, sem grupo de controle | Mesmo proibido, o pato entrega bloco de código em até 1/4 das vezes | **Prompt não é trava.** A regra precisa estar no código |

Modos de estudo do ChatGPT, do Gemini e do Claude [existem desde 2025](https://techcrunch.com/2025/08/06/google-takes-on-chatgpts-study-mode-with-new-guided-learning-tool-in-gemini/), mas não achei estudo independente medindo aprendizado neles.

**Decisões para o tutor:**

1. Fatos vêm do motor de compiladores do jogo. A IA nunca calcula FIRST nem tabela de cabeça. Se errasse, ensinaria errado.
2. Dica em três degraus: apontar o lugar, dizer por quê, mostrar resolvendo. A direta vem primeiro.
3. O último degrau só abre depois de tentar. Quem controla isso é o código.
4. Toda explicação termina num teste curto, porque o ganho do Bastani aparece só quando a pessoa precisa fazer sozinha.
5. Nada sai do aparelho. Sem API, sem chave e sem internet. O tutor é o motor do jogo, o modelo do aluno e a busca nas aulas, que já rodam no celular. Pergunta fora do que o jogo sabe recebe um "ainda não sei", e nunca um chute.

## Explicação interativa

- A [meta-análise de Hundhausen, Douglas e Stasko](https://www.academia.edu/15636659/A_Meta_Study_of_Algorithm_Visualization_Effectiveness) (24 experimentos) mostrou que importa mais o que o aluno faz com a animação do que como ela é.
- [Naps et al.](https://www.springerprofessional.de/doi/10.1145/1513593.1513600) organizaram isso em níveis: ver, responder, mudar, construir, apresentar. Os resultados [são mistos](https://research.utu.fi/converis/portal/detail/Publication/2808230?lang=en_GB), e [quem sabe pouco parece ganhar mais](https://research.utu.fi/converis/portal/detail/Publication/2808230?lang=en_GB) nos níveis altos.
- **Contraexemplo funciona:** no [Automata Tutor](https://www.microsoft.com/en-us/research/publication/can-automatic-feedback-help-students-construct-automata/), com 377 alunos, mostrar a cadeia em que o autômato erra foi considerado útil, aumentou a persistência e pode reduzir o tempo. Em [950 respostas](https://arxiv.org/abs/2005.01419v4), a avaliação foi muito positiva.
- Em compiladores, [LLparse e LRparse](https://scholars.duke.edu/publication/796188) já pediam a tabela passo a passo com retorno a cada passo. O [ParseIT++](https://cse.iitk.ac.in/users/karkare/MTP/2021-22/tushar2022parseit.pdf) tem até uma seção de jogo para tabela SLR.
- Não achei estudo isolando "mudar a entrada" contra "só ver". O ganho é esperado pela teoria, não medido.

**Decisão:** o Laboratório e as aulas sobem de nível. Hoje o aluno vê. Agora ele prevê, muda e constrói.

## Erros comuns como inimigos

Texto que nomeia o erro comum e o desmonta funciona: [g = 0,41 em 44 comparações](https://link.springer.com/article/10.1007/s10648-021-09656-z). Uma [meta-análise de 2025](https://par.nsf.gov/servlets/purl/10629626), com 76 estudos, manteve o resultado.

**Decisão:** cada erro comum vira um Bug com nome. Pegar o Bug é provar que você não cai mais nele. Isso junta o conteúdo à ficção do jogo, e o álbum de figurinhas já tinha agradado no primeiro painel.

## Mecânicas

| Ideia | Fonte | Peso | Uso aqui |
|---|---|---|---|
| Soma que sobe de tom | [Balatro](https://www.avclub.com/balatro-hones-the-art-of-making-numbers-go-up): cada efeito sobe o tom e a velocidade da contagem. [As camadas se somam](https://blakecrosley.com/guides/design/balatro) | Crítica, sem medição | Fim de partida e combo |
| Construir a máquina | [Manufactoria](https://www.404techsupport.com/2010/05/manufactoria-making-turing-machines-fun-again/): você monta a máquina que aceita ou rejeita robôs. A graça é [usar menos peças](https://tasvideos.org/Forum/Topics/9634) | Relato de jogadores | Fábrica de autômatos com contraexemplo |
| Um desafio por dia | [Wordle](https://www.moengage.com/blog/wordle-viral-growth-story/): limite diário cria escassez. O mesmo texto relata queda forte depois do pico | Blog de marketing | Desafio do dia |
| Corrida com escolhas | Jogos como [Learning Rogue](https://nodal.gg/game/learning-rogue-5013100) ligam estudo a combate. Não achei estudo | Sem evidência | Corrida intercalada, se agradar |
| Sequência com amigo | Duolingo: [22% mais chance de fazer a lição](https://blog.duolingo.com/friends-social-features/) | Dado da empresa | Fora daqui: o jogo não tem servidor |

**Efeitos:** um golpe bom costuma ser [5 a 8 reações pequenas em cerca de 100 ms](https://eastondev.com/blog/en/posts/dev/20260521-game-feedback-feel/), e a pausa no impacto fica [entre 0,05 e 0,12 s](https://uhiyama-lab.com/en/notes/unity/unity-game-feel-hit-feedback/). São guias de desenvolvedor, não estudos. Efeito demais [atrapalha](https://www.wayline.io/blog/juice-overload-sensory-feedback-hurts-gameplay), e [Juul e Begy](https://adk.elsevierpure.com/en/publications/good-feedback-for-bad-players-a-preliminary-study-of-juicy-interf) já tinham visto isso. Vale a regra anterior: efeito forte só em consequência.

**Shader:** dá para usar [RuntimeShader no Android 13 ou mais novo](https://proandroiddev.com/how-to-implement-shaders-in-compose-multiplatform-android-ios-desktop-web-c86a36dd9666) e [RuntimeShaderBuilder no desktop](https://proandroiddev.com/how-to-implement-shaders-in-compose-multiplatform-android-ios-desktop-web-c86a36dd9666), com plano B para versões antigas. O jogo aceita Android 7, então todo efeito precisa funcionar sem shader.

## Texto: o que existe para melhorar texto de IA

Não existe um site que resolva sozinho. Cada um cobre uma parte, e parte das listas de "melhores humanizadores" é [propaganda](https://www.rankmyai.com/rankings/top-ai-text-humanizer-tools).

| Ferramenta | Faz o quê | Em português? | Situação aqui |
|---|---|---|---|
| [LanguageTool](https://github.com/languagetool-org/languagetool) | gramática e estilo | sim | **Usado**, local, porta 8010. O modo exigente foi testado e descartado: deu 60 alertas, quase todos pedindo texto mais formal, como "utilizar" no lugar de "usar" e "perecer" no lugar de "morrem" |
| [ALT](https://legibilidade.com/sobre.html) | 6 índices de legibilidade adaptados ao português | sim | **Usado**: as fórmulas estão publicadas e entraram no verificador |
| [NILC-Metrix](https://github.com/sidleal/nilcmetrix) | 200 métricas da USP | sim | [Site no ar](https://pln.venturus.dev.br/nilcmetrix), mas com captcha. Dá para colar à mão, não para automatizar |
| [Lei 15.263/2025](https://www.planalto.gov.br/ccivil_03/_Ato2023-2026/2025/Lei/L15263.htm) | regras de linguagem simples | sim | **Usado**: ordem direta, frase curta, uma ideia por parágrafo, palavra comum, jargão explicado |
| [Humanizer](https://raw.githubusercontent.com/blader/humanizer/main/SKILL.md) | 26 padrões de texto de IA | adaptado | **Usado**: padrões traduzidos no verificador |
| [Wikipedia: Signs of AI writing](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing) | catálogo de vícios | adaptado | **Usado**. Avisa que só um conjunto de sinais conta, e que limpar a superfície não resolve |
| [Vale](https://github.com/jdkato/vale) | motor de regras de estilo | com regras próprias | Fora: nosso verificador já faz isso |
| [Clarice.ai](https://similarlabs.com/p/clarice-ai) | clareza em pt-BR | sim | Pago. Não testado |
| [DeepL Write](https://deepl.com/en/features/paraphrasing-tool) | reescreve | [fontes divergem](https://www.datahackers.news/p/melhorar-textos-com-ia-em-1-clique-vale-a-pena) sobre a qualidade em português | Bom para frase solta, sempre conferindo o sentido |
| [QuillBot](https://programminginsider.com/7-best-ai-humanizer-tools-for-content-creators-in-2026-tested-ranked/), [Quetext](https://www.quetext.com/blog/best-ai-humanizers-2026) | parafraseiam | parcial | Fora: em Compiladores, trocar palavra muda sentido |
| Detectores de IA | dizem se "parece IA" | | Fora: [erram demais](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing) e não dizem se o texto se entende |

**Cuidado com índice de legibilidade.** Em [estudo com leitores de segunda língua](https://www.lltjournal.org/item-detail/1186/), texto simplificado por IA e por gente não ficou mais fácil que o original quando se controlou a familiaridade com o tema. O próprio [ALT avisa](https://legibilidade.com/sobre.html) que o índice não mede coesão e recomenda encurtar a frase, não trocar o termo técnico. Modelos de linguagem [também não imitam bem um aluno real](https://aclanthology.org/2025.bea-1.75), então "pedir a uma IA que finja ser calouro" não substitui gente.

**Decisão:** quatro filtros em sequência, e no fim um sensor de verdade.

1. Escrita com as regras da [Lei 15.263/2025](https://www.planalto.gov.br/ccivil_03/_Ato2023-2026/2025/Lei/L15263.htm) e exemplo antes do termo.
2. `scripts/text_lint.py`: padrões de IA, voz passiva, nominalização, frase longa.
3. Legibilidade pelas fórmulas do ALT, sempre como alerta, nunca como meta.
4. LanguageTool no modo normal.
5. No jogo, cada fala ganha um "não entendi" que guarda qual fala foi. Só gente diz se entendeu.

Resultado nas falas do jogo: 683 falas, 6,6 palavras por frase, Flesch 91,7 e Gulpease 94,3 pelas fórmulas do ALT, zero violação de estilo. Isso diz que as frases são curtas. Não diz que são boas.

## Prompt

- O [guia da Anthropic para o Opus 5.5](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompting-claude-opus-5-5) recomenda listar padrões concretos a evitar, porque pedir "sem cara de IA" só troca um padrão por outro, e dizer quando a tarefa termina.
- Personas e "pense passo a passo" seguem fora, pelas razões de [PESQUISA-DIVERSAO.md](PESQUISA-DIVERSAO.md).

Essas regras guiaram a segunda reforma.
