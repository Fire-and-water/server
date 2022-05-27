package com.example

import ClientSession
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.delay
import java.net.ServerSocket
import kotlin.concurrent.thread

var games = mutableMapOf<Int, Game>();
const val SERVER_PORT = 1234
data class UserSession(val id: Int, val count: Int)

fun main(args: Array<String>) {
    thread(start = true) {
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
    embeddedServer(Netty, port = 8082, host = "0.0.0.0") {// 185.178.47.135
        configureRouting()
        install(Sessions){
            cookie<UserSession>("COOKIE_NAME")
           /* val secretSignKey = hex("6819b57a326945c1968f45236589")
            header<UserSession>("user_session", directorySessionStorage(File("build/.sessions"))) {
                transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
            }*/
        }
    }.start(wait = true)
}
