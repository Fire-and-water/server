package com.example

import java.io.BufferedReader
import java.io.InputStream
import java.io.PrintWriter

enum class ROLE {
    FIRE, WATER
}

class Game(idCreator: Int, var level : Int, var role1 : ROLE) {
    var gameId : Int = 0;
    var player1 : Int = idCreator;
    lateinit var p1i : BufferedReader;
    lateinit var p1o : PrintWriter;
    lateinit var p2i : BufferedReader;
    lateinit var p2o : PrintWriter;
    var player2 : Int = -1;
    init {
        gameId = (100_000..999_999).random(); // TODO: хардкод((
    }
    fun connect(player2 : Int) {
        this.player2 = player2;
    }

    fun go(inputss: BufferedReader, outputss : PrintWriter, oneortwo: Int,  who : Int = 0) {
        val myPlayerStream = TODO();
        val otherPlayerStream = TODO();

        if(oneortwo == 1) {
            p1i = inputss;
            p1o = outputss;
        } else {
            p2i = inputss;
            p2o = outputss;
        }

    }
}