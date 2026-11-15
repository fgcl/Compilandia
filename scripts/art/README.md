# Arte da oficina

As camadas da sala (`room_item_*` e a silhueta amarela `room_ghost_*`) vieram de renders do
[Furniture Pack da Kenney](https://opengameart.org/content/furniture-kit) (CC0) e já estão em
`composeApp/src/commonMain/composeResources/drawable`.

O render saía do Blender, que não é mais usado no projeto (pedido do dono, 15/11/2026). A sala desenhada por código está
nas tarefas (`docs/TAREFAS.md`, seção 8). Até lá, móvel novo não entra na sala.

`layers.py` continua: ele recebe um render da sala vazia e um por móvel, e o que muda entre os dois (móvel, sombra e luz)
vira a camada.

```bash
python3 scripts/art/layers.py <renders> composeApp/src/commonMain/composeResources/drawable \
  composeApp/src/commonMain/kotlin/com/compilandia/ui/room/RoomLayers.kt 1100
```
