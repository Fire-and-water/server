package com.example

import com.example.include.Id
import java.io.PrintWriter


object GameSystem {

    private var gamesByKey : MutableMap<Int, Game> = mutableMapOf()
    private var idGameByPlayerId : MutableMap<Int, Int> = mutableMapOf()

    fun createGame(idPlayer : Id, level : Int, roleCreator: ROLE, output : PrintWriter) : Int {
        val game = Game(idPlayer, level, roleCreator, output)
        gamesByKey[game.gameId] = game
        idGameByPlayerId[idPlayer] = game.gameId
        return game.gameId
    }

    fun getCurrentGameId(idPlayer: Id) : Id? {
        return idGameByPlayerId[idPlayer]
    }

    fun getGameById(idGame : Id) : Game? {
        return gamesByKey[idGame];
    }

    fun removeGame(idGame : Id) {
        val game: Game = gamesByKey[idGame] ?: return
        val id1: Id = game.firePlayer?.id ?: -1
        val id2: Id = game.waterPlayer?.id ?: -1
        gamesByKey.remove(idGame)
        idGameByPlayerId.remove(id1)
        idGameByPlayerId.remove(id2)
    }

}