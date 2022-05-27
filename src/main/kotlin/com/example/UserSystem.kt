package com.example

import UsersDB
import com.example.include.Id
import com.example.include.User

object UserSystem {
    fun authById(id : Id, secretKey: String) : Boolean {
        return UsersDB().getUserById(id)?.secretKey == secretKey
    }
}