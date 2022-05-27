import com.example.Game
import com.example.games
import com.example.include.StatusWithMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.Socket

fun gameStream(game : Game, inputss: BufferedReader, outputss : PrintWriter, who : Int = 0) {
    while(game.player2 == -1) {
        outputss.println(Json.encodeToString(StatusWithMessage(2, "waiting...")))
        sleep(2347)
    }
    while(true) {
        val commands = inputss.readLine().split(' ')
        when(commands[0]) {
            "step" -> {

            }
            "end" -> {
                break
            }
        }
    }
    outputss.println(Json.encodeToString(StatusWithMessage(3, "The end of the game!")))
    games.remove(game.gameId);
    return;
}

class ClientSession(private var socket: Socket) {
    private var output : PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private var input : BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private var clientId : Int = -1;
    private lateinit var game : Game;

    private fun endLine(line : String): Boolean {
        return line == "" || line == "."
    }

    private fun handler(line: String) {
        val commands: List<String> = line.split(' ')
        when (commands[0]) {
            "auth" -> {
                clientId = commands[1].toInt()
                output.println("{ \"status\": 1, \"player-id\": $clientId }");
            }

            "create-game" -> {
                val level = commands[1].toInt();
                println("Client $clientId wants to make a game: level $level!");
                game = Game(clientId, level);
                games[game.gameId] = game;
                println("Client $clientId made a game: level $level , idGame: ${game.gameId}");
                output.println("{ \"status\": 1, \"game-id\": ${game.gameId} }");
                gameStream(game, input, output);
            }

            "connect-to-game" -> {
                val gameId = commands[1].toInt();
                if (!games.containsKey(gameId)) {
                    output.println(Json.encodeToString(StatusWithMessage(2, "Нету игры с таким идентификатором")))
                }
                val thisGame = games[gameId];
                thisGame?.connect(clientId) ?: return
                output.println("{ \"status\": 1, \"game-id\": ${thisGame.gameId} }");
                gameStream(thisGame, input, output)

            }

            else -> { // Note the block
                output.println(Json.encodeToString(StatusWithMessage(0, "Bad message!")))
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
