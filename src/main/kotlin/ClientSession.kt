import com.example.*
import com.example.include.Id
import com.example.include.StatusWithMessage
import jdk.jfr.internal.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.lang.Thread.sleep
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

    private var clientId : Int = -1;

    private fun endLine(line : String?): Boolean {
        return line == null || line == "" || line == "."
    }

    private fun badCommand(status : Int = 0, msg : String = "Bad message!") {
        output.println(Json.encodeToString(StatusWithMessage(0, msg)))
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
                    output.println("{ \"status\": 1, \"player-id\": $clientId }");
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

                val level = commands[1].toInt();
                val role : ROLE = try {
                    ROLE.valueOf(commands[2])
                } catch (ex : Exception) {
                    badCommand(msg = "<ROLE: fire/water>, not "+commands[2])
                    return;
                }

                println("Client $clientId wants to make a game: level $level!");


                val gameId : Id = GameSystem.createGame(clientId, level, role); // TODO: валидация уровня

                println("Client $clientId made a game: level $level , idGame: $gameId");
                output.println("{ \"status\": 1, \"game-id\": $gameId }");
                gameStream(gameId);

            }



            "connect-to-game" -> {
                val gameId = commands[1].toInt();

                if (!games.containsKey(gameId)) {
                    output.println(Json.encodeToString(StatusWithMessage(2, "Нету игры с таким идентификатором")))
                }
                val thisGame = games[gameId];
                thisGame?.connect(clientId) ?: return
                output.println("{ \"status\": 1, \"game-id\": ${thisGame.gameId} }");
                gameSstream(thisGame, input, output)

            }

            else -> { // Note the block
                badCommand()
            }
        }
    }

    private fun gameStream(gameId : Id) {
        val game : Game = GameSystem.getGameById(gameId)!!
        game.sendGameStatus()

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
