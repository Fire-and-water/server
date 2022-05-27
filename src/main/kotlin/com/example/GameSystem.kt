package com.example

import com.example.include.Id

object GameSystem {

    private var gamesByKey : MutableMap<Int, Game> = mutableMapOf<Int, Game>()
    private var idGameByPlayerId : MutableMap<Int, Int> = mutableMapOf<Int, Int>()

    fun createGame(idPlayer : Id, level : Int, roleCreator: ROLE) : Int {
        val game = Game(idPlayer, level, roleCreator);
        gamesByKey[game.gameId] = game;
        idGameByPlayerId[idPlayer] = game.gameId;
        return game.gameId;
    }

    fun getCurrentGame(idPlayer: Id) : Int? {
        return idGameByPlayerId[idPlayer];
    }

}