# Pesquisa: como deixar a Compilândia divertida

Feita em 2 de novembro de 2026. Cada decisão abaixo aponta para a fonte.

Nem toda fonte tem o mesmo peso. Meta-análises e ensaios randomizados valem mais. Números do Duolingo e do Finch vêm de palestras e estimativas de terceiros: servem de pista, não de prova.

## O que estava errado

Olhando as capturas de tela do jogo:

- **Aula:** chat de texto com a tela quase vazia.
- **Jogo:** parece formulário, com cerca de 60% da tela em branco.
- **Mapa:** lista parada de gavetas.
- **Personagens:** nenhum se mexe.

## Texto: existe site que melhora texto de IA?

Sim, mas nenhum resolve sozinho. Cada um faz uma parte.

| Ferramenta | Faz o quê | Serve aqui? |
|---|---|---|
| [LanguageTool](https://github.com/languagetool-org/languagetool) | gramática e estilo, com pt-BR | **Sim.** Já roda neste computador, na porta 8010 (versão 6.8), então nada sai da máquina |
| [DeepL Write](https://www.deepl.com/pt-BR/press-release/deepl-launches-new-italian-and-portuguese-ai-writing-assistant) | reescreve, tem pt-BR desde 2024 | Bom para frase solta. [Um blog](https://doutoranathalia.com.br/blog/deepl-write-para-escrita-academica-em-portugues/) aponta qualidade menor em áreas técnicas. Conferir o sentido |
| [Hemingway](https://omidsaffari.com/blog/grammarly-alternatives) | marca frase longa e difícil | A ideia é ótima, mas foi feito para inglês. Reproduzida no nosso verificador |
| [NILC-Metrix](https://arxiv.org/pdf/2201.03445) | 200 métricas de complexidade em pt-BR (USP) | Referência. Usamos o índice Flesch adaptado |
| [Wikipedia: Signs of AI writing](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing) | catálogo dos vícios de texto de IA | **Sim.** É a base da lista de proibições |
| [Humanizer](https://www.claudepluginhub.com/plugins/blader-humanizer) | skill do Claude baseada nesse catálogo | A ideia entrou no verificador |

**Detectores de IA ficam de fora.** Em [Liang et al. (2023)](https://themarkup.org/machine-learning/2023/08/14/ai-detection-tools-falsely-accuse-international-students-of-cheating), sete detectores marcaram como IA cerca de 61% das redações de quem não é nativo em inglês. A própria Wikipedia [recomenda não confiar só neles](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing).

**"Humanizadores" que parafraseiam ficam de fora também.** Isso é decisão minha, sem estudo de apoio: em Compiladores, trocar uma palavra muda o sentido, e não achei pesquisa medindo se essas ferramentas preservam o significado.

**O que faremos:** frases curtas escritas uma a uma, passando por `scripts/text_lint.py` (legibilidade, vícios de IA e LanguageTool local). A melhor correção para texto ruim é ter menos texto.

## Voz

**Kokoro `pm_santa` funciona.** O pacote tem [três vozes pt-BR](https://huggingface.co/hexgrad/Kokoro-82M/raw/main/VOICES.md): `pm_santa`, `pm_alex` e `pf_dora`, licença Apache 2.0. O próprio projeto avisa que o suporte fora do inglês pode ser limitado.

Testado neste computador (CPU, 16 núcleos, máquina ocupada):
- Gera fala cerca de 1,8× mais rápido que o tempo real.
- O Whisper transcreveu quase tudo certo. Os erros foram termos técnicos: "Byte" virou "Bitte", "scanner" virou "caneiros". Isso pede um dicionário de pronúncia.
- O Kokoro entrega a duração de cada fonema, com precisão de amostra. Dá para sincronizar a boca sem reconhecimento de fala.

**Decisão:** renderizar antes e guardar os áudios no jogo. Assim o APK não carrega modelo de IA, e o jogo continua offline.

Alternativas não testadas: [Chatterbox e Rápido TTS](https://huggingface.co/edwixx/rapido-tts) aparecem como opções para português. Ficam como plano B, já que você pediu o Kokoro.

**Por que voz ajuda:**
- Narração com imagem ganha de texto com imagem. No laboratório do Mayer, a [mediana foi d = 1,02](https://www.cambridge.org/core/books/abs/multimedia-learning/modality-principle/E5CD6E01CEA0B568CE260F66A3CD0D1F). Em [91 estudos](https://journal.psych.ac.cn/adps/EN/volumn/volumn_204.shtml) o efeito é bem menor: d ≈ 0,25. O ganho é real, mas modesto.
- Voz sintética moderna [ensina tanto quanto voz humana](https://www.doi.org/10.1007/s10639-022-11255-6) nos estudos de Craig e Schroeder. A voz humana só ganha em simpatia.
- Repetir a fala como legenda atrapalha ([princípio da redundância](https://educationaltechnology.net/?p=1878)). Por isso a legenda só aparece com o som desligado.

## Animação e efeitos

- Animação ganha de imagem parada, com efeito pequeno: [g = 0,23](https://archive-ouverte.unige.ch/unige:92234) e [d = 0,37](https://www.leibniz-ipn.de/en/research/publications/instructional-animation-versus-static-pictures-a-meta-analysis).
- Ela funciona mesmo [quando o que se aprende é a própria mudança](https://link.springer.com/article/10.1007/s11251-021-09541-w). Compiladores é isso: autômato andando, pilha crescendo, árvore subindo.
- Cortar em pedaços controlados por quem aprende [ajuda](https://www.doi.org/10.1007/S10648-018-9456-4).
- Rosto em objetos e cores agradáveis [aumentam o aprendizado](https://artemis.ms.mff.cuni.cz/main/papers/emodesign_manuscript_1rev_180928_FINAL_norev.pdf): retenção d = 0,39, compreensão d = 0,32. Isso justifica o Byte com rosto.
- **O limite:** enfeite interessante mas sem relação com o conteúdo [prejudica](https://news.wsu.edu/press-release/2020/03/19/seductive-details-inhibit-learning) (58 estudos, mais de 7.500 alunos).
- Em jogos, [Juul e Begy](https://adk.elsevierpure.com/en/publications/good-feedback-for-bad-players-a-preliminary-study-of-juicy-interf) viram que efeito demais agrada mais, porém a pessoa joga pior.

**Decisão:** efeito forte só no acerto, no erro e na recompensa. Durante a explicação, só se move o que explica.

**Como o Duolingo faz:** [o blog da empresa](https://blog.duolingo.com/world-character-visemes) conta que eles geram a fala, extraem o tempo de cada fonema e ligam cada fonema a uma das mais de 20 bocas. Piscada e cabeça se movem à parte, e há um estado final de reação ao acerto ou erro. Vamos seguir o mesmo caminho em tamanho menor.

**Ferramenta:** o Rive, usado pelo Duolingo, não apareceu com suporte a Compose Multiplatform. O [Compottie](https://github.com/lightsummer233/Compottie) toca Lottie em Android e desktop, mas tudo desenhado em Compose evita dependência e deixa a boca e o palco sob nosso controle.

## Mecânicas

Do mais forte para o mais fraco:

1. **Aprender dentro da mecânica.** Em [Zombie Division](https://shura.shu.ac.uk/3556/), a versão com o conteúdo embutido na ação ensinou mais e foi jogada 7 vezes mais tempo por vontade própria. Um [estudo pré-registrado](https://universityxp.com/research/2023/9/20/learning-by-doing-intrinsic-integration-directs-attention-to-increase-learning-in-games) atribui o ganho à atenção. O verbo do jogo precisa ser o verbo do compilador.
2. **Testar antes de explicar.** Resolver antes da aula tem efeito [g = 0,87](https://www.timeshighereducation.com/campus/using-productive-failure-activate-deeper-learning) em STEM; [outra versão](https://https-sage-cnpereading-com-443.webvpn1.xju.edu.cn/doi/10.3102/00346543211019105) do trabalho dá 0,55 a 0,64. Lembrar em vez de reler [tem g ≈ 0,6](https://www.learningscientists.org/blog/2017/2/9-1).
3. **Jogos digitais** ganham de ensino sem jogo, [g = 0,33](https://sri.com/publication/digital-games-design-and-learning-a-systematic-review-and-meta-analysis-executive-summary/). Pontos sem contexto rendem menos: em gamificação, [ficção e interação social](https://link.springer.com/article/10.1007/s10648-019-09498-w) mudam o resultado no comportamento. Por isso o Bug e a sala, e não só mais XP.
4. **Puzzles estilo Human Resource Machine** [escondem a sintaxe](https://www.theworkprint.com/human-resource-machine-review/123), sobem a dificuldade aos poucos e dão um segundo objetivo de otimização. [Parsons](https://repository.falmouth.ac.uk/5216/) (montar código com blocos) ensina igual a escrever do zero, em menos tempo.
5. **Sequência diária** (Duolingo, dados de palestra): mostrar a sequência no topo deu [+3% de usuários diários](https://econsultancy.com/six-a-b-tests-used-by-duolingo-to-tap-into-habit-forming-behaviour/), e a proteção contra perder reduziu perdas. Mas o teste de conquista só por entrar não teve efeito.
6. **Bloqueio por erro** (Duolingo, dado da empresa): trocaram corações por energia porque [iniciantes ficavam sem corações no meio da lição](https://www.androidauthority.com/quitting-duolingo-energy-system-3599842/). Aqui errar nunca bloqueia.
7. **Sala e bichinho** (Finch, estimativa): o laço tarefa → recompensa → sala [cria apego](https://www.deconstructoroffun.com/blog/x0hd2ssr80y5n7gv0w967pg7hwd7tl). Retenção estimada de D1/D7 54%/37%. A fonte é um blog e usa dados estimados, mas a ideia combina com a oficina.

## Sala e assets

| Pacote | Licença | Conteúdo | Observação |
|---|---|---|---|
| [Kenney Furniture Pack](https://opengameart.org/content/furniture-kit) | CC0, conferida no arquivo | 120 modelos 3D | Baixado e inspecionado. Os sprites isométricos têm só 27×49 px, então é preciso renderizar os modelos |
| [KayKit Furniture Bits](https://kaylousberg.itch.io/furniture-bits) | CC0 (versão grátis) | mais de 50 modelos | Não baixado |
| [Quaternius Furniture](https://opengameart.org/content/furniture-low-poly) | CC0 | móveis 3D | Não baixado |
| [Quaternius Animated Robot](https://opengameart.org/content/animated-lowpoly-robot) | CC0 | robô com 14 animações | Referência, o Byte precisa de rosto próprio |

**O que foi feito:** a sala usa os modelos 3D do Kenney Furniture Pack, renderizados no Blender. Cada móvel é uma camada com a própria sombra e luz, então comprar uma peça não exige refazer a imagem. O Byte, a Profª Gramática e o Bug são desenhados em Compose, em estilo chapado: foi o que passou no voto do ar-board, e é o que permite boca e reação em tempo real.

## Estudos com IA

**Estudar com IA:**
- Tutor bem desenhado funciona: em Harvard, [um ensaio cruzado com 194 alunos](https://aiedresearch.substack.com/p/ai-tutoring-outperforms-active-learning) aprendeu mais, em menos tempo, do que em aula ativa. Não houve teste tardio.
- IA sem proteção pode prejudicar: em [quase mil alunos de matemática](https://www.psypost.org/unrestricted-generative-ai-harms-high-school-math-learning-by-acting-as-a-crutch/), quem usou ChatGPT comum foi 17% pior depois que o acesso saiu. O grupo com tutor que dava pistas evitou boa parte disso.
- Com supervisão humana, [Eedi e Google](https://arxiv.org/abs/2512.23633v1) acharam desempenho igual ou melhor que o de tutores sozinhos (estudo exploratório, 165 alunos, ainda em preprint).
- [Tutor CoPilot](https://nssa.stanford.edu/studies/tutor-copilot-human-ai-approach-scaling-real-time-expertise): IA ajudando o tutor rendeu +4 pontos percentuais de domínio, e +9 com tutores mais fracos.
- Na [Nigéria](https://openknowledge.worldbank.org/bitstreams/cd9cca5b-2ffd-4be1-8313-0f4d2ed6ef4f/download), um programa com IA deu 0,3 desvio-padrão, mas [a comparação foi contra nenhum programa](https://pershmail.substack.com/p/ai-is-maybe-sometimes-better-than).
- Meta-análises dão g de [0,58](https://onlinelibrary.wiley.com/doi/10.1111/jcal.70096) a [0,67](https://www.nature.com/articles/s41599-026-07019-z), com estudos fracos e muita variação. Em uma delas, [o efeito some depois de 10 semanas](https://journals.sagepub.com/doi/10.1177/07356331251349620).

**A lição:** o que importa é o desenho pedagógico, não ter IA. O jogo já roda sem LLM, com BKT e rede pequena. Falta aplicar o que esses estudos mostram: dica em degraus e nunca a resposta de graça.

**Escrever prompts:**
- [Personas de especialista](https://arxiv.org/abs/2512.05858) não melhoraram a precisão.
- [Pedir raciocínio passo a passo](https://arxiv.org/abs/2506.07142v1) rende pouco em modelos de raciocínio.
- [Educação no prompt](https://arxiv.org/pdf/2503.04818) às vezes ajuda, às vezes atrapalha. [Outro estudo](https://sloanreview.mit.edu/article/three-things-to-know-about-prompting-llms) discorda, então a questão segue aberta.
- [A Anthropic](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompting-claude-opus-5-5) recomenda dizer quando a tarefa termina e dar lista concreta do que evitar. Pedir "sem cara de IA" só troca um padrão por outro.

Essas regras moldaram esta reforma.
