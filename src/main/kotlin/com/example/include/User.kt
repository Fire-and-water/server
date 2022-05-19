package com.example.include


typealias Id = Int
typealias Password = String

@kotlinx.serialization.Serializable
class User(var id : Int, var secretKey : Password, var nickName : String, private var photo : String, var status : String, var eMail : String, var password: String) {

    // private var isAuth  : Boolean = false;
    fun auth(secretKeyAttempt : Password) : Boolean {
        return secretKeyAttempt == secretKey
    }

}