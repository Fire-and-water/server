package com.example

import com.example.include.Id

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.PrintWriter

enum class ROLE {
    FIRE, WATER
}

data class Player(val id : Id, val role : ROLE, val output: PrintWriter)
@Serializable
data class GameStatus(var isOver : Boolean)

class Game(var idGame : Id, idCreator: Int, private var level: Int, roleCreator: ROLE, outputCreator: PrintWriter) {
    var waterPlayer : Player? = null
    var firePlayer : Player? = null
    private var gameStatus : GameStatus = GameStatus(false)
    // var game = TODO();

    init {

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

    fun reConnect(idPlayer: Int, outputPlayer: PrintWriter) { // Переподключение. Вдруг вылетело
        if(firePlayer != null && firePlayer!!.id == idPlayer) {
            firePlayer = Player(idPlayer, ROLE.FIRE, outputPlayer)
            sendGameStatus()
            return
        }
        if(waterPlayer != null && waterPlayer!!.id == idPlayer) {
            waterPlayer = Player(idPlayer, ROLE.WATER, outputPlayer)
            sendGameStatus()
        }
    }

    private fun sendPlayersPositions() {
        TODO("Берём из игры позиции игроков и отсылаем")
    }

    fun getStatusGame() {
        TODO("Отослать весь json с игрой")
    }

    private fun saveResultsToDB() {
        TODO("Сохранить в базу результаты игры. Выполняется в конце")
    }

    fun sendGameStatus() {
        waterPlayer?.output?.println(Json.encodeToString(gameStatus))
        firePlayer?.output?.println(Json.encodeToString(gameStatus))
    }

    fun cancel() {
        gameStatus.isOver = true
        sendGameStatus()
    }

}