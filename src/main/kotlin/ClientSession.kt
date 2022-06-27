import com.example.*
import com.example.include.Id
import com.example.include.StatusWithMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket
import kotlinx.coroutines.*
import javax.management.relation.Role


class ClientSession(private var socket: Socket) {
    private var output : PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private var input : BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private var clientId : Int = -1

    private fun endLine(line : String?): Boolean {
        return line == null || line == "" || line == "."
    }

    private fun badCommand(status : Int = 0, msg : String = "Bad message!") {
        output.println(Json.encodeToString(StatusWithMessage(status, msg)))
    }

    private fun handler(line: String) {
        val commands: List<String> = line.split(' ')
        when (commands[0]) {
            "auth" -> {
                if(commands.size != 3) {
                    badCommand(msg = "use : auth <id> <secretKey>")
                    return
                } // корректность команды
                val clientIdGot = commands[1].toInt()
                val secretKeyGot = commands[2]
                if(UserSystem.authById(clientIdGot, secretKeyGot)) {
                    clientId = clientIdGot
                    val idGame: Id = GameSystem.getCurrentGameId(clientId) ?: 0
                    //@Serializable data class
                    output.println("{ \"status\": 1, \"player-id\": $clientId, \"current-game-id\": $idGame}")
                    if(idGame!=0) {
                        GameSystem.removeGame(idGame);
                    }
                } else {
                    badCommand(0, "id or secretKey is incorrect")
                }
            }

            "create-game" -> {
                if(commands.size != 3) {
                    badCommand(msg = "use : create-game <level> <ROLE: FIRE/WATER>")
                    return
                } // корректность команды
                if(clientId == -1) {
                    badCommand(0, msg = "user is not authorized!")
                    return
                } // проверка авторизации

                val level = commands[1].toInt()
                val role : ROLE = try {
                    ROLE.valueOf(commands[2])
                } catch (ex : Exception) {
                    badCommand(msg = "<ROLE: FIRE/WATER>, not "+commands[2])
                    return
                }

                println("Client $clientId wants to make a game: level $level!")

                if(GameSystem.getCurrentGameId(clientId)!=null) {
                    badCommand(7, "Пользователь уже во что-то играет")
                    return
                }

                val idGame: Id = GameSystem.createGame(clientId, level, role, output) // TODO: валидация уровня

                println("Client $clientId made a game: level $level , idGame: $idGame")

                output.println("{ \"status\": 1, \"game-id\": $idGame }")
                gameStream(idGame, role)


            }

            "connect-to-game" -> {
                val idGame = commands[1].toInt()

                if(GameSystem.getCurrentGameId(clientId)!=null) {
                    badCommand(7, "Пользователь уже во что-то играет")
                    return
                }

                val game : Game? = GameSystem.getGameById(idGame)
                if(game == null) {
                    badCommand(2, "Нету игры с таким идентификатором")
                    return
                }
                GameSystem.idGameByPlayerId[clientId] = game.idGame;
                val r = game.connectSecondPlayer(clientId, output)
                output.println("{ \"status\": 1, \"game-id\": ${game.idGame} }")
                gameStream(idGame, r)
                // gameSstream(thisGame, input, output)

            }

            "reconnect" -> {

                val idGame = GameSystem.getCurrentGameId(clientId)
                if(idGame == null) {
                    badCommand(9, "Нет текущей игры")
                    return
                }

                val game = GameSystem.getGameById(idGame) ?: return

                game.reConnect(clientId, output)
                gameStream(idGame, game.whoAmI(clientId))
            }

            else -> { // Note the block
                badCommand()
            }
        }
    }

    private fun gameStream(idGame : Id, role: ROLE) {
        val game : Game = GameSystem.getGameById(idGame) ?: return
        game.sendGameStatus()
        while(true) {
            val lineInput = input.readLine()
            if(endLine(lineInput)) {
                socket.close()
                break
            }
            val commands : List<String> = lineInput.split(' ')
            if(commands[0] == "cancel-game") {
                game.cancel()
             //   GameSystem.removeGame(game.idGame)
                return
            }
            gameCommandsHandler(commands, game, role)
        }

    }

    private fun gameCommandsHandler(commands : List<String>, game: Game, role: ROLE) {

        if(!game.gameStatus.isTwoConnected || game.gameStatus.isOver){
            return
        }
        when(commands[0]) {

            "send-move" -> {
                game.mainGame.step(commands[1], role)
                if(false && (game.lastSendTime + 5000 < System.currentTimeMillis())){
                    game.sendFullGame()

                } else {
                    game.lastSendTime = System.currentTimeMillis()
                    if(role == ROLE.WATER) {
                        game.wat = CoorP(commands[1].toFloat(), commands[2].toFloat())
                    } else {
                        game.fir = CoorP(commands[1].toFloat(), commands[2].toFloat())
                    }
                    game.sendPlayersPositions();
                }
            }
            "game-end" -> {
                game.isWin = true;
                game.gameStatus.isOver = true;
            }
            else -> {
                println("ok")
            }
        }
    }

    fun start() {
        while(true) {
            val lineInput = input.readLine()
            if(endLine(lineInput)) {
                socket.close()
                break
            }
            handler(lineInput)
        }
    }

}
