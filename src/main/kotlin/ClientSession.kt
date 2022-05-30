import com.example.*
import com.example.include.Id
import com.example.include.StatusWithMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket

/*
fun gameSstream(game : Game, inputss: BufferedReader, outputss : PrintWriter, who : Int = 0) {
    while(game.player2 == -1) {
        outputss.println(Json.encodeToString(StatusWithMessage(2, "waiting...")))
        sleep(2347)
    }

    while(true) {
        val commands = inputss.readLine().split(' ')
        when(commands[0]) {
            "step" -> {

            }
            "сancel-game" -> {

            }
            "end" -> {
                break
            }
        }
    }
    outputss.println(Json.encodeToString(StatusWithMessage(1, "Two players connected.")))

    games.remove(game.gameId);
    return;
}*/

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
                    val gameId: Id = GameSystem.getCurrentGameId(clientId) ?: 0
                    output.println("{ \"status\": 1, \"player-id\": $clientId, \"current-game-id\": $gameId}")
                } else {
                    badCommand(0, "id or secretKey is incorrect")
                }
            }

            "create-game" -> {
                if(commands.size != 3) {
                    badCommand(msg = "use : create-game <level> <ROLE: fire/water>")
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
                    badCommand(msg = "<ROLE: fire/water>, not "+commands[2])
                    return
                }

                println("Client $clientId wants to make a game: level $level!")

                if(GameSystem.getCurrentGameId(clientId)!=null) {
                    badCommand(7, "Пользователь уже во что-то играет")
                    return
                }

                val gameId: Id = GameSystem.createGame(clientId, level, role, output) // TODO: валидация уровня

                println("Client $clientId made a game: level $level , idGame: $gameId")

                output.println("{ \"status\": 1, \"game-id\": $gameId }")
                gameStream(gameId)


            }

            "connect-to-game" -> {
                val gameId = commands[1].toInt()

                if(GameSystem.getCurrentGameId(clientId)!=null) {
                    badCommand(7, "Пользователь уже во что-то играет")
                    return
                }

                val game : Game? = GameSystem.getGameById(gameId)
                if(game == null) {
                    badCommand(2, "Нету игры с таким идентификатором")
                    return
                }

                game.connectSecondPlayer(clientId, output)
                output.println("{ \"status\": 1, \"game-id\": ${game.gameId} }")
                gameStream(gameId)
                // gameSstream(thisGame, input, output)

            }

            else -> { // Note the block
                badCommand()
            }
        }
    }

    private fun gameStream(gameId : Id) {
        val game : Game = GameSystem.getGameById(gameId)!!
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
                GameSystem.removeGame(game.gameId);
                return
            }
            gameCommandsHandler(commands, game)
        }

    }

    private fun gameCommandsHandler(commands : List<String>, game: Game) {
        when(commands[0]) {
            "step" -> {

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
