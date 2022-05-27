package com.example

import ClientSession
import UsersDB
import java.net.ServerSocket
import kotlin.concurrent.thread

var games = mutableMapOf<Int, Game>();
const val SERVER_PORT = 1234
fun main() {
    val socketListener = ServerSocket(SERVER_PORT);
    socketListener.use {
        while (true) {
            val socket = socketListener.accept()
            thread(start = true) {
                ClientSession(socket).start()
            }
        }
    }

}