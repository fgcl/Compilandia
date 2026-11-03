package com.compilandia.ui.room

import com.compilandia.resources.Res
import com.compilandia.resources.room_ghost_abajur
import com.compilandia.resources.room_ghost_cadeira
import com.compilandia.resources.room_ghost_cafe
import com.compilandia.resources.room_ghost_caixasom
import com.compilandia.resources.room_ghost_estante
import com.compilandia.resources.room_ghost_lixeira
import com.compilandia.resources.room_ghost_luminaria
import com.compilandia.resources.room_ghost_planta
import com.compilandia.resources.room_ghost_quadro
import com.compilandia.resources.room_ghost_radio
import com.compilandia.resources.room_ghost_sofa
import com.compilandia.resources.room_ghost_ursinho
import com.compilandia.resources.room_item_abajur
import com.compilandia.resources.room_item_cadeira
import com.compilandia.resources.room_item_cafe
import com.compilandia.resources.room_item_caixasom
import com.compilandia.resources.room_item_estante
import com.compilandia.resources.room_item_lixeira
import com.compilandia.resources.room_item_luminaria
import com.compilandia.resources.room_item_planta
import com.compilandia.resources.room_item_quadro
import com.compilandia.resources.room_item_radio
import com.compilandia.resources.room_item_sofa
import com.compilandia.resources.room_item_ursinho
import org.jetbrains.compose.resources.DrawableResource

/** Gerado por scripts/art/layers.py. Posição e tamanho como fração da largura da sala. */
data class RoomLayer(val art: DrawableResource, val ghost: DrawableResource, val x: Float, val y: Float, val w: Float, val h: Float)

/** Na ordem de pintura: de trás para a frente. */
val roomLayers: Map<String, RoomLayer> = linkedMapOf(
    "quadro" to RoomLayer(Res.drawable.room_item_quadro, Res.drawable.room_ghost_quadro, 0.7893f, 0.2693f, 0.1127f, 0.2082f),
    "planta" to RoomLayer(Res.drawable.room_item_planta, Res.drawable.room_ghost_planta, 0.6786f, 0.3814f, 0.0982f, 0.1836f),
    "luminaria" to RoomLayer(Res.drawable.room_item_luminaria, Res.drawable.room_ghost_luminaria, 0.4129f, 0.2336f, 0.5300f, 0.4336f),
    "lixeira" to RoomLayer(Res.drawable.room_item_lixeira, Res.drawable.room_ghost_lixeira, 0.3600f, 0.3493f, 0.0964f, 0.1564f),
    "caixasom" to RoomLayer(Res.drawable.room_item_caixasom, Res.drawable.room_ghost_caixasom, 0.3371f, 0.3450f, 0.0364f, 0.0909f),
    "cadeira" to RoomLayer(Res.drawable.room_item_cadeira, Res.drawable.room_ghost_cadeira, 0.2736f, 0.4293f, 0.3918f, 0.2400f),
    "abajur" to RoomLayer(Res.drawable.room_item_abajur, Res.drawable.room_ghost_abajur, 0.1114f, 0.3821f, 0.1364f, 0.1364f),
    "estante" to RoomLayer(Res.drawable.room_item_estante, Res.drawable.room_ghost_estante, 0.0379f, 0.4679f, 0.2536f, 0.2100f),
    "radio" to RoomLayer(Res.drawable.room_item_radio, Res.drawable.room_ghost_radio, 0.0379f, 0.4171f, 0.2200f, 0.2045f),
    "sofa" to RoomLayer(Res.drawable.room_item_sofa, Res.drawable.room_ghost_sofa, 0.7057f, 0.4629f, 0.2655f, 0.2382f),
    "cafe" to RoomLayer(Res.drawable.room_item_cafe, Res.drawable.room_ghost_cafe, 0.1486f, 0.5493f, 0.3527f, 0.2264f),
    "ursinho" to RoomLayer(Res.drawable.room_item_ursinho, Res.drawable.room_ghost_ursinho, 0.3171f, 0.6143f, 0.1591f, 0.1318f),
)
