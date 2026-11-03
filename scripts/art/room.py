"""Renderiza a oficina do Byte no Blender, sem interface.

Uso (ver scripts/art/README.md):
  flatpak run --filesystem=/tmp org.blender.Blender -b --python scripts/art/room.py -- <Models> <saida> <textura> [resolucao]

Gera <saida>/base.png e um <saida>/with_<item>.png por móvel comprável.
layers.py compara cada par e extrai só o que o móvel mudou: ele, a sombra e a luz dele.
"""
import bpy, sys, math, os, mathutils

argv = sys.argv[sys.argv.index("--") + 1:]
MODELS, OUT, TEX = argv[0], argv[1], argv[2]
RES = int(argv[3]) if len(argv) > 3 else 1400
os.makedirs(OUT, exist_ok=True)

bpy.ops.wm.read_factory_settings(use_empty=True)
scene = bpy.context.scene
scene.render.engine = 'BLENDER_EEVEE'
scene.render.resolution_x = RES
scene.render.resolution_y = RES
scene.render.film_transparent = True
scene.render.image_settings.file_format = 'PNG'
scene.render.image_settings.color_mode = 'RGBA'
scene.view_settings.view_transform = 'Standard'
scene.view_settings.look = 'None'


def hexcol(h):
    h = h.lstrip('#')
    r, g, b = [int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)]
    f = lambda c: c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    return (f(r), f(g), f(b), 1)


def principled(m):
    return next(n for n in m.node_tree.nodes if n.type == 'BSDF_PRINCIPLED')


def mat(name, col, rough=0.7, metal=0.0, emit=0.0):
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    b = principled(m)
    b.inputs['Base Color'].default_value = hexcol(col)
    b.inputs['Roughness'].default_value = rough
    b.inputs['Metallic'].default_value = metal
    if emit:
        b.inputs['Emission Color'].default_value = hexcol(col)
        b.inputs['Emission Strength'].default_value = emit
    return m


def texmat(name, path, emit=0.0):
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    b = principled(m)
    t = m.node_tree.nodes.new('ShaderNodeTexImage')
    t.image = bpy.data.images.load(path)
    m.node_tree.links.new(t.outputs['Color'], b.inputs['Base Color'])
    b.inputs['Roughness'].default_value = 0.5
    if emit:
        m.node_tree.links.new(t.outputs['Color'], b.inputs['Emission Color'])
        b.inputs['Emission Strength'].default_value = emit
    return m


def box(name, loc, size, m):
    bpy.ops.mesh.primitive_cube_add(size=1, location=loc)
    o = bpy.context.object
    o.name = name
    o.scale = size
    bpy.ops.object.transform_apply(scale=True)
    o.data.materials.append(m)
    return o


def cyl(name, loc, r, h, m, rot=(0, 0, 0)):
    bpy.ops.mesh.primitive_cylinder_add(radius=r, depth=h, location=loc, rotation=rot, vertices=32)
    o = bpy.context.object
    o.name = name
    o.data.materials.append(m)
    return o


def load(name, loc=(0, 0, 0), rotz=0, scale=0.33):
    """Importa um modelo da Kenney com a base no chão e o centro em loc. Devolve todos os objetos."""
    before = set(bpy.data.objects)
    bpy.ops.wm.obj_import(filepath=os.path.join(MODELS, name + '.obj'))
    new = [o for o in bpy.data.objects if o not in before]
    lo = mathutils.Vector((1e9,) * 3)
    hi = -lo
    for o in new:
        for c in o.bound_box:
            w = o.matrix_world @ mathutils.Vector(c)
            lo = mathutils.Vector(map(min, lo, w))
            hi = mathutils.Vector(map(max, hi, w))
    root = bpy.data.objects.new(name + '_root', None)
    scene.collection.objects.link(root)
    c = (lo + hi) / 2
    for o in new:
        o.parent = root
        o.location = o.location - mathutils.Vector((c.x, c.y, lo.z))
    root.location = loc
    root.rotation_euler = (0, 0, math.radians(rotz))
    root.scale = (scale,) * 3
    return new + [root]


hazard = texmat('hazard', f'{TEX}/hazard.png')
screen = texmat('screen', f'{TEX}/screen.png', 1.6)
wall_a = mat('wall', '#39464b', 0.65, 0.2)
wall_b = mat('wall2', '#2f3a3e', 0.65, 0.2)
floor_m = mat('floor', '#20282a', 0.8)
yellow = mat('hazY', '#f5b700', 0.5)
black = mat('hazK', '#1f2628', 0.5)
board = mat('peg', '#c9b48a', 0.8)
red = mat('tool', '#c8432a', 0.45, 0.3)
steel = mat('steel', '#aeb9bc', 0.3, 0.9)
wood = mat('wood2', '#a0743f', 0.6)
glow = mat('glow', '#ffd98a', 0.5, 0.0, 4.0)
cab = mat('cab', '#5e7076', 0.5, 0.4)
tag = mat('tag', '#f2eee3', 0.6)

S, T, H = 6.0, 0.25, 4.0
box('floor', (0, 0, -0.1), (S, S, 0.2), floor_m)
box('wallB', (0, -S / 2 - T / 2, H / 2), (S + T * 2, T, H), wall_a)
box('wallL', (-S / 2 - T / 2, 0, H / 2), (T, S, H), wall_b)
for i in range(1, 4):
    box(f'seam{i}', (-S / 2 + i * S / 4, -S / 2 + 0.01, H / 2), (0.03, 0.03, H), black)
for i in range(1, 3):
    box(f'seamL{i}', (-S / 2 + 0.01, -S / 2 + i * S / 3, H / 2), (0.03, 0.03, H), black)


def strip(name, loc, size, rotz):
    bpy.ops.mesh.primitive_plane_add(size=1, location=loc, rotation=(math.pi / 2, 0, rotz))
    o = bpy.context.object
    o.name = name
    o.scale = size
    o.data.materials.append(hazard)


strip('hb', (0, -S / 2 + 0.02, 0.17), (S, 0.34, 1), 0)
strip('hl', (-S / 2 + 0.02, 0, 0.17), (S, 0.34, 1), -math.pi / 2)
box('curbB', (0, -S / 2 + 0.01, 0.37), (S, 0.03, 0.04), black)
box('curbL', (-S / 2 + 0.01, 0, 0.37), (0.03, S, 0.04), black)

# Pegboard com ferramentas: é o Painel do Cérebro.
box('peg', (0.9, -S / 2 + 0.05, 2.55), (3.0, 0.06, 1.6), board)
for ix in range(13):
    for iz in range(7):
        cyl(f'h{ix}_{iz}', (-0.45 + ix * 0.225, -S / 2 + 0.09, 1.87 + iz * 0.225), 0.022, 0.02, black, rot=(math.pi / 2, 0, 0))
box('wrench', (-0.2, -S / 2 + 0.1, 2.55), (0.1, 0.05, 1.0), red)
cyl('wr1', (-0.2, -S / 2 + 0.1, 3.08), 0.16, 0.05, red, rot=(math.pi / 2, 0, 0))
box('hammer', (0.25, -S / 2 + 0.1, 2.45), (0.1, 0.05, 0.9), wood)
box('hammerh', (0.25, -S / 2 + 0.1, 2.95), (0.46, 0.08, 0.2), steel)
cyl('gear', (1.05, -S / 2 + 0.1, 2.6), 0.46, 0.06, steel, rot=(math.pi / 2, 0, 0))
cyl('gear2', (1.05, -S / 2 + 0.13, 2.6), 0.2, 0.06, black, rot=(math.pi / 2, 0, 0))
for k in range(10):
    a = k * math.pi / 5
    box(f'tooth{k}', (1.05 + math.cos(a) * 0.5, -S / 2 + 0.1, 2.6 + math.sin(a) * 0.5), (0.13, 0.06, 0.13), steel)
box('saw', (1.85, -S / 2 + 0.1, 2.85), (0.8, 0.05, 0.16), steel)
box('sawh', (2.28, -S / 2 + 0.1, 2.85), (0.2, 0.06, 0.24), red)
box('square', (1.8, -S / 2 + 0.1, 2.3), (0.1, 0.05, 0.7), yellow)
box('square2', (2.05, -S / 2 + 0.1, 2.0), (0.6, 0.05, 0.1), yellow)

# Gaveteiro: nove gavetas, uma por região, com a terceira de cima aberta.
cx, cy = -2.45, -1.15
box('cabinet', (cx, cy, 1.45), (0.9, 2.2, 2.9), cab)
for i in range(9):
    z = 0.3 + i * 0.3
    is_open = i == 6
    dx = 0.75 if is_open else 0.0
    box(f'drawer{i}', (cx + 0.47 + dx / 2, cy, z), (0.06 + dx, 2.0, 0.25), yellow if is_open else cab)
    box(f'tag{i}', (cx + 0.51 + dx, cy, z), (0.02, 1.0, 0.14), tag)

# Bancada (começa na sala: abre o Laboratório).
load('desk', (0.55, -2.2, 0))
load('computerScreen', (0.2, -2.3, 1.27))
bpy.ops.mesh.primitive_plane_add(size=1, location=(0.2, -2.285, 1.77), rotation=(math.radians(90), 0, 0))
_s = bpy.context.object
_s.scale = (1.14, 0.78, 1)
_s.data.materials.append(screen)
load('computerKeyboard', (0.2, -1.88, 1.27))

# Plataforma de recarga: é onde o Byte fica.
cyl('pad', (0.1, 0.65, 0.05), 1.15, 0.1, black)
cyl('padring', (0.1, 0.65, 0.11), 1.05, 0.02, yellow)
cyl('padin', (0.1, 0.65, 0.13), 0.9, 0.02, black)
cyl('padglow', (0.1, 0.65, 0.15), 0.72, 0.01, glow)
cyl('padtop', (0.1, 0.65, 0.16), 0.66, 0.01, black)
load('cardboardBoxClosed', (2.55, 2.6, 0), 25)
load('cardboardBoxOpen', (2.6, 1.55, 0), -10)

world = bpy.data.worlds.new('w')
scene.world = world
world.use_nodes = True
bg = next(n for n in world.node_tree.nodes if n.type == 'BACKGROUND')
bg.inputs[0].default_value = (0.55, 0.6, 0.65, 1)
bg.inputs[1].default_value = 0.55
sun = bpy.data.lights.new('sun', 'SUN')
sun.energy = 2.6
sun.angle = math.radians(14)
sun.color = (1, .95, .86)
so = bpy.data.objects.new('sun', sun)
scene.collection.objects.link(so)
so.rotation_euler = (math.radians(52), math.radians(8), math.radians(38))
pt = bpy.data.lights.new('warm', 'POINT')
pt.energy = 260
pt.color = (1, .78, .45)
pt.shadow_soft_size = 1.2
po = bpy.data.objects.new('warm', pt)
scene.collection.objects.link(po)
po.location = (0.8, -0.9, 3.0)


def lamp(loc, energy=26):
    li = bpy.data.lights.new('lamp', 'POINT')
    li.energy = energy
    li.color = (1, .8, .5)
    li.shadow_soft_size = 0.3
    o = bpy.data.objects.new('lamp', li)
    scene.collection.objects.link(o)
    o.location = loc
    return [o]


def poster():
    frame = box('frame', (-S / 2 + 0.03, 1.7, 2.45), (0.06, 1.36, 1.66), black)
    bpy.ops.mesh.primitive_plane_add(size=1, location=(-S / 2 + 0.07, 1.7, 2.45), rotation=(math.pi / 2, 0, math.pi / 2))
    o = bpy.context.object
    o.scale = (1.26, 1.56, 1)
    o.data.materials.append(texmat('poster', f'{TEX}/poster.png', 0.9))
    return [frame, o]


# Móveis comprados com engrenagens: (nome, função que cria).
ITEMS = [
    ('cadeira', lambda: load('chairDesk', (0.45, -1.2, 0), 180)),
    ('abajur', lambda: load('lampRoundTable', (1.5, -2.38, 1.27)) + lamp((1.5, -2.2, 2.2), 18)),
    ('estante', lambda: load('bookcaseOpenLow', (2.4, -2.62, 0)) + load('books', (2.4, -2.66, 0.72)) + load('books', (2.25, -2.66, 0.12))),
    ('radio', lambda: load('radio', (2.35, -2.62, 1.34))),
    ('luminaria', lambda: load('lampRoundFloor', (-2.55, 0.95, 0)) + lamp((-2.3, 1.1, 2.5), 34)),
    ('sofa', lambda: load('loungeSofa', (-2.42, 2.15, 0), -90, 0.25)),
    ('planta', lambda: load('pottedPlant', (-2.6, 0.32, 0), 40, 0.26)),
    ('cafe', lambda: load('sideTableDrawers', (2.7, -0.75, 0), -90) + load('kitchenCoffeeMachine', (2.7, -0.75, 0.84))),
    ('lixeira', lambda: load('trashcan', (-1.15, -2.62, 0))),
    ('ursinho', lambda: load('bear', (1.8, 0.1, 0), -40, 0.2)),
    ('caixasom', lambda: load('speakerSmall', (-0.32, -2.42, 1.27), 20, 0.24)),
    ('quadro', lambda: poster()),
]
groups = []
for name, make in ITEMS:
    objs = make()
    for o in objs:
        o.hide_render = True
    groups.append((name, objs))

cam = bpy.data.cameras.new('cam')
cam.type = 'ORTHO'
cam.ortho_scale = 9.6
co = bpy.data.objects.new('cam', cam)
scene.collection.objects.link(co)
scene.camera = co
co.rotation_euler = (math.radians(58), 0, math.radians(135))
co.location = mathutils.Vector((-0.2, -0.2, 1.4)) + co.rotation_euler.to_matrix() @ mathutils.Vector((0, 0, 22))


def shot(name):
    scene.render.filepath = os.path.join(OUT, name + '.png')
    bpy.ops.render.render(write_still=True)
    print('RENDERED', name)


shot('base')
for name, objs in groups:
    for o in objs:
        o.hide_render = False
    shot('with_' + name)
    for o in objs:
        o.hide_render = True
# tudo junto, para conferir a composição
for _, objs in groups:
    for o in objs:
        o.hide_render = False
shot('all')
