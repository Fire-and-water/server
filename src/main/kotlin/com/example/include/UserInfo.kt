package com.example.include

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(val id : Id, val nickName: String, val email : String, val status : String)