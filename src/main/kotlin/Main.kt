import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

const val SERVER_PORT = 1234

fun main() {

    println("Hello  World!")
    val socketListener = ServerSocket(SERVER_PORT)
    var clientNumber = 0

    socketListener.use {
        while (true) {
            val socket = socketListener.accept()
            thread(start = true) {
                clientNumber++
                val thisClient = clientNumber
                val output = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output.println("Echo Server. You are client #$thisClient.")
                output.println("End this conversation by sending a single '.' on a new line.")
                while (true) {
                    val line = input.readLine()
                    if (line == "." || line == null) {
                        println("Client #$thisClient closed connection.")
                        output.println("___!")
                        socket.close()
                        return@thread
                    }
                    println("Client #$thisClient said: $line")
                    output.println("Fire & Water: $line")
                }
            }
        }
    }

}