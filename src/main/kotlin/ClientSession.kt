import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientSession<DataStorage>(private var socket: Socket, private var ds: DataStorage) {
    private var output : PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private var input : BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private fun endLine(line : String): Boolean {
        return line == "" || line == "."
    }

    private fun handler(line: String) {

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
