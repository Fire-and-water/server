package com.example.include

@kotlinx.serialization.Serializable
data class StatusWithMessage(val status: Int, val msg : String)