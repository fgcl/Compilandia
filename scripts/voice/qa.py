#!/usr/bin/env python3
"""Confere as falas renderizadas transcrevendo cada uma com o Whisper.

Ninguém ouve 300 áudios de ouvido. Aqui a máquina escuta e compara com o texto: fala com muita
diferença entra na lista para corrigir no lexicon.tsv. Sai com a taxa de acerto por palavra.

Uso: qa.py [--limite 0.6] [--amostra N]
"""
import argparse
import difflib
import re
import subprocess
import sys
from pathlib import Path

import numpy as np
from faster_whisper import WhisperModel

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / 'composeApp/src/commonMain/composeResources/files/voice'


def norm(t):
    t = t.lower().replace('**', '').replace('`', '')
    t = re.sub(r'[^\w\sà-ÿ]', ' ', t)
    return t.split()


def carrega(ogg):
    """Decodifica com o ffmpeg para 16 kHz, sem depender do PyAV."""
    pcm = subprocess.run(
        ['ffmpeg', '-hide_banner', '-loglevel', 'error', '-i', str(ogg), '-f', 'f32le', '-ac', '1', '-ar', '16000', '-'],
        capture_output=True, check=True,
    ).stdout
    return np.frombuffer(pcm, dtype=np.float32).copy()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--tsv', default=str(ROOT / 'composeApp/build/voice-lines.tsv'))
    ap.add_argument('--limite', type=float, default=0.62)
    ap.add_argument('--amostra', type=int, default=0)
    ap.add_argument('--threads', type=int, default=4)
    a = ap.parse_args()
    linhas = [l.rstrip('\n').split('\t') for l in open(a.tsv, encoding='utf8') if l.strip()]
    if a.amostra:
        linhas = linhas[:: max(len(linhas) // a.amostra, 1)]
    m = WhisperModel('small', device='cpu', compute_type='int8', cpu_threads=a.threads)
    certas = total = 0
    ruins = []
    for fid, quem, texto in linhas:
        segs, _ = m.transcribe(carrega(OUT / f'{fid}.ogg'), language='pt', beam_size=1, vad_filter=False)
        ouvido = ' '.join(s.text.strip() for s in segs)
        x, y = norm(texto), norm(ouvido)
        r = difflib.SequenceMatcher(None, x, y).ratio()
        iguais = sum(b.size for b in difflib.SequenceMatcher(None, x, y).get_matching_blocks())
        certas += iguais
        total += len(x)
        if r < a.limite:
            ruins.append(fid)
            print(f'{r:.2f} [{quem}]\n  texto: {texto}\n  ouvi : {ouvido}', flush=True)
    print(f'{len(linhas)} falas, {100 * certas / max(total, 1):.1f}% das palavras reconhecidas, {len(ruins)} abaixo de {a.limite}', flush=True)


if __name__ == '__main__':
    sys.exit(main())
