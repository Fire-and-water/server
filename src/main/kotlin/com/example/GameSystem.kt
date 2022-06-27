package com.example

import com.example.include.Id
import java.io.PrintWriter
import java.util.Collections


object GameSystem {

    private var gamesByKey: MutableMap<Int, Game> =
        mutableMapOf()
    var idGameByPlayerId: MutableMap<Int, Int> =
        mutableMapOf()

    fun createGame(
        idPlayer: Id,
        level: Int,
        roleCreator: ROLE,
        output: PrintWriter
    ): Int {
        var idGame: Id = 0;
        while (idGame == 0 || gamesByKey.containsKey(idGame)) {
            idGame = (100_000..999_999).random();
        }

        val game = Game(
            idGame,
            idPlayer,
            level,
            roleCreator,
            output
        )
        gamesByKey[game.idGame] = game
        idGameByPlayerId[idPlayer] = game.idGame
        return game.idGame
    }

    fun getCurrentGameId(idPlayer: Id): Id? { // Получить текущую игру пользователя. Если её нет -- null
        return idGameByPlayerId[idPlayer]
    }

    fun getGameById(idGame: Id): Game? { // игра по ключу
        return gamesByKey[idGame];
    }

    fun removeGame(idGame: Id) { // удалить игру
        val game: Game = gamesByKey[idGame] ?: return
        val id1: Id = game.firePlayer?.id ?: -1
        val id2: Id = game.waterPlayer?.id ?: -1
        gamesByKey.remove(idGame)
        idGameByPlayerId.remove(id1)
        idGameByPlayerId.remove(id2)
    }

    fun sendGames() {
        gamesByKey.forEach { (key, game) ->
            run {
                if (game.isWin and game.gameStatus.isOver) {
                    //  TODO("send status");
                    game.saveResultsToDB()
                    removeGame(key);
                }
            }
        }
    }

}