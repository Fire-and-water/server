package com.example.include

@kotlinx.serialization.Serializable
data class UserPlace(val id: Int, val nickname : String, val place : Int, val rating : Int)