package com.example

import com.example.include.Id
import com.sun.xml.internal.bind.v2.TODO
import java.io.BufferedReader
import java.io.InputStream
import java.io.PrintWriter

enum class ROLE {
    FIRE, WATER
}

data class Player(val id : Id, val role : ROLE, val output: PrintWriter)

class Game(idCreator: Int, private var level : Int, roleCreator : ROLE, var outputCreator : PrintWriter) {
    var gameId : Int = 0;
    var waterPlayer : Player? = null
     var firePlayer : Player? = null
    // var game = TODO();

    init {
        gameId = (100_000..999_999).random(); // TODO: хардкод((

        // создаем первого игрока (того, кем захотел быть создатель)
        val player = Player(idCreator, roleCreator, outputCreator)
        if(roleCreator == ROLE.FIRE) {
            firePlayer = player
        } else {
            waterPlayer = player
        }
    }
    fun connectSecondPlayer(idPlayer: Int, outputPlayer : PrintWriter) {
        // роль второго игрока определяется остаточным принципом
        if(firePlayer == null){
            waterPlayer = Player(idPlayer, ROLE.WATER, outputPlayer)
        } else {
            firePlayer = Player(idPlayer, ROLE.WATER, outputPlayer)
        }
    }

    fun connect(idPlayer: Int, outputPlayer: PrintWriter) {
        if(firePlayer != null && firePlayer!!.id == idPlayer) {
            firePlayer = Player(idPlayer, ROLE.FIRE, outputPlayer)
            return
        }
        if(waterPlayer != null && waterPlayer!!.id == idPlayer) {
            waterPlayer = Player(idPlayer, ROLE.WATER, outputPlayer)
        }
    }

    fun sendPlayersPositions() {

    }

    fun sendGameStatus() {

    }

    fun setOutput(who : ROLE, output: PrintWriter) {

    }

    /*fun go(inputss: BufferedReader, outputss : PrintWriter, oneortwo: Int,  who : Int = 0) {
        val myPlayerStream = TODO();
        val otherPlayerStream = TODO();

        if(oneortwo == 1) {
            p1i = inputss;
            p1o = outputss;
        } else {
            p2i = inputss;
            p2o = outputss;
        }

    }*/
}