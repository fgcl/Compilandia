# Arte da oficina

A sala é renderizada no Blender a partir do [Furniture Pack da Kenney](https://opengameart.org/content/furniture-kit) (CC0).

```bash
flatpak run --filesystem=/tmp org.blender.Blender -b --python scripts/art/room.py -- <Models> <saida> <texturas> 1400
python3 scripts/art/layers.py <saida> composeApp/src/commonMain/composeResources/drawable \
  composeApp/src/commonMain/kotlin/com/compilandia/ui/room/RoomLayers.kt 1100
```

`room.py` faz um render da sala vazia e um por móvel. `layers.py` compara cada par: o que mudou (móvel, sombra e luz)
vira a camada `room_item_*`. A silhueta amarela `room_ghost_*` mostra o que ainda dá para comprar.

Ao adicionar um móvel: entre em `ITEMS` (room.py), em `ORDER` (layers.py) e em `RoomItem` (Skills.kt).
