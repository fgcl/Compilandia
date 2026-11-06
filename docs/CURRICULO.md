# Currículo: Compiladores (PUC Minas) × Compilândia

> Este documento liga **o que a disciplina de Compiladores costuma cobrar** a **onde isso está no jogo** (região, habilidade, minijogo), para você saber o que treinar e quando.

## Resumo rápido

- **A ementa oficial de Compiladores da universidade não foi encontrada.** Buscamos em pucminas.br (inclusive com `site:pucminas.br`), em repositórios públicos de alunos e em outros sites. O site da universidade não abria a partir do nosso ambiente, e nenhuma busca retornou a página da ementa ou o plano de ensino.
- **Encontramos dois indícios:**
  1. Um repositório público de um aluno da universidade com a **matriz curricular 3814** do curso. Nela, **Compiladores** está no **7º período**, com pré-requisitos **FTC** e **Arquitetura de Computadores II**. **FTC** está no **6º período**, com pré-requisito Teoria dos Grafos e Computabilidade (4º). [F1]
  2. **Editais de seleção de professores da universidade** (098/2024 e 032/2026) que, segundo os resultados de busca, listam para Compiladores: compilação e interpretação; análise léxica; análise sintática top-down e bottom-up; geradores de analisadores léxicos e sintáticos; tradução dirigida por sintaxe; análise semântica; geração de código intermediário; introdução à otimização. Não conseguimos abrir os PDFs para conferir o texto. [F2]
- **Novidade (2ª pesquisa): o trabalho prático real da disciplina.** Um relatório público de alunos de Engenharia de Computação da universidade, em BH (Compiladores, 2025/2, orientador Felipe de Castro Belém) mostra o que o TP exige [F14]:
  - o aluno projeta uma linguagem própria e uma gramática **LL(1)**;
  - escreve o **analisador léxico a partir de expressões regulares e AFDs**;
  - escreve um **parser descendente recursivo**, com a precedência em métodos encadeados;
  - faz a **análise semântica** com **tabela de símbolos com escopos aninhados** e verificação de tipos;
  - gera **assembly MIPS de 32 bits**, executado no simulador **MARS**;
  - aplica otimização simples (**constant folding**).
  
  No campus Poços de Caldas, um trabalho público de aluno faz o analisador léxico com **lex** (arquivo `.l`) [F15]. Isso orientou a 2ª versão do jogo, que ganhou novas habilidades, minijogos (AFN→AFD, Flex, recuperação de erros, registros de ativação, MIPS, código da descida recursiva) e o **Guia do Trabalho Prático**.
- Por isso, a **referência** deste documento é uma ementa síntese, montada a partir de disciplinas equivalentes de universidades brasileiras (UFU, UFRGS, UFFS, UFSCar, UFSC) e do **livro do Dragão** (Aho et al.). Ela bate com os tópicos dos editais da universidade.
- **Confirme com o professor.** O plano de ensino oficial costuma ser entregue na 1ª aula ou publicado no Canvas/SGA. Quando você tiver o plano, compare com a tabela da seção 3.

---

## 1. O que sabemos sobre a universidade

| Fato | Valor | Fonte | Confiança |
|---|---|---|---|
| Compiladores é obrigatória | Sim | Matriz 3814 em repositório de aluno [F1]; levantamento anterior | Alta |
| Período de Compiladores | 7º | [F1] | Alta (para a matriz 3814, campus Coração Eucarístico) |
| Pré-requisitos de Compiladores | FTC e Arquitetura de Computadores II | [F1] | Alta |
| Carga horária | ~80–84 h | Levantamento anterior; **não reconfirmado nesta pesquisa** | Média |
| Período de FTC | 6º (pré-req.: Teoria dos Grafos e Computabilidade) | [F1] | Média (matriz de um aluno; pode variar por versão) |
| Conteúdo de Compiladores | Léxica; sintática top-down e bottom-up; geradores (Lex/Yacc); tradução dirigida por sintaxe; semântica; código intermediário; introdução à otimização | Editais docentes PUC Minas, via busca [F2] | Média (documento oficial, mas não é a ementa; não lido na íntegra) |
| Ementa e plano de ensino oficiais | **Não encontrados** | — | — |
| Trabalho prático (BH, Eng. Computação, 2025/2) | Compilador completo em Java: léxico por AFD, gramática LL(1), descida recursiva, tabela de símbolos com escopos, tipos, geração de MIPS (MARS), constant folding | Relatório público de alunos [F14] | Alta (documento real de aluno; o enunciado pode variar por professor/semestre) |
| Trabalho de léxico (Poços de Caldas) | Analisador léxico com lex (`lex arquivo.l && gcc lex.yy.c`) | Repositório público de aluno [F15] | Média |

---

## 2. "Estou no 4º período": onde entra cada coisa

Na matriz 3814, quem está no 4º período **ainda não cursou FTC (6º) nem Compiladores (7º)**. Se a sua matriz for outra (versão nova, outro campus, dispensa), confira no SGA. Lá, a base de **gramáticas e autômatos** costuma ficar em **Fundamentos Teóricos da Computação (FTC)**. Nas disciplinas equivalentes de outras universidades, o conteúdo de FTC inclui autômatos finitos, expressões regulares, gramáticas livres de contexto, autômatos de pilha e máquinas de Turing [F9].

**O Compilândia cobre as duas disciplinas.** Ordem sugerida:

| Fase do seu estudo | Disciplina | Regiões do jogo |
|---|---|---|
| Agora (base) | FTC | Floresta Léxica (regex/AFD) · Vale das Gramáticas (GLC, derivação, ambiguidade) |
| Ponte | FTC → Compiladores | Porto da Partida (fases) · Ponte da Transformação (recursão e fatoração à esquerda) |
| Núcleo de Compiladores | Compiladores | Templo Top-Down · Fortaleza Bottom-Up · Oficina Yacc/Bison |
| Fim de Compiladores | Compiladores | Cidade Semântica · Forja do Código |

> FTC vai além do jogo: autômatos de pilha formais, hierarquia de Chomsky, máquinas de Turing e decidibilidade **não** estão no Compilândia.

---

## 3. Mapeamento: ementa → Compilândia

A coluna "Evidência" lista as ementas e planos em que o tópico aparece **explicitamente** (pelos trechos a que tivemos acesso; ausência não significa que a disciplina não cubra). Seções do Dragão = 2ª edição (Aho, Lam, Sethi, Ullman).

| # | Tópico da ementa | Evidência | Região do jogo | Habilidade (`Skill`) | Minijogos | Dragão |
|---|---|---|---|---|---|---|
| 1 | Compilação vs. interpretação; fases do compilador | Editais [F2], UFFS [F5], UFSC [F7] | Porto da Partida | Fases de um compilador (`PHASES`) | Esteira das Fases · Detetive de Erros | Cap. 1 |
| 2 | Tokens, lexemas, padrões (análise léxica) | Editais [F2], UFU [F3], UFRGS [F4], UFFS [F5], UFSC [F7] | Floresta Léxica | Tokens, lexemas e padrões (`TOKENS`) | Fatiador de Tokens · Porteiro Regex · Detetive de Erros | 3.1–3.3 |
| 3 | Expressões regulares e autômatos finitos | UFU [F3], UFRGS [F4], UFFS [F5]; FTC [F9] | Floresta Léxica | Expressões regulares e autômatos (`REGEX_DFA`) | Trem do Autômato · Porteiro Regex | 3.3–3.7 |
| 4 | Gramáticas livres de contexto e derivações | UFU [F3], UFRGS [F4], UFSC [F7]; FTC [F9] | Vale das Gramáticas | Gramáticas e derivações (`CFG_DERIVATION`) | Derivação Turbo · Duas Árvores | 4.2 |
| 5 | Árvores sintáticas e ambiguidade | Dragão (pré-requisito de LL/LR em todas) | Vale das Gramáticas | Árvores e ambiguidade (`AMBIGUITY`) | Duas Árvores · Derivação Turbo | 4.2.4–4.2.5, 4.3.2 |
| 6 | Eliminação de recursão à esquerda | Dragão (pré-requisito de LL(1)) | Ponte da Transformação | Recursão à esquerda (`LEFT_RECURSION`) | Cirurgia de Gramática · Sudoku LL(1) | 4.3.3 |
| 7 | Fatoração à esquerda | Dragão (pré-requisito de LL(1)) | Ponte da Transformação | Fatoração à esquerda (`LEFT_FACTORING`) | Cirurgia de Gramática · Sudoku LL(1) | 4.3.4 |
| 8 | Conjunto FIRST | UFRGS [F4] | Templo Top-Down | Conjunto FIRST (`FIRST`) | Alquimia FIRST/FOLLOW · Sudoku LL(1) | 4.4.2 |
| 9 | Conjunto FOLLOW | UFRGS [F4] | Templo Top-Down | Conjunto FOLLOW (`FOLLOW`) | Alquimia FIRST/FOLLOW · Sudoku LL(1) | 4.4.2 |
| 10 | Tabela preditiva LL(1) | Editais "top-down" [F2], UFRGS [F4] | Templo Top-Down | Tabela LL(1) (`LL1_TABLE`) | Sudoku LL(1) · Torre da Pilha | 4.4.3 |
| 11 | Analisador preditivo não recursivo (pilha) | Editais "top-down" [F2], UFRGS [F4] | Templo Top-Down | Analisador preditivo (`LL1_PARSING`) | Torre da Pilha · Derivação Turbo | 4.4.4 |
| 12 | Descida recursiva | Editais "top-down" [F2] | Templo Top-Down | Descida recursiva (`RECURSIVE_DESCENT`) | Torre da Pilha · Derivação Turbo | 4.4.1 |
| 13 | Análise bottom-up: empilha-reduz (shift-reduce) | Editais "bottom-up" [F2], UFRGS [F4], UFFS [F5] | Fortaleza Bottom-Up | Shift-reduce (`SHIFT_REDUCE`) | Esteira Shift-Reduce · Oficina Bison | 4.5 |
| 14 | Itens LR(0), closure, goto | UFFS [F5], UFSCar [F6] | Fortaleza Bottom-Up | Itens LR(0) (`LR0_ITEMS`) | Construtor de Estados · Esteira Shift-Reduce | 4.6.1–4.6.2 |
| 15 | Tabela SLR(1) e conflitos | UFRGS "tabelas LR" [F4], UFFS [F5], UFSCar [F6] | Fortaleza Bottom-Up | Tabela SLR(1) e conflitos (`SLR_TABLE`) | Caça-Conflitos · Esteira Shift-Reduce | 4.6.3–4.6.4 |
| 16 | LR(1) canônico e LALR(1) | UFSCar [F6] | Fortaleza Bottom-Up | LR(1) e LALR(1) (`LALR`) | Construtor de Estados · Caça-Conflitos | 4.7 |
| 17 | Resolução de conflitos com precedência/associatividade | UFSCar [F6] | Oficina Yacc/Bison | Yacc/Bison (`YACC_BISON`) | Oficina Bison · Caça-Conflitos | 4.8 |
| 18 | Geradores de analisadores (Lex/Flex, Yacc/Bison) | Editais [F2], UFU "metacompiladores" [F3], UFRGS [F4], UFFS [F5] | Oficina Yacc/Bison (Bison); Floresta Léxica (conceitos do Lex) | Yacc/Bison (`YACC_BISON`) | Oficina Bison | 3.5, 4.9 |
| 19 | Tradução dirigida por sintaxe; atributos sintetizados/herdados; S-/L-atribuídas | Editais [F2], UFU [F3], UFRGS [F4], UFFS [F5] | Cidade Semântica | Atributos e verificação de tipos (`ATTRIBUTES_TYPES`) | Árvore Anotada | Cap. 5 |
| 20 | Análise semântica e verificação de tipos | Editais [F2], UFU [F3], UFSCar [F6] | Cidade Semântica | Atributos e verificação de tipos (`ATTRIBUTES_TYPES`) | Árvore Anotada · Detetive de Erros | 6.3, 6.5 |
| 21 | Escopo e tabela de símbolos | UFSCar [F6], UFV [F8b] | Cidade Semântica | Escopo e tabela de símbolos (`SCOPE_SYMBOLS`) | Esconde-Esconde do Escopo · Detetive de Erros | 2.7 |
| 22 | Código intermediário: três endereços | Editais [F2], UFRGS [F4], UFFS [F5] | Forja do Código | Código de três endereços (`TAC`) | Linha de Montagem TAC · Árvore Anotada | 6.2, 6.4 |
| 23 | Otimização (introdução: dobra de constantes, blocos básicos, DAG) | Editais [F2], UFRGS [F4], UFFS [F5], UFV [F8b] | Forja do Código | Otimização (`OPTIMIZATION`) | Linha de Montagem TAC · Árvore Anotada | 8.5, 9.1 |

Os tópicos 1–23 cobrem todos os itens que os editais listam para Compiladores [F2]. A ficha da UFU GBC071 [F3] traz na ementa gramáticas, autômatos, análise léxica e sintática, geração de código, recuperação de erros e metacompiladores. Nos títulos do programa aparecem tradução dirigida por sintaxe, análise semântica e construção de AST. O plano da UFSC [F7] termina com a entrega de um compilador completo.

---

## 4. Lacunas: o que costuma cair e o jogo ainda não treina

Na 2ª versão, várias lacunas da 1ª foram fechadas:

| Tópico | Situação no Compilândia v2 |
|---|---|
| Recuperação de erros sintáticos (modo pânico, `error` no Yacc) | ✅ habilidade **Erros sintáticos e recuperação** + jogo **Sala do Pânico** + widget passo a passo |
| AFN→AFD (subconjuntos) | ✅ habilidade **AFN → AFD** + jogo **Fábrica de Subconjuntos** |
| Flex na prática (arquivo `.l`) | ✅ habilidade **Lex/Flex** + jogo **Oficina Flex** (maior casamento, ordem das regras) |
| Ambientes de execução (registros de ativação) | ✅ habilidade **Ambientes de execução** + jogo **Pilha de Chamadas** |
| Geração de código de máquina (MIPS, como no TP) | ✅ habilidade **Geração de código MIPS** + jogo **Montadora MIPS** + simulador "MARS de bolso" |
| Código do parser descendente recursivo (casaToken) | ✅ jogo **Programador Descendente** + capítulo do **Guia do TP** |
| Tabela LALR(1) e derivação mais à direita | ✅ widgets novos (também no Laboratório) |

O que **ainda não** está no jogo:

| Tópico | Sugestão |
|---|---|
| Minimização de AFD feita à mão (o motor minimiza, mas não há jogo) | Dragão 3.9.6 |
| Construção explícita de AST com classes | Guia do TP, capítulos de parser e semântica; Dragão 5.3 |
| Alocação de registradores, análise de fluxo de dados | Appel, caps. 10–11 (se cobrado) |
| Desvios e chamadas no simulador MIPS (o MARS de bolso só executa aritmética) | Testar no MARS de verdade; Guia do TP, cap. de MIPS |
| Autômatos de pilha formais, hierarquia de Chomsky, Turing | Material de FTC |

---

## 5. Bibliografia

| Livro | Por que usar | Partes mais úteis para o jogo |
|---|---|---|
| **AHO, A. V.; LAM, M. S.; SETHI, R.; ULLMAN, J. D.** *Compiladores: princípios, técnicas e ferramentas*. 2. ed. Trad. Daniel Vieira; rev. técnica Mariza Bigonha. São Paulo: Pearson Addison-Wesley, 2008. ISBN 978-85-88639-24-9. ("livro do Dragão") [F10] | Referência padrão das ementas; é a notação usada no jogo | Cap. 3 (léxica), Cap. 4 (sintática: LL, SLR, LR(1), LALR, Yacc), Cap. 5 (SDD), Cap. 6 (código intermediário, tipos) |
| **LOUDEN, K. C.** *Compiladores: princípios e práticas*. São Paulo: Pioneira Thomson Learning, 2004. ISBN 978-85-221-0422-2. [F11] | Mais didático e com um compilador completo (TINY) de exemplo; aparece na bibliografia da UFFS [F5] | Caps. 4 (top-down) e 5 (bottom-up), com exemplos passo a passo |
| **LEVINE, J.** *flex & bison*. Sebastopol: O'Reilly, 2009. ISBN 978-0-596-15597-1. [F12] | O guia prático de Flex/Bison, sucessor de *lex & yacc* | Caps. 1–3 (calculadora), cap. 7 (conflitos de ambiguidade) → Oficina Bison |
| **APPEL, A. W.** *Modern Compiler Implementation in Java*. 2. ed. (com J. Palsberg). Cambridge University Press, 2002. ISBN 978-0-521-82060-8. Há versões em C e ML. [F13] | Visão de projeto de um compilador real, do parser ao backend | Caps. 2–4 (léxica, sintática, AST), 5 (semântica), 7–8 (código intermediário) |

---

## 6. Fontes

> **Transparência:** a partir do nosso ambiente, não foi possível abrir pucminas.br, facom.ufu.br, inf.ufrgs.br e arxiv.org. Itens marcados com "(via busca)" foram verificados pelo título, resumo e trechos retornados pela busca, não pela leitura do documento inteiro. O repositório [F1] foi clonado e lido.

**PUC Minas**

- **[F1]** giusfds. *Materias do Curso de Ciência da Computação — Curriculo 3814 (PUC Minas)*. README lido em 01/11/2026. https://github.com/giusfds/computacaoPucMinas
- **[F2]** PUC Minas, Pró-Reitoria de Recursos Humanos. Editais de seleção docente da graduação, com o conteúdo de Compiladores (via busca):
  - Edital 098/2024: https://www.pucminas.br/selecaodocentes/Documents/2024/edital098-2024SE-Graduacao.pdf
  - Edital 032/2026: https://www.pucminas.br/selecaodocentes/Documents/2026/edital032.2026-SEPUCMinasGraduacao2.2026.pdf
- Guia da Carreira, *Ciência da Computação na PUC Minas* (sem detalhamento de disciplinas) (via busca): https://www.guiadacarreira.com.br/faculdades/puc-minas/cursos/ciencia-da-computacao

**Ementas comparáveis** (UDESC e IFCE foram buscadas; a ementa da UDESC não apareceu, e o PUD de Compiladores do IFCE Iguatu apareceu nos resultados sem trecho de conteúdo)

- **[F3]** UFU/FACOM. *Ficha de componente curricular GBC071 — Construção de Compiladores* (via busca): https://facom.ufu.br/system/files/conteudo/sei_5128918_ficha_de_componente_curricular_gbc071.pdf · Ficha anterior: https://facom.ufu.br/system/files/conteudo/facom39043-compiladores.pdf
- **[F4]** UFRGS/INF. *Plano de ensino INF01147 — Compiladores* (via busca): https://www.inf.ufrgs.br/dep.ina/planos/inf01147.pdf · INF01033: https://www.inf.ufrgs.br/dep.ina/planos/inf01033.pdf
- **[F5]** UFFS. *Plano de ensino — Construção de Compiladores* (via busca): https://acervo.uffs.edu.br/uploads/r/departamento-de-gestao-documental-2/4/d/f/4dfaa134bddfda33e0110b64b6dce0d459ce3b3e5f1cd6d8dfb894873adebe13/Constru____o_de_Compiladores.pdf
- **[F6]** UFSCar. *Plano de ensino — Construção de Compiladores 1* (2018) (via busca): https://www.aloc.ufscar.br/felice/ensino/2018s1compiladores1/PlanoEnsino.pdf
- **[F7]** UFSC. *Plano de ensino INE5426 — Construção de Compiladores* (via busca): https://programas-planos2.inf.ufsc.br/planos/5081.pdf
- **[F8]** IFCE Iguatu. *PUD S8 — Compiladores* (encontrado na busca, conteúdo não verificado): https://acervo.ifce.edu.br/iguatu/estudante/puds/puds_graduacao_bacharelado-em-ciencias-da-computacao/s8-compiladores.pdf/@@download/file/S8-Compiladores.pdf
- **[F8b]** UFV. *Programa analítico — Compiladores* (via busca): https://www3.dti.ufv.br/dti/catalogo/programa-analitico/54706
- **[F9]** Referências de FTC/Linguagens Formais (via busca): UFMG DCC, *Fundamentos da Teoria da Computação — plano*: https://homepages.dcc.ufmg.br/~nvieira/cursos/ftc/a14s1/plano.pdf · IFMG Bambuí: https://projetos.bambui.ifmg.edu.br/ementasifmg/ementas/Computação/BiSuCOM.125.pdf

**Bibliografia (dados de catálogo, via busca)**

- **[F10]** Ficha catalográfica da 2ª ed. brasileira do Dragão (revisão técnica UFMG): https://www.llp.dcc.ufmg.br/Publications/RevisaoTecnicaLivro2007/2007-compiladores-2a-edicao.pdf
- **[F11]** Louden, *Compiladores: Princípios e Práticas* (edições): https://www.goodreads.com/work/editions/1758327-compiler-construction-principles-and-practice?page=1
- **[F12]** Levine, *flex & bison*: https://bookshop.org/p/books/flex-bison-levine-john/803bc7754d42daf0 · https://oreilly.com/pub/pr/2380
- **[F13]** Appel, *Modern Compiler Implementation in Java*: https://www.cambridge.org/ci/universitypress/subjects/computer-science/programming-languages-and-applied-logic/modern-compiler-implementation-java?format=HB · https://www.cs.princeton.edu/~appel/modern/java/
- **[F14]** Relatório "Trabalho Prático – Compiladores: Compilador CF (Compila-Fofo)", PUC Minas, Eng. de Computação, 2025 — https://github.com/Maicon-Gomes-Messias/portfolio-academico/tree/main/compilador-java (lido na íntegra)
- **[F15]** Analisador léxico com lex, PUC Minas Poços de Caldas — https://github.com/andredfaria/analisador-lexico (lido)
