package com.example.include

@kotlinx.serialization.Serializable
data class GameHistory(val id_water: Id, val id_fire: Id, val nickOtherPlayer: String, val id_level : Int, val level: Int, val stars: Int, val timeStart: Int, val time: Int)