# Fábrica de Gramáticas: arquitetura e regras para crescer

A Fábrica é um jogo de automação no jeito do Mindustry: o jogador compra blocos na loja, põe no chão, liga com esteiras e escolhe a regra de cada máquina. **A fábrica é a gramática**: a gramática que o Laboratório analisa é a soma das regras das máquinas que estão no chão.

## Como o Mindustry organiza isso (e o que copiamos)

O Mindustry é código aberto (github.com/Anuken/Mindustry). Ele não é feito de "módulos" no sentido de plugins; ele separa **definição** de **instância** e registra todo o conteúdo num lugar só.

| Mindustry | Aqui | Onde |
|---|---|---|
| `Block`: a definição de um tipo (tamanho, `category`, `requirements`, flags como `hasItems`) | `BlocoTipo` | `fabrica/Conteudo.kt` |
| `Building`: a cópia numa célula, com o estado dela, criada por `buildType` | `FabGram.Bloco` (posição, tamanho, letra, regra) | `tutor/FabGram.kt` |
| `Blocks.load()`: todo bloco do jogo nasce ali, com nome estável | `object Blocos` | `fabrica/Conteudo.kt` |
| `Category` e o `PlacementFragment` (o menu de construção por abas) | `Categoria` e `MenuConstrucao` | `fabrica/Conteudo.kt`, `ui/fabgram/FabGramMenu.kt` |
| `TechTree` e `researchCost` (destravar com recurso) | `BlocoTipo.preco`, `BlocoTipo.requer`, `object Loja` | `fabrica/Loja.kt` |
| Os módulos de um `Building` (`ItemModule`, `PowerModule`...), ligados por flags no `Block` | `Papel`: o que o bloco faz na corrida | `fabrica/Conteudo.kt`, `tutor/FabGramCorrida.kt` |
| `acceptItem`/`handleItem`/`dump`: o item só entra no bloco para onde a esteira aponta | `rota()` e `segue()` | `tutor/FabGramCorrida.kt` |
| Sorter configurável (escolhe o item) | Máquina e separador configuráveis (escolhem regra e letra) | `ui/fabgram/FabGramConfig.kt` |
| Sprites por bloco | `Desenhos.porTipo` (desenho por `id`) | `ui/fabgram/Desenhos.kt` |

Diferença de propósito: o Mindustry roda uma simulação a cada tique. Aqui a corrida é calculada de uma vez a partir da árvore de derivação (a frase é o produto) e depois animada. Isso deixa o resultado exato e testável, que é o que importa para ensinar.

## Camadas

```
fabrica/   conteúdo puro: tipos de bloco, categorias, loja. Não importa UI.
tutor/     o modelo da planta (FabGram), a corrida, a planta automática, as missões. Não importa UI.
learning/  o save: missões feitas, estrelas, blocos comprados (fabGramBlocos).
ui/fabgram o palco, os desenhos, o menu de construção, a loja, o painel de configurar.
```

O `ArchitectureAndLimitsTest` barra import de UI em `fabrica/` e `tutor/`.

## Regras

1. **Um tipo de bloco nasce em um lugar só**: `Blocos` em `fabrica/Conteudo.kt`. Nada de `when` espalhado com a lista de blocos; quem precisa da lista lê `Blocos.todos`, `Blocos.doMenu(categoria)` ou `Blocos.maquinas`.
2. **O `id` é para sempre.** Ele vai para o save (`FabGram.salva`) e para a chave do texto. Renomear um id perde as plantas guardadas e as compras dos jogadores.
3. **O que é igual em todas as cópias fica no `BlocoTipo`; o que muda por cópia fica no `Bloco`.** Preço, tamanho, capacidade e categoria são do tipo. Posição, letra e regra são da instância.
4. **Comportamento novo é um `Papel` novo.** Se o bloco faz algo que nenhum papel faz, crie o papel e trate na corrida (`segue` para o caminho do item, `processa` para o que acontece com a caixa). Se faz o mesmo que outro, reaproveite o papel e mude só o tipo (o Cofre e o Armário são as duas `MAQUINA`).
5. **A UI não decide regra de jogo.** Comprar é `Loja.situacao` + `Learner.compraBlocoFabrica`; construir, desmontar e configurar são métodos da `Planta`, que devolvem a planta nova (ou nulo, se não pode). A tela só chama e guarda.
6. **A planta é imutável.** Toda mudança devolve uma `Planta` nova; é isso que faz o Desfazer funcionar de graça.
7. **Erro do jogador vira quebra com motivo, nunca exceção.** Letra sem separador, esteira que acaba, bloco sem saída: tudo vira `Quebra` com um `Motivo` e um texto que diz o que consertar.
8. **Cada desenho cabe na célula de 32 "pixels" da arte** (`Pincel.u`) e usa a cor da categoria na faixa de baixo. Cores ficam em `CoresFabrica.kt`.
9. **Textos nos dois idiomas**: `fabgram_bloco_<id>` e `fabgram_bloco_<id>_desc` em `textos/pt` e `textos/en`.

## Receita: criar um bloco novo

Exemplo: uma "Ponte" que leva o item três células adiante.

1. `fabrica/Conteudo.kt`: `val PONTE = BlocoTipo("ponte", Papel.PONTE, Categoria.DISTRIBUICAO, 1, 1, preco = 25, requer = JUNCAO)` e ponha em `todos`.
2. Se o papel é novo: acrescente `PONTE` em `Papel` e trate em `segue()` (`tutor/FabGramCorrida.kt`), como a `JUNCAO`.
3. `ui/fabgram/Desenhos.kt`: escreva `ponte(...)` e registre em `Desenhos.porTipo`.
4. Textos: `fabgram_bloco_ponte` e `fabgram_bloco_ponte_desc` nos dois idiomas.
5. Teste de comportamento em `ConteudoFabricaTest.kt` (veja `aJuncaoDeixaOItemAtravessar`).

O `ConteudoFabricaTest` reclama sozinho se faltar o desenho, um texto, se o id repetir ou se a árvore da loja fechar um ciclo.

## Economia

- Comprar na loja destrava o tipo para sempre e custa engrenagens (as mesmas da sala). Construir no chão não custa: a dificuldade está em montar, não em juntar dinheiro.
- A planta automática (a que nasce do texto do painel) pode usar qualquer bloco. A loja limita só o que o jogador põe à mão.
- Missões rendem 5 engrenagens na primeira vez; desafios, 5 por estrela nova (`Learner.cumpreMissaoFabrica`).

## Save

`FabGram.salva` grava a versão `3`: cada bloco com id, tipo, posição, tamanho, letra e regra, e cada esteira. Planta da versão 2 (só posições) não é lida; a tela refaz a planta pela gramática do painel.
