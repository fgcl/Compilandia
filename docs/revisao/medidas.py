import re,glob,collections,sys
rows=[]
ab=collections.Counter(); pv=collections.Counter()
for f in sorted(glob.glob('dump/regiao*_A_trilha.md')):
    t=open(f).read()
    for sec in re.split(r'\n## ASSUNTO ',t)[1:]:
        skill=sec.split(':')[0]
        m=re.search(r'Opções: (.*)',sec)
        if m:
            o=m.group(1).split(' | '); ab[[i for i,x in enumerate(o) if x.startswith('✔')][0]]+=1
        eps=re.split(r'\n#### ',sec)
        for e in eps:
            if not e.startswith('AULA ANIMADA BÁSICA'): continue
            scenes=re.split(r'\n\d+\. TELA:',e)[1:]
            inter=[('PERGUNTA (toque)' in s) or ('BRINQUEDO' in s) for s in scenes]
            run=mx=0
            for x in inter:
                run=0 if x else run+1; mx=max(mx,run)
            first=inter.index(True)+1 if True in inter else 0
            words=sum(len(re.findall(r'\w+',l)) for s in scenes for l in re.findall(r'(?:BYTE|PROF|LEXI|PILHA|NARRADOR): "(.*)"',s))
            secs=re.search(r'~(\d+)s',e).group(1)
            # perguntas binarias (certo/errado com 1 alternativa errada)
            bins=sum(1 for s in scenes if 'PERGUNTA' in s and len(re.search(r'errado = (.*)',s).group(1).split(' | '))==1)
            rows.append((skill,len(scenes),sum(inter),sum('BRINQUEDO' in s for s in scenes),bins,first,mx,words,secs))
print('assunto cenas interacoes brinquedos binarias 1a_interacao maior_seq_passiva palavras_faladas seg_estimado')
for r in rows: print(*r)
print('total cenas',sum(r[1] for r in rows),'interacoes',sum(r[2] for r in rows),'binarias',sum(r[4] for r in rows))
print('abertura posicao certa',dict(ab))
for f in sorted(glob.glob('dump/*_B_texto.md'))+['dump/seu_compilador_emC_e_guiaTP.md']:
    for m in re.finditer(r'    opções: (.*)',open(f).read()):
        o=m.group(1).split(' | '); pv[([i for i,x in enumerate(o) if x.startswith('✔')] or [-1])[0],len(o)]+=1
print('PREVEJA (posicao certa, n opcoes)',sorted(pv.items()))
# estilos por assunto
for f in sorted(glob.glob('dump/regiao*_B_texto.md')):
    t=open(f).read()
    for sec in re.split(r'\n## ASSUNTO ',t)[1:]:
        skill=sec.split(':')[0]; st=re.findall(r'#### ESTILO (\w+)',sec)
        kinds=collections.Counter(re.findall(r'\n- (TEXTO|CÓDIGO|CAIXA|PREVEJA|REVELAR|PASSOS|WIDGET|[A-Z]+):',sec))
        print(skill,len(st),'estilos',len(sec.split()),'palavras',dict(kinds))
