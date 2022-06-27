package com.example

import UsersDB
import com.example.game.MainGame
import com.example.include.Id

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.PrintWriter
import java.util.Collections.max
import kotlin.math.max

enum class ROLE {
    FIRE, WATER
}

data class Player(
    val id: Id,
    val role: ROLE,
    val output: PrintWriter
)

data class CoorP(var posX: Float, var posY: Float);
@Serializable
data class GameStatus(
    var isOver: Boolean,
    var isTwoConnected: Boolean,
    var what: String
)

class Game(
    var idGame: Id,
    idCreator: Int,
    private var level: Int,
    roleCreator: ROLE,
    outputCreator: PrintWriter
) {
    var waterPlayer: Player? = null
    var firePlayer: Player? = null
    var lastSendTime: Long = System.currentTimeMillis();
    var gameStatus: GameStatus = GameStatus(
        isOver = false,
        isTwoConnected = false,
        what = "game-status"
    )
    var mainGame: MainGame = MainGame(level);
    var wat: CoorP = CoorP(0.0f, 0.0f);
    var fir: CoorP = CoorP(0.0f, 0.0f);
    var isWin: Boolean = false;
    var timeStart: Long = System.currentTimeMillis();

    init {

        // создаем первого игрока (того, кем захотел быть создатель)
        val player =
            Player(idCreator, roleCreator, outputCreator)
        if (roleCreator == ROLE.FIRE) {
            firePlayer = player
        } else {
            waterPlayer = player
        }
    }

    fun connectSecondPlayer(
        idPlayer: Int,
        outputPlayer: PrintWriter
    ): ROLE {
        // роль второго игрока определяется остаточным принципом
        gameStatus.isTwoConnected = true;
        timeStart = System.currentTimeMillis();
        return if (firePlayer == null) {
            firePlayer =
                Player(idPlayer, ROLE.FIRE, outputPlayer)
            ROLE.FIRE
        } else {
            waterPlayer =
                Player(idPlayer, ROLE.WATER, outputPlayer)
            ROLE.WATER
        }
    }

    fun whoAmI(id: Id): ROLE {
        return if (id == waterPlayer?.id) ROLE.WATER;
        else ROLE.FIRE;
    }

    fun reConnect(
        idPlayer: Int,
        outputPlayer: PrintWriter
    ) { // Переподключение. Вдруг вылетело
        if (firePlayer != null && firePlayer!!.id == idPlayer) {
            firePlayer =
                Player(idPlayer, ROLE.FIRE, outputPlayer)
            return
        }
        if (waterPlayer != null && waterPlayer!!.id == idPlayer) {
            waterPlayer =
                Player(idPlayer, ROLE.WATER, outputPlayer)
        }
    }

    fun isCancel(): Boolean {
        return gameStatus.isOver;
    }


    private fun sendAll(str: String) {
        waterPlayer?.output?.println(str)
        firePlayer?.output?.println(str)
    }

    fun sendPlayersPositions() {

        @Serializable
        data class Point(val posX: Float, val posY: Float);

        @Serializable
        data class Coordinates(
            val water: Point,
            val fire: Point,
            val what: String
        );

        sendAll(
            Json.encodeToString(
                Coordinates(
                    Point(
                        wat.posX,
                        wat.posY
                    ),
                    Point(fir.posX, fir.posY),
                    "coordinates"
                )
            )
        )
        //sendAll(game.getCoordinates).
    }

    fun sendFullGame() {
        sendAll(mainGame.serialize())
    }

    fun saveResultsToDB() {
        val time: Long =
            System.currentTimeMillis() - timeStart
        UsersDB().saveGameResults(
            idGame,
            firePlayer!!.id,
            waterPlayer!!.id,
            max(0, 10 - time / 10000),
            time,
            mainGame.level
        );
    }

    fun sendGameStatus() {
        sendAll(Json.encodeToString(gameStatus))
    }

    fun cancel() {
        gameStatus.isOver = true
        sendGameStatus()
    }

}