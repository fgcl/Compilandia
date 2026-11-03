#!/usr/bin/env python3
"""Transforma os renders da oficina em camadas para o jogo.

Compara base.png com cada with_<item>.png. O que mudou (móvel, sombra e luz) vira uma camada
recortada, com a posição anotada em ui/RoomLayers.kt. Assim comprar um móvel é só desenhar a camada.

Uso: layers.py <renders> <composeResources/drawable> <RoomLayers.kt> [largura_final]
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

src, drawable, kotlin = Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3])
FINAL = int(sys.argv[4]) if len(sys.argv) > 4 else 1100
# Ordem de pintura: de trás para a frente, para um móvel na frente cobrir o de trás.
ORDER = ['quadro', 'planta', 'luminaria', 'lixeira', 'caixasom', 'cadeira', 'abajur', 'estante', 'radio', 'sofa', 'cafe', 'ursinho']

base_full = Image.open(src / 'base.png').convert('RGBA')
scale = FINAL / base_full.width
base_full.resize((FINAL, FINAL), Image.LANCZOS).save(drawable / 'room_base.webp', 'WEBP', quality=84, method=6)
base = np.asarray(base_full).astype(np.float32) / 255

rows = []
total = 0
for name in ORDER:
    im = Image.open(src / f'with_{name}.png').convert('RGBA')
    a = np.asarray(im).astype(np.float32) / 255
    diff = np.abs(a - base).max(axis=2)
    # O render tem chuvisco nas sombras suaves: borrar antes de medir tira o ruído da máscara.
    soft = np.asarray(Image.fromarray((diff * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.2))).astype(np.float32) / 255
    # Abaixo de 1,6% é ruído; acima de 6% a camada é totalmente opaca.
    mask = np.clip((np.maximum(soft, (diff > 0.13) * 1.0) - 0.016) / (0.06 - 0.016), 0, 1)
    ys, xs = np.where(mask > 0.02)
    if len(xs) == 0:
        raise SystemExit(f'{name}: nada mudou no render')
    pad = 4
    x0, x1 = max(xs.min() - pad, 0), min(xs.max() + pad + 1, a.shape[1])
    y0, y1 = max(ys.min() - pad, 0), min(ys.max() + pad + 1, a.shape[0])
    out = a.copy()
    out[..., 3] = mask * np.maximum(a[..., 3], base[..., 3])
    crop = Image.fromarray((out[y0:y1, x0:x1] * 255 + .5).astype(np.uint8), 'RGBA')
    w, h = max(int(crop.width * scale), 1), max(int(crop.height * scale), 1)
    crop = crop.resize((w, h), Image.LANCZOS)
    f = drawable / f'room_item_{name}.webp'
    crop.save(f, 'WEBP', quality=82, alpha_quality=88, method=6)
    total += f.stat().st_size
    # Silhueta: só o móvel (a sombra muda pouco o tom do piso escuro), com borda amarela.
    solid = np.asarray(Image.fromarray(((diff > 0.13) * 255).astype(np.uint8)).crop((x0, y0, x1, y1)).resize((w, h), Image.BILINEAR)) > 127
    grow = np.zeros_like(solid)
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1), (2, 0), (-2, 0), (0, 2), (0, -2)):
        grow |= np.roll(np.roll(solid, dx, axis=1), dy, axis=0)
    edge = grow & ~solid
    g = np.zeros((h, w, 4), dtype=np.uint8)
    g[..., 0], g[..., 1], g[..., 2] = 245, 183, 0
    g[..., 3] = np.where(edge, 235, np.where(solid, 34, 0))
    gf = drawable / f'room_ghost_{name}.webp'
    Image.fromarray(g, 'RGBA').save(gf, 'WEBP', quality=70, alpha_quality=70, method=6)
    total += gf.stat().st_size
    W = float(base_full.width)
    rows.append((name, x0 / W, y0 / W, w / FINAL, h / FINAL))
    print(f'{name:10s} {w:4d}x{h:<4d} {f.stat().st_size // 1024:4d} KB')

body = ',\n'.join(
    f'    "{n}" to RoomLayer(Res.drawable.room_item_{n}, Res.drawable.room_ghost_{n}, {x:.4f}f, {y:.4f}f, {w:.4f}f, {h:.4f}f)' for n, x, y, w, h in rows)
kotlin.write_text(f'''package com.compilandia.ui.room

import com.compilandia.resources.Res
{chr(10).join(sorted([f"import com.compilandia.resources.room_ghost_{n}" for n in ORDER] + [f"import com.compilandia.resources.room_item_{n}" for n in ORDER]))}
import org.jetbrains.compose.resources.DrawableResource

/** Gerado por scripts/art/layers.py. Posição e tamanho como fração da largura da sala. */
data class RoomLayer(val art: DrawableResource, val ghost: DrawableResource, val x: Float, val y: Float, val w: Float, val h: Float)

/** Na ordem de pintura: de trás para a frente. */
val roomLayers: Map<String, RoomLayer> = linkedMapOf(
{body},
)
''')
print('camadas:', total // 1024, 'KB; sala base:', (drawable / 'room_base.webp').stat().st_size // 1024, 'KB')
