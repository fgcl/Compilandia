# Arte da oficina

As camadas da sala (`room_item_*` e a silhueta amarela `room_ghost_*`) vieram de renders do
[Furniture Pack da Kenney](https://opengameart.org/content/furniture-kit) (CC0) e já estão em
`composeApp/src/commonMain/composeResources/drawable`.

O render saía do Blender, que não é mais usado no projeto (pedido do dono, 15/11/2026). Desde 17/11/2026 a sala é
desenhada por código, em `composeApp/src/commonMain/kotlin/com/compilandia/ui/room/art`, e essas imagens não são mais
usadas. Móvel novo entra em `MoveisArte.kt`: um desenho feito de caixas, discos e cilindros, um centro e um tamanho.

`layers.py` continua: ele recebe um render da sala vazia e um por móvel, e o que muda entre os dois (móvel, sombra e luz)
vira a camada.

```bash
python3 scripts/art/layers.py <renders> composeApp/src/commonMain/composeResources/drawable \
  composeApp/src/commonMain/kotlin/com/compilandia/ui/room/RoomLayers.kt 1100
```
