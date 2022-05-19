package com.example.include

@kotlinx.serialization.Serializable
data class Level(val id : Id, val level: Int, val shortDescription: String, val pictureLink: String, val difficulty: Int, val resultWater: Int, val resultFire: Int)